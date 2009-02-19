package org.miv.graphstream.algorithm.generator;

import org.miv.graphstream.graph.*;
import org.miv.miterator.*;
import java.util.*;

/**
 * Life generator.
 * 
 * <p>
 * The life generator simulates a game of life and produces the corresponding
 * graph.<ul>
 * 		<li>Cells of the game map to nodes of the graph when they are alive;</li>
 * 		<li>There is an edge between live cells if they are direct neighbors.</li>
 * </ul>
 * </p>
 *
 * @author Antoine Dutot
 * @since 20070107
 */
public class LifeGenerator implements Generator, MIteratorListener
{
	protected Graph graph;

	protected MIterator miterator;
	
//	protected 

	/**
	 * Build a generator with a prebuilt iterator. The given iterator will be
	 * used as an event source for generating graph events. It will be called
	 * by the {@link #nextElement()} method. Therefore, it is dangerous to call
	 * the {@link org.miv.miterator.MIterator#step()} method if the iterator
	 * has been registered inside this graph generator. Use {@link #nextElement()}
	 * instead.
	 */
	public LifeGenerator( MIterator miterator )
	{
		this.miterator = miterator;
		miterator.addListener( this );
	}
	
	public void begin( Graph graph )
	{
		this.graph = graph;
	}

	public void end()
	{
	}

	public boolean nextElement()
	{
		miterator.step();
		return true;
	}
	
// MicroIteratorListener

	protected ArrayList<Cell> cellsChanged = new ArrayList<Cell>();
	
	protected static class Cell
	{
		public int x, y;
		public int oldValue, newValue;
		public Cell( int x, int y, int oldValue, int newValue ) { this.x = x; this.y = y; this.newValue = newValue; this.oldValue = oldValue; }
		public String getId() { return getId( x, y ); }
		public static String getId( int x, int y ) { return ""+x+"-"+y; }
	}
	
	public void cellChanged( int x, int y, int oldValue, int newValue )
	{
		cellsChanged.add( new Cell( x, y, oldValue, newValue ) );
	}

	public void initIteration( int width, int height )
	{
	}

	public void iterationBegins( int iteration )
	{
		cellsChanged.clear();
	}

	public void iterationEnds( int iteration )
	{
		for( Cell cell: cellsChanged )
		{
			String id = cell.getId();
			
			if( cell.newValue == 0 )
			{
				// The cell disapeared.
				graph.removeNode( id );
			}
			else
			{
				// The cell appeared.
				
				graph.addNode( id );
				
				// Look for neighbor nodes in order to create edges.
				
				int x = cell.x;
				int y = cell.y;
				int ox, oy;
				
				for( int dx=-1; dx<2; ++dx )
				{
					for( int dy=-1; dy<2; ++dy )
					{
						ox = x+dx;
						oy = y+dy;
						
						if( ox == x && oy == y )
						{
							
						}
						else
						{
							if( miterator.getCell( ox, oy, true ) != 0 )
							{
								String otherId = Cell.getId( ox, oy );
								graph.addEdge( id+"-"+otherId, id, otherId );
							}
						}
					}
				}
			}
		}
	}

	public void mapCleared()
	{
		graph.clear();
	}
}