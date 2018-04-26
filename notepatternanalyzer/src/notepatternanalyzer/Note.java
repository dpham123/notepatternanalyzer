package notepatternanalyzer;

import notepatternanalyzer.Accidental;

enum Note {
	C(0), 
	C_SHARP(1, Accidental.SHARP), 
	D_FLAT(1, Accidental.FLAT), 
	D(2),
	D_SHARP(3, Accidental.SHARP), 
	E_FLAT(3, Accidental.FLAT), 
	E(4), 
	F(5), 
	F_SHARP(6, Accidental.SHARP), 
	G_FLAT(6, Accidental.FLAT), 
	G(7), 
	G_SHARP(8, Accidental.SHARP), 
	A_FLAT(8, Accidental.FLAT), 
	A(9), 
	A_SHARP(10, Accidental.SHARP), 
	B_FLAT(10, Accidental.FLAT), 
	B(11), 
	C_FLAT(11, Accidental.FLAT);

=======
	C_SHARP(1, Accidental.SHARP),
	D_FLAT(1, Accidental.FLAT),
	D(2),
	D_SHARP(3, Accidental.SHARP),
	E_FLAT(3, Accidental.FLAT),
	E(4),
	F(5),
	F_SHARP(6, Accidental.SHARP),
	G_FLAT(6, Accidental.FLAT),
	G(7),
	G_SHARP(8, Accidental.SHARP),
	A_FLAT(8, Accidental.FLAT),
	A(9),
	A_SHARP(10, Accidental.SHARP),
	B_FLAT(10, Accidental.FLAT),
	B(11),
	C_FLAT(11, Accidental.FLAT);
	
	private int value;
	private Accidental accidental;

	private Note(int value, Accidental accidental) {
		this.value = value;
		this.accidental = accidental;
	}

	private Note(int value) {
		this.value = value;
	}

	int getValue() {
		return value;
	}

	Accidental getAccidental() {
		return accidental;
	}

	/**
	 * Returns the note with the correct accidental based on the key signature. In music, an
	 * accidental is determined by the harmonic function of the chord, which we cannot detect in
	 * this program. Thus, I used the convention in Musescore after experimenting a bit. The 
	 * convention, with the major key signature as 0, is as follows:
	 * 
	 * 0
	 * 1 - sharp
	 * 2
	 * 3 - flat
	 * 4
	 * 5
	 * 6 - sharp
	 * 7
	 * 8 - sharp
	 * 9
	 * 10 - flat
	 * 11
	 * @param value of midi note
	 * @param ks key signature
	 * @return a note
	 */
	static Note getNote(int value, KeySignature ks) {
		for (Note n : Note.values()) {
			
			// First checks to make sure the note n is the same as the note we're passing in
			if (n.getValue() == value % 12) {
				
				/* 
				 * Checks to see if the note is 3 semitones from the root note of the key signature
				 * then returns flatted version of note
				 */
				if ((value % 12) - ks.getNote().getValue() == 3) {
					if (n.getAccidental() == Accidental.FLAT || n.getAccidental() == null) {
						return n;
					}
					
				/* 
				 * Checks to see if the note is 10 semitones from the root note of the key signature
				 * then returns flatted version of note
				 */
				} else if ((value % 12) - ks.getNote().getValue() == 10) {
					if (n.getAccidental() == Accidental.FLAT || n.getAccidental() == null) {
						return n;
					}
					
				/*
				 * All other notes are checked with the key signature "ks" to get the correct 
				 * accidental. For example, if the key signature was D, a sharp key, and we had to 
				 * decide between F# and Gb, since D is a sharp key, we return F#.
				 * 
				 * The other two corner cases check if the note "n" or key signature "ks" don't
				 * have any accidentals. In these cases, n would be a natural note and ks would 
				 * be C major.
				 */
				} else if (ks.getAccidental() == n.getAccidental() || n.getAccidental() == null
						|| ks.getAccidental() == null) {
=======
	// Need to define accidentals of notes in C major scale here
	static Note getNote(int value, Accidental accidental) {
		for (Note n : Note.values()) {
			if (n.getValue() == value % 12) {
				if (n.getAccidental() == null || n.getAccidental() == accidental || accidental == null) {
					return n;
				}

			}
		}
		return null; // Lol, rip if this happens
	}

=======
		return null;
	}
}
