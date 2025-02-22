package io.onedev.server.web.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.Model;

public class DisplayNoneBehavior extends AttributeAppender {
	
	public DisplayNoneBehavior() {
		super("style", Model.of("display:none"));
	}

	@Override
	public boolean isTemporary(Component component) {
		return true;
	}
	
}