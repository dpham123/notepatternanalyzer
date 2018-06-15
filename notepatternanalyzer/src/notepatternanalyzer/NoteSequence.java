package notepatternanalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Prototype data structure to store and iterate over note sequences
 */
public class NoteSequence implements Iterable<NoteCluster> {


	// Constants and variables
	public static final String[] InterestingTags = new String[] {"Tempo","KeySig","TimeSig","On","Off"};
	private int size = 0;
	private NoteClusterNode root;
	
	// MetaData
	private int ppq = 480;	// Seemingly Musescore's default
	
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
		TreeMap<Integer, List<List<String>>> eventList = parseMidiText(file);
		
		// Set the default values
		int time = 0;
		KeySignature keySig = KeySignature.C;
		int tempo = 500000;
		int bpb = 4;
		int beatNote = 4;
		NoteClusterNode prev = null;
		
		// Iterate through the data to fill the nodes
		for (Map.Entry<Integer, List<List<String>>> entry : eventList.entrySet()) {
	        int timestamp = entry.getKey();
	        List<List<String>> events = entry.getValue();
	        
	        List<Integer> onNotes = new LinkedList<>();
	        List<Integer> offNotes = new LinkedList<>();
	        
	        for (List<String> event : events) {
	        	switch (event.get(0)) {
	        	case "Tempo":
	        		tempo = Integer.parseInt(event.get(1));
	        		break;
	        	case "KeySig":
	        		keySig = KeySignature.getKeySig(Integer.parseInt(event.get(1)));
	        		break;
	        	case "TimeSig":
	        		break;
	        	case "On":
	        		if (event.contains("v=0")) {
	        			offNotes.add(Integer.parseInt(event.get(2).substring(2)));
	        		} else {
	        			onNotes.add(Integer.parseInt(event.get(2).substring(2)));
	        		}
	        		break;
	        	case "Off":
        			offNotes.add(Integer.parseInt(event.get(2).substring(2)));
	        		break;
	        	default:
	        		break;
	        	}
	        }

	        // Create the node and push it onto the list
        	NoteClusterNode node;
	        if (prev != null) { 
	        	node = new NoteClusterNode(new NoteCluster(prev.getNotes(), onNotes, offNotes, timestamp, keySig, tempo, ppq), prev, null);
	        	prev.setNext(node);
	        	prev.getNotes().setDuration(timestamp - time);
	        } else {
	        	node = new NoteClusterNode(new NoteCluster(null, onNotes, offNotes, timestamp, keySig, tempo, ppq), prev, null);
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
	 * @return a map in the form [timestamp]: {{Tag, args...}, ...}
	 * @throws IOException
	 */
	public TreeMap<Integer, List<List<String>>> parseMidiText(File file) throws IOException {
		// Parse the file for only the lines that matter
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		
		// This data structure is weird, but I think it will work fine
		TreeMap<Integer, List<List<String>>> eventList = new TreeMap<>();
		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(" ");
			if (parts.length >= 2) {
			    try {
			        int timestamp = Integer.parseInt(parts[0]);
			        if (Arrays.asList(InterestingTags).contains(parts[1])) {
			        	List<List<String>> events;
			        	if (!eventList.containsKey(timestamp)) {
			        		events = new LinkedList<>();
			        	} else {
			        		events = eventList.get(timestamp);
			        	}
			        	
			        	List<String> eventTags = new ArrayList<>();
			        	for (int i = 1; i < parts.length; i++) {
			        		eventTags.add(parts[i]);
			        	}
			        	
			        	// Yeah... when I'm tired, I build weird data structures
			        	events.add(eventTags);
			        	eventList.put(timestamp, events);
			        }
			    } catch(NumberFormatException e) {
			    	if (parts[0] == "MFile") {
			    		try {
			    			this.ppq = Integer.parseInt(parts[3]);
			    		} catch (NumberFormatException e2) {
			    			continue;
			    		}
			    	}
			    }
			}
		}
		
		br.close();
		
		return eventList;
	}
	
	/**
	 * Gets the size of the structure
	 * @return the size
	 */
	public int size() {
		return size;
	}

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