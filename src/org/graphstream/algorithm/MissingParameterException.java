/*
 * This file is part of GraphStream.
 * 
 * GraphStream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GraphStream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphStream.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2006 - 2010
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */
package org.graphstream.algorithm;

/**
 * A parameter is missing during the processing. This exception is thrown when
 * a parameter marked as non-optional is not set during the initialization process. 
 */
public class MissingParameterException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3406263218480949750L;

	public MissingParameterException()
	{
		super();
	}

	public MissingParameterException( String msg, Object ... args )
	{
		super( String.format(msg,args) );
	}
}
