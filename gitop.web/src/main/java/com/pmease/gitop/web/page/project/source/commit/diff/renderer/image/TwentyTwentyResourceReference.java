package com.pmease.gitop.web.page.project.source.commit.diff.renderer.image;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@SuppressWarnings("serial")
public class TwentyTwentyResourceReference extends JavaScriptResourceReference {

	public TwentyTwentyResourceReference() {
		super(TwentyTwentyResourceReference.class, "res/twentytwenty.js");
	}

	private static final JavaScriptResourceReference EVENT_MOVE_JS = 
			new JavaScriptResourceReference(TwentyTwentyResourceReference.class, "res/jquery.event.move.js");
	
	private static final CssResourceReference TWENTYTWENTY_CSS =
			new CssResourceReference(TwentyTwentyResourceReference.class, "res/twentytwenty.css");
	
	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Iterables.concat(super.getDependencies(),
				ImmutableList.<HeaderItem>builder()
					.add(JavaScriptHeaderItem.forReference(EVENT_MOVE_JS))
					.add(CssHeaderItem.forReference(TWENTYTWENTY_CSS))
					.build());
	}
	
	private final static TwentyTwentyResourceReference instance =
			new TwentyTwentyResourceReference();
	
	public final static TwentyTwentyResourceReference getInstance() {
		return instance;
	}
}
