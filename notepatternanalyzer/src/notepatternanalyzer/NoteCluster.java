package notepatternanalyzer;

import java.util.List;
import java.util.TreeSet;

class NoteCluster {
    
	// Primitive data
	private boolean[][] notes = new boolean[9][12];
	private boolean[][] newNotes = new boolean[9][12];
	private int duration = 0;
    
	// More "meta" data
	private KeySignature keySig;
	private int tempo;
	
	/**
	 * Constructor with full parameters
	 * @param prev the preceding note cluster
	 * @param onNotes a list of the notes being turned on
	 * @param offNotes a list of the notes being turned off
	 * @param duration the duration
	 */
	public NoteCluster(NoteCluster prev, List<Integer> onNotes, List<Integer> offNotes, int duration, KeySignature keySig, int tempo) {
		if (prev != null) {
			for (int octave = 0; octave < 9; octave++) {
				for (int value = 0; value < 12; value++) {
					notes[octave][value] = prev.noteOn(value, octave);
				}
			}
		}
		
		for (int note : offNotes) {
			setNote(note, false);
		}
		
		for (int note : onNotes) {
			setNote(note, true);
		}
		
		this.duration = duration;
		this.keySig = keySig;
		this.tempo = tempo;
	}
	
	/**
	 * Checks if a note is on
	 * @param value the value [0,11] of the note to check
	 * @param octave the octave to check
	 * @return boolean of whether note is on
	 */
	public boolean noteOn(int value, int octave) {
		return notes[octave][value];
	}
	
	/**
	 * Checks if a note is on
	 * @param note the note to check
	 * @param octave the octave to check
	 * @return boolean of whether note is on
	 */
	public boolean noteOn(Note note, int octave) {
		return notes[octave][note.getValue()];
	}
	
	/**
	 * Sets the boolean value for the corresponding note
	 * @param rawValue the raw value of the note
	 * @param on the boolean we're seting the note to
	 */
	private void setNote(int rawValue, boolean on) {
		int octave = rawValue / 12 - 1;
		int value = rawValue % 12;
		
		this.notes[octave][value] = on;
		this.newNotes[octave][value] = on;
	}
	
	@Override
	public String toString() {
		String ret = "" + duration + ": ";
		
		for (int octave = 0; octave < 9; octave++) {
			for (int value = 0; value < 12; value++) {
				if (newNotes[octave][value]) {
					ret += "*" + Note.getNote(value, keySig) + "_" + octave + "* ";
				} else if (notes[octave][value]) {
					ret += "" + Note.getNote(value, keySig) + "_" + octave + " ";
				}
			}
		}
		
		return ret;
	}
}
