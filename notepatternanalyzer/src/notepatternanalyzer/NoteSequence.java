package notepatternanalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import hmm.HMMObservations;
import notepatternanalyzer.KeySignature;

/**
 * Prototype data structure to store and iterate over note sequences
 */
public class NoteSequence implements HMMObservations<NoteCluster> {


	// Constants and variables
	public static final String[] InterestingTags = new String[] {"Tempo","KeySig","TimeSig","On","Off"};
	private int size = 0;
	
	// MetaData
	private int ppq = 96;	// Seemingly the default value, but musescore's is 480
	private boolean probablyMusescore = true;
	
	// The real data is here
	private List<NoteTrack> tracks;
	private NoteTrack main;
	
	/**
	 * Constructs a sequence from a file
	 * @param file the file to parse the data from
	 * @throws IOException
	 */
	public NoteSequence(File file) throws IOException {
		
		// Get data in a readable format from the file
		List<TreeMap<Integer, List<List<String>>>> trackList = parseMidiText(file);
		tracks = new ArrayList<>();
		
		// Set the default values
		int time = 0;
		KeySignature keySig = KeySignature.C;
		int tempo = 500000;
		int bpb = 4;
		int beatNote = 4;
		
		// Iterate through all the note tracks
		for (int i = 1; i < trackList.size(); i++) {
			
			// Set up the track
			NoteTrack currTrack = new NoteTrack(i, ppq, probablyMusescore);
			tracks.add(currTrack);
			
			// Set up the event iterator
			Iterator<Map.Entry<Integer, List<List<String>>>> eventItr = trackList.get(0).entrySet().iterator();
			Map.Entry<Integer, List<List<String>>> eventEntry = eventItr.next();
			int eventTime = eventEntry.getKey();
			List<List<String>> nextEvents = eventEntry.getValue();
			
			// Iterate through each TreeMap sequentially
			for (Iterator<Map.Entry<Integer, List<List<String>>>> entryItr = trackList.get(i).entrySet().iterator(); entryItr.hasNext();) {
				Map.Entry<Integer, List<List<String>>> entry = entryItr.next();
				
				int timestamp = entry.getKey();
		        List<List<String>> noteEvents = entry.getValue();
		        
		        // Update the events if the events changed
		        if (timestamp >= eventTime) {
		        	
		        	// TODO if not equal then create a dummy event node. Edge case when time signature changes twice when no note are played
		        	
		        	// get the relevant events
		        	for (List<String> event : nextEvents) {
			        	switch (event.get(0)) {
			        	case "Tempo":
			        		tempo = Integer.parseInt(event.get(1));
			        		break;
			        	case "KeySig":
			        		keySig = KeySignature.getKeySig(Integer.parseInt(event.get(1)));
			        		break;
			        	case "TimeSig":
			        		String[] timeSigNumbers = event.get(1).split("/");
			        		bpb = Integer.parseInt(timeSigNumbers[0]);
			        		beatNote = Integer.parseInt(timeSigNumbers[1]);
			        		break;
			        	}
		        	}
		        	// Set the metadata for the track
		        	currTrack.setTempo(tempo);
		        	currTrack.setKeySignature(keySig);
		        	currTrack.setBpb(bpb);
		        	currTrack.setBeatNote(beatNote);
		        	
		        	while (timestamp >= eventTime && eventItr.hasNext()) {
		        		eventEntry = eventItr.next();
		    			eventTime = eventEntry.getKey();
		    			nextEvents = eventEntry.getValue();
		    		}
		        }

		        // Get the note data from the events
	        	for (List<String> event : noteEvents) {
		        	switch (event.get(0)) {
		        	case "On":
		        		currTrack.NoteOn(timestamp, Integer.parseInt(event.get(2).substring(2)), i);
		        		break;
		        	case "Off":
		        		currTrack.NoteOff(timestamp, Integer.parseInt(event.get(2).substring(2)));
		        		break;
		        	default:
		        		break;
		        	}
		        }
		    }
			
			// wrap up last notes
			currTrack.cluster();
		}
		
		
		// combine all the tracks into the main track
		if (tracks.size() == 1) {
			main = tracks.get(0);
		} else {
			for (int i = 1; i < tracks.size(); i++) {
				if (i == 1) {
					main = new NoteTrack(0, tracks.get(i - 1), tracks.get(i));
				} else {
					main = new NoteTrack(0, main, tracks.get(i));
				}
			}
		}
		size = main.size();
		
//		for (NoteTrack track : tracks) {
//			System.out.println(track + "\n===============================================\n");
//		}
	}
	
	/**
	 * Parses a mf2t txt file and retrives the note data
	 * @param file the file to parse
	 * @return a list of maps in the form [timestamp]: {{Tag, args...}, ...}, each representing a track.
	 * 		   The first entry will contain all events.
	 * @throws IOException
	 */
	public List<TreeMap<Integer, List<List<String>>>> parseMidiText(File file) throws IOException {
		// Parse the file for only the lines that matter
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		
		// This data structure is weird, but I think it will work fine
		List<TreeMap<Integer, List<List<String>>>> trackList = new ArrayList<>();
		TreeMap<Integer, List<List<String>>> eventList = new TreeMap<>();
		TreeMap<Integer, List<List<String>>> currTrackNotes = new TreeMap<>();
		trackList.add(eventList);
		
		// Read the lines one by one
		boolean notesInTrack = false;
		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(" ");
			if (parts.length > 0) {
			    try {
			    	// Check for timestamped events
			        int timestamp = Integer.parseInt(parts[0]);
			        
			        // Capture interesting timestamped events
			        if (Arrays.asList(InterestingTags).contains(parts[1])) {

			        	// Initialize list to existing list, or initialize new list
			        	List<List<String>> events;
			        	
			        	// Parse the event tags into a list
			        	List<String> eventTags = new ArrayList<>();
			        	for (int i = 1; i < parts.length; i++) {
			        		eventTags.add(parts[i]);
			        	}
			        	
			        	// Capture note events into note track
			        	if (parts[1].equals("On") || parts[1].equals("Off")) {
			        		if (!currTrackNotes.containsKey(timestamp)) {
				        		events = new LinkedList<>();
				        	} else {
				        		events = currTrackNotes.get(timestamp);
				        	}
				        	
				        	// Simplify the syntax
				        	if (parts[4].equals("v=0")) eventTags.set(0, "Off");
				        	
				        	events.add(eventTags);
				        	currTrackNotes.put(timestamp, events);
				        	
				        	notesInTrack = true;
				        } else {
				        	if (!eventList.containsKey(timestamp)) {
				        		events = new LinkedList<>();
				        	} else {
				        		events = eventList.get(timestamp);
				        	}
				        	
				        	events.add(eventTags);
				        	eventList.put(timestamp, events);
				        	
				        }
			        	
			        	// Part of the Musescore detection
			        	if (parts[1].equals("Off")) this.probablyMusescore = false;
			        }
			    } catch(NumberFormatException e) {
			    	
			    	// Catch non-timestamped events
			    	if (parts[0].equals("MFile")) {
			    		// Catch file header
			    		try {
			    			
			    			// Capture the ppq value
			    			this.ppq = Integer.parseInt(parts[3]);
			    		} catch (NumberFormatException e2) {
			    			continue;
			    		}
			    	} else if (parts[0].equals("TrkEnd") && notesInTrack) {
			    		// Catch the end of track
			    		// Push the current track and start a new one
			    		//System.out.println("Read " + currTrackNotes.size() + " notes into track " + trackList.size());
			    		trackList.add(currTrackNotes);
			    		currTrackNotes = new TreeMap<>();
			    		notesInTrack = false;
			    	}
			    }
			}
		}
		//System.out.println(eventList.size() + " events total");
		
		// Another point of musescore detection
		if (this.ppq != 480) this.probablyMusescore = false;
		
		br.close();
		
		return trackList;
	}
	
	/**
	 * Gets the size of the structure
	 * @return the size
	 */
	public int size() {
		return size;
	}
	
	/**
	 * Accessor for pulses per quarter
	 * @return ppq
	 */
	public int getPpq() {
		return this.ppq;
	}

	@Override
	public Iterator<NoteCluster> iterator() {
		return main.iterator();
	}
}