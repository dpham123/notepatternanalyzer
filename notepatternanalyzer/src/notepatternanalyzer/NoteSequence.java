package notepatternanalyzer;

import java.util.*;

/**
 * Prototype data structure to store and iterate over note sequences
 */
public class NoteSequence implements Iterable<NoteNode> {

	public int size() {
		return 0;
	}

	public NoteNode get(int i) {
		return null;
	}

	public Iterator<NoteNode> iterator() {
		return new NoteIterator();
	}

	class NoteIterator implements Iterator<NoteNode> {

		private int index = 0;

		public boolean hasNext() {
			return index < size();
		}

		public NoteNode next() {
			return null;
		}

		public void remove() {
			
		}
	}
}
