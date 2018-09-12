package notepatternanalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import notepatternanalyzer.HeldNote;
import notepatternanalyzer.KeySignature;

/**
 * Prototype data structure to store and iterate over note sequences
 */
public class NoteSequence implements Iterable<NoteCluster> {
	
	public class HeldNoteTemp {
		public int value;
		public int track;
		
		public HeldNoteTemp(int value, int track) {
			this.value = value;
			this.track = track;
		}
	}


	// Constants and variables
	public static final String[] InterestingTags = new String[] {"Tempo","KeySig","TimeSig","On","Off"};
	private int size = 0;
	private NoteClusterNode root;
	
	// MetaData
	private int ppq = 96;	// Seemingly the default value, but musescore's is 480
	private boolean probablyMusescore = true;
	
	private List<HeldNote> notes;
	
	/**
	 * Node that holds note data
	 * @author Alumina
	 */
	private class NoteClusterNode {
		NoteCluster data;
		NoteClusterNode prev;
		NoteClusterNode next;
		
		public NoteClusterNode(NoteCluster data, NoteClusterNode prev, NoteClusterNode next) {
			this.data = data;
			this.prev = prev;
			this.next = next;
		}
		
		public NoteClusterNode getNext() {
			return next;
		}
		
		public NoteCluster getNotes() {
			return data;
		}
		
		public void setNext(NoteClusterNode next) {
			this.next = next;
		}
	}
	
	/**
	 * Constructs a sequence from a file
	 * @param file the file to parse the data from
	 * @throws IOException
	 */
	public NoteSequence(File file) throws IOException {
		
		// Get data in a readable format from the file
		List<TreeMap<Integer, List<List<String>>>> trackList = parseMidiText(file);
		
		// Set the default values
		int time = 0;
		KeySignature keySig = KeySignature.C;
		int tempo = 500000;
		int bpb = 4;
		int beatNote = 4;
		NoteClusterNode prev = null;
		HeldNote[] heldNotes = new HeldNote[109];
		
		// Iterate through the data to fill the nodes
		for (Map.Entry<Integer, List<List<String>>> entry : trackList.get(0).entrySet()) {
	        int timestamp = entry.getKey();
	        List<List<String>> events = entry.getValue();
	        
	        List<HeldNoteTemp> onNotes = new LinkedList<>();
	        List<HeldNoteTemp> offNotes = new LinkedList<>();
	        
	        for (List<String> event : events) {
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
	        	case "On":
	        		if (event.contains("v=0")) {
	        			//System.out.println("Note Off");
	        			offNotes.add(new HeldNoteTemp(Integer.parseInt(event.get(2).substring(2)), 0));
	        		} else {
	        			//System.out.println("Note On");
	        			onNotes.add(new HeldNoteTemp(Integer.parseInt(event.get(2).substring(2)), Integer.parseInt(event.get(4))));
	        		}
	        		break;
	        	case "Off":
        			offNotes.add(new HeldNoteTemp(Integer.parseInt(event.get(2).substring(2)), 0));
	        		break;
	        	default:
	        		break;
	        	}
	        }

	        // Create the node and push it onto the list
        	NoteClusterNode node;
	        if (prev != null) { 
	        	node = new NoteClusterNode(new NoteCluster(prev.getNotes(), onNotes, offNotes, timestamp, keySig, tempo, bpb, beatNote, ppq), prev, null);
	        	prev.setNext(node);
	        	prev.getNotes().setDuration(timestamp - time);
	        } else {
	        	node = new NoteClusterNode(new NoteCluster(null, onNotes, offNotes, timestamp, keySig, tempo, bpb, beatNote, ppq), prev, null);
	        	root = node;
	        }
	        prev = node;
	        time = timestamp;
	        size++;
	   }
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
		int track = 1;
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
			        	if (!eventList.containsKey(timestamp)) {
			        		events = new LinkedList<>();
			        	} else {
			        		events = eventList.get(timestamp);
			        	}
			        	
			        	// Parse the event tags into a list
			        	List<String> eventTags = new ArrayList<>();
			        	for (int i = 1; i < parts.length; i++) {
			        		eventTags.add(parts[i]);
			        	}
			        	
			        	events.add(eventTags);
			        	eventList.put(timestamp, events);
			        	
			        	// Capture note events into note track
			        	if (parts[1].equals("On") || parts[1].equals("Off")) {
			        		// do the same
			        		List<List<String>> trackEvents;
				        	if (!currTrackNotes.containsKey(timestamp)) {
				        		trackEvents = new LinkedList<>();
				        	} else {
				        		trackEvents = currTrackNotes.get(timestamp);
				        	}
				        	
				        	eventTags.add("" + track);
				        	
				        	trackEvents.add(eventTags);
				        	currTrackNotes.put(timestamp, trackEvents);
				        	
				        	notesInTrack = true;
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
			    		System.out.println("Read " + currTrackNotes.size() + " notes into track " + trackList.size());
			    		trackList.add(currTrackNotes);
			    		currTrackNotes = new TreeMap<>();
			    		notesInTrack = false;
			    		track++;
			    	}
			    }
			}
		}
		System.out.println(eventList.size() + " events total");
		
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
		return new NoteIterator(root);
	}

	/**
	 * Iterator class so foreach can be used
	 * @author Alumina
	 */
	class NoteIterator implements Iterator<NoteCluster> {

		private NoteClusterNode curr;

		public NoteIterator(NoteClusterNode begin) {
			this.curr = begin;
		}
		
		public boolean hasNext() {
			return curr != null;
		}

		public NoteCluster next() {
			NoteCluster ret = curr.getNotes();
			curr = curr.getNext();
			return ret;
		}
	}
}