/*
 * Copyright 2006 - 2013
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
 *     Julien Baudry    <julien.baudry@graphstream-project.org>
 *     Antoine Dutot    <antoine.dutot@graphstream-project.org>
 *     Yoann Pigné      <yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin   <guilhelm.savin@graphstream-project.org>
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
package org.graphstream.algorithm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allowing to define parameters in algorithm.
 * 
 * @author Guilhelm Savin
 * 
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefineParameter {
	/**
	 * Name of the parameter.
	 */
	String name();

	/**
	 * Description of the parameter.
	 */
	String description() default "";

	/**
	 * Minimum value used for number parameter.
	 */
	double min() default Double.NaN;

	/**
	 * Maximum value used for number parameter.
	 */
	double max() default Double.NaN;

	/**
	 * Class of the parameter.
	 */
	Class<?> type() default Object.class;

	/**
	 * For String parameters, this allows to define a restricted set of values
	 * for the parameter.
	 */
	String[] strings() default {};

	/**
	 * Defines name of a function to call before setting the value of the
	 * parameter. If argument count of the function is zero, then the function
	 * will not receive argument. Else, if one argument is needed, the function
	 * will receive the value of parameter, if two arguments are needed, the
	 * function will receive the name of the parameter as a String and the
	 * value.
	 */
	String beforeSet() default "";

	/**
	 * Defines name of a function to call after setting the value of the
	 * parameter. If argument count of the function is zero, then the function
	 * will not receive argument. Else, if one argument is needed, the function
	 * will receive the value of parameter, if two arguments are needed, the
	 * function will receive the name of the parameter as a String and the
	 * value.
	 */
	String afterSet() default "";

	/**
	 * Defines name of a function to call to set the value of the parameter. If
	 * one argument is needed, the function will receive the value of parameter,
	 * if two arguments are needed, the function will receive the name of the
	 * parameter as a String and the value.
	 */
	String setter() default "";

	/**
	 * Defines the priority of the parameter. This is not yet implemented.
	 */
	int priority() default 0;

	/**
	 * Defines if the parameter is optional. This is used to throw a
	 * MissingParameterException during an initialization step to be sure that
	 * core parameters are present.
	 */
	boolean optional() default true;
}
