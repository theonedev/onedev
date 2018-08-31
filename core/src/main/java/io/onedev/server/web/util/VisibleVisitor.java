package io.onedev.server.web.util;

import org.apache.wicket.Component;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

public class VisibleVisitor implements IVisitor<Component, Component> {

	@Override
	public void component(Component component, IVisit<Component> visit) {
		component.configure();
		if (component.isVisible())
			visit.stop(component);
		else
			visit.dontGoDeeper();
	}
		
}
