package notepatternanalyzer;

import java.io.File;
import java.io.IOException;

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

	private void printTempo(NoteCluster notes) {
		if (notes.getTempo() != tempo) {
			tempo = notes.getTempo();
			
			if (!alreadyPrinted) {
				sop("=================================================");
			}
			sop("Tempo: " + tempo);
		}
	}
	
	private void printTimeSig(NoteCluster notes, NoteSequence ns) {
		if (notes.getBpb() != bpb || notes.getBeatNote() != beatNote) {
			sop("=================================================");
			bpb = notes.getBpb();
			beatNote = notes.getBeatNote();
			int ppq = ns.getPpq();
			alreadyPrinted = true;
			
			measureDurationInTicks = (ppq * beatNote) * (bpb / beatNote);
			sop(bpb + "/" + beatNote);
		}
	}
	
	private int calculateNextMeasure(int oldMeasureTimeStamp) {
		return oldMeasureTimeStamp + measureDurationInTicks;
	}
	
	private void printMeasure(NoteCluster notes) {
		if (notes.getTimeStamp() >= nextMeasureInTicks) {
			
			if (!alreadyPrinted) {
				sop("=================================================");
			}
			
			sop("Measure " + currentMeasure);
			sop("=================================================");
			nextMeasureInTicks = calculateNextMeasure(nextMeasureInTicks);
			currentMeasure++;
		}
	}
	
	private void updateAlreadyPrinted(boolean alreadyPrinted) {
		this.alreadyPrinted = alreadyPrinted;
	}
	
	private static void sop(Object x) {
		System.out.println(x);
	}
	
	public static void main(String[] args) {
		NoteAnalyzer na = new NoteAnalyzer(new File("data/theishterSample2.txt"));
		
		NoteSequence ns;
		try {
			ns = new NoteSequence(na.file);
			
			for (NoteCluster notes : ns) {
				na.printTimeSig(notes, ns);
				na.printTempo(notes);
				na.printMeasure(notes);
				sop(notes);
				na.updateAlreadyPrinted(false);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}