/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.miv.graphstream.algorithm.generator.test;

import javax.swing.*;

import org.miv.graphstream.algorithm.generator.*;
import org.miv.graphstream.graph.implementations.DefaultGraph;
import org.miv.graphstream.ui.GraphViewer;
import org.miv.graphstream.ui.GraphViewerRemote;
import org.miv.graphstream.ui.swing.SwingGraphViewer;

/**
 * Test several graph generators.
 *
 * @author Antoine Dutot
 * @author Yoann Pigné
 * @since 2007
 */
public class TestGenerators extends JFrame
{
	private static final long serialVersionUID = 3555564365600583158L;

	public static void main( String args[] )
	{
		new TestGenerators();
	}
	
	public TestGenerators()
	{
		DefaultGraph pa = new DefaultGraph( "Preferential Attachement Graph" );
		DefaultGraph dm = new DefaultGraph( "Dorogovtsev Mendes Graph" );
		DefaultGraph gr = new DefaultGraph( "Grid Graph" );
		
		GraphViewer ppa = new SwingGraphViewer( pa, true ); 
		GraphViewer pdm = new SwingGraphViewer( dm, true );
		GraphViewer pgr = new SwingGraphViewer( gr, true );
		
		GraphViewerRemote ppar = ppa.newViewerRemote();
		GraphViewerRemote pdmr = pdm.newViewerRemote();
		GraphViewerRemote pgrr = pgr.newViewerRemote();
		
		ppar.setQuality( 4 );
		pdmr.setQuality( 4 );
		pgrr.setQuality( 4 );
		
		setLayout( new java.awt.GridLayout( 1, 3 ) );
		add( (JComponent) ppa.getComponent() );
		add( (JComponent) pdm.getComponent() );
		add( (JComponent) pgr.getComponent() );
		
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		setVisible( true );
		pack();
		
		Generator pag = new PreferentialAttachmentGenerator();
		Generator dmg = new DorogovtsevMendesGenerator();
		Generator grg = new GridGenerator( false, false );
		
		pag.begin( pa );
		dmg.begin( dm );
		
		for( int i=0; i<100; ++i )
		{
			pag.nextElement();
			dmg.nextElement();
		}
		
		pag.end();
		dmg.end();
		
		grg.begin( gr );
		
		for( int i=0; i<10; ++i )
		{
			grg.nextElement();
		}
		
		grg.end();
		
		pa.addAttribute( "stylesheet", styleSheet1 );
		dm.addAttribute( "stylesheet", styleSheet2 );
		gr.addAttribute( "stylesheet", styleSheet3 );
	}
	
	protected static final String styleSheet1 =
		"node { width: 6px; color: white; border-width: 1px; border-color: #202020; shadow-style: simple; shadow-color: #40B050; shadow-width: 8px; }" +
		"edge { color: #303030; }";
	protected static final String styleSheet2 =
		"node { width: 6px; color: white; border-width: 1px; border-color: #202020; shadow-style: simple; shadow-color: #4050B0; shadow-width: 8px; }" +
		"edge { color: #303030; }";
	protected static final String styleSheet3 =
		"node { width: 6px; color: white; border-width: 1px; border-color: #202020; shadow-style: simple; shadow-color: #B04050; shadow-width: 8px; }" +
		"edge { color: #303030; }";
}