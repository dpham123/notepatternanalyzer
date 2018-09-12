package notepatternanalyzer;

import java.util.List;
import java.util.TreeSet;

import notepatternanalyzer.KeySignature;
import notepatternanalyzer.Note;
import notepatternanalyzer.NoteSequence.HeldNoteTemp;

/**
 * Class that represents a "cross section" of a sequence of notes.
 * @author Alumina
 */
class NoteCluster {
    
	// Primitive data
	private int[][] notes = new int[9][12];
	private int[][] newNotes = new int[9][12];
	private int duration = 0;
    
	// More "meta" data
	private KeySignature keySig;
	private int tempo;
	private int ppq;
	private int bpb;
	private int beatNote;
	private int timestamp;
	
	
	/**
	 * Constructor with full parameters
	 * @param prev the preceding note cluster
	 * @param onNotes a list of the notes being turned on
	 * @param offNotes a list of the notes being turned off
	 * @param duration the duration
	 */
	public NoteCluster(NoteCluster prev, List<HeldNoteTemp> onNotes, List<HeldNoteTemp> offNotes, int timestamp, KeySignature keySig, float tempo, int bpb, int beatNote, int ppq) {
		if (prev != null) {
			for (int octave = 0; octave < 9; octave++) {
				for (int value = 0; value < 12; value++) {
					notes[octave][value] = prev.getTrack(value, octave);
				}
			}
		}
		
		for (HeldNoteTemp note : offNotes) {
			setNote(note.value, 0);
		}
		
		for (HeldNoteTemp note : onNotes) {
			setNote(note.value, note.track);
		}
		
		this.timestamp = timestamp;
		this.keySig = keySig;
		this.tempo = Math.round(60000000 / tempo);
		this.bpb = bpb;
		this.beatNote = beatNote;
		this.ppq = ppq;
	}
	
	/**
	 * Checks if a note is on
	 * @param value the value [0,11] of the note to check
	 * @param octave the octave to check
	 * @return boolean of whether note is on
	 */
	public boolean noteOn(int value, int octave) {
		return notes[octave][value] != 0;
	}
	
	/**
	 * Checks if a note is on
	 * @param note the note to check
	 * @param octave the octave to check
	 * @return boolean of whether note is on
	 */
	public boolean noteOn(Note note, int octave) {
		return notes[octave][note.getValue()] != 0;
	}
	
	public int getTrack(int value, int octave) {
		return notes[octave][value];
	}
	
	public int getTrack(Note note, int octave) {
		return notes[octave][note.getValue()];
	}
	
	/**
	 * Sets the boolean value for the corresponding note
	 * @param rawValue the raw value of the note
	 * @param on the boolean we're seting the note to
	 */
	private void setNote(int rawValue, int track) {
		int octave = rawValue / 12 - 1;
		int value = rawValue % 12;
		
		this.notes[octave][value] = track;
		this.newNotes[octave][value] = track;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public int getDuration() {
		return duration;
	}
	
	int getTimeStamp() {
		return timestamp;
	}
	
	int getTempo(){
		return tempo;
	}
	
	int getBpb() {
		return bpb;
	}
	
	int getBeatNote() {
		return beatNote;
	}
	
	@Override
	public String toString() {
		//String ret = timestamp + "~" + (float)duration / ppq / 4 + ": ";
		String ret = timestamp + "~" + duration + ": ";
		int track = 0;
		for (int octave = 0; octave < 9; octave++) {
			for (int value = 0; value < 12; value++) {
				if (newNotes[octave][value] != 0) {
					if (newNotes[octave][value] != track) {
						if (track != 0) {
							ret += "] ";
						}
						track = newNotes[octave][value];
						ret += "(" + track + ")[ ";
					}
					ret += "*" + Note.getNote(value, keySig) + "_" + octave + "* ";
				} else if (notes[octave][value] != 0) {
					if (notes[octave][value] != track) {
						if (track != 0) {
							ret += "] ";
						}
						track = notes[octave][value];
						ret += "(" + track + ")[ ";
					}
					ret += "" + Note.getNote(value, keySig) + "_" + octave + " ";
				}
			}
		}
		if (track != 0)
			ret += "]";
		
		return ret;
	}
}