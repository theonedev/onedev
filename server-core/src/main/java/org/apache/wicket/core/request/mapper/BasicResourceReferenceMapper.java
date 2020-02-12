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

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.MetaInfStaticResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.ResourceReferenceRegistry;
import org.apache.wicket.request.resource.caching.IResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.request.resource.caching.ResourceUrl;
import org.apache.wicket.resource.ResourceUtil;
import org.apache.wicket.resource.bundles.ResourceBundleReference;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.string.Strings;

/**
 * Generic {@link ResourceReference} encoder that encodes and decodes non-mounted
 * {@link ResourceReference}s.
 * <p>
 * Decodes and encodes the following URLs:
 * 
 * <pre>
 *    /wicket/resource/org.apache.wicket.ResourceScope/name
 *    /wicket/resource/org.apache.wicket.ResourceScope/name?en
 *    /wicket/resource/org.apache.wicket.ResourceScope/name?-style
 *    /wicket/resource/org.apache.wicket.ResourceScope/resource/name.xyz?en_EN-style
 * </pre>
 * 
 * @author Matej Knopp
 * @author igor.vaynberg
 * @author Peter Ertl
 */
public class BasicResourceReferenceMapper extends AbstractResourceReferenceMapper
{
	protected final IPageParametersEncoder pageParametersEncoder;

	/** resource caching strategy */
	protected final IProvider<? extends IResourceCachingStrategy> cachingStrategy;
	
	private static Map<String, String> classNames = new ConcurrentHashMap<>();

	/**
	 * Construct.
	 * 
	 * @param pageParametersEncoder
	 * @param cachingStrategy
	 */
	public BasicResourceReferenceMapper(IPageParametersEncoder pageParametersEncoder,
		IProvider<? extends IResourceCachingStrategy> cachingStrategy)
	{
		this.pageParametersEncoder = Args.notNull(pageParametersEncoder, "pageParametersEncoder");
		this.cachingStrategy = cachingStrategy;
	}

	@Override
	public IRequestHandler mapRequest(Request request)
	{
		Url url = request.getUrl();

		if (canBeHandled(url))
		{
			final int segmentsSize = url.getSegments().size();

			// extract the PageParameters from URL if there are any
			PageParameters pageParameters = extractPageParameters(request, segmentsSize,
					pageParametersEncoder);

			String normalizedClassName = url.getSegments().get(2);
			String className = classNames.get(normalizedClassName);
			if (className == null)
				className = normalizedClassName;
			StringBuilder name = new StringBuilder(segmentsSize * 2);

			for (int i = 3; i < segmentsSize; ++i)
			{
				String segment = url.getSegments().get(i);

				// ignore invalid segments
				if (segment.indexOf('/') > -1)
				{
					return null;
				}

				// remove caching information
				if (i + 1 == segmentsSize && Strings.isEmpty(segment) == false)
				{
					// The filename + parameters eventually contain caching
					// related information which needs to be removed
					ResourceUrl resourceUrl = new ResourceUrl(segment, pageParameters);
					getCachingStrategy().undecorateUrl(resourceUrl);
					segment = resourceUrl.getFileName();

					Checks.notEmpty(segment, "Caching strategy returned empty name for '%s'", resourceUrl);
				}
				if (name.length() > 0)
				{
					name.append('/');
				}
				name.append(segment);
			}

			ResourceReference.UrlAttributes attributes = ResourceUtil.decodeResourceReferenceAttributes(url);

			Class<?> scope = resolveClass(className);

			if (scope != null && scope.getPackage() != null)
			{
				ResourceReference res = getContext().getResourceReferenceRegistry()
					.getResourceReference(scope, name.toString(), attributes.getLocale(),
						attributes.getStyle(), attributes.getVariation(), true, true);

				if (res != null)
				{
					return new ResourceReferenceRequestHandler(res, pageParameters);
				}
			}
		}
		return null;
	}

	protected final IResourceCachingStrategy getCachingStrategy()
	{
		return cachingStrategy.get();
	}

	protected Class<?> resolveClass(String name)
	{
		return WicketObjects.resolveClass(name);
	}

	protected String getClassName(Class<?> scope)
	{
		return scope.getName();
	}

	@Override
	public Url mapHandler(IRequestHandler requestHandler)
	{
		if (requestHandler instanceof ResourceReferenceRequestHandler)
		{
			ResourceReferenceRequestHandler referenceRequestHandler = (ResourceReferenceRequestHandler)requestHandler;
			ResourceReference reference = referenceRequestHandler.getResourceReference();

			Url url;

			while (reference instanceof ResourceBundleReference)
			{
				// unwrap the bundle to render the url for the actual reference
				reference = ((ResourceBundleReference)reference).getBundleReference();
			}

			if (reference instanceof MetaInfStaticResourceReference)
			{
				url = ((MetaInfStaticResourceReference)reference).mapHandler(referenceRequestHandler);
				// if running on Servlet 3.0 engine url is not null
				if (url != null)
				{
					return url;
				}
				// otherwise it has to be served by the standard wicket way
			}

			if (reference.canBeRegistered())
			{
				ResourceReferenceRegistry resourceReferenceRegistry = getContext().getResourceReferenceRegistry();
				resourceReferenceRegistry.registerResourceReference(reference);
			}

			url = new Url();

			List<String> segments = url.getSegments();
			segments.add(getContext().getNamespace());
			segments.add(getContext().getResourceIdentifier());
			
			String className = getClassName(reference.getScope());
			
			/* 
			 * Avoid using mixed case in url as some agents will convert url to lower case
			 */
			String normalizedClassName = className.toLowerCase();
			classNames.put(normalizedClassName, className);
			segments.add(normalizedClassName);

			// setup resource parameters
			PageParameters parameters = referenceRequestHandler.getPageParameters();

			if (parameters == null)
			{
				parameters = new PageParameters();
			}
			else
			{
				parameters = new PageParameters(parameters);

				// need to remove indexed parameters otherwise the URL won't be able to decode
				parameters.clearIndexed();
			}

			ResourceUtil.encodeResourceReferenceAttributes(url, reference);

			StringTokenizer tokens = new StringTokenizer(reference.getName(), "/");

			while (tokens.hasMoreTokens())
			{
				String token = tokens.nextToken();

				// on the last component of the resource path
				if (tokens.hasMoreTokens() == false && Strings.isEmpty(token) == false)
				{
					final IResource resource = reference.getResource();

					// is resource supposed to be cached?
					if (resource instanceof IStaticCacheableResource)
					{
						final IStaticCacheableResource cacheable = (IStaticCacheableResource)resource;
						
						// is caching enabled?
						if(cacheable.isCachingEnabled())
						{
							// apply caching scheme to resource url
							final ResourceUrl resourceUrl = new ResourceUrl(token, parameters);
							getCachingStrategy().decorateUrl(resourceUrl, cacheable);
							token = resourceUrl.getFileName();
	
						  Checks.notEmpty(token, "Caching strategy returned empty name for '%s'", resource);
						}
					}
				}
				segments.add(token);
			}

			if (parameters.isEmpty() == false)
			{
				url = encodePageParameters(url, parameters, pageParametersEncoder);
			}

			return url;
		}
		return null;
	}

	@Override
	public int getCompatibilityScore(Request request)
	{
		Url url = request.getUrl();

		int score = -1;
		if (canBeHandled(url))
		{
			score = 1;
		}

		return score;
	}

	/**
	 * Checks whether the passed Url can be handled by this mapper
	 *
	 * @param url
	 *      the Url to check
	 * @return {@code true} - if the Url can be handled, {@code false} - otherwise
	 */
	protected boolean canBeHandled(final Url url)
	{
		List<String> segments = url.getSegments();
		return (segments.size() >= 4 &&
				urlStartsWith(url, getContext().getNamespace(), getContext().getResourceIdentifier()) &&
				Strings.isEmpty(segments.get(3)) == false
		);

	}
}
