/*
 * Copyright 2006 - 2016
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedList;
import java.util.Properties;

import org.graphstream.algorithm.DefineParameter;
import org.graphstream.algorithm.InvalidParameterException;
import org.graphstream.algorithm.MissingParameterException;

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
		if (params == null || params.length == 0)
			return;

		//
		// Create a new ParametersProcessor to process the parameters and
		// run it.
		//
		ParametersProcessor pp = new ParametersProcessor(env, params);
		pp.process();
	}

	public static void processParameters(Object env, Properties prop)
			throws InvalidParameterException, MissingParameterException {
		//
		// Create a new ParametersProcessor to process the parameters and
		// run it.
		//
		ParametersProcessor pp = new ParametersProcessor(env, prop);
		pp.process();
	}

	public static void processParameters(Object env, String propertiesPath)
			throws InvalidParameterException, MissingParameterException, InvalidPropertiesFormatException, IOException {
		boolean xml = propertiesPath.endsWith(".xml");
		FileInputStream in = new FileInputStream(propertiesPath);
		
		processParameters(env, in, xml);
		
		in.close();
	}
	public static void processParameters(Object env, InputStream in, boolean xml)
	throws InvalidParameterException, MissingParameterException, InvalidPropertiesFormatException, IOException {
		Properties prop = new Properties();
		
		if (xml)
			prop.loadFromXML(in);
		else
			prop.load(in);
		
		processParameters(env, prop);
	}

	public static Properties exportParameters(Object env) {
		ParametersProcessor pp = new ParametersProcessor(env);
		Properties prop = new Properties();
		
		for (Field f : pp.fields.keySet()) {
			DefineParameter dp = f.getAnnotation(DefineParameter.class);
			
			try {
				f.setAccessible(true);
			} catch (Exception e) {
				// Can't change permission...
				// Trying anyway to continue !
			}
			
			try {
				prop.setProperty(dp.name(), f.get(env).toString());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		return prop;
	}

	/**
	 * Defines the object which will process parameters.
	 * 
	 */
	public static class ParametersProcessor {
		HashMap<String, Field> definedParameters;
		HashMap<Field, Object> fields;
		HashMap<String, Object> params;

		public ParametersProcessor(Object obj) {
			init(obj);
		}
		
		public ParametersProcessor(Object obj, Properties prop) {
			init(obj);
			buildParametersMap(prop);
		}

		public ParametersProcessor(Object obj, Parameter... parameters) {
			init(obj);

			if (parameters != null)
				buildParametersMap(parameters);
		}

		private void init(Object obj) {
			fields = new HashMap<Field, Object>();
			params = new HashMap<String, Object>();
			definedParameters = new HashMap<String, Field>();

			if (obj.getClass().isArray()) {
				Object[] objects = (Object[]) obj;

				for (Object o : objects)
					buildFields(o);
			} else {
				buildFields(obj);
			}
		}

		/**
		 * Start the processing part. First, this checks is all non-optional
		 * parameters will receive a value, else a MissingParameterException is
		 * thrown. Then, values are assigned to the attribute (
		 * {@link ParametersProcessor#setValue(DefineParameter, Field, Object)}
		 * ). Finally, the method checks is no parameter remaining. In this
		 * case, an InvalidParameterException is thrown.
		 * 
		 * @throws InvalidParameterException
		 * @throws MissingParameterException
		 */
		public void process() throws InvalidParameterException,
				MissingParameterException {
			checkNonOptionalParameters();

			LinkedList<String> remainingParameters = new LinkedList<String>(
					params.keySet());
			
			fields.keySet().stream().forEach(f -> {
				final DefineParameter dp = f
						.getAnnotation(DefineParameter.class);

				if (params.containsKey(dp.name())) {
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

					final Object value = params.get(dp.name());

					try {
						setValue(dp, f, value);
					} catch (InvalidParameterException e) {
						throw new RuntimeException(e);
					}

					remainingParameters.remove(dp.name());
				}
			});


			//
			// If values remain in paramsMap, user try to define parameters
			// which do not exist.
			//
			if (remainingParameters.size() > 0) {
				String uneatenParams = "";
				
				for (String s : remainingParameters)
					uneatenParams += String.format("%s\"%s\"", (uneatenParams
							.length() > 0 ? ", " : ""), s);

				throw new InvalidParameterException(
						"some parameters does not exist : %s", uneatenParams);
			}
		}

		/**
		 * Find fields owning a DefineParameter annotation.
		 * 
		 * @param obj
		 *            the object on which searching fields.
		 */
		protected void buildFields(Object obj) {
			Class<?> cls = obj.getClass();

			while (cls != Object.class) {
				Field[] clsFields = cls.getDeclaredFields();

				if (clsFields != null) {
					for (Field f : clsFields) {
						DefineParameter dp = f
								.getAnnotation(DefineParameter.class);

						if (dp != null) {
							fields.put(f, obj);
							definedParameters.put(dp.name(), f);
						}
					}
				}

				cls = cls.getSuperclass();
			}
		}

		/**
		 * Create a map "key -> value" for parameters.
		 * 
		 * @param parameters
		 *            parameters which will be used in the process part.
		 */
		protected void buildParametersMap(Parameter... parameters) {
			for (Parameter p : parameters)
				params.put(p.getKey(), p.getValue());
		}

		protected void buildParametersMap(Properties prop) {
			prop.stringPropertyNames().forEach(key -> {
				Field f = definedParameters.get(key);
				String v = prop.getProperty(key);

				Class<?> type = f.getType();

				if (type.equals(String.class))
					params.put(key, v);
				else if (type.equals(Double.class) || type.equals(Double.TYPE))
					params.put(key, Double.valueOf(v));
				else if (type.equals(Float.class) || type.equals(Float.TYPE))
					params.put(key, Float.valueOf(v));
				else if (type.equals(Integer.class) || type.equals(Integer.TYPE))
					params.put(key, Integer.valueOf(v));
				else if (type.equals(Long.class) || type.equals(Long.TYPE))
					params.put(key, Long.valueOf(v));
				else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE))
					params.put(key, Boolean.valueOf(v));
				else if (type.isEnum()) {
					@SuppressWarnings("unchecked")
					Class<? extends Enum<?>> e = (Class<? extends Enum<?>>) type;
					
					for (Enum<?> en : e.getEnumConstants()) {
						if (en.name().equals(v)) {
							params.put(key, en);
							break;
						}
					}
				} else
					throw new UnsupportedOperationException(type.getName());
			});
		}

		/**
		 * Set the value of a field according to a parameter. First, the value
		 * is check ( {@link #checkValue(DefineParameter, Field, Object)} ).
		 * Then, the beforeSet trigger is called (
		 * {@link #callBeforeSetTrigger(DefineParameter, Field, Object)} ).
		 * Then, if 'setters' value is empty, value is simply assigned to the
		 * field, else 'setter' is called (
		 * {@link #callSetter(DefineParameter, Field, Object)} ). Finally, the
		 * afterSet trigger is called (
		 * {@link #callAfterSetTrigger(DefineParameter, Field, Object)}).
		 * 
		 * @param dp
		 *            the DefineParameter annotation being set.
		 * @param f
		 *            field associated to the annotation.
		 * @param value
		 *            value which will be assigned to the field.
		 * @throws InvalidParameterException
		 */
		protected void setValue(DefineParameter dp, Field f, Object value)
				throws InvalidParameterException {
			checkValue(dp, f, value);

			Object env = fields.get(f);

			callBeforeSetTrigger(dp, f, value);

			if (dp.setter().length() == 0) {
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
			} else {
				callSetter(dp, f, value);
			}

			callAfterSetTrigger(dp, f, value);
		}

		/**
		 * Check is the value is valid according to the DefineParameter
		 * annotation.
		 * 
		 * @param dp
		 *            the DefineParameter annotation associated to the field.
		 * @param f
		 *            the field.
		 * @param value
		 *            the value.
		 * @throws InvalidParameterException
		 */
		protected void checkValue(DefineParameter dp, Field f, Object value)
				throws InvalidParameterException {
			final boolean isNumber = value instanceof Number;

			//
			// If type is defined, value should be assignable to this
			// type.
			//
			if (dp.type() != Object.class
					&& !dp.type().isAssignableFrom(value.getClass()))
				throw new InvalidParameterException(
						"invalid parameter type, should be %s", dp.type()
								.getName());

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

				if (dp.min() != Double.NaN && n.doubleValue() < dp.min())
					throw new InvalidParameterException(String.format(
							"bad value for \"%s\", %f < min", dp.name(), n
									.doubleValue()));

				if (dp.max() != Double.NaN && n.doubleValue() > dp.max())
					throw new InvalidParameterException(String.format(
							"bad value for \"%s\", %f > max", dp.name(), n
									.doubleValue()));
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
		}

		/**
		 * Check if all non-optional parameters will receive a value.
		 * 
		 * @throws MissingParameterException
		 *             a non-optional parameter does not receive its value.
		 */
		protected void checkNonOptionalParameters()
				throws MissingParameterException {
						
			fields.keySet().stream()
				.filter(f -> (!f.getAnnotation(DefineParameter.class).optional() 
						&& !params.containsKey(f.getAnnotation(DefineParameter.class).name())))
				.forEach(f -> {
					MissingParameterException ex = new MissingParameterException(
							"parameter \"%s\" is missing", f.getAnnotation(DefineParameter.class).name());
					
					throw new RuntimeException(ex);
				});
			}

		/**
		 * Call setter of a parameter. This is called when
		 * {@link DefineParameter#setter()} is not empty. If arguments count of
		 * the setter is 1, then the value is passed as argument. If count is 2,
		 * then parameter name and value are passed as arguments. Else, a
		 * InvalidParameterException is thrown.
		 * 
		 * @param dp
		 *            the DefineParameter annotation associated to the field.
		 * @param f
		 *            the field.
		 * @param value
		 *            the value.
		 * @throws InvalidParameterException
		 */
		protected void callSetter(DefineParameter dp, Field f, Object value)
				throws InvalidParameterException {
			Object env = fields.get(f);

			Method setter = null;

			{
				Class<?> cls = env.getClass();

				while (setter == null && cls != Object.class) {
					Method[] methods = cls.getDeclaredMethods();

					if (methods != null) {
						for (Method m : methods)
							if (m.getName().equals(dp.setter())) {
								setter = m;
								break;
							}
					}

					cls = cls.getSuperclass();
				}
			}

			if (setter == null)
				throw new InvalidParameterException(
						"'setter' '%s()' can not be found for %s", dp.setter(),
						dp.name());

			Object[] args = null;

			switch (setter.getParameterTypes().length) {
			case 1:
				// If trigger has one argument, we pass the value of
				// the parameter.
				args = new Object[] { value };
				break;
			case 2:
				// If trigger has two arguments, we pass the key and
				// the value of the parameter.
				args = new Object[] { dp.name(), value };
				break;
			default:
				throw new InvalidParameterException(
						"bad argument count in 'setter' '%s()' for %s", dp
								.setter(), dp.name());
			}

			try {
				setter.invoke(env, args);
			} catch (IllegalArgumentException e) {
				throw new InvalidParameterException(
						"bad arguments in 'setter' '%s()'for %s", dp.setter(),
						dp.name());
			} catch (IllegalAccessException e) {
				throw new InvalidParameterException(
						"illegal access to 'setter' '%s()' for %s",
						dp.setter(), dp.name());
			} catch (InvocationTargetException e) {
				throw new InvalidParameterException(
						"invocation error of 'setter' '%s()' for %s", dp
								.setter(), dp.name());
			}
		}

		/**
		 * Call the beforeSet trigger. The name of the method to call can be
		 * defined in @link {@link DefineParameter#beforeSet()}. If arguments
		 * count of the method is 0, no argument is given. If 1, then the value
		 * is given. If 2, then the parameter name and its value are given.
		 * Else, throw an InvalidParameterException.
		 * 
		 * @param dp
		 *            the DefineParameter annotation associated to the field.
		 * @param f
		 *            the field.
		 * @param value
		 *            the value.
		 * @throws InvalidParameterException
		 */
		protected void callBeforeSetTrigger(DefineParameter dp, Field f,
				Object value) throws InvalidParameterException {
			if (dp.beforeSet().length() > 0) {
				Object env = fields.get(f);

				Method beforeSet = null;

				{
					Class<?> cls = env.getClass();

					while (beforeSet == null && cls != Object.class) {
						Method[] methods = cls.getDeclaredMethods();

						if (methods != null) {
							for (Method m : methods)
								if (m.getName().equals(dp.beforeSet())) {
									beforeSet = m;
									break;
								}
						}

						cls = cls.getSuperclass();
					}
				}

				if (beforeSet == null)
					throw new InvalidParameterException(
							"'beforeSet' trigger '%s()' can not be found for %s",
							dp.beforeSet(), dp.name());

				Object[] args = null;

				switch (beforeSet.getParameterTypes().length) {
				case 0:
					// Nothing
					break;
				case 1:
					// If trigger has one argument, we pass the value of
					// the parameter.
					args = new Object[] { value };
					break;
				case 2:
					// If trigger has two arguments, we pass the key and
					// the value of the parameter.
					args = new Object[] { dp.name(), value };
					break;
				default:
					throw new InvalidParameterException(
							"two much arguments in 'beforeSet' trigger '%s()' for %s",
							dp.beforeSet(), dp.name());
				}

				try {
					beforeSet.invoke(env, args);
				} catch (IllegalArgumentException e) {
					throw new InvalidParameterException(
							"bad arguments in 'beforeSet' trigger '%s()'for %s",
							dp.beforeSet(), dp.name());
				} catch (IllegalAccessException e) {
					throw new InvalidParameterException(
							"illegal access to 'beforeSet' trigger '%s()' for %s",
							dp.beforeSet(), dp.name());
				} catch (InvocationTargetException e) {
					throw new InvalidParameterException(
							"invocation error of 'beforeSet' trigger '%s()' for %s",
							dp.beforeSet(), dp.name());
				}
			}
		}

		/**
		 * Call the afterSet trigger. The name of the method to call can be
		 * defined in @link {@link DefineParameter#afterSet()}. If arguments
		 * count of the method is 0, no argument is given. If 1, then the value
		 * is given. If 2, then the parameter name and its value are given.
		 * Else, throw an InvalidParameterException.
		 * 
		 * @param dp
		 *            the DefineParameter annotation associated to the field.
		 * @param f
		 *            the field.
		 * @param value
		 *            the value.
		 * @throws InvalidParameterException
		 */
		protected void callAfterSetTrigger(DefineParameter dp, Field f,
				Object value) throws InvalidParameterException {
			Object env = fields.get(f);

			if (dp.afterSet().length() > 0) {
				Method afterSet = null;

				{
					Class<?> cls = env.getClass();

					while (afterSet == null && cls != Object.class) {
						Method[] methods = cls.getDeclaredMethods();

						if (methods != null) {
							for (Method m : methods)
								if (m.getName().equals(dp.afterSet())) {
									afterSet = m;
									break;
								}
						}

						cls = cls.getSuperclass();
					}
				}

				if (afterSet == null)
					throw new InvalidParameterException(
							"'afterSet' trigger '%s()' can not be found for %s",
							dp.afterSet(), dp.name());

				Object[] args = null;

				switch (afterSet.getParameterTypes().length) {
				case 0:
					// Nothing
					break;
				case 1:
					// If trigger has one argument, we pass the value of
					// the parameter.
					args = new Object[] { value };
					break;
				case 2:
					// If trigger has two arguments, we pass the key and
					// the value of the parameter.
					args = new Object[] { dp.name(), value };
					break;
				default:
					throw new InvalidParameterException(
							"two much arguments in 'afterSet' trigger '%s()' for %s",
							dp.afterSet(), dp.name());
				}

				try {
					afterSet.invoke(env, args);
				} catch (IllegalArgumentException e) {
					throw new InvalidParameterException(
							"bad arguments in 'afterSet' trigger '%s()'for %s",
							dp.afterSet(), dp.name());
				} catch (IllegalAccessException e) {
					throw new InvalidParameterException(
							"illegal access to 'afterSet' trigger '%s()' for %s",
							dp.afterSet(), dp.name());
				} catch (InvocationTargetException e) {
					throw new InvalidParameterException(
							"invocation error of 'afterSet' trigger '%s()' for %s",
							dp.afterSet(), dp.name());
				}
			}
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
