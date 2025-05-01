package io.onedev.server.web.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;

public class ZoneIdBehavior extends Behavior {
	
	@Override
	public void bind(Component component) {
		super.bind(component);
		component.setOutputMarkupId(true);
	}

}
