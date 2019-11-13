/*
 * This file is part of GraphStream <http://graphstream-project.org>.
 * 
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 * 
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 *
 *
 * @since 2010-07-23
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.atan;

/**
 * Generate a <b>social</b> dynamic graph. Graph is composed of high-connected
 * group of nodes, modeling organizations, and few connections between
 * organizations.
 * 
 * This is done by creating <i>points of interest</i>. Nodes can be interested
 * by these points or loose them interest. When two nodes are interested by at
 * least one common point, then there are connected.
 * 
 * Some probabilities can be set defining the following events :
 * <ul>
 * <li>remove a node ;</li>
 * <li>add a node ;</li>
 * <li>remove a point of interest ;</li>
 * <li>add a point of interest.</li>
 * </ul>
 * 
 * Initial parameters are :
 * <ul>
 * <li>initial count of points of interest ;</li>
 * <li>initial count of nodes ;</li>
 * </ul>
 * 
 * @author Guilhelm Savin
 * 
 */
public class PointsOfInterestGenerator extends BaseGenerator {
	/**
	 * Defines a point of interest. It is just a set of <i>addicted</i> nodes.
	 */
	protected class PointOfInterest {
		/**
		 * Set of nodes interested by this point.
		 */
		Set<Addict> addict;

		PointOfInterest() {
			addict = new HashSet<Addict>();
		}

		/**
		 * Registers a node as an addict of this point. The node will be linked
		 * to all nodes already addict of this point. The list of points of
		 * interest of the node will be updated.
		 * 
		 * @param addictA
		 *            the addicted node
		 */
		void newAddict(Addict addictA) {
			if (!addict.contains(addictA)) {
				addict.forEach(addictB -> addictA.link(addictB));

				addict.add(addictA);
				addictA.pointsOfInterest.add(this);
			}
		}

		/**
		 * Unregisters a node. The node will be unlinked to all nodes already
		 * addict of this point. The list of points of interest of the node will
		 * be updated.
		 * 
		 * @param addictA
		 *            the addicted node
		 */
		void delAddict(Addict addictA) {
			if (addict.contains(addictA)) {
				addict.remove(addictA);
				addictA.pointsOfInterest.remove(this);

				addict.forEach(addictB -> addictA.unlink(addictB));
			}
		}

		/**
		 * Check is a node is addict of this point.
		 * 
		 * @param a
		 *            the addict
		 * @return true if a is addict of this point
		 */
		boolean isAddict(Addict a) {
			return addict.contains(a);
		}
	}

	protected static class AddictNeighbor {
		AtomicInteger counter;
		boolean connected;

		public AddictNeighbor() {
			this.counter = new AtomicInteger(0);
			this.connected = false;
		}

		public AddictNeighbor(AtomicInteger i) {
			this.counter = i;
			this.connected = false;
		}

		int incrementAndGet() {
			return counter.incrementAndGet();
		}

		int decrementAndGet() {
			return counter.decrementAndGet();
		}

		boolean isConnected() {
			return connected;
		}
	}

	/**
	 * Defines data of a node. We have to keep id of the node and to backup
	 * points of interest of this node and neighbor of the node.
	 */
	protected class Addict {
		/**
		 * Id of the node.
		 */
		String id;

		/**
		 * List of points of interest of this node.
		 */
		LinkedList<PointOfInterest> pointsOfInterest;

		/**
		 * List of neighbors.
		 */
		Map<Addict, AddictNeighbor> neighbor;

		Addict(String id) {
			this.id = id;
			pointsOfInterest = new LinkedList<PointOfInterest>();
			neighbor = new HashMap<Addict, AddictNeighbor>();
		}

		/**
		 * Defines a step for a node. Node will iterate over points-of-interest.
		 * For each point p, if node is already interest by p, node will check
		 * if it is still interested by this point (according to
		 * <i>lostInterestProbability</i> probability). Else, node will checked
		 * if it can be interested by p, according to
		 * <i>haveInterestProbability</i> probability and its points count (the
		 * probability will decrease when the count of points increases).
		 */
		void step() {
			//
			// Avoid that all nodes are interested by the same point.
			//
			Collections.shuffle(
					PointsOfInterestGenerator.this.pointsOfInterest, random);
			
			
			PointsOfInterestGenerator.this.pointsOfInterest.forEach(poi -> {
				if (pointsOfInterest.contains(poi)) {
					if (random.nextFloat() < lostInterestProbability)
						poi.delAddict(this);
				} else {
					double p = atan(20.0
							* min(pointsOfInterest.size(),
									averagePointsOfInterestCount)
							/ (double) averagePointsOfInterestCount - 10);
					p = (p - atan(-10)) / (atan(10) - atan(-10));

					if (random.nextFloat() < haveInterestProbability * (1 - p))// pow(
																				// haveInterestProbability,
																				// 1.2
																				// *
																				// pointsOfInterest.size()
																				// )
																				// )
						poi.newAddict(this);
				}
			});
		}

		/**
		 * Link this node to another. Both nodes will share a common counter.
		 * Links these two nodes will increase the counter and so unlink will
		 * decrease the counter. If counter not exists, it is initialized and
		 * edge is created. Else, if counter is equal to zero, counter is
		 * removed and edge is removed too.
		 * 
		 * @param a
		 *            the node to link
		 */
		void link(Addict a) {
			if (!neighbor.containsKey(a)) {
				AddictNeighbor an = new AddictNeighbor();
				neighbor.put(a, an);
				a.neighbor.put(this, an);
			}

			AddictNeighbor an = neighbor.get(a);

			if (an.incrementAndGet() >= linksNeededToCreateEdge
					&& !an.connected) {
				if (random.nextDouble() < pow(linkProbability,
						1.0 / (double) (an.counter.get()
								- linksNeededToCreateEdge + 1))) {
					an.connected = true;
					PointsOfInterestGenerator.this.addEdge(getEdgeId(id, a.id),
							id, a.id);
				}
			}
		}

		/**
		 * Unlink this node with another. Links-counter between these two nodes
		 * is decreased and edge is removed is needed.
		 * 
		 * @param a
		 *            the node to unlink
		 */
		void unlink(Addict a) {
			if (neighbor.containsKey(a)) {
				if (neighbor.get(a).decrementAndGet() < linksNeededToCreateEdge) {
					neighbor.remove(a);
					a.neighbor.remove(this);
					PointsOfInterestGenerator.this.delEdge(getEdgeId(id, a.id));
				}
			}
		}

		/**
		 * Unlink all neighbor.
		 */
		void fullUnlink() {
			
			neighbor.keySet().forEach(a -> {
				a.neighbor.remove(this);
				PointsOfInterestGenerator.this.delEdge(getEdgeId(id, a.id));
			});
			
			neighbor.clear();
		}
	}

	protected static String getEdgeId(String nodeA, String nodeB) {
		return nodeA.compareTo(nodeB) < 0 ? String.format("%s---%s", nodeA,
				nodeB) : String.format("%s---%s", nodeB, nodeA);
	}

	public static enum Parameter {
		INITIAL_PEOPLE_COUNT, ADD_PEOPLE_PROBABILITY, DEL_PEOPLE_PROBABILITY, INITIAL_POINT_OF_INTEREST_COUNT, AVERAGE_POINTS_OF_INTEREST_COUNT, ADD_POINT_OF_INTEREST_PROBABILITY, DEL_POINT_OF_INTEREST_PROBABILITY, HAVE_INTEREST_PROBABILITY, LOST_INTEREST_PROBABILITY, LINKS_NEEDED_TO_CREATE_EDGE, LINK_PROBABILITY
	}

	/**
	 * Initial count of nodes.
	 */
	protected int initialPeopleCount;

	/**
	 * Probability to add a node during a step.
	 */
	protected float addPeopleProbability;

	/**
	 * Probability to remove a node during a step.
	 */
	protected float delPeopleProbability;

	/**
	 * Probability that a node becomes interested in a point-of-interest it was
	 * not already interested.
	 */
	protected float haveInterestProbability;

	/**
	 * Probability that a node looses its interest for a point-of-interest.
	 */
	protected float lostInterestProbability;

	/**
	 * Initial count of point-of-interest.
	 */
	protected int initialPointOfInterestCount;

	/**
	 * Probability to add a new point-of-interest.
	 */
	protected float addPointOfInterestProbability;

	/**
	 * Probability to remove a point-of-interest.
	 */
	protected float delPointOfInterestProbability;

	/**
	 * Average points of interest by addict.
	 */
	protected float averagePointsOfInterestCount;

	protected int linksNeededToCreateEdge;

	protected float linkProbability;

	/**
	 * List of addicts.
	 */
	protected LinkedList<Addict> addicts;
	/**
	 * List of point-of-interest.
	 */
	protected LinkedList<PointOfInterest> pointsOfInterest;

	private long currentId;

	private long currentStep;

	public PointsOfInterestGenerator() {
		setUseInternalGraph(false);

		initialPeopleCount = 500;
		addPeopleProbability = delPeopleProbability = 0.001f;

		haveInterestProbability = 0.001f;
		lostInterestProbability = 0.005f;

		initialPointOfInterestCount = 15;
		addPointOfInterestProbability = delPointOfInterestProbability = 0.001f;

		linkProbability = 0.3f;
		averagePointsOfInterestCount = 3;
		linksNeededToCreateEdge = 2;

		addicts = new LinkedList<Addict>();
		pointsOfInterest = new LinkedList<PointOfInterest>();

		currentStep = 0;
	}

	public void setParameter(Parameter p, Object value) {
		switch (p) {
		case INITIAL_PEOPLE_COUNT:
			this.initialPeopleCount = (Integer) value;
			break;
		case ADD_PEOPLE_PROBABILITY:
			this.addPeopleProbability = (Float) value;
			break;
		case DEL_PEOPLE_PROBABILITY:
			this.delPeopleProbability = (Float) value;
			break;
		case INITIAL_POINT_OF_INTEREST_COUNT:
			this.initialPointOfInterestCount = (Integer) value;
			break;
		case AVERAGE_POINTS_OF_INTEREST_COUNT:
			this.averagePointsOfInterestCount = (Float) value;
			break;
		case ADD_POINT_OF_INTEREST_PROBABILITY:
			this.addPointOfInterestProbability = (Float) value;
			break;
		case DEL_POINT_OF_INTEREST_PROBABILITY:
			this.delPointOfInterestProbability = (Float) value;
			break;
		case HAVE_INTEREST_PROBABILITY:
			this.haveInterestProbability = (Float) value;
			break;
		case LOST_INTEREST_PROBABILITY:
			this.lostInterestProbability = (Float) value;
			break;
		case LINKS_NEEDED_TO_CREATE_EDGE:
			this.linksNeededToCreateEdge = (Integer) value;
			break;
		case LINK_PROBABILITY:
			this.linkProbability = ((Number) value).floatValue();
			break;
		}
	}

	/**
	 * Add initial count of points of interest, and initial count of people.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
		pointsOfInterest.clear();

		for (int i = 0; i < initialPointOfInterestCount; i++)
			addPointOfInterest();

		for (int i = 0; i < initialPeopleCount; i++)
			addAddict();
	}

	/**
	 * Step of the generator. Try to remove a node according to the
	 * {@link #delPeopleProbability}. Try to add a node according to the
	 * {@link #addPeopleProbability}. Try to remove a point of interest
	 * according to the {@link #delPointOfInterestProbability}. Try to add a
	 * point of interest according to the {@link #addPointOfInterestProbability}
	 * . Then, step of <i>addicts</i>.
	 * 
	 * @see PointsOfInterestGenerator.Addict#step()
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		sendStepBegins(sourceId, currentStep++);

		if (random.nextDouble() < delPeopleProbability)
			killSomeone();

		if (random.nextDouble() < addPeopleProbability)
			addAddict();

		if (random.nextDouble() < delPointOfInterestProbability)
			removeRandomPointOfInterest();

		if (random.nextDouble() < addPointOfInterestProbability)
			addPointOfInterest();

		for (Addict a : addicts)
			a.step();

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#end()
	 */
	@Override
	public void end() {
		super.end();
	}

	protected void addPointOfInterest() {
		pointsOfInterest.add(new PointOfInterest());
	}

	protected void removePointOfInterest(PointOfInterest poi) {
		pointsOfInterest.remove(poi);

		poi.addict.forEach(a -> poi.delAddict(a));
	}

	protected void removeRandomPointOfInterest() {
		pointsOfInterest.remove(random.nextInt(pointsOfInterest.size()));
	}

	protected void addAddict() {
		Addict a = new Addict(String.format("%08x", currentId++));

		addicts.add(a);
		addNode(a.id);
	}

	protected void killAddict(Addict a) {
		while (a.pointsOfInterest.size() > 0)
			a.pointsOfInterest.peek().delAddict(a);

		a.fullUnlink();

		addicts.remove(a);
		delNode(a.id);

		a.id = null;
		a.pointsOfInterest.clear();
		a.pointsOfInterest = null;
	}

	protected void killSomeone() {
		killAddict(addicts.get(random.nextInt(addicts.size())));
	}

	public static void main(String... args) {
		PointsOfInterestGenerator gen = new PointsOfInterestGenerator();

		gen.setParameter(Parameter.INITIAL_PEOPLE_COUNT, 300);
		gen.setParameter(Parameter.ADD_PEOPLE_PROBABILITY, 0.01f);
		gen.setParameter(Parameter.DEL_PEOPLE_PROBABILITY, 0.01f);
		gen.setParameter(Parameter.INITIAL_POINT_OF_INTEREST_COUNT, 30);
		gen.setParameter(Parameter.AVERAGE_POINTS_OF_INTEREST_COUNT, 5.0f);
		gen.setParameter(Parameter.ADD_POINT_OF_INTEREST_PROBABILITY, 0.0f);
		gen.setParameter(Parameter.DEL_POINT_OF_INTEREST_PROBABILITY, 0.0f);
		gen.setParameter(Parameter.HAVE_INTEREST_PROBABILITY, 0.1f);
		gen.setParameter(Parameter.LOST_INTEREST_PROBABILITY, 0.001f);
		gen.setParameter(Parameter.LINKS_NEEDED_TO_CREATE_EDGE, 2);
		gen.setParameter(Parameter.LINK_PROBABILITY, 0.05f);

		Graph g = new DefaultGraph("theGraph");
		gen.addSink(g);

		String stylesheet = "graph { " + "  fill-color: white;"
				+ "  padding: 50px;" + "}" + "node { " + "  fill-color: black;"
				+ "}" + "edge {" + "  fill-color: black;" + "}";

		g.setAttribute("ui.stylesheet", stylesheet);
		g.setAttribute("ui.quality");
		// g.addAttribute( "ui.antialias" );

		g.display();

		gen.begin();

		while (true) {
			gen.nextEvents();

			try {
				Thread.sleep(60);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
