/*
 * Copyright 2006 - 2011 
 *     Julien Baudry	<julien.baudry@graphstream-project.org>
 *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *     Yoann Pigné		<yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
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
 */
package org.graphstream.ui.layout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.graphstream.algorithm.generator.PreferentialAttachmentGenerator;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.PipeBase;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.LayoutListener;
import org.graphstream.ui.swingViewer.Viewer;
import org.miv.pherd.Particle;
import org.miv.pherd.ParticleBox;
import org.miv.pherd.ParticleBoxListener;
import org.miv.pherd.geom.Vector3;
import org.miv.pherd.ntree.Anchor;
import org.miv.pherd.ntree.BarycenterCellData;
import org.miv.pherd.ntree.CellSpace;
import org.miv.pherd.ntree.OctreeCellSpace;
import org.miv.pherd.ntree.QuadtreeCellSpace;

public class Eades84Layout extends PipeBase implements Layout,
		ParticleBoxListener {

	boolean is3D;

	double c1;
	double c2;
	double c3;
	double c4;

	double M;

	ParticleBox pbox;
	CellSpace space;

	HashMap<String, Spring> springs;

	Random random;

	int nodeMoved = 0;
	LinkedList<LayoutListener> listeners;
	double stabilization = 0;

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

		Anchor up = new Anchor(1, 1, is3D ? 1 : 0.01);
		Anchor down = new Anchor(-1, -1, is3D ? -1 : -0.01);

		space = is3D ? new OctreeCellSpace(down, up) : new QuadtreeCellSpace(
				down, up);

		pbox = new ParticleBox(30, space, new BarycenterCellData());
		springs = new HashMap<String, Spring>();
		random = new Random();
		listeners = new LinkedList<LayoutListener>();

		pbox.addParticleBoxListener(this);
	}

	public String getLayoutAlgorithmName() {
		return "Eades1984";
	}

	public int getNodeMoved() {
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
		org.miv.pherd.geom.Point3 p = pbox.getNTree().getLowestPoint();
		return new Point3(p.x, p.y, p.z);
	}

	public Point3 getHiPoint() {
		org.miv.pherd.geom.Point3 p = pbox.getNTree().getHighestPoint();
		return new Point3(p.x, p.y, p.z);
	}

	public int getSteps() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getLastStepTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getQuality() {
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

	public void addListener(LayoutListener listener) {
		listeners.add(listener);
	}

	public void removeListener(LayoutListener listener) {
		listeners.remove(listener);
	}

	public void setForce(double value) {
		// TODO Auto-generated method stub

	}

	public void setQuality(int qualityLevel) {
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

		pbox.step();

		stabilization += 1;

		for (LayoutListener listener : listeners)
			listener.stepCompletion(getStabilization());
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		pbox.addParticle(getNewParticle(nodeId));
		stabilization = 0;
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		pbox.removeParticle(nodeId);
		stabilization = 0;
	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		EadesParticle p1, p2;
		Spring spring;

		p1 = (EadesParticle) pbox.getParticle(fromNodeId);
		p2 = (EadesParticle) pbox.getParticle(toNodeId);

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

	public void particleAdded(Object id, double x, double y, double z) {
		// System.out.printf("new particle : %s\n", id);
	}

	public void particleMoved(Object id, double x, double y, double z) {
		for (LayoutListener listener : listeners)
			listener.nodeMoved((String) id, x, y, z);

		Object xyz[] = new Object[3];
		xyz[0] = x;
		xyz[1] = y;
		xyz[2] = z;

		sendNodeAttributeChanged(getLayoutAlgorithmName(), (String) id, "xyz",
				xyz, xyz);

		// System.out.printf("particle %s moved : %f;%f;%f\n", id, x, y, z);
	}

	public void particleAttributeChanged(Object id, String attribute,
			Object newValue, boolean removed) {
	}

	public void particleRemoved(Object id) {
	}

	public void stepFinished(int time) {
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

	protected class EadesParticle extends Particle {
		HashMap<EadesParticle, Spring> springs;
		Vector3 dir;
		Vector3 sum;

		public EadesParticle(String id) {
			super(id);
			springs = new HashMap<EadesParticle, Spring>();
			dir = new Vector3();
			sum = new Vector3();
		}

		public void move(int time) {
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

			Iterator<? extends Particle> it = cell.getParticles();
			double i = 0;

			while (it.hasNext()) {
				Particle p = it.next();

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
					i++;
				}
			}

			if (i + springs.size() > 0)
				sum.scalarDiv(i + springs.size());

			dir.add(sum);
			dir.scalarMult(c4);

			assert (!Double.isNaN(dir.data[0]) && !Double.isNaN(dir.data[1]));
		}

		public void inserted() {
			pos.setX(random.nextDouble()
					* (space.getHiAnchor().x - space.getLoAnchor().x)
					+ space.getLoAnchor().x);
			pos.setY(random.nextDouble()
					* (space.getHiAnchor().y - space.getLoAnchor().y)
					+ space.getLoAnchor().y);

			if (is3D)
				pos.setZ(random.nextDouble()
						* (space.getHiAnchor().z - space.getLoAnchor().z)
						+ space.getLoAnchor().z);
		}

		public void removed() {

		}

		public double getEnergy() {
			return dir.length();
		}

		public void nextStep(int time) {
			nextPos.x = pos.x + dir.data[0];
			nextPos.y = pos.y + dir.data[1];

			assert (!Double.isNaN(nextPos.x) && !Double.isNaN(nextPos.y));

			if (is3D)
				nextPos.z = pos.z + dir.data[2];

			nodeMoved++;
			moved = true;

			super.nextStep(time);
		}

		protected double d(Particle p) {
			return getPosition().distance(p.getPosition());
		}
	}

	public static void main(String... args) {
		DefaultGraph g = new DefaultGraph("g");
		PreferentialAttachmentGenerator gen = new PreferentialAttachmentGenerator();
		Eades84Layout layout = new Eades84Layout();

		int size = 100;

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
