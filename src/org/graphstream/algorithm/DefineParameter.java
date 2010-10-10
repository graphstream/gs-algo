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
