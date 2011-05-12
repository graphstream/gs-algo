package org.graphstream.algorithm.generator;

import static java.lang.Math.*;

/**
 * A generator following the small-world model of Watts and Strogatz.
 * 
 * <p>
 * This generator creates small-world graphs of arbitrary size.
 * </p>
 * 
 * <p>
 * This generator is based on the Watts-Strogatz model.
 * 
 * 
 * @reference Watts, D.J. and Strogatz, S.H.
 *            "Collective dynamics of 'small-world' networks". Nature 393
 *            (6684): 409–10. doi:10.1038/30918. PMID 9623998. 1998.
 */
public class WattsStrogatzGenerator extends BaseGenerator {
	/** The number of nodes to generate. */
	protected int n;
	
	/** Base degree of each node. */
	protected int k;
	
	/** Probability to "rewire" an edge. */
	protected double beta;
	
	/** Current rewired node, used to allo nextEvents() iteration. */
	protected int current;
	
	/**
	 * New Watts-Strogatz generator.
	 * @param n The number of nodes to generate.
	 * @param k The base degree of each node.
	 * @param beta Probability to "rewire" an edge.
	 */
	public WattsStrogatzGenerator(int n, int k, double beta) {
		keepNodesId = true;
		keepEdgesId = true;
		
		if(n<=k)
			throw new RuntimeException("parameter n must be >> k");
		if(beta<0 || beta>1)
			throw new RuntimeException("parameter beta must be between 0 and 1");
		if(k%2!=0)
			throw new RuntimeException("parameter k must be even");
		if(k<2)
			throw new RuntimeException("parameter k must be >= 2");
		
		this.n    = n;
		this.k    = k;
		this.beta = beta;
	}
	
	public void begin() {
		double step = (2*PI)/n;
		double x = 0;
		
		for(int i=0; i<n; i++) {
			addNode(nodeId(i), cos(x), sin(x));
			x += step;
		}
		
		// Add the circle links.
		
		int kk = k/2;
		
		for(int i=0; i<n; i++) {
			for(int j=1; j<=kk; j++) {
				int jj = (i+j)%n;
				addEdge(edgeId(i, jj),nodeId(i),nodeId(jj));
			}
		}
		
		current = 0;
	}

	public boolean nextEvents() {
		int kk = k/2;
		
		if(current < n) {
			for(int j=1; j<=kk; j++) {
				int jj = (current+j)%n;
				
				if(random.nextDouble() < beta) {
					delEdge(edgeId(current, jj));
					int newTarget = chooseNewNode(current, jj);
					addEdge(edgeId(current, newTarget), nodeId(current), nodeId(newTarget));
				}
			}
			
			current += 1;
			
			return true;
		} else {
			return false;
		}
	}

	
	@Override
	public void end() {
		super.end();
	}
	
	protected String nodeId(int id) {
		return String.format("%d", id);
	}
	
	protected String edgeId(int from, int to) {
		return String.format("%d_%d", from, to);
	}
	
	protected int chooseNewNode(int avoid, int old) {
		int newId = 0;
		boolean exists = true;
		
		do {
			newId  = random.nextInt(n);
			exists = edgesData.get(edgeId(avoid, newId)) != null; 
		} while(newId == avoid || exists);
		
		return newId;
	}
}