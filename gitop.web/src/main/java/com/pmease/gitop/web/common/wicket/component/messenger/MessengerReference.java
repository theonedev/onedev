package com.pmease.gitop.web.common.wicket.component.messenger;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@SuppressWarnings("serial")
public class MessengerReference extends JavaScriptResourceReference {

	private static final MessengerReference instance = new MessengerReference();
	
	public static MessengerReference instance() {
		return instance;
	}
	
	public MessengerReference() {
		super(MessengerReference.class, "res/js/messenger.min.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Iterables.concat(super.getDependencies(),
				ImmutableList.<HeaderItem>builder()
					.add(JavaScriptHeaderItem.forReference(newJsReference("res/js/messenger-theme-flat.js")))
					.add(CssHeaderItem.forReference(newCssReference("res/css/messenger.css")))
					.add(CssHeaderItem.forReference(newCssReference("res/css/messenger-theme-flat.css")))
					.build());
	}
	
	private static ResourceReference newJsReference(String url) {
		return new JavaScriptResourceReference(MessengerResourcesBehavior.class, url);
	}
	
	private static ResourceReference newCssReference(String url) {
		return new CssResourceReference(MessengerResourcesBehavior.class, url);
	}

}
