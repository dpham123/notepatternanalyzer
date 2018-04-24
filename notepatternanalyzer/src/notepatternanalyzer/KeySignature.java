package notepatternanalyzer;

import notepatternanalyzer.Accidental;

enum KeySignature {
	
	C(0), 
	C_SHARP(7, Accidental.SHARP),
	D_FLAT(251, Accidental.FLAT),
	D(2, Accidental.SHARP),
	E_FLAT(253, Accidental.FLAT),
	E(4, Accidental.SHARP),
	F(255, Accidental.FLAT),
	F_SHARP(6, Accidental.SHARP),
	G_FLAT(250, Accidental.FLAT),
	G(1, Accidental.SHARP),
	A_FLAT(252, Accidental.FLAT),
	A(3, Accidental.SHARP),
	B_FLAT(254, Accidental.FLAT),
	B(5, Accidental.SHARP),
	C_FLAT(249, Accidental.FLAT);
	
	private int value;
	private Accidental accidental;

	private KeySignature(int value) {
		this.value = value;
	}

	private KeySignature(int value, Accidental accidental) {
		this.value = value;
		this.accidental = accidental;
	}

	int getValue() {
		return value;
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
