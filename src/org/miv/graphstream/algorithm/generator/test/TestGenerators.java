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
import org.miv.graphstream.graph.*;
import org.miv.graphstream.ui.viewer.*;

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
		CheckedGraph pa = new CheckedGraph( "Preferential Attachement Graph" );
		CheckedGraph dm = new CheckedGraph( "Dorogovtsev Mendes Graph" );
		CheckedGraph gr = new CheckedGraph( "Grid Graph" );
		
		GraphPanel ppa = new GraphPanel( pa, true );
		GraphPanel pdm = new GraphPanel( dm, true );
		GraphPanel pgr = new GraphPanel( gr, true );
		
		setLayout( new java.awt.GridLayout( 1, 3 ) );
		add( ppa );
		add( pdm );
		add( pgr );
		
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
	}
}