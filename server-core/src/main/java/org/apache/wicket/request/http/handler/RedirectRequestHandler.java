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
package org.apache.wicket.request.http.handler;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Args;

/**
 * A request handler that redirects to the given url.
 * 
 * the url should be one of the following:
 * <ul>
 * <li>Fully qualified "http://foo.com/bar"</li>
 * <li>Relative to the Wicket filter/servlet, e.g. "?wicket:interface=foo", "mounted_page"</li>
 * <li>Absolute within your web application's <strong>context root</strong>, e.g. "/foo.html"</li>
 * </ul>
 * 
 * @author igor.vaynberg
 * @author jcompagner
 */
public class RedirectRequestHandler implements IRequestHandler
{
	public enum Mode
	{
		/**
		 * Use {@link WebResponse#sendRedirect(String)}.
		 * It will call {@link WebResponse#encodeRedirectURL(CharSequence)} and may URL-encode some parts of the {@code location}
		 */
		REDIRECT,
		/**
		 * Use {@link WebResponse#setStatus(int)} + {@link WebResponse#setHeader(String, String)}.
		 * Sets the {@code location} as a value of {@code Location} response header as is, i.e. without url-encoding.
		 */
		STATUS,
		/**
		 * Decide dynamically depending on the value of {@code status} field
		 */
		AUTO;
	}

	private final String redirectUrl;
	private final int status;

	private Mode mode = Mode.AUTO;

	/**
	 * @param redirectUrl
	 *            URL to redirect to.
	 */
	public RedirectRequestHandler(final String redirectUrl)
	{
		this(redirectUrl, HttpServletResponse.SC_MOVED_TEMPORARILY);
	}

	/**
	 * @param redirectUrl
	 *            URL to redirect to.
	 * @param status
	 *            301 (Moved permanently) or 302 (Moved temporarily)
	 */
	public RedirectRequestHandler(final String redirectUrl, final int status)
	{
		if ((status != HttpServletResponse.SC_MOVED_PERMANENTLY) &&
			(status != HttpServletResponse.SC_MOVED_TEMPORARILY) &&
			(status != HttpServletResponse.SC_SEE_OTHER))
		{
			throw new IllegalStateException("Status must be either 301, 302 or 303, but was: " + status);
		}
		this.redirectUrl = Args.notEmpty(redirectUrl, "redirectUrl");
		this.status = status;
	}

	/**
	 * @return redirect url
	 */
	public String getRedirectUrl()
	{
		return redirectUrl;
	}

	/**
	 * @return http redirect status code
	 */
	public int getStatus()
	{
		return status;
	}

	public void detach(final IRequestCycle requestCycle)
	{
	}

	public RedirectRequestHandler mode(Mode mode)
	{
		this.mode = mode != null ? mode : Mode.AUTO;
		return this;
	}

	@Override
	public void respond(final IRequestCycle requestCycle)
	{
		String location = requestCycle.getUrlRenderer().renderRelativeUrl(Url.parse(getRedirectUrl()));

		WebResponse response = (WebResponse)requestCycle.getResponse();

		if (mode == Mode.REDIRECT)
		{
			response.sendRedirect(location);
		}
		else if (mode == Mode.STATUS)
		{
			setStatus(response, requestCycle, location);
		}
		// Mode.AUTO
		else if (status == HttpServletResponse.SC_MOVED_TEMPORARILY)
		{
			response.sendRedirect(location);
		}
		else
		{
			setStatus(response, requestCycle, location);
		}
	}

	private void setStatus(final WebResponse response, final IRequestCycle requestCycle, final String location) {
		response.setStatus(status);

		if (((WebRequest)requestCycle.getRequest()).isAjax())
		{
			response.setHeader("Ajax-Location", location);
		}
		else
		{
			response.setHeader("Location", location);
		}
	}
}
