package hmm;

import java.util.ArrayList;
import java.util.List;

public class CPT {

	private List<List<Double>> cpt;
	private List<Boolean> normalized;
	private int numToStates = 0;
	private static final double correctionOffsetRatio = 0.2;
	
	public CPT() {
		cpt = new ArrayList<List<Double>>();
		normalized = new ArrayList<Boolean>();
	}
	
	public void addFromState(int toAdd) {
		for (int i = 0; i < toAdd; i++) {
			List<Double> temp = new ArrayList<>();
			for (int j = 0; j < numToStates; j++) {
				temp.add(0.0);
			}
			cpt.add(temp);
			normalized.add(false);
		}
	}
	
	public void addToState(int toAdd) {
		for (List<Double> row : cpt) {
			for (int i = 0; i < toAdd; i++) {
				row.add(0.0);
			}
		}
		numToStates += toAdd;
	}
	
	public double get(int from, int to) {
		if (!normalized.get(from)) normalize(from);
		return cpt.get(from).get(to);
	}
	
	public void set(int from, int to, double value) {
		cpt.get(from).set(to, value);
		normalized.set(from, false);
	}
	
	private void normalize(int rowInd) {
		List<Double> row = cpt.get(rowInd);
		if (!row.isEmpty()) {
			double min = row.get(0);
			double max = min;
			double sum = min;
			for (int i = 1; i < row.size(); i++) {
				double x = row.get(i);
				min = (x < min) ? x : min;
				max = (x > max) ? x : max;
				sum += x;
			}
			// Correct negatives (note that this causes all elements to be nonzero)
			double correction = 0.0;
			if (min < 0.0 || min == 0.0 && sum == 0.0) {
				correction = -min + (max - min) * correctionOffsetRatio;
				sum += row.size() * correction;
			}
			// Normalize
			for (int i = 0; i < row.size(); i++) {
				row.set(i, (row.get(i) + correction) / sum);
			}
		}
		normalized.set(rowInd, true);
	}
	
	public void FRIGGIN_POWER_UP() {
		// TODO (this gunna be great)
	}
	
	@Override
	public String toString() {
		String str = "";
		int rowInd = 0;
		for (List<Double> row : cpt) {
			if (!normalized.get(rowInd)) normalize(rowInd);
			for (Double x : row) {
				str += String.format("%6.3e\t", x);
			}
			str += "\n";
			rowInd++;
		}
		return str;
	}
}
