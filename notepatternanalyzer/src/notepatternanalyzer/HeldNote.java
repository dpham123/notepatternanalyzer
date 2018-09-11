package notepatternanalyzer;

public class HeldNote {
	private int timestamp;
	private int value;
	private int duration = 0;
	
	private KeySignature keySig = KeySignature.C;
	private int tempo = 500000;
	private int bpb = 4;
	private int beatNote = 4;
	private int ppq = 96;
	
	public HeldNote(HeldNote n) {
		this(n.timestamp, n.value, n.duration, n.keySig, n.tempo, n.bpb, n.beatNote, n.ppq);
	}
	
	public HeldNote(int timestamp, int value, KeySignature keySig, int tempo, int bpb, int beatNote, int ppq) {
		this(timestamp, value, 0, keySig, tempo, bpb, beatNote, ppq);
	}
	
	public HeldNote(int timestamp, int value, int duration, KeySignature keySig, int tempo, int bpb, int beatNote, int ppq) {
		this.timestamp = timestamp;
		this.value = value;
		this.duration = duration;
		this.keySig = keySig;
		this.tempo = tempo;
		this.bpb = bpb;
		this.beatNote = beatNote;
		this.ppq = ppq;
	}
	
	public int getStartTime() {
		return timestamp;
	}
	
	public int getRawValue() {
		return value;
	}
	
	public Note getNote() {
		return Note.getNote(value, keySig);
	}
	
	public int getValue() {
		return value % 12;
	}
	
	public int getOctave() {
		return value / 12 - 1;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public void convertMusescoreDuration() {
		if (getMusescoreDuration(this.duration) != -1) {
			this.duration = getMusescoreDuration(this.duration);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof HeldNote) {
			return ((HeldNote) o).getRawValue() == this.getRawValue();
		}
		return false;
	}
	
	public String toString() {
		return "[" + this.getNote() + "_" + this.getOctave() + ":" + (duration == 0 ? "?" : (duration / ppq / 4)) + "]";
	}
	
	private static int getMusescoreDuration(int ticks) {
		switch (ticks) {
		case 13:	// 1/128
			return 15;
		case 27: 	// 1/64
			return 30;
		case 37:	// 1/48
			return 40;
		case 41:	// 3/128
			return 45;
		case 56:	// 1/32
			return 60;
		case 75:	// 1/24
			return 80;
		case 84:	// 3/64
			return 90;
		case 90:	// 1/20
			return 96;
		case 113:	// 1/16
			return 120;
		case 151:	// 1/12
			return 160;
		case 170:	// 3/32
			return 180;
		case 227:	// 1/8
			return 240;
		case 303:	// 1/6
			return 320;
		case 341:	// 3/16
			return 360;
		case 455:	// 1/4
			return 480;
		case 683:	// 3/8
			return 720;
		case 911:	// 1/2
			return 960;
		case 1367:	// 3/4
			return 1440;
		case 1823:	// 1
			return 1920;
		case 2051:	// 9/8
			return 2160;
		case 2279:	// 5/4
			return 2400;
		case 2735:	// 3/2
			return 2880;
		case 3191:	// 7/4
			return 3360;
		case 3647:	// 2
			return 3840;
		}
		return -1;
	}

	public int getEndTime() {
		return timestamp + duration;
	}
}
