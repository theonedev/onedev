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
package org.apache.wicket.extensions.ajax.markup.html.autocomplete;

import java.util.Iterator;

import org.apache.wicket.Application;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Args;

import io.onedev.server.util.ComponentContext;


/**
 * This behavior builds on top of {@link AbstractAutoCompleteBehavior} by introducing the concept of
 * a {@link IAutoCompleteRenderer} to make response writing easier.
 * 
 * @param <T>
 * 
 * @see IAutoCompleteRenderer
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Janne Hietam&auml;ki (jannehietamaki)
 */
public abstract class AutoCompleteBehavior<T> extends AbstractAutoCompleteBehavior
{
	private static final long serialVersionUID = 1L;

	private final IAutoCompleteRenderer<T> renderer;

	/**
	 * Constructor
	 * 
	 * @param renderer
	 *            renderer that will be used to generate output
	 */
	public AutoCompleteBehavior(final IAutoCompleteRenderer<T> renderer)
	{
		this(renderer, false);
	}

	/**
	 * Constructor
	 * 
	 * @param renderer
	 *            renderer that will be used to generate output
	 * @param preselect
	 *            highlight/preselect the first item in the autocomplete list automatically
	 */
	public AutoCompleteBehavior(final IAutoCompleteRenderer<T> renderer, final boolean preselect)
	{
		this(renderer, new AutoCompleteSettings().setPreselect(preselect));
	}

	/**
	 * Constructor
	 * 
	 * @param renderer
	 *            renderer that will be used to generate output
	 * @param settings
	 *            settings for the autocomplete list
	 */
	public AutoCompleteBehavior(final IAutoCompleteRenderer<T> renderer,
		final AutoCompleteSettings settings)
	{
		super(settings);

		this.renderer = Args.notNull(renderer, "renderer");
	}

	@Override
	protected final void onRequest(final String val, final RequestCycle requestCycle)
	{
		IRequestHandler target = new IRequestHandler()
		{
			@Override
			public void respond(final IRequestCycle requestCycle)
			{
				ComponentContext.push(new ComponentContext(getComponent()));				
				try {
					WebResponse r = (WebResponse)requestCycle.getResponse();
	
					// Determine encoding
					final String encoding = Application.get()
						.getRequestCycleSettings()
						.getResponseRequestEncoding();
	
					r.setContentType("text/xml; charset=" + encoding);
					r.disableCaching();
	
					Iterator<T> comps = getChoices(val);
					int count = 0;
					renderer.renderHeader(r);
					while (comps.hasNext())
					{
						final T comp = comps.next();
						renderer.render(comp, r, val);
						count += 1;
					}
					renderer.renderFooter(r, count);
				} finally {
					ComponentContext.pop();
				}
			}

			@Override
			public void detach(final IRequestCycle requestCycle)
			{
			}
		};

		requestCycle.scheduleRequestHandlerAfterCurrent(target);
	}

	/**
	 * Callback method that should return an iterator over all possible choice objects. These
	 * objects will be passed to the renderer to generate output. Usually it is enough to return an
	 * iterator over strings.
	 * 
	 * @param input
	 *            current input
	 * @return iterator over all possible choice objects
	 */
	protected abstract Iterator<T> getChoices(String input);
}