package notepatternanalyzer;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TreeSet;

/**
 * Class that represents a "cross section" of a sequence of notes.
 * @author Alumina
 */
class NoteCluster {
    
	// Primitive data
	private HeldNote[][] notes = new HeldNote[9][12];
	private HeldNote[][] newNotes = new HeldNote[9][12];
	private int duration = 0;
    
	// new stuff
	List<HeldNote> heldNotes, releasedNotes;//, pressedNotes;
	
	// More "meta" data
	private KeySignature keySig;
	private int tempo;
	private int ppq;
	private int bpb;
	private int beatNote;
	private int timestamp;
	
	/**
	 * pls don't ask
	 * @param prevNotes
	 * @param currNotes
	 * @param timestamp
	 * @param keySig
	 * @param tempo
	 * @param bpb
	 * @param beatNote
	 * @param ppq
	 */
	public NoteCluster(List<HeldNote> prevNotes, List<HeldNote> currNotes, int timestamp, KeySignature keySig, float tempo, int bpb, int beatNote, int ppq) {
		
		heldNotes = new ArrayList<>();
		releasedNotes = new ArrayList<>();
		//pressedNotes = new ArrayList<>();
		
		// This section here can definitely be shortened somehow
		
		// get the held notes from previous notes not released at timestamp
		for (HeldNote note : prevNotes) {
			if (note.getEndTime() != timestamp) {
				this.setNote(note, false);
				this.heldNotes.add(note);
			}
		}
		
		// add all the notes to get the new notes
		for (HeldNote note : currNotes) {
			if (note.getStartTime() == timestamp) {
				this.setNote(note, true);
				this.heldNotes.add(note);
				//this.pressedNotes.add(note);
			}
		}
		
		// do opposite of first loop to get released notes
		for (HeldNote note : prevNotes) {
			if (note.getEndTime() == timestamp) {
				this.releasedNotes.add(note);
			}
		}
		
		this.timestamp = timestamp;
		this.keySig = keySig;
		this.tempo = Math.round(60000000 / tempo);
		this.bpb = bpb;
		this.beatNote = beatNote;
		this.ppq = ppq;

		Collections.sort(heldNotes);
		Collections.sort(releasedNotes);
	}
	
	/**
	 * Checks if a note is on
	 * @param value the value [0,11] of the note to check
	 * @param octave the octave to check
	 * @return boolean of whether note is on
	 */
	public boolean noteOn(int value, int octave) {
		return notes[octave][value] != null;
	}
	
	/**
	 * Checks if a note is on
	 * @param note the note to check
	 * @param octave the octave to check
	 * @return boolean of whether note is on
	 */
	public boolean noteOn(Note note, int octave) {
		return notes[octave][note.getValue()] != null;
	}
	
	/**
	 * Gets the track of the note
	 * @param value the value [0,11] of the note to check
	 * @param octave the octave to check
	 * @return track or -1 if no note
	 */
	public int getTrack(int value, int octave) {
		return notes[octave][value] != null ? notes[octave][value].getTrack() : -1;
	}
	
	/**
	 * Gets the track of the note
	 * @param note the note to check
	 * @param octave the octave to check
	 * @return track
	 */
	public int getTrack(Note note, int octave) {
		return notes[octave][note.getValue()] != null ? notes[octave][note.getValue()].getTrack() : -1;
	}
	
	/**
	 * Sets the boolean value for the corresponding note
	 * @param rawValue the raw value of the note
	 * @param on the boolean we're seting the note to
	 */
	private void setNote(HeldNote n, boolean newNote) {
		int octave = n.getRawValue() / 12 - 1;
		int value = n.getRawValue() % 12;
		this.notes[octave][value] = n;
		if (newNote) this.newNotes[octave][value] = n;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public int getTimeStamp() {
		return timestamp;
	}
	
	public KeySignature getKeySignature() {
		return keySig;
	}
	
	public int getTempo(){
		return tempo;
	}
	
	public int getBpb() {
		return bpb;
	}
	
	public int getBeatNote() {
		return beatNote;
	}
	
	public List<HeldNote> getNotes() {
		return heldNotes;
	}
	
	public List<HeldNote> getReleasedNotes() {
		return releasedNotes;
	}
	
	private static long gcm(long a, long b) {
	    return b == 0 ? a : gcm(b, a % b); // Not bad for one line of code :)
	}

	private static String asFraction(long a, long b) {
	    long gcm = gcm(a, b);
	    return (a / gcm) + "/" + (b / gcm);
	}
	
	@Override
	public String toString() {
		String ret = timestamp + "~" + asFraction(duration, ppq * 4) + ": ";

		int track = -1;
		
		for (HeldNote n : heldNotes) {
			if (n.getTrack() != track) {
				if (track != -1) {
					ret += "] ";
				}
				track = n.getTrack();
				ret += "(" + track + ")[ ";
			}
			if (n.getStartTime() == timestamp) ret += "*" + n + "* ";
			else ret += n + " ";
		}
		if (track != -1) ret += "]";
		
		return ret;
	}
}