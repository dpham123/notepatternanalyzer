package hmm;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CPT {

	private List<List<Double>> cpt;
	private List<Boolean> normalized;
	private int numToStates = 0, numFromStates = 0;
	private static final double correctionOffsetRatio = 0.2;
	
	public CPT() {
		cpt = new ArrayList<List<Double>>();
		normalized = new ArrayList<Boolean>();
	}
	
	public CPT(String filename) {
		this();
		parse(new File(filename));
	}
	
	private void addFromState(int toAdd) {
		for (int i = 0; i < toAdd; i++) {
			List<Double> temp = new ArrayList<>();
			for (int j = 0; j < numToStates; j++) {
				temp.add(0.0);
			}
			cpt.add(temp);
			normalized.add(false);
		}
		numFromStates += toAdd;
	}
	
	private void addToState(int toAdd) {
		for (List<Double> row : cpt) {
			for (int i = 0; i < toAdd; i++) {
				row.add(0.0);
			}
		}
		numToStates += toAdd;
	}
	
	public int getNumFromStates() {
		return numFromStates;
	}
	
	public int getNumToStates() {
		return numToStates;
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
	
	public CPT powerup() {
		int poweredStates = 1 << numToStates;
		CPT powered = new CPT();
		powered.addToState(poweredStates);
		powered.addFromState(numFromStates);
		for (int i = 0; i < numFromStates; i++) {
			for (int j = 0; j < poweredStates; j++) {
				double sum = 0.0;
				for (int k = 0; k < numToStates; k++) {
					if ((j & (1 << k)) != 0) sum += this.get(i, k);
				}
				powered.set(i, j, sum);
			}
		}
		return powered;
	}
	
	private void parse(File file) {
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				
				// Expand states
				this.addFromState(1);
				if (parts.length > numToStates) this.addToState(parts.length - numToStates);
				
				for (int i = 0; i < parts.length; i++) {
					this.set(numFromStates - 1, i, Double.parseDouble(parts[i]));
				}
			}
			
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		String str = "";
		int rowInd = 0;
		for (int i = 0; i < numFromStates; i++) {
			for (int j = 0; j < numToStates; j++) {
				str += String.format("%6.3e\t", this.get(i, j));
			}
			str += "\n";
			rowInd++;
		}
		return str;
	}
}