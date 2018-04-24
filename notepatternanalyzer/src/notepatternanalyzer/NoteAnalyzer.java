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
	
	enum KeySignature{
		C(0), 
		Cs(7),
		Db(251),
		D(2),
		Eb(253),
		E(4),
		F(255),
		Fs(6),
		Gb(250),
		G(1),
		Ab(252),
		A(3),
		Bb(254),
		B(5),
		Cb(249);
		
		private int value;
		
		private KeySignature(int value){
			this.value = value;
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
	}
}