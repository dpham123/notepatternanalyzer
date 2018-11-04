package hmm;

import java.io.*;
import java.util.*;

public class HMM<T extends HMMObservable> {
	private CPT initial;
	private CPT transition;
	private CPT emission;
	private List<String> labels;
	private HMMObservations<T> observations;
	
	private HMM(CPT init, CPT trans, CPT emit, List<String> labels, HMMObservations<T> observ) {
		this.initial = init;
		this.transition = trans;
		this.emission = emit;
		this.labels = labels;
		this.observations = observ;
	}
	
	public static <T extends HMMObservable> HMM<T> createHMM(CPT init, CPT trans, CPT emit, List<String> labels, HMMObservations<T> observ) {
		// Check sizes
		int n = labels.size();
		if (n != init.getNumToStates() || n != trans.getNumFromStates() || n != trans.getNumToStates() || n != emit.getNumFromStates()) {
			System.out.println("invalid file size " + n + " " + init.getNumToStates() + " " + trans.getNumFromStates() + " " + trans.getNumToStates() + " " + emit.getNumFromStates());
			return null;
		}
		
		// check empty observations
		if (observ.size() == 0) {
			System.out.println("empty observations");
			return null; 
		}
		
		return new HMM<T>(init, trans, emit, labels, observ);
	}
	
	public static List<String> readLabels(String filename) {
		try {
			FileReader fr = new FileReader(new File(filename));
			BufferedReader br = new BufferedReader(fr);
			
			String line;
			List<String> labels = new ArrayList<>();
			while ((line = br.readLine()) != null) labels.add(line);
			
			br.close();
			
			return labels;
			
		} catch (IOException e) {
			return null;
		}
	}
	
	public List<String> inferHidden() {
            
        // Perform Viterbi algorithm
		int n = labels.size();
		int T = observations.size();
		
        double[] liLast = new double[n];
        double[] liCurr = new double[n];
        int[][] phi = new int[T][];
        double max;
        int argmax;
        
        // Get observation iterator
        Iterator<T> iter = observations.iterator();

        // base case t=0
        int observation = iter.next().getState();
        System.out.print(observation + " ");
        for (int i = 0; i < n; i++) {
            liLast[i] = Math.log10(initial.get(0, i) * emission.get(i, observation));
            System.out.print(liLast[i] + " ");
        }
        System.out.println();

        // recursive step
        for (int t = 1;iter.hasNext();t++) {
        	observation = iter.next().getState();
        	
            phi[t] = new int[n];
            for (int j = 0; j < n; j++) {

                // get max
                max = liLast[0] + Math.log10(transition.get(0, j));
                argmax = 0;
                for (int i = 1; i < n; i++) {
                    double expr = liLast[i] + Math.log10(transition.get(i, j));
                    if (expr > max) {
                        max = expr;
                        argmax = i;
                    }
                }

                // set l matrix
                liCurr[j] = max + Math.log10(emission.get(j, observation));

                // set phi matrix
                phi[t][j] = argmax;
                System.out.print(phi[t][j] + " ");
            }

            // update last seen l
            System.out.print("\n" + observation + " ");
            for (int i = 0; i < n; i++) {
                liLast[i] = liCurr[i];
                System.out.print(liLast[i] + " ");
            }
            System.out.println();
        }

        // backtrack to get most likely set of states
        // base case
        max = liCurr[0];
        argmax = 0;
        for (int i = 1; i < n; i++) {
            if (liCurr[i] > max) {
                max = liCurr[i];
                argmax = i;
            }
        }

        // backtracking
        int state = argmax;
        List<String> hiddenStates = new LinkedList<>();
        hiddenStates.add(labels.get(argmax));
        for (int t = T - 1; t > 0; t--) {
        	state = phi[t][state];
            hiddenStates.add(labels.get(state));
        }
        
        return hiddenStates;
	}
}
