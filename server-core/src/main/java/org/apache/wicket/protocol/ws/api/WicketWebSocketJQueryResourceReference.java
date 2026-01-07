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
package org.apache.wicket.protocol.ws.api;

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.ajax.WicketAjaxJQueryResourceReference;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A resource reference that provides the JavaScript that may be used to create WebSocket
 * connections in the browser. The benefit over usage of plain &lt;code&gt;window.WebSocket&lt;code&gt;
 * is that it supports handling of Wicket's &lt;ajax-response&gt; responses.
 *
 * @since 6.0
 */
public class WicketWebSocketJQueryResourceReference extends JavaScriptResourceReference
{
	private static final long serialVersionUID = 1;

	private static final WicketWebSocketJQueryResourceReference instance = new WicketWebSocketJQueryResourceReference();

	/**
	 * @return the singleton instance
	 */
	public static WicketWebSocketJQueryResourceReference get()
	{
		return instance;
	}

	private WicketWebSocketJQueryResourceReference()
	{
		super(WicketWebSocketJQueryResourceReference.class, "wicket-websocket-jquery.js");
	}

	@Override
	public List<HeaderItem> getDependencies()
	{
		final ResourceReference wicketAjaxReference;
		if (Application.exists())
		{
			wicketAjaxReference = Application.get().getJavaScriptLibrarySettings().getWicketAjaxReference();
		}
		else
		{
			wicketAjaxReference = WicketAjaxJQueryResourceReference.get();
		}
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(wicketAjaxReference));
		return dependencies;
	}
}
