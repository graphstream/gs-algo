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
 * @since 2011-04-21
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 */
package org.graphstream.ui.layout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.PipeBase;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.geom.Vector3;

public class Eades84Layout extends PipeBase implements Layout {

	boolean is3D;

	double c1;
	double c2;
	double c3;
	double c4;

	double M;

	HashMap<String, Spring> springs;
	HashMap<String, EadesParticle> particles;

	Random random;

	int nodeMoved = 0;
//	LinkedList<LayoutListener> listeners;
	double stabilization = 0;

	Point3 high;
	Point3 low;

	public Eades84Layout() {
		//
		// Appropriate for most graphs :
		//
		c1 = 2;
		c2 = 1;
		c3 = 2;
		c4 = 0.5;
		M = 100;
		//

		is3D = false;

		springs = new HashMap<String, Spring>();
		particles = new HashMap<String, EadesParticle>();
		random = new Random();
//		listeners = new LinkedList<LayoutListener>();

		high = new Point3(1, 1, 1);
		low = new Point3(-1, -1, -1);
	}

	public String getLayoutAlgorithmName() {
		return "Eades1984";
	}

	public int getNodeMovedCount() {
		return nodeMoved;
	}

	public double getStabilization() {
		return stabilization;
	}

	public double getStabilizationLimit() {
		return M;
	}

	public void setStabilizationLimit(double l) {
		M = l;
	}

	public Point3 getLowPoint() {
		return low;
	}

	public Point3 getHiPoint() {
		return high;
	}

	public int getSteps() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getLastStepTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getQuality() {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getForce() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void clear() {
		// TODO Auto-generated method stub

	}

//	public void addListener(LayoutListener listener) {
//		listeners.add(listener);
//	}
//
//	public void removeListener(LayoutListener listener) {
//		listeners.remove(listener);
//	}

	public void setForce(double value) {
		// TODO Auto-generated method stub

	}

	public void setQuality(double qualityLevel) {
		// TODO Auto-generated method stub

	}

	public void setSendNodeInfos(boolean send) {
		// TODO Auto-generated method stub

	}

	public void shake() {
		// TODO Auto-generated method stub

	}

	public void moveNode(String id, double x, double y, double z) {
		// TODO Auto-generated method stub

	}

	public void freezeNode(String id, boolean frozen) {
		// TODO Auto-generated method stub

	}

	public void compute() {
		nodeMoved = 0;

		for (Spring s : springs.values())
			s.computeForce();

		for (EadesParticle p : particles.values())
			p.step();

		double minx, miny, minz, maxx, maxy, maxz;

		minx = miny = minz = Double.MAX_VALUE;
		maxx = maxy = maxz = Double.MIN_VALUE;

		for (EadesParticle p : particles.values()) {
			p.commit();

			if (p.getEnergy() > 0)
				particleMoved(p.id, p.pos.x, p.pos.y, p.pos.z);

			minx = Math.min(minx, p.pos.x);
			miny = Math.min(miny, p.pos.y);
			minz = Math.min(minz, p.pos.z);
			maxx = Math.max(minx, p.pos.x);
			maxy = Math.max(miny, p.pos.y);
			maxz = Math.max(minz, p.pos.z);
		}

		high.x = maxx;
		high.y = maxy;
		high.z = maxz;

		low.x = minx;
		low.y = miny;
		low.z = minz;

		stabilization += 1;

//		for (LayoutListener listener : listeners)
//			listener.stepCompletion(getStabilization());
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		particles.put(nodeId, getNewParticle(nodeId));
		stabilization = 0;
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		particles.remove(nodeId);
		stabilization = 0;
	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		EadesParticle p1, p2;
		Spring spring;

		p1 = (EadesParticle) particles.get(fromNodeId);
		p2 = (EadesParticle) particles.get(toNodeId);

		spring = getNewSpring(p1, p2);
		springs.put(edgeId, spring);

		p1.springs.put(p2, spring);
		p2.springs.put(p1, spring);

		stabilization = 0;
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		Spring s = springs.remove(edgeId);

		if (s != null) {
			s.p1.springs.remove(s.p2);
			s.p2.springs.remove(s.p1);

			stabilization = 0;
		}
	}

	public void inputPos(String filename) throws IOException {
		throw new RuntimeException("unhandle feature");
	}

	public void outputPos(String filename) throws IOException {
		throw new RuntimeException("unhandle feature");
	}

	public void particleMoved(Object id, double x, double y, double z) {
		//for (LayoutListener listener : listeners)
		//	listener.nodeMoved((String) id, x, y, z);

		Object xyz[] = new Object[3];
		xyz[0] = x;
		xyz[1] = y;
		xyz[2] = z;

		sendNodeAttributeChanged(getLayoutAlgorithmName(), (String) id, "xyz",
				xyz, xyz);

		// System.out.printf("particle %s moved : %f;%f;%f\n", id, x, y, z);
	}

	protected EadesParticle getNewParticle(String id) {
		return new EadesParticle(id);
	}

	protected Spring getNewSpring(EadesParticle p1, EadesParticle p2) {
		return new Spring(p1, p2);
	}

	protected class Spring {
		EadesParticle p1;
		EadesParticle p2;

		double force;

		Spring(EadesParticle p1, EadesParticle p2) {
			this.p1 = p1;
			this.p2 = p2;
		}

		/**
		 * Force of a spring is : c1 * log(d/c2)
		 */
		void computeForce() {
			double d = p1.d(p2);
			force = c1 * Math.log(d / c2);
		}

		void set(EadesParticle p, Vector3 v) {
			v.set(Math.signum(p2.getPosition().x - p1.getPosition().x),
					Math.signum(p2.getPosition().y - p1.getPosition().y),
					Math.signum(p2.getPosition().z - p1.getPosition().z));

			if (p == p2)
				v.scalarMult(-1);

			v.scalarMult(force);
		}
	}

	protected class EadesParticle {
		HashMap<EadesParticle, Spring> springs;
		Vector3 dir;
		Vector3 sum;
		Point3 pos;
		String id;

		public EadesParticle(String id) {
			this.id = id;
			springs = new HashMap<EadesParticle, Spring>();
			dir = new Vector3();
			sum = new Vector3();
			pos = new Point3();

			pos.x = random.nextDouble() * (high.x - low.x) + low.x;
			pos.y = random.nextDouble() * (high.y - low.y) + low.y;

			if (is3D)
				pos.z = random.nextDouble() * (high.z - low.z) + low.z;
		}

		public void step() {
			Vector3 v = new Vector3();

			dir.fill(0);
			sum.fill(0);

			for (Spring s : springs.values()) {
				s.set(this, v);
				sum.add(v);
			}

			// if (springs.size() > 0)
			// sum.scalarDiv(springs.size());
			// dir.add(sum);

			// sum.fill(0);

			Iterator<EadesParticle> it = particles.values().iterator();
			//double i = 0;

			while (it.hasNext()) {
				EadesParticle p = it.next();

				if (!springs.containsKey(p) && p != this) {
					double d = d(p);
					/*
					 * Force of a non-spring particle : c3 / sqrt(d)
					 */
					double f = Double.isNaN(d) ? c3 : c3 / Math.sqrt(d);

					v.set(Math.signum(getPosition().x - p.getPosition().x),
							Math.signum(getPosition().y - p.getPosition().y),
							Math.signum(getPosition().z - p.getPosition().z));

					v.scalarMult(f);

					sum.add(v);
					//i++;
				}
			}

			//if (i + springs.size() > 0)
			//	sum.scalarDiv(i + springs.size());

			dir.add(sum);
			dir.scalarMult(c4);

			assert (!Double.isNaN(dir.data[0]) && !Double.isNaN(dir.data[1]));
		}

		public double getEnergy() {
			return dir.length();
		}

		public void commit() {
			pos.x = pos.x + dir.data[0];
			pos.y = pos.y + dir.data[1];

			assert (!Double.isNaN(pos.x) && !Double.isNaN(pos.y));

			if (is3D)
				pos.z = pos.z + dir.data[2];

			nodeMoved++;
		}

		protected double d(EadesParticle p) {
			return getPosition().distance(p.getPosition());
		}

		public Point3 getPosition() {
			return pos;
		}
	}

	public static void main(String... args) {
		DefaultGraph g = new DefaultGraph("g");
		BarabasiAlbertGenerator gen = new BarabasiAlbertGenerator();
		Eades84Layout layout = new Eades84Layout();

		int size = 30;

		gen.addSink(g);
		g.addSink(layout);
		layout.addAttributeSink(g);

		g.display(false);

		gen.begin();
		while (size-- > 0)
			gen.nextEvents();
		gen.end();

		while (true) {
			layout.compute();
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
		}
	}
}
