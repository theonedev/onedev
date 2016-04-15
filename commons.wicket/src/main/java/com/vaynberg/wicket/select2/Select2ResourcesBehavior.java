/*
 * Copyright 2012 Igor Vaynberg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.vaynberg.wicket.select2;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

/**
 * Adds various resources needed by Select2 such as JavaScript and CSS. Which resources are added is controlled by the
 * {@link ApplicationSettings} object. Minified versions of JavaScript resources will be used when the application is
 * configured in deployment mode.
 * 
 * @author igor
 * 
 */
public class Select2ResourcesBehavior extends Behavior {

	private static final long serialVersionUID = 1L;

	@Override
    public void renderHead(Component component, IHeaderResponse response) {

	final ApplicationSettings settings = ApplicationSettings.get();

	// Include Wicket's provided jQuery reference
	response.render(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings()
		.getJQueryReference()));

	if (settings.isIncludeMouseWheel()) {
	    response.render(JavaScriptHeaderItem.forReference(settings.getMouseWheelReference()));
	}

	if (settings.isIncludeJavascript()) {
	    response.render(JavaScriptHeaderItem.forReference(settings.getJavaScriptReference()));
	}

	if (settings.isIncludeCss()) {
	    response.render(CssHeaderItem.forReference(settings.getCssReference()));
	}
    }

}
