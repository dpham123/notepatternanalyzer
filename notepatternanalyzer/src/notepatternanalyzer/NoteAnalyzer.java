package notepatternanalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

class NoteAnalyzer {
	private File file;
	
	NoteAnalyzer(File file){
		this.file = file;
	}
	
	enum Accidental{
		FLAT, SHARP;
	}
	
	enum Note{
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
		
		private Note(int value, Accidental accidental){
			this.value = value;
			this.accidental = accidental;
		}
		
		private Note(int value){
			this.value = value;
		}
		
		private int getValue() {
			return value;
		}
		
		private Accidental getAccidental() {
			return accidental;
		}
		
		// Need to define accidentals of notes in C major scale here
		private static Note getNote(int value, Accidental accidental) {
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
	
	enum KeySignature{
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
		
		private KeySignature(int value){
			this.value = value;
		}
		
		private KeySignature(int value, Accidental accidental){
			this.value = value;
			this.accidental = accidental;
		}
		
		private int getValue() {
			return value;
		}
		
		private static KeySignature getKeySig(int value) {
			for (KeySignature ks : KeySignature.values()) {
				if (ks.getValue() == value) {
					return ks;
				}
			}
			return null;
		}
	}
	
	KeySignature extractKeySig(String keySig) {
		String[] parts = keySig.split(" ");
		
		// Key signature comes in form <ind> KeySig <num> <manor>
		return KeySignature.getKeySig(Integer.parseInt(parts[parts.length - 2]));
	}
	
	void parseMidiText() throws IOException {
		// Initializes readers
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		
		// Initializes current line and key signature index
		String line = "";
		
		while (!line.equals("TrkEnd")) {
			line = br.readLine();
			
			// Scans for key signature changes
			if (line.contains("KeySig")) {
				
				// Testing
				sop(extractKeySig(line));
			}
		}
		
		// This code keeps track of the length of the index, making the runtime O(n+logn) 
		// If used it should probably be put in another method.
		// I think the runtime of substring depends on the difference of the two indices
		// This optimizes theoretical runtime, but if we really want to optimize practical runtime, I think we should use c/c++
//		String line = "";
//		int indexLength = 1;
//		
//		while (!line.equals("TrkEnd")) {
//			line = br.readLine();
//			
//			while (Character.isDigit(line.charAt(indexLength - 1)) && line.charAt(indexLength) != ' ') indexLength++;
//			
//			// Scans for key signature changes
//			if (indexLength + 1 < line.length() && line.charAt(indexLength + 1) == 'K') {
//				KeySignature k;
//				int keyPos = indexLength + 8;
//				if (line.charAt(keyPos + 1) == ' ') {
//					k = KeySignature.getKeySig(line.charAt(keyPos) - '0');
//				} else {
//					k = KeySignature.getKeySig(Integer.parseInt(line.substring(keyPos, keyPos + 3)));
//				}
//				
//				// Testing
//				sop(k);
//			}
//		}
	}
	
	private static void sop(Object x) {
		System.out.println(x);
	}
	
	public static void main(String[] args) {
		NoteAnalyzer na = new NoteAnalyzer(new File("data/midi.txt"));
		try {
			na.parseMidiText();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sop(Note.getNote(74, Accidental.FLAT));
	}
}
