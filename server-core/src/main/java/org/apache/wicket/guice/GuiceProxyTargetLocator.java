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
package org.apache.wicket.guice;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.util.lang.Objects;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;

import io.onedev.commons.loader.AppLoader;

class GuiceProxyTargetLocator implements IProxyTargetLocator
{
	private static final long serialVersionUID = 1L;

	private final Annotation bindingAnnotation;

	private final boolean optional;

	private final String className;

	private final String fieldName;

	private Boolean isSingletonCache = null;

	public GuiceProxyTargetLocator(final Field field, final Annotation bindingAnnotation,
								   final boolean optional)
	{
		this.bindingAnnotation = bindingAnnotation;
		this.optional = optional;
		className = field.getDeclaringClass().getName();
		fieldName = field.getName();
	}

	@Override
	public Object locateProxyTarget()
	{
		Injector injector = getInjector();

		final Key<?> key = newGuiceKey();

		// if the Inject annotation is marked optional and no binding is found
		// then skip this injection (WICKET-2241)
		if (optional)
		{
			// Guice 2.0 throws a ConfigurationException if no binding is find while 1.0 simply
			// returns null.
			try
			{
				if (injector.getBinding(key) == null)
				{
					return null;
				}
			}
			catch (RuntimeException e)
			{
				return null;
			}
		}

		return injector.getInstance(key);
	}

	private Key<?> newGuiceKey()
	{
		final Type type;
		try
		{
			Class<?> clazz = WicketObjects.resolveClass(className);
			final Field field = clazz.getDeclaredField(fieldName);
			type = field.getGenericType();
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("Error accessing member: " + fieldName +
				" of class: " + className, e);
		}

		// using TypeLiteral to retrieve the key gives us automatic support for
		// Providers and other injectable TypeLiterals
		if (bindingAnnotation == null)
		{
			return Key.get(TypeLiteral.get(type));
		}
		else
		{
			return Key.get(TypeLiteral.get(type), bindingAnnotation);
		}
	}

	public boolean isSingletonScope()
	{
		if (isSingletonCache == null)
		{
			try
			{
				isSingletonCache = Scopes.isSingleton(getInjector().getBinding(newGuiceKey()));
			}
			catch (ConfigurationException ex)
			{
				// No binding, if optional can pretend this is null singleton
				if (optional)
					isSingletonCache = true;
				else
					throw ex;
			}
		}
		return isSingletonCache;
	}

	private Injector getInjector()
	{
		return AppLoader.injector;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof GuiceProxyTargetLocator))
			return false;
		GuiceProxyTargetLocator that = (GuiceProxyTargetLocator) o;
		return Objects.equal(optional, that.optional) &&
				Objects.equal(bindingAnnotation, that.bindingAnnotation) &&
				Objects.equal(className, that.className) &&
				Objects.equal(fieldName, that.fieldName);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(bindingAnnotation, optional, className, fieldName);
	}

}
