package notepatternanalyzer;

import notepatternanalyzer.Accidental;

enum KeySignature {
	
	C(0, Note.C), 
	C_SHARP(7, Note.C_SHARP, Accidental.SHARP),
	D_FLAT(251, Note.D_FLAT, Accidental.FLAT),
	D(2, Note.D, Accidental.SHARP),
	E_FLAT(253, Note.E_FLAT, Accidental.FLAT),
	E(4, Note.E, Accidental.SHARP),
	F(255, Note.F, Accidental.FLAT),
	F_SHARP(6, Note.F_SHARP, Accidental.SHARP),
	G_FLAT(250, Note.G_FLAT, Accidental.FLAT),
	G(1, Note.G, Accidental.SHARP),
	A_FLAT(252, Note.A_FLAT, Accidental.FLAT),
	A(3, Note.A, Accidental.SHARP),
	B_FLAT(254, Note.B_FLAT, Accidental.FLAT),
	B(5, Note.B, Accidental.SHARP),
	C_FLAT(249, Note.C_FLAT, Accidental.FLAT);
	
	private int value;
	private Accidental accidental;
	private Note note;

	private KeySignature(int value, Note note) {
		this.value = value;
		this.note = note;
	}

	private KeySignature(int value, Note note, Accidental accidental) {
		this.value = value;
		this.note = note;
		this.accidental = accidental;
	}
	

	int getValue() {
		return value;
	}
	
	Note getNote() {
		return note;
	}
	
	Accidental getAccidental() {
		return accidental;
	}

	static KeySignature getKeySig(int value) {
		for (KeySignature ks : KeySignature.values()) {
			if (ks.getValue() == value) {
				return ks;
			}
		}
		return null;
	}
}
