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
package org.apache.wicket.protocol.http.servlet;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Time;

/**
 * WebResponse that wraps a {@link ServletWebResponse}.
 * 
 * @author Matej Knopp
 */
public class ServletWebResponse extends WebResponse
{
	private final HttpServletResponse httpServletResponse;
	private final ServletWebRequest webRequest;

	private boolean redirect = false;

	/**
	 * Construct.
	 * 
	 * @param webRequest
	 * @param httpServletResponse
	 */
	public ServletWebResponse(ServletWebRequest webRequest, HttpServletResponse httpServletResponse)
	{
		Args.notNull(webRequest, "webRequest");
		Args.notNull(httpServletResponse, "httpServletResponse");

		this.httpServletResponse = httpServletResponse;
		this.webRequest = webRequest;
	}

	@Override
	public void addCookie(Cookie cookie)
	{
		httpServletResponse.addCookie(cookie);
	}

	@Override
	public void clearCookie(Cookie cookie)
	{
		cookie.setMaxAge(0);
		cookie.setValue(null);
		addCookie(cookie);
	}

	@Override
	public void setContentLength(long length)
	{
		httpServletResponse.addHeader("Content-Length", Long.toString(length));
	}

	@Override
	public void setContentType(String mimeType)
	{
		httpServletResponse.setContentType(mimeType);
	}

	@Override
	public void setDateHeader(String name, Time date)
	{
		Args.notNull(date, "date");
		httpServletResponse.setDateHeader(name, date.getMilliseconds());
	}

	@Override
	public void setHeader(String name, String value)
	{
		httpServletResponse.setHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value)
	{
		httpServletResponse.addHeader(name, value);
	}

	@Override
	public void write(CharSequence sequence)
	{
		try
		{
			httpServletResponse.getWriter().append(sequence);
		}
		catch (IOException e)
		{
			throw new ResponseIOException(e);
		}
	}

	@Override
	public void write(byte[] array)
	{
		try
		{
			httpServletResponse.getOutputStream().write(array);
		}
		catch (IOException e)
		{
			throw new ResponseIOException(e);
		}
	}

	@Override
	public void write(byte[] array, int offset, int length)
	{
		try
		{
			httpServletResponse.getOutputStream().write(array, offset, length);
		}
		catch (IOException e)
		{
			throw new ResponseIOException(e);
		}
	}


	@Override
	public void setStatus(int sc)
	{
		httpServletResponse.setStatus(sc);
	}

	@Override
	public void sendError(int sc, String msg)
	{
		try
		{
			if (msg == null)
			{
				httpServletResponse.sendError(sc);
			}
			else
			{
				httpServletResponse.sendError(sc, msg);
			}
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	@Override
	public String encodeURL(CharSequence url)
	{
		Args.notNull(url, "url");

		UrlRenderer urlRenderer = getUrlRenderer();

		Url originalUrl = Url.parse(url);

		/*
		  WICKET-4645 - always pass absolute url to the web container for encoding
		  because when REDIRECT_TO_BUFFER is in use Wicket may render PageB when
		  PageA is actually the requested one and the web container cannot resolve
		  the base url properly
		 */
		String fullUrl = urlRenderer.renderFullUrl(originalUrl);
		String encodedFullUrl = httpServletResponse.encodeURL(fullUrl);

		final String encodedUrl;
		if (originalUrl.isFull())
		{
			encodedUrl = encodedFullUrl;
		}
		else
		{
			if (fullUrl.equals(encodedFullUrl))
			{
				// no encoding happened so just reuse the original url
				encodedUrl = url.toString();
			}
			else
			{
				// get the relative url with the jsessionid encoded in it
				Url _encoded = Url.parse(encodedFullUrl);
				encodedUrl = urlRenderer.renderRelativeUrl(_encoded);
			}
		}
		return encodedUrl;
	}

	private UrlRenderer getUrlRenderer()
	{
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle == null)
		{
			return new UrlRenderer(webRequest);
		}
		return requestCycle.getUrlRenderer();
	}

	@Override
	public String encodeRedirectURL(CharSequence url)
	{
		Args.notNull(url, "url");

		UrlRenderer urlRenderer = getUrlRenderer();

		Url originalUrl = Url.parse(url);

		/*
		 * WICKET-4645 - always pass absolute url to the web container for encoding because when
		 * REDIRECT_TO_BUFFER is in use Wicket may render PageB when PageA is actually the requested
		 * one and the web container cannot resolve the base url properly
		 */
		String fullUrl = urlRenderer.renderFullUrl(originalUrl);
		String encodedFullUrl = httpServletResponse.encodeRedirectURL(fullUrl);

		final String encodedUrl;
		if (originalUrl.isFull())
		{
			encodedUrl = encodedFullUrl;
		}
		else
		{
			if (fullUrl.equals(encodedFullUrl))
			{
				// no encoding happened so just reuse the original url
				encodedUrl = url.toString();
			}
			else
			{
				// get the relative url with the jsessionid encoded in it
				Url _encoded = Url.parse(encodedFullUrl);
				encodedUrl = urlRenderer.renderRelativeUrl(_encoded);
			}
		}
		return encodedUrl;
	}

	@Override
	public void sendRedirect(String url)
	{
		try
		{
			redirect = true;
			url = encodeRedirectURL(url);
			
			// wicket redirects should never be cached
			disableCaching();

			if (webRequest.isAjax())
			{
				setHeader("Ajax-Location", url);
				setContentType("text/xml;charset=" +
					webRequest.getContainerRequest().getCharacterEncoding());

				/*
				 * usually the Ajax-Location header is enough and we do not need to the redirect url
				 * into the response, but sometimes the response is processed via an iframe (eg
				 * using multipart ajax handling) and the headers are not available because XHR is
				 * not used and that is the only way javascript has access to response headers.
				 */
				httpServletResponse.getWriter().write(
					"<ajax-response><redirect><![CDATA[" + url + "]]></redirect></ajax-response>");
			}
			else
			{
				String userAgent = webRequest.getContainerRequest().getHeader("User-Agent").toLowerCase();
				if (userAgent.contains("bot") || userAgent.contains("crawler") 
						|| userAgent.contains("spider") || userAgent.contains("crawling")) {
					httpServletResponse.sendRedirect(url);
				} else {
					httpServletResponse.resetBuffer();
					httpServletResponse.setContentType("text/html");
					
					String content = String.format(""
							+ "<!doctype html>"
							+ "<html lang='en'>"
							+ "<head>"
							+ "<script type='application/javascript'>"
							+ "  function getKey(url) {"
							+ "    var key = url;"
							+ "    if (key.indexOf('#') != -1)"
							+ "      key = key.substr(0, key.indexOf('#'));"
							+ "    if (key.indexOf(':') != -1)"
							+ "      key = key.substr(key.indexOf(':'), key.length);"
							+ "    return key;"
							+ "  }"
							+ "  if (location.hash) {"
							+ "    var url = location.pathname;"
							+ "    if (location.search)"
							+ "      url += location.search;"
							+ "    url += location.hash;"
							+ "    sessionStorage.setItem(getKey(url), location.hash);"
							+ "  }"
							+ "  var redirect = '%s';"
							+ "  var key = getKey(redirect);"
							+ "  if (redirect.indexOf('#') == -1) {"
							+ "    var hash = sessionStorage.getItem(key);"
							+ "    if (hash) "
							+ "      redirect += hash;"
							+ "  }"
							+ "  sessionStorage.removeItem(key);"
							+ "  window.location.replace(redirect);"
							+ "</script>"
							+ "</head>"
							+ "</html>", url);
					httpServletResponse.getOutputStream().write(content.getBytes());
					httpServletResponse.getOutputStream().close();
				}
			}
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	@Override
	public boolean isRedirect()
	{
		return redirect;
	}

	@Override
	public void flush()
	{
		try
		{
			HttpServletRequest httpServletRequest = webRequest.getContainerRequest();
			if (httpServletRequest.isAsyncStarted() == false)
			{
				httpServletResponse.flushBuffer();
			}
		}
		catch (IOException e)
		{
			throw new ResponseIOException(e);
		}
	}

	@Override
	public void reset()
	{
		super.reset();
		httpServletResponse.reset();
		redirect = false;
	}

	@Override
	public HttpServletResponse getContainerResponse()
	{
		return httpServletResponse;
	}

}
