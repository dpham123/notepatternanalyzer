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
		Db(251),
		D(2),
		Eb(253),
		E(4),
		F(255),
		Gb(250),
		G(1),
		Ab(252),
		A(3),
		Bb(254),
		B(5);
		
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
	
	/**
	 * 
	 * @param index
	 * @param keySig
	 * @return
	 */
	KeySignature extractKeySig(int index, String keySig) {
		String keySignature = "";
		index += 7;
		
		// Scans for specific key signature in line
		while (!keySig.substring(index, index + 1).equals(" ")) {
			keySignature += keySig.substring(index, index + 1);
			index++;
		}
		
		return KeySignature.getKeySig(Integer.parseInt(keySignature));
	}
	
	void parseMidiText() throws IOException {
		// Initializes readers
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		
		// Initializes current line and key signature index
		String line = "";
		int keySigIndex = -1;
		
		while (!line.equals("TrkEnd")) {
			line = br.readLine();
			keySigIndex = line.indexOf("KeySig");
			
			// Scans for key signature changes
			if (keySigIndex != -1) {
				
				// Testing
				sop(extractKeySig(keySigIndex, line));
			}
		}
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
