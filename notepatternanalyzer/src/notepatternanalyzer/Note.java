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

	// Need to define accidentals of notes in C major scale here
	static Note getNote(int value, Accidental accidental) {
		for (Note n : Note.values()) {
			if (n.getValue() == value % 12) {
				if (n.getAccidental() == null || n.getAccidental() == accidental || accidental == null) {
					return n;
				}

			}
		}
		return null;
	}
}
