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

package org.miv.graphstream.algorithm.layout;

import java.io.IOException;
import java.util.Map;

import org.miv.mbox.*;
import org.miv.graphstream.graph.*;

/**
 * SpringBox shell that runs a SpringBox in a thread.
 * 
 * <p>
 * A layout runner creates a thread and runs a
 * {@link org.miv.graphstream.algorithm.layout.Layout}
 * algorithm inside it. Additionally, it creates a mail box for
 * receiving events, commands and settings, and is able to send resulting layout
 * events to another mailbox.
 * </p>
 * 
 * <p>
 * Layouts are here iterative algorithms that must be called in a loop. It
 * is often better to run these algorithms in a distinct thread. This class does
 * this job.
 * </p>
 * 
 * <p>
 * In order to communicate with this thread, two message boxes are used:
 * <ul>
 * 		<li>The first is created and owned by this class: it allows to receive
 * 			events, commands and settings, for example "add a node" or "remove
 * 			an edge".</li>
 * 		<li>The second is given as argument and is the message box of the thing
 * 			that want to receive node positioning events, for example "this node
 * 			is at (x,y,z) in the 3D space".</li>
 * </ul> 
 * </p>
 * 
 * TODO: indicate how node (or edge) weight are passed.
 * TODO: lots of documentation is lacking on this class.
 * 
 * @author Antoine Dutot
 * @author Yoann Pigné
 * @since 20061208
 */
public class LayoutRunner extends Thread implements MBoxListener, LayoutListener
{
// Attributes
	
	/**
	 * Mail box for receiving incoming events, commands and settings.
	 */
	protected MBoxStandalone inbox;
	
	/**
	 * Mail box to post layout events.
	 */
	protected MBox outbox;
	
	/**
	 * The layout algorithm.
	 */
	protected Layout layout;
	
	/**
	 * While true, this thread runs.
	 */
	protected boolean loop;
	
	/**
	 * Is the layout paused ?.
	 */
	protected boolean pause;
	
	/**
	 * Pause in milliseconds between each step. Allows to yield the processor
	 * to other tasks and not eat all the CPU cycles. 
	 */
	protected int pauseMs = 2;
	
// Constructors
	
	public LayoutRunner( Layout layout )
	{
		this( null, layout );
	}
	
	public LayoutRunner( MBox outbox, Layout layout )
	{
		this.outbox = outbox;
		this.inbox  = new MBoxStandalone( this );
		this.layout = layout;

		//this.springBox = new SpringBox( false, 8, 4, true, 1 );
		// no3d, 8 divisions, viewZone of 4, averageMap, 1 thread.
		
		layout.addListener( this );
	}
	
// Accessors
	
	/**
	 * Message box of this runner. This is the message box where you can post
	 * message to give commands, events and settings to this runner. See the
	 * {@link InputProtocol} class to see which events can be sent.
	 * @return A message box to send events, commands and settings.
	 * @see InputProtocol
	 */
	public MBox getInbox()
	{
		return inbox;
	}
	
	/**
	 * The underlying layout (be careful! it runs in this runner thread).
	 * @return The layout algorithm.
	 */
	public Layout getLayout()
	{
		return layout;
	}
	
// Commands

	/**
	 * Changes the out box.
	 */
	public void setOutbox( MBox outbox )
	{
		this.outbox = outbox;
	}
	
	/**
	 * The main loop. Process messages, then run one step of the layout
	 * algorithm, in a loop.
	 */
	public void run()
	{
		loop = true;
		
		while( loop )
		{
			inbox.processMessages();

			if( ! pause )
			{
				layout.compute();
				sleep( pauseMs );
			}
			else
			{
				sleep( 20 );
			}
		}
	}
	
	/**
	 * Sleep during the given number of milliseconds.
	 * @param ms The number of milliseconds to sleep.
	 */
	protected void sleep( int ms )
	{
		try
		{
			Thread.sleep( ms );
		}
		catch( InterruptedException e )
		{
		}
	}

	/**
	 * Process the {@link InputProtocol} of messages. Does nothing when receiving
	 * a non valid message.
	 * TODO: what to do when receiving such a message ?
	 */
	public void processMessage( String from, Object[] data )
	{
		if( data.length > 0 )
		{
			if( data[0] instanceof String )
			{
				if( data[0].equals( InputProtocol.ADD_NODE.tag ) )
				{
					if( data.length >= 2 && data[1] instanceof String )
						layout.addNode( (String) data[1] );
				}
				else if( data[0].equals( InputProtocol.ADD_EDGE.tag ) )
				{
					if( data.length >= 4
					&&  data[1] instanceof String
					&&  data[2] instanceof String 
					&&  data[3] instanceof String )
					{
						layout.addEdge( (String) data[1],
							(String) data[2], (String) data[3], false );
					}
				}
				else if( data[0].equals( InputProtocol.DEL_NODE.tag ) )
				{
					if( data.length >= 2 && data[1] instanceof String )
						layout.removeNode( (String) data[1] );
				}
				else if( data[0].equals( InputProtocol.DEL_EDGE.tag ) )
				{
					if( data.length >= 2 && data[1] instanceof String )
						layout.removeEdge( (String) data[1] );					
				}
				else if( data[0].equals( InputProtocol.CLEAR_GRAPH.tag ) )
				{
						layout.clear();			
				}
				else if( data[0].equals( InputProtocol.FORCE.tag ) )
				{
					if( data.length >= 2 && data[1] instanceof Number )
					{
						Number n = (Number) data[1];
						layout.setForce( n.floatValue() );
					}
				}
				else if( data[0].equals( InputProtocol.QUALITY.tag ) )
				{
					if( data.length >= 2 && data[1] instanceof Number )
					{
						Number v = (Number) data[1];
						int    n = v.intValue(); 
						
						layout.setQuality( n );
						
						if( n < 0 ) n = 0;
						if( n > 4 ) n = 4;
						
						pauseMs = 5-n;
					}
				}
				else if( data[0].equals( InputProtocol.PAUSE.tag ) )
				{
					pause = true;
					System.err.printf( "PAUSE!!! %b %n", pause );
				}
				else if( data[0].equals( InputProtocol.PLAY.tag ) )
				{
					pause = false;
					System.err.printf( "PLAY!!! %b %n", pause );
				}
				else if( data[0].equals( InputProtocol.STOP.tag ) )
				{
					loop = false;
				}
				else if( data[0].equals( InputProtocol.SHAKE.tag ) )
				{
					layout.shake();
				}
				else if( data[0].equals( InputProtocol.SAVE_POS.tag ) )
				{
					if( data.length >= 2 && data[1] instanceof String )
					{
						try
						{
							layout.outputPos( (String) data[1] );
						}
						catch( IOException e )
						{
							e.printStackTrace();
						}
					}
				}
				else if( data[0].equals( InputProtocol.FORCE_MOVE_NODE.tag ) )
				{
					if( data.length == 5 && data[1] instanceof String
					&&  data[2] instanceof Number && data[3] instanceof Number
					&&  data[4] instanceof Number )
					{
						String nodeId = (String) data[1];
						float  x      = ((Number)data[2]).floatValue();
						float  y      = ((Number)data[3]).floatValue();
						float  z      = ((Number)data[4]).floatValue();
						
						layout.moveNode( nodeId, x, y, z );
					}
				}
				else if( data[0].equals( InputProtocol.FREEZE_NODE.tag ) )
				{
					if( data.length == 3 && data[1] instanceof String && data[2] instanceof Boolean )
					{
						layout.freezeNode( (String)data[1], ((Boolean)data[2]).booleanValue() );
					}
				}
				else
				{
					// What to do ?
					System.err.printf( "LayoutRunner: uncaught message from %s: %s[%d]%n", from, data[0], data.length );
				}
			}
			else
			{
				// What to do ?
				System.err.printf( "LayoutRunner: uncaught message from %s: [%d]%n", from, data.length );
				for( int i=0; i<data.length; ++i )
					System.err.printf( "    %s%n", data[i].getClass().getName() );
			}
		}
		else
		{
			// What to do ?
			System.err.printf( "LayoutRunner: uncaught message from %s: empty%n", from );
		}
	}

	public void nodeMoved( String id, float x, float y, float z )
	{
		if( outbox != null )
		{
			try
			{
				outbox.post( "SpringBox", OutputProtocol.NODE_MOVED.tag, id, x, y, z );
			}
			catch( CannotPostException e )
			{
				System.err.printf( "LayoutRunner: cannot post message to listeners: %s%n", e.getMessage() );
			}
		}
	}

	public void nodesMoved( Map<String, float[]> nodes )
	{
		if( outbox != null )
		{
			try
			{
				outbox.post( "SpringBox", OutputProtocol.NODES_MOVED.tag, nodes );
			}
			catch( CannotPostException e )
			{
				System.err.printf( "LayoutRunner: cannot post message to listeners: %s%n", e.getMessage() );
			}
		}
	}

	public void edgeChanged( String id, float[] points )
	{
		if( outbox != null )
		{
			try
			{
				outbox.post( "SpringBox", OutputProtocol.EDGE_CHANGED.tag, id, points );
			}
			catch( CannotPostException e )
			{
				System.err.printf( "LayoutRunner: cannot post message to listeners: %s%n", e.getMessage() );
			}
		}
	}

	public void edgesChanged( Map<String, float[]> edges )
	{
		if( outbox != null )
		{
			try
			{
				outbox.post( "SpringBox", OutputProtocol.EDGES_CHANGED.tag, edges );
			}
			catch( CannotPostException e )
			{
				System.err.printf( "LayoutRunner: cannot post message to listeners: %s%n", e.getMessage() );
			}
		}		
	}

	public void stepCompletion( float percent )
	{
		if( outbox != null )
		{
			try
			{
				outbox.post( "SpringBox", OutputProtocol.STEP_COMPLETED.tag, percent );
			}
			catch( CannotPostException e )
			{
				System.err.printf( "LayoutRunner: cannot post message to listeners: %s%n", e.getMessage() );
			}
		}
	}

// Constants
	
	/**
	 * Commands that can be sent from this layout runner. Messages are array
	 * of objects. The first element of the array is a string identifying the
	 * message. The other elements depend on the message kind, and for all
	 * messages are given in order under the form "elementName:Type".
	 * @author Antoine Dutot
	 * @author Yoann Pigné
	 * @since 20061208
	 */
	public static enum OutputProtocol
	{
		/**
		 * One node moved. Format: nodeId:String, x:Float, y:Float, z:Float.
		 */
		NODE_MOVED( "sbMov" ),
		/**
		 * One edge changed. Format: edgeId:String, points:Float[].
		 */
		EDGE_CHANGED( "sbChg" ),
		/**
		 * Several nodes changed at once. Format: nodes:Map<String,float[]>.
		 */
		NODES_MOVED( "sbMovs" ),
		/**
		 * Several edges changed at once. Format: edges:Map<String,float[]>.
		 */
		EDGES_CHANGED( "sbChgs" ),
		/**
		 * One step of the layout has completed. A step is the computation of
		 * the displacement of all nodes. Format: percent:Float.
		 */
		STEP_COMPLETED( "sbStep" );
		
		/**
		 * The message identifier.
		 */
		public String tag;
		
		OutputProtocol( String tag ) { this.tag = tag; }
		
		/**
		 * Get the message identifier.
		 * @return The message identifier.
		 */
		public String getTag() { return tag; }
	}
	
	/**
	 * Commands that can be sent to the spring box runner. Messages are array
	 * of objects. The first element of the array is a string identifying the
	 * message. The other elements depend on the message kind, and for all
	 * messages are given in order under the form "elementName:Type". For
	 * example to send a node add event, you can use:
	 * <pre>
	 * 		mbox.post( "fromMe", InputProtocol.ADD_NODE.tag, "nodeId" );
	 * </pre>
	 * or:
	 * <pre>
	 * 		mbox.post( "fromMe", "an", "nodeId" ); 
	 * </pre>
	 * The post() method handles variable argument list, which simplify the
	 * creation of messages. 
	 * @author Antoine Dutot
	 * @author Yoann Pigné
	 * @since 20061208
	 */
	public static enum InputProtocol
	{
		/**
		 * A new node appeared. Format[1]: id:String.
		 */
		ADD_NODE( "an" ),
		/**
		 * A node disapeared. Format[1]: id:String.
		 */
		DEL_NODE( "dn" ),
		/**
		 * A new edge appeared. Format[3]: id:String, node0Id:String,
		 * node1Id:String.
		 */
		ADD_EDGE( "ae" ),
		/**
		 * An edge disapeared. Format[1]: id:String.
		 */
		DEL_EDGE( "de" ),
		/**
		 * clear the whole graph. Format[0]: empty.
		 */
		CLEAR_GRAPH("clear"),
		/**
		 * The attraction/repulsion force changed. Format[1]: value:Float.
		 */
		FORCE( "fo" ),
		/**
		 * Quality of the layout: Format[1]:  value:Integer (0..5). 
		 */
		QUALITY( "qlty" ),
		/**
		 * Pause the layout algorithm (consume 0 CPU). Format[0]: empty.
		 */
		PAUSE( "||" ),
		/**
		 * Unpause the layout algorithm. Format[0]: empty.
		 */
		PLAY( ">" ),
		/**
		 * Stop the layout algorithm, the runner stops and its thread closes.
		 * Format[0]: empty.
		 */
		STOP( "stop!" ),
		/**
		 * Shake the layout. Format[0]: empty.
		 */
		SHAKE( "sk!" ),
		/**
		 * Save the nodes position in a position file. Format[1]: fileName:String.
		 */
		SAVE_POS( "pos" ),
		/**
		 * Force a node to be moved of the given vector. The vector must be
		 * expressed in percents of the graph space (1=100%, 0.5=50%).
		 * Format[4]: nodeId:String, x:Number, y:Number, z:Number.
		 */
		FORCE_MOVE_NODE( "fmn" ),
		/**
		 * Freeze or unfreeze a node. Format[2]: id:String, freeze:Boolean.
		 */
		FREEZE_NODE( "frz" );
		
		/**
		 * The message identifier.
		 */
		public String tag;
		
		InputProtocol( String tag ) { this.tag = tag; }
		
		/**
		 * Get the message identifier.
		 * @return The message identifier.
		 */
		public String getTag() { return tag; }
	}
	
	/**
	 * Helper class that implements a graph listener and sends all graph events
	 * to a layout runner.
	 * 
	 * <p>
	 * Simply instatiate this class and pass it to a
	 * {@link org.miv.graphstream.graph.Graph} as a graph listener and all
	 * events occuring in this graph will be sent to the layout runner whose
	 * mailbox has been passed in the constructor.
	 * </p>
	 * 
	 * <p>
	 * Note however that only graph events are sent, other spring box commands
	 * like the force setting, play/pause and stop for example are not handled,
	 * and must be sent by another mean into the message box.
	 * </p>
	 * 
	 * @see org.miv.graphstream.graph.GraphListener
	 * @see LayoutRunner
	 * @author antoine Dutot
	 * @author Yoann Pigné
	 * @since 20061208
	 */
	public static class GraphAdapter implements GraphListener
	{
		/**
		 * The message box of the LayoutRunner.
		 */
		protected MBox outbox;
		
		/**
		 * Name of the message sender. Usually the graph name.
		 */
		protected String from;
		
		/**
		 * New adapter that catch all events occuring in a graph and send them
		 * in the mail box of a spring box runner. The replay argument allow to
		 * send all data already in the graph, in case this one is already
		 * (maybe partially) constructed.
		 * @param LayoutRunnerMBox The message box of a spring box runner
		 *        that will receive the graph events.
		 * @param graph The graph we adapt, this reference is used only to get
		 * 			the graph name (used as "from" field when sending messages),
		 * 			and to replay the graph is needed.
		 * @param replayGraph If the graph already contains data, this data is sent
		 * 			to the distant message box.
		 */
		public GraphAdapter( MBox LayoutRunnerMBox, Graph graph, boolean replayGraph )
		{
			outbox = LayoutRunnerMBox;
			from   = graph.getId();
			
			if( replayGraph )
				replayGraph( graph );
		}
		
		/**
		 * Like the other constructor, but always replay the graph.
		 * @param LayoutRunnerMBox The message box of a spring box runner
		 *        that will receive the graph events.
		 * @param graph The graph we adapt, this reference is used only to get
		 * 			the graph name (used as "from" field when sending messages),
		 * 			and to replay the graph is needed.
		 */
		public GraphAdapter( MBox LayoutRunnerMBox, Graph graph )
		{
			this( LayoutRunnerMBox, graph, true );
		}

		/**
		 * Send all nodes and edges of the given graph to the distant message
		 * box, with their attributes. This method is primarily used to send the
		 * graph state if the distant mbox is connected to a graph that is
		 * already (maybe partially) built.
		 * @param graph The graph to process.
		 */
		protected void
		replayGraph( Graph graph )
		{
			try
			{
				for( Node node: graph.getNodeSet() )
				{
					outbox.post( from, InputProtocol.ADD_NODE.tag, node.getId() );
				
					// The spring box is not interested by attibutes.
				}
				
				for( Edge edge: graph.getEdgeSet() )
				{
					outbox.post( from, InputProtocol.ADD_EDGE.tag,
						edge.getId(), edge.getSourceNode().getId(),
						edge.getTargetNode().getId() );
					
					// The spring box is not interested by attibutes.
				}
			}
			catch( CannotPostException e )
			{
				System.err.printf( "GraphRendererRunner: cannot post message to listeners: %s%n", e.getMessage() );				
			}
		}
		
		public void attributeChanged( Element element, String attribute, Object oldValue, Object newValue )
		{
			// NOP ! Not used in the SpringBox.
		}

		public void afterNodeAdd( Graph graph, Node node )
		{
			try
			{
				outbox.post( from, InputProtocol.ADD_NODE.tag, node.getId() );
			}
			catch( CannotPostException e )
			{
				System.err.printf( "LayoutRunner: cannot post message to listeners: %s%n", e.getMessage() );
			}
		}
		
		public void afterEdgeAdd( Graph graph, Edge edge )
		{
			try
			{
				outbox.post( from, InputProtocol.ADD_EDGE.tag, edge.getId(),
						edge.getSourceNode().getId(), edge.getTargetNode().getId() );
			}
			catch( CannotPostException e )
			{
				System.err.printf( "LayoutRunner: cannot post message to listeners: %s%n", e.getMessage() );
			}
		}

		public void beforeNodeRemove( Graph graph, Node node )
		{
			try
			{
				outbox.post( from, InputProtocol.DEL_NODE.tag, node.getId() );
			}
			catch( CannotPostException e )
			{
				System.err.printf( "LayoutRunner: cannot post message to listeners: %s%n", e.getMessage() );
			}
		}

		public void beforeEdgeRemove( Graph graph, Edge edge )
		{
			try
			{
				outbox.post( from, InputProtocol.DEL_EDGE.tag, edge.getId() );
			}
			catch( CannotPostException e )
			{
				System.err.printf( "LayoutRunner: cannot post message to listeners: %s%n", e.getMessage() );
			}
		}

		public void beforeGraphClear( Graph graph )
		{
			try
			{
				outbox.post( from, InputProtocol.CLEAR_GRAPH.tag );
			}
			catch( CannotPostException e )
			{
				System.err.printf( "LayoutRunner: cannot post message to listeners: %s%n", e.getMessage() );
			}
		}
	}
}