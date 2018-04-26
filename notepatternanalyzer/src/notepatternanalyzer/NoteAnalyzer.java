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
		
		// Test
		sop("------------------");
		for (int i = 3; i < 15; i++) {
			sop(Note.getNote(i, KeySignature.E_FLAT));
		}
	}
}
