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
package org.apache.wicket.core.request.mapper;

import org.apache.wicket.Application;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.Url.QueryParameter;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.AbstractMapper;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

import io.onedev.server.buildspec.param.ParamUtils;

/**
 * Convenience class for implementing page/components related encoders.
 *
 * @author Matej Knopp
 */
public abstract class AbstractComponentMapper extends AbstractMapper implements IRequestMapper
{
	/**
	 * Construct.
	 */
	public AbstractComponentMapper()
	{
	}

	protected IMapperContext getContext()
	{
		return Application.get().getMapperContext();
	}

	/**
	 * Extracts the {@link PageComponentInfo} from the URL. The {@link PageComponentInfo} is encoded
	 * as the very first query parameter and the parameter consists of name only (no value).
	 *
	 * @param url
	 *
	 * @return PageComponentInfo instance if one was encoded in URL, <code>null</code> otherwise.
	 */
	protected PageComponentInfo getPageComponentInfo(final Url url)
	{
		return MapperUtils.getPageComponentInfo(url);
	}

	/**
	 * Encodes the {@link PageComponentInfo} instance as the first query string parameter to the
	 * URL.
	 *
	 * @param url
	 * @param info
	 */
	protected void encodePageComponentInfo(Url url, PageComponentInfo info)
	{
		Args.notNull(url, "url");

		if (info != null)
		{
			String s = info.toString();
			if (!Strings.isEmpty(s))
			{
				QueryParameter parameter = new QueryParameter(s, "");
				url.getQueryParameters().add(parameter);
			}
		}
	}

	/**
	 * Loads page class with given name.
	 *
	 * @param name
	 * @return class
	 */
	protected Class<? extends IRequestablePage> getPageClass(String name)
	{
		String cleanedClassName = cleanClassName(name);
		// Resolving parameter bean classes deserializes data embedded in their names.
		// Page URLs are untrusted and must never trigger this deserialization.
		if (cleanedClassName.startsWith(ParamUtils.PARAM_BEAN_CLASS_NAME_PREFIX))
			return null;
		return WicketObjects.resolveClass(cleanedClassName);
	}

	/**
	 * Cleans the class name from any extra information that may be there.
	 *
	 * @param className
	 *              The raw class name parsed from the url
	 * @return The cleaned class name
	 */
	protected String cleanClassName(String className)
	{
		Args.notEmpty(className, "className");

		if (Strings.indexOf(className, ';') > -1)
		{
			// remove any path parameters set manually by the user. WICKET-5500
			className = Strings.beforeFirst(className, ';');
		}

		return className;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Removes the first query parameter only if {@link PageComponentInfo#parse(String)} returns
	 * non-null instance
	 */
	@Override
	protected void removeMetaParameter(final Url urlCopy)
	{
		if (MapperUtils.parsePageComponentInfoParameter(urlCopy.getQueryParameters().get(0)) != null)
		{
			urlCopy.getQueryParameters().remove(0);
		}
	}

}
