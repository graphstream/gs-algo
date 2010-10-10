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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Defines a parameter as an association between a String and an Object.
 */
public class Parameter {
	/**
	 * Shortcut for "new Parameter(key,value)".
	 * 
	 * @param key
	 *            key of the parameter
	 * @param value
	 *            value of the parameter
	 * @return new Parameter(key,value)
	 */
	public static final Parameter parameter(String key, Object value) {
		return new Parameter(key, value);
	}

	/**
	 * Process parameters. Throws an exception if something is wrong.
	 * 
	 * @param env
	 *            object to set attributes
	 * @param params
	 *            parameters
	 * @throws InvalidParameterException
	 */
	public static void processParameters(Object env, Parameter... params)
			throws InvalidParameterException, MissingParameterException {
		//
		// We are lazy, if no parameters there is no need to work !
		//
		if (params == null|| params.length==0)
			return;

		//
		// Building a map associating keys with values.
		// This map should be empty at the end of the process.
		//
		HashMap<String, Object> paramsMap = new HashMap<String, Object>();

		for (Parameter p : params)
			paramsMap.put(p.getKey(), p.getValue());

		//
		// We now retrieve fields which contains
		// the DefineParameter annotations.
		// Search is made in superclass to.
		//
		LinkedList<Field> fields = new LinkedList<Field>();

		{
			Class<?> cls = env.getClass();

			while (cls != Object.class) {
				Field[] clsFields = cls.getDeclaredFields();

				if (clsFields != null) {
					for (Field f : clsFields)
						if (f.getAnnotation(DefineParameter.class) != null)
							fields.add(f);
				}

				cls = cls.getSuperclass();
			}
		}
		
		for (Field f : fields) {
			try {
				final DefineParameter dp = f
						.getAnnotation(DefineParameter.class);

				if (dp != null && paramsMap.containsKey(dp.name())) {
					//
					// We try to make the field accessible,
					// if it is a protected or private field.
					//
					try {
						f.setAccessible(true);
					} catch (Exception e) {
						// Can't change permission...
						// Trying anyway to continue !
					}

					final Object value = paramsMap.get(dp.name());
					final boolean isNumber = value instanceof Number;

					paramsMap.remove(dp.name());

					//
					// If type is defined, value should be assignable to this
					// type.
					//
					if (dp.type() != Object.class
							&& !dp.type().isAssignableFrom(value.getClass()))
						throw new InvalidParameterException(
								"invalid parameter type, should be %s", dp
										.type().getName());

					//
					// If min or max are defined, value should be a number
					// between min and max.
					//
					if (!Double.isNaN(dp.min()) || !Double.isNaN(dp.max())) {
						if (!isNumber)
							throw new InvalidParameterException(
									"min or max defined but value is not a number for %s",
									dp.name());

						Number n = (Number) value;

						if (dp.min() != Double.NaN
								&& n.doubleValue() < dp.min())
							throw new InvalidParameterException(String.format(
									"bad value for \"%s\", %f < min",
									dp.name(), n.doubleValue()));

						if (dp.max() != Double.NaN
								&& n.doubleValue() > dp.max())
							throw new InvalidParameterException(String.format(
									"bad value for \"%s\", %f > max",
									dp.name(), n.doubleValue()));
					}

					//
					// If strings is defined, value should be a String and must
					// be one of the defined values.
					//
					if (dp.strings().length > 0) {
						if (value.getClass() != String.class)
							throw new InvalidParameterException(
									"value should be a String");

						String s = (String) value;
						boolean found = false;

						for (String alt : dp.strings())
							if (alt.equals(s)) {
								found = true;
								break;
							}

						if (!found)
							throw new InvalidParameterException(
									"\"%s\" is not in the allowed values for %s",
									value, dp.name());
					}

					//
					// Check for the 'beforeSet' trigger.
					//
					if( dp.beforeSet().length() > 0 ) {
						Method beforeSet = null;
						
						{
							Class<?> cls = env.getClass();
							
							while( beforeSet == null && cls != Object.class )
							{
								Method [] methods = cls.getDeclaredMethods();
								
								if( methods != null )
								{
									for( Method m : methods )
										if( m.getName().equals(dp.beforeSet()) )
										{
											beforeSet = m;
											break;
										}
								}
								
								cls = cls.getSuperclass();
							}
						}
						
						if( beforeSet == null )
							throw new InvalidParameterException("'beforeSet' trigger '%s()' can not be found for %s",dp.beforeSet(),dp.name());
						
						Object [] args = null;
						
						switch(beforeSet.getParameterTypes().length)
						{
						case 0:
							// Nothing
							break;
						case 1:
							// If trigger has one argument, we pass the value of the parameter.
							args = new Object [] { value };
							break;
						case 2:
							// If trigger has two arguments, we pass the key and the value of the parameter.
							args = new Object [] { dp.name(), value };
							break;
						default:
							throw new InvalidParameterException("two much arguments in 'beforeSet' trigger '%s()' for %s",dp.beforeSet(),dp.name());
						}
						
						try {
							beforeSet.invoke(env,args);
						} catch (IllegalArgumentException e) {
							throw new InvalidParameterException("bad arguments in 'beforeSet' trigger '%s()'for %s",dp.beforeSet(),dp.name());
						} catch (IllegalAccessException e) {
							throw new InvalidParameterException("illegal access to 'beforeSet' trigger '%s()' for %s",dp.beforeSet(),dp.name());							
						} catch (InvocationTargetException e) {
							throw new InvalidParameterException("invocation error of 'beforeSet' trigger '%s()' for %s",dp.beforeSet(),dp.name());
						}
					}
					
					//
					// Now we try to set the value of this field.
					//
					if( dp.setter().length() == 0 ) {
						try {
							f.set(env, value);
						} catch (IllegalArgumentException e) {
							throw new InvalidParameterException(
									"invalid value type for %s, %s expected",
									dp.name(), f.getType().getName());
						} catch (IllegalAccessException e) {
							throw new InvalidParameterException(
							"parameter value can not be set. maybe a permission problem");
						}
					}
					else {
						Method setter = null;
						
						{
							Class<?> cls = env.getClass();
							
							while( setter == null && cls != Object.class )
							{
								Method [] methods = cls.getDeclaredMethods();
								
								if( methods != null )
								{
									for( Method m : methods )
										if( m.getName().equals(dp.setter()) )
										{
											setter = m;
											break;
										}
								}
								
								cls = cls.getSuperclass();
							}
						}
						
						if( setter == null )
							throw new InvalidParameterException("'setter' '%s()' can not be found for %s",dp.setter(),dp.name());
						
						Object [] args = null;
						
						switch(setter.getParameterTypes().length)
						{
						case 1:
							// If trigger has one argument, we pass the value of the parameter.
							args = new Object [] { value };
							break;
						case 2:
							// If trigger has two arguments, we pass the key and the value of the parameter.
							args = new Object [] { dp.name(), value };
							break;
						default:
							throw new InvalidParameterException("bad argument count in 'setter' '%s()' for %s",dp.setter(),dp.name());
						}
						
						try {
							setter.invoke(env,args);
						} catch (IllegalArgumentException e) {
							throw new InvalidParameterException("bad arguments in 'setter' '%s()'for %s",dp.setter(),dp.name());
						} catch (IllegalAccessException e) {
							throw new InvalidParameterException("illegal access to 'setter' '%s()' for %s",dp.setter(),dp.name());							
						} catch (InvocationTargetException e) {
							throw new InvalidParameterException("invocation error of 'setter' '%s()' for %s",dp.setter(),dp.name());
						}
					}

					//
					// Check for the 'afterSet' trigger.
					//
					if( dp.afterSet().length() > 0 ) {
						Method afterSet = null;
						
						{
							Class<?> cls = env.getClass();
							
							while( afterSet == null && cls != Object.class )
							{
								Method [] methods = cls.getDeclaredMethods();
								
								if( methods != null )
								{
									for( Method m : methods )
										if( m.getName().equals(dp.afterSet()) )
										{
											afterSet = m;
											break;
										}
								}
								
								cls = cls.getSuperclass();
							}
						}
						
						if( afterSet == null )
							throw new InvalidParameterException("'afterSet' trigger '%s()' can not be found for %s",dp.afterSet(),dp.name());
						
						Object [] args = null;
						
						switch(afterSet.getParameterTypes().length)
						{
						case 0:
							// Nothing
							break;
						case 1:
							// If trigger has one argument, we pass the value of the parameter.
							args = new Object [] { value };
							break;
						case 2:
							// If trigger has two arguments, we pass the key and the value of the parameter.
							args = new Object [] { dp.name(), value };
							break;
						default:
							throw new InvalidParameterException("two much arguments in 'afterSet' trigger '%s()' for %s",dp.afterSet(),dp.name());
						}
						
						try {
							afterSet.invoke(env,args);
						} catch (IllegalArgumentException e) {
							throw new InvalidParameterException("bad arguments in 'afterSet' trigger '%s()'for %s",dp.afterSet(),dp.name());
						} catch (IllegalAccessException e) {
							throw new InvalidParameterException("illegal access to 'afterSet' trigger '%s()' for %s",dp.afterSet(),dp.name());							
						} catch (InvocationTargetException e) {
							throw new InvalidParameterException("invocation error of 'afterSet' trigger '%s()' for %s",dp.afterSet(),dp.name());
						}
					}
				}
				else if( ! dp.optional() ) {
					throw new MissingParameterException("parameter \"%s\" is missing",dp.name());
				}
			} catch (NullPointerException e) {
				// Nothing, it is not a parameter
			}
		}

		//
		// If values remain in paramsMap, user try to define parameters
		// which do not exist.
		//
		if (paramsMap.size() > 0) {
			String uneatenParams = "";

			for (String s : paramsMap.keySet())
				uneatenParams += String.format("%s\"%s\"",
						(uneatenParams.length() > 0 ? ", " : ""), s);

			throw new InvalidParameterException(
					"some parameters does not exist : %s", uneatenParams);
		}
	}

	/**
	 * Key of the parameter.
	 */
	protected String key;
	/**
	 * Value of the parameter.
	 */
	protected Object value;

	/**
	 * Build a new parameter.
	 * 
	 * @param key
	 * @param value
	 */
	public Parameter(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Get the key of this parameter.
	 * 
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Get the value of this parameter.
	 * 
	 * @param <T>
	 *            type asked for the value
	 * @return the value as a T
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue() {
		return (T) value;
	}
}
