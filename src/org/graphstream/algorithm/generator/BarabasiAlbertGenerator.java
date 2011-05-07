package org.graphstream.algorithm.generator;

import java.util.ArrayList;

/**
 * Scale-free graph generator using the preferential attachment rule as defined
 * in the Barabási-Albert model.
 * 
 * <p>
 * This is a very simple graph generator that generates a graph using the
 * preferential attachment rule defined in the Barabási-Albert model: nodes are
 * generated one by one, and each time attached by one or more edges other
 * nodes. The other nodes are chosen using a biased random selection giving more
 * chance to a node if it has a high degree.
 * </p>
 * 
 * <p>
 * The more this generator is iterated, the more nodes are generated. It can
 * therefore generate graphs of any size. One node is generated at each call to
 * {@link #nextEvents()}. At each node added at least one new edge is added. The
 * number of edges added at each step is given by the
 * {@link #getMaxLinksPerStep()}. However by default the generator creates a
 * number of edges per new node chosen randomly between 1 and
 * {@link #getMaxLinksPerStep()}. To have exactly this number of edges at each
 * new node, use {@link #setExactlyMaxLinksPerStep(boolean)}.
 * </p>
 * 
 * @reference Albert-László Barabási & Réka Albert
 *            "Emergence of scaling in random networks", Science 286: 509–512.
 *            October 1999. doi:10.1126/science.286.5439.509.
 */
public class BarabasiAlbertGenerator extends BaseGenerator {
	/**
	 * Degree of each node.
	 */
	protected ArrayList<Integer> degrees;

	/**
	 * Maximal degree at time t.
	 */
	protected int degreeMax = 0;

	/**
	 * Number of edges.
	 */
	protected int edgesCount = 0;
	
	/**
	 * The maximum number of links created when a new node is added.
	 */
	protected int maxLinksPerStep = 1;
	
	/**
	 * Does the generator generates exactly {@link #maxLinksPerStep}.
	 */
	protected boolean exactlyMaxLinksPerStep = false;
	
	/**
	 * New generator.
	 */
	public BarabasiAlbertGenerator() {
		this(1, false);
	}
	
	public BarabasiAlbertGenerator(int maxLinksPerStep) {
		this(maxLinksPerStep, false);
	}
	
	public BarabasiAlbertGenerator(int maxLinksPerStep, boolean exactlyMaxLinksPerStep) {
		this.directed               = false;
		this.maxLinksPerStep        = maxLinksPerStep;
		this.exactlyMaxLinksPerStep = exactlyMaxLinksPerStep;
	}

	/**
	 * Maximum number of edges created when a new node is added.
	 * @return The maximum number of links per step.
	 */
	public int getMaxLinksPerStep() {
		return maxLinksPerStep;
	}
	
	/**
	 * True if the generator produce exactly {@link #getMaxLinksPerStep()}, else it produce
	 * a random number of links ranging between 1 and {@link #getMaxLinksPerStep()}.
	 * @return Does the generator generates exactly {@link #getMaxLinksPerStep()}.
	 */
	public boolean produceExactlyMaxLinkPerStep() {
		return exactlyMaxLinksPerStep;
	}
	
	/**
	 * Set how many edge (maximum) to create for each new node added.
	 * @param max The new maximum, it must be strictly greater than zero.
	 */
	public void setMaxLinksPerStep(int max) {
		maxLinksPerStep = max>0 ? max : 1;
	}
	
	/**
	 * Set if the generator produce exactly {@link #getMaxLinksPerStep()} (true), else it produce
	 * a random number of links ranging between 1 and {@link #getMaxLinksPerStep()} (false).
	 * @param on Does the generator generates exactly {@link #getMaxLinksPerStep()}.
	 */
	public void setExactlyMaxLinksPerStep(boolean on) {
		exactlyMaxLinksPerStep = on;
	}

	/**
	 * Start the generator. A single node is added.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
		this.degrees = new ArrayList<Integer>();
		this.degreeMax = 0;

		addNode("0");
		degrees.add(0);
	}

	/**
	 * Step of the generator. Add a node and try to connect it with some others.
	 * 
	 * The number of links is randomly chosen between 1 and the maximum number of
	 * links per step specified in {@link #setMaxLinksPerStep(int)}.
	 * 
	 * The complexity of this method is O(n) with n the number of nodes if the
	 * number of edges created per new node is 1, else it is O(nm) with m
	 * the number of edges generated per node.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		// Generate a new node.

		int    index = degrees.size();
		String id    = Integer.toString(index);
		int    n     = maxLinksPerStep;

		addNode(id);
		degrees.add(0);
		
		if(! exactlyMaxLinksPerStep)
			n = random.nextInt(n) + 1;
		
		// Choose the nodes to attach to.

		for(int i=0; i<n; i++) {
			attachToOtherNode(i, index, id, chooseAnotherNode(index));
		}

		// It is always possible to add an element.

		return true;
	}

	/**
	 * Randomly choose a node to attach to, the node is chosen 
	 * @param index
	 * @return
	 */
	protected int chooseAnotherNode(int index) {
		int    sumDeg   = (edgesCount-degrees.get(index)) * 2;
		double sumProba = 0;
		double rnd      = random.nextDouble();
		int    otherIdx = -1;

		for(int i = 0; i < index; ++i) {
			double proba = sumDeg == 0 ? 1 : degrees.get(i) / ((double) sumDeg);

			sumProba += proba;

			if (sumProba > rnd) {
				otherIdx = i;
				break;
			}
		}
		
		return otherIdx;
	}

	protected void attachToOtherNode(int i, int index, String id, int otherIdx) {
		if (otherIdx >= 0) {
			String oid = Integer.toString(otherIdx);
			String eid = id + "_" + oid + "_" + i;

			addEdge(eid, oid, id);
			edgesCount++;
			degrees.set(otherIdx, degrees.get(otherIdx) + 1);
			degrees.set(index, degrees.get(index) + 1);
		} else {
			System.err.printf("PreferentialAttachmentGenerator: *** Aieuu!%n");
		}
	}
	
	/**
	 * Clean degrees.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#end()
	 */
	@Override
	public void end() {
		degrees.clear();
		degrees = null;
		degreeMax = 0;
		super.end();
	}
}