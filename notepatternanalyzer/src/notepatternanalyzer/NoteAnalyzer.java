package notepatternanalyzer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

class NoteAnalyzer {
	private File file;
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
			sop(bpb + "/" + beatNote);
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
	
	private void updateAlreadyPrinted(boolean alreadyPrinted) {
		this.alreadyPrinted = alreadyPrinted;
	}
	
	private static void sop(Object x) {
		System.out.println(x);
	}
	
	public static void main(String[] args) {
		NoteAnalyzer na = new NoteAnalyzer(new File("data/input/animenzSample1.txt"));
		
		NoteSequence ns;
		try {
			ns = new NoteSequence(na.file);
			
			for (NoteCluster notes : ns) {
				na.printTimeSig(notes, ns);
				na.printTempo(notes);
				na.printMeasure(notes);
				sop(notes);
				na.printToFile(notes.toString());
				na.updateAlreadyPrinted(false);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}