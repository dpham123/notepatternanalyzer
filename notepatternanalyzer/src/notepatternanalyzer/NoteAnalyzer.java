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
		Db(250),
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
	
	void parseMidiText() throws IOException {
		// Initializes readers
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		
		// Initializes current line and key signature
		String line = "";
		int keySig = -1;
		
		while (!line.equals("TrkEnd")) {
			line = br.readLine();
			keySig = line.indexOf("KeySig");
			
			// Scans for key signature changes
			if (keySig != -1) {
				keySig += 7;
				String keySignature = "";
				
				// Scans for specific key signature in line
				while (!line.substring(keySig, keySig + 1).equals(" ")) {
					keySignature += line.substring(keySig, keySig + 1);
					keySig++;
				}
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
		
		sop(KeySignature.getKeySig(1));
	}
}
