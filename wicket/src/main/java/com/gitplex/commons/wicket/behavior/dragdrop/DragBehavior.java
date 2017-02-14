package com.gitplex.commons.wicket.behavior.dragdrop;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

@SuppressWarnings("serial")
public class DragBehavior extends Behavior {

	private String dragData;
	
	private String dragText;
	
	public DragBehavior(String dragData, String dragText) {
		this.dragData = dragData;
		this.dragText = dragText;
	}
	
	@Override
	public void bind(Component component) {
		super.bind(component);
		component.setOutputMarkupId(true);
	}

	@Override
	public boolean getStatelessHint(Component component) {
		return false;
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		response.render(JavaScriptReferenceHeaderItem.forReference(new DragDropResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript(String.format("gitplex.commons.dragdrop.setupDraggable('#%s', '%s', '%s');", 
				component.getMarkupId(), dragData, dragText)));
	}

}
