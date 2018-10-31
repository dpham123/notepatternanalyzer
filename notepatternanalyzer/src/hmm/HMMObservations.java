package hmm;

public interface HMMObservations<T extends HMMObservable> extends Iterable<T> {
	public int size();
}
