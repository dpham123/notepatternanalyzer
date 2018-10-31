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
public class NoteTrack implements Iterable<NoteCluster> {

	// Constants and variables
	private int id;
	private int numNotes = 0, size = 0;
	private NoteClusterNode root;
	
	// Temporary variables mid generation
	private int currTime, currTempo, currBpb, currBeatNote;
	private NoteClusterNode curr, prev;
	private KeySignature currKeySig;
	
	// MetaData
	private int ppq = 96;	// Seemingly the default value, but musescore's is 480
	private boolean probablyMusescore = false;
	
	private HeldNote[] openNotes = new HeldNote[88];	// 88 key keyboard assumed
	private static int FIRST_NOTE = 21;
	private List<HeldNote> notes, prevNotes, currNotes, pressedNotes;
	
	/**
	 * Simple constructors are nice
	 * @param ppq - pulses per quarter note
	 */
	public NoteTrack(int id, int ppq) {
		this(id, ppq, false);
	}
	
	/**
	 * Less simple constructor, but still nice
	 * @param ppq - pulses per quarter note
	 * @param ms - false if not musescore
	 */
	public NoteTrack(int id, int ppq, boolean ms) {
		this.id = id;
		this.currTime = 0;
		this.prev = null;
		this.notes = new LinkedList<>();
		this.currNotes = new ArrayList<>();
		this.prevNotes = new ArrayList<>();
		this.pressedNotes = new ArrayList<>();
		this.currKeySig = KeySignature.C;
		this.currTempo = 500000;
		this.currBpb = 4;
		this.currBeatNote = 4;
		this.ppq = ppq;
		this.probablyMusescore = ms;
	}
	
	/**
	 * Merges two tracks into one
	 * @param t1
	 * @param t2
	 */
	public NoteTrack(int id, NoteTrack t1, NoteTrack t2) {
		this.id = id;
		this.currTime = 0;
		this.notes = new LinkedList<>();
		this.currNotes = new ArrayList<>();
		this.prevNotes = new ArrayList<>();
		this.pressedNotes = new ArrayList<>();
		this.currKeySig = KeySignature.C;
		this.currTempo = 500000;
		this.currBpb = 4;
		this.currBeatNote = 4;
		this.ppq = t1.getPpq();
		this.probablyMusescore = t1.isMusescore();
		
		// intitialize iteration
		Iterator<NoteCluster> itr1 = t1.iterator();
		Iterator<NoteCluster> itr2 = t2.iterator();
		
		NoteCluster c1 = null;
		NoteCluster c2 = null;
		boolean take1 = true;
		boolean take2 = true;
		
		// iterate while iterable
		while (itr1.hasNext() || itr2.hasNext()) {
			
			// iterate over already taken clusters
			if (itr1.hasNext() && take1) {
				c1 = itr1.next();
				take1 = false;
			}
			if (itr2.hasNext() && take2) {
				c2 = itr2.next();
				take2 = false;
			}
			
			// get cluster data from t1
			if (c1.getTimeStamp() <= c2.getTimeStamp()) {
				// metadata
				this.currKeySig = c1.getKeySignature();
				this.currTempo = c1.getTempo();
				this.currBpb = c1.getBpb();
				this.currBeatNote = c1.getBeatNote();
				
				// set the notes
				for (HeldNote n : c1.getReleasedNotes()) {
					this.NoteOff(n.getEndTime(), n.getRawValue());
				}
				
				// mark as available to take again
				take1 = true;
			}
			
			if (c1.getTimeStamp() >= c2.getTimeStamp()) {
				// metadata
				this.currKeySig = c2.getKeySignature();
				this.currTempo = c2.getTempo();
				this.currBpb = c2.getBpb();
				this.currBeatNote = c2.getBeatNote();
				
				// set the notes
				for (HeldNote n : c2.getReleasedNotes()) {
					this.NoteOff(n.getEndTime(), n.getRawValue());
				}
				
				// mark as available to take again
				take2 = true;
			}
			
			if (take1) {
				for (HeldNote n : c1.getNotes()) {
					this.NoteOn(n.getStartTime(), n.getRawValue(), true, n.getTrack());
				}
			}
			if (take2) {
				for (HeldNote n : c2.getNotes()) {
					this.NoteOn(n.getStartTime(), n.getRawValue(), true, n.getTrack());
				}
			}
		}
		
		// wrap up
		this.cluster();
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
	
	public int getId() {
		return id;
	}
	
	public List<HeldNote> getNotes() {
		return notes;
	}
	
	public boolean isMusescore() {
		return this.probablyMusescore;
	}
	
	public void setKeySignature(KeySignature keySig) {
		this.currKeySig = keySig;
	}
	
	public void setTempo(int tempo) {
		this.currTempo = tempo;
	}
	
	public void setBpb(int bpb) {
		this.currBpb = bpb;
	}
	
	public void setBeatNote(int beatNote) {
		this.currBeatNote = beatNote;
	}
	
	/**
	 * Set a note's value to on (not safe)
	 * @param timestamp
	 * @param value
	 */
	public void NoteOn(int timestamp, int value, int track) {
		this.NoteOn(timestamp, value, false, track);
	}
	
	public void NoteOn(int timestamp, int value, boolean safe, int track) {
		if (openNotes[value - FIRST_NOTE] == null || !safe) {
			
			// If new timestamp, register previous timestamp as a note cluster
			if (timestamp > currTime) {
				curr = new NoteClusterNode(new NoteCluster(copyNotes(prevNotes), copyNotes(pressedNotes), currTime, currKeySig, currTempo, currBpb, currBeatNote, ppq), prev, null);
				size++;
				if (prev != null) {
					prev.setNext(curr);
				} else {
					root = curr;
				}
				
				
				// Update current
				curr.getNotes().setDuration(timestamp - currTime);
				prev = curr;
				prevNotes = currNotes;
				currNotes = copyNotes(currNotes);
				currTime = timestamp;
			}
			
			// Turn off note if previously on
			if (openNotes[value - FIRST_NOTE] != null) {
				this.NoteOff(timestamp, value);
			}
			
			// Add the note
			openNotes[value - FIRST_NOTE] = new HeldNote(timestamp, value, currKeySig, currTempo, currBpb, currBeatNote, ppq, track);
			currNotes.add(openNotes[value - FIRST_NOTE]);
			pressedNotes.add(openNotes[value - FIRST_NOTE]);
		}
	}
	
	/**
	 * Turn off a note. assumes timestamp is after time turned on
	 * @param timestamp
	 * @param value
	 */
	public void NoteOff(int timestamp, int value) {
		if (openNotes[value - FIRST_NOTE] != null) {
			if (timestamp > currTime) {
				if (prev != null) {
					curr = new NoteClusterNode(new NoteCluster(copyNotes(prevNotes), copyNotes(pressedNotes), currTime, currKeySig, currTempo, currBpb, currBeatNote, ppq), prev, null);
					prev.setNext(curr);
				} else {
					curr = new NoteClusterNode(new NoteCluster(copyNotes(prevNotes), copyNotes(pressedNotes), currTime, currKeySig, currTempo, currBpb, currBeatNote, ppq), prev, null);
					root = curr;
				}

				size++;
				prevNotes = currNotes;
				currNotes = copyNotes(currNotes);
				pressedNotes.clear();
			}
			
			// Retrieve the partially created note
			HeldNote removedNote = openNotes[value - FIRST_NOTE];
			
			// Skip if note already added in timestamp (since NoteOff should have been called already)
			if (timestamp - removedNote.getStartTime() == 0) return;
			
			// Set the duration based on the time turned off
			removedNote.setDuration(timestamp - removedNote.getStartTime());
			
			// Convert musescore duration
			if (probablyMusescore) removedNote.convertMusescoreDuration();
			
			// Set the note into the track
			notes.add(removedNote);
			numNotes++;
			
			// Close off the note
			openNotes[value - FIRST_NOTE] = null;
			currNotes.remove(removedNote);
			
			if (timestamp > currTime) {
				
				// Update current
				curr.getNotes().setDuration((removedNote.getStartTime() + removedNote.getDuration()) - currTime);
				prev = curr;
				currTime = removedNote.getStartTime() + removedNote.getDuration();
				pressedNotes.clear();
			}
		}
	}
	
	public void cluster() {
		if (curr != null) {
			size++;
			curr = new NoteClusterNode(new NoteCluster(copyNotes(prevNotes), copyNotes(pressedNotes), currTime, currKeySig, currTempo, currBpb, currBeatNote, ppq), prev, null);
			prev.setNext(curr);
			curr.getNotes().setDuration(0);
			prev = curr;
			prevNotes = currNotes;
			curr = null;
			currNotes = null;
		}
	}
	
	private static List<HeldNote> copyNotes(List<HeldNote> notes) {
		List<HeldNote> newNotes = new ArrayList<>();
		for (HeldNote note : notes) {
			newNotes.add(note);
		}
		return newNotes;
	}
	
	public String toString() {
		String ret = "";
		for (NoteCluster notes : this) {
			ret += notes + "\n";
		}
		return ret;
	}

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