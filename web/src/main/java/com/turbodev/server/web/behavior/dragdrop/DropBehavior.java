package com.turbodev.server.web.behavior.dragdrop;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;

import com.turbodev.server.web.behavior.AbstractPostAjaxBehavior;

@SuppressWarnings("serial")
public abstract class DropBehavior extends AbstractPostAjaxBehavior {

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		response.render(JavaScriptReferenceHeaderItem.forReference(new DragDropResourceReference()));
		String script = String.format("turbodev.server.dragdrop.setupDroppable('#%s', %s, %s);", 
				component.getMarkupId(), getAccept(), getCallbackFunction(explicit("dragData")));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected void respond(AjaxRequestTarget target) {
		IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
		String dragData = params.getParameterValue("dragData").toString();
		onDropped(target, dragData);
	}

	protected abstract void onDropped(AjaxRequestTarget target, String dragData);

	protected abstract String getAccept();
}