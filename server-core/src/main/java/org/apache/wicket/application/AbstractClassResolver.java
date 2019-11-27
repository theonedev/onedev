/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.application;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.collections.UrlExternalFormComparator;

import io.onedev.server.buildspec.job.paramsupply.ParamSupply;
import io.onedev.server.util.IssueUtils;

/**
 * An abstract implementation of a {@link IClassResolver} which uses a {@link ClassLoader} for
 * resolving classes.
 * 
 * @see org.apache.wicket.settings.ApplicationSettings#getClassResolver()
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public abstract class AbstractClassResolver implements IClassResolver
{
	/**
	 * Usually class loaders implement more efficient caching strategies than we could possibly do,
	 * but we experienced synchronization issue resulting in stack traces like:
	 * java.lang.LinkageError: duplicate class definition:
	 * 
	 * <pre>
	 *    wicket/examples/repeater/RepeatingPage at java.lang.ClassLoader.defineClass1(Native Method)
	 * </pre>
	 * 
	 * This problem has gone since we synchronize the access.
	 */
	private final ConcurrentMap<String, WeakReference<Class<?>>> classes = new ConcurrentHashMap<>();

	@Override
	public final Class<?> resolveClass(final String className) throws ClassNotFoundException
	{
		Class<?> clazz = IssueUtils.loadFieldBeanClass(className); 
		
		if (clazz != null)
			return clazz;
		
		clazz = ParamSupply.loadBeanClass(className);
		
		if (clazz != null)
			return clazz;
		
		WeakReference<Class<?>> ref = classes.get(className);

		// Might be garbage-collected between getting the WeakRef and retrieving
		// the Class from it.
		if (ref != null)
		{
			clazz = ref.get();
		}
		if (clazz == null)
		{
			switch (className)
			{
				case "byte":
					clazz = byte.class;
					break;
				case "short":
					clazz = short.class;
					break;
				case "int":
					clazz = int.class;
					break;
				case "long":
					clazz = long.class;
					break;
				case "float":
					clazz = float.class;
					break;
				case "double":
					clazz = double.class;
					break;
				case "boolean":
					clazz = boolean.class;
					break;
				case "char":
					clazz = char.class;
					break;
				default:
					// synchronize on the only class member to load only one class at a time and
					// prevent LinkageError. See above for more info
					synchronized (classes)
					{
						clazz = Class.forName(className, false, getClassLoader());
						if (clazz == null)
						{
							throw new ClassNotFoundException(className);
						}
					}
					classes.put(className, new WeakReference<Class<?>>(clazz));
					break;
			}
		}
		return clazz;
	}

	@Override
	public Iterator<URL> getResources(final String name)
	{
		Set<URL> resultSet = new TreeSet<>(new UrlExternalFormComparator());

		try
		{
			// Try the classloader for the wicket jar/bundle
			Enumeration<URL> resources = Application.class.getClassLoader().getResources(name);
			loadResources(resources, resultSet);

			// Try the classloader for the user's application jar/bundle
			resources = Application.get().getClass().getClassLoader().getResources(name);
			loadResources(resources, resultSet);

			// Try the context class loader
			resources = getClassLoader().getResources(name);
			loadResources(resources, resultSet);
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException(e);
		}

		return resultSet.iterator();
	}

	/**
	 * 
	 * @param resources
	 * @param loadedResources
	 */
	private void loadResources(Enumeration<URL> resources, Set<URL> loadedResources)
	{
		if (resources != null)
		{
			while (resources.hasMoreElements())
			{
				final URL url = resources.nextElement();
				loadedResources.add(url);
			}
		}
	}
}
