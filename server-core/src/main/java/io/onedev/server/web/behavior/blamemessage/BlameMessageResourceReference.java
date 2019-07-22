package io.onedev.server.web.behavior.blamemessage;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class BlameMessageResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public BlameMessageResourceReference() {
		super(BlameMessageResourceReference.class, "blame-message.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BlameMessageCssResourceReference()));
		return dependencies;
	}

}
