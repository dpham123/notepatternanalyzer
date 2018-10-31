package notepatternanalyzer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import hmm.*;

class NoteAnalyzer {
	private File file;
	private int prevHeldNoteValue = 0;
	private int tempo;
	private int bpb;
	private int beatNote;
	private int measureDurationInTicks;
	private int nextMeasureInTicks;
	private int currentMeasure;
	private boolean alreadyPrinted;
	
	NoteAnalyzer(File file){
		this.file = file;
		nextMeasureInTicks = 0;
		currentMeasure = 1;
		alreadyPrinted = false;
	}

	private void printTempo(NoteCluster notes) throws IOException {
		if (notes.getTempo() != tempo) {
			tempo = notes.getTempo();
			
			if (!alreadyPrinted) {
				sop("=================================================");
				printToFile("=================================================");
			}
			sop("Tempo: " + tempo);
			printToFile("Tempo: " + tempo);
		}
	}
	
	private void printTimeSig(NoteCluster notes, NoteSequence ns) throws IOException {
		if (notes.getBpb() != bpb || notes.getBeatNote() != beatNote) {
			sop("=================================================");
			printToFile("=================================================");
			bpb = notes.getBpb();
			beatNote = notes.getBeatNote();
			int ppq = ns.getPpq();
			alreadyPrinted = true;
			
			measureDurationInTicks = (ppq * beatNote) * (bpb / beatNote);
			sop(bpb + "/" + beatNote + " " + notes.getKeySignature());
			printToFile(bpb + "/" + beatNote);
		}
	}
	
	private int calculateNextMeasure(int oldMeasureTimeStamp) {
		return oldMeasureTimeStamp + measureDurationInTicks;
	}
	
	private void printMeasure(NoteCluster notes) throws IOException {
		if (notes.getTimeStamp() >= nextMeasureInTicks) {
			
			if (!alreadyPrinted) {
				sop("=================================================");
				printToFile("=================================================");
			}
			
			sop("Measure " + currentMeasure);
			printToFile("Measure " + currentMeasure);
			sop("=================================================");
			printToFile("=================================================");
			nextMeasureInTicks = calculateNextMeasure(nextMeasureInTicks);
			currentMeasure++;
		}
	}
	
	/**
	 * Prints data to file in directory "data/output" if printToFile is true in the config file
	 * @param string to print
	 * @throws IOException
	 */
	private void printToFile(String string) throws IOException {
		if (PropertyAccessObject.getProperty("printToFile").equals("true")) {
			String[] filename = file.getName().split("data/input/");
			FileWriter fw = new FileWriter(new File("data/output/" + filename[0]), true);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(string);
			
			pw.close();
			fw.close();
		}
	}
	/**
	 * Calculates distance between the note of a melody (track 1) and the note 
	 * immediately before the specified note. Works only with midis with 2 tracks. 
	 * If multiple notes are found in the same track, the highest note will be used.
	 * @author UnknownEX1
	 * @param notes The NoteCluster of notes or 0 if no notes in track 1
	 * @return relativeDistance The relative distance between the two notes
	 */
	private int calculateRelativeDistance(NoteCluster notes) {
		List<HeldNote> heldNotes = notes.getNotes();
		HeldNote highestHeldNote = new HeldNote(0,0,KeySignature.C, 0, 
				0, 0, 0, -1);
		
		// Finds the highest note of the notes in track 1
		for (HeldNote hn : heldNotes) {
			if (hn.getTrack() == 1 && hn.getStartTime() == notes.getTimeStamp()) {
				if (hn.getRawValue() > highestHeldNote.getRawValue()) {
					highestHeldNote = hn;
				}
			}
		}
		
		if (highestHeldNote.getTrack() == -1) {
			return 0;
		}
		
		int relativeDistance = highestHeldNote.getRawValue() - prevHeldNoteValue;
		prevHeldNoteValue = highestHeldNote.getRawValue();
		return relativeDistance;
	}
	
	private String printNotes(NoteCluster nc) {
		// 86 was chosen to be the place to print the relative distances because
		// the longest note cluster string length was 85.
		String s = nc.toString();
		int numberOfSpaces = 86 - s.length();
				
		// Appends spaces to print relative distances
		for (int i = 0; i < numberOfSpaces; i++) {
			s += " ";
		}
		
		s += calculateRelativeDistance(nc);
		return s;
	}
	
	private void updateAlreadyPrinted(boolean alreadyPrinted) {
		this.alreadyPrinted = alreadyPrinted;
	}
	
	private static void sop(Object x) {
		System.out.println(x);
	}
	
	public static void main(String[] args) {
		NoteAnalyzer na = new NoteAnalyzer(new File("data/input/theishterSample2.txt"));
		NoteSequence ns;
		try {
			ns = new NoteSequence(na.file);
			
			HMM<NoteCluster> chordGuesser = HMM.createHMM(
					new CPT("data/cpt/aluminaInitial1"),
					new CPT("data/cpt/aluminaTransition1"),
					(new CPT("data/cpt/aluminaEmission1")).powerup(),
					HMM.readLabels("data/cpt/aluminaLabels1"),
					ns);
			if (chordGuesser == null) {
				System.out.println("blah");
				System.out.println(ns.size());
				return;
			}
			List<String> guessedChords = chordGuesser.inferHidden();
			int i = 0;
			
			for (NoteCluster notes : ns) {
				na.printTimeSig(notes, ns);
				na.printTempo(notes);
				na.printMeasure(notes);
				
				sop(na.printNotes(notes) + " " + guessedChords.get(i++));
				
				na.printToFile(na.printNotes(notes));
				na.updateAlreadyPrinted(false);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}