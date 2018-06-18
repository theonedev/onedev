package io.onedev.server.web.behavior.infinitescroll;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.unbescape.javascript.JavaScriptEscape;

import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.util.ajaxlistener.DisableGlobalLoadingIndicatorListener;

public abstract class InfiniteScrollBehavior extends AbstractPostAjaxBehavior {

	private static final long serialVersionUID = 1L;

	private final int pageSize;
	
	public InfiniteScrollBehavior(int pageSize) {
		this.pageSize = pageSize;
	}
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.getAjaxCallListeners().add(new DisableGlobalLoadingIndicatorListener());
	}

	@Override
	protected void respond(AjaxRequestTarget target) {
		IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
		int page = params.getParameterValue("page").toInt();
		target.prependJavaScript(String.format("$('#%s .loading-indicator').remove();", 
				getComponent().getMarkupId()));
		
		appendPage(target, page);

		target.appendJavaScript(String.format("onedev.infiniteScroll.check('%s');", 
				getComponent().getMarkupId()));
	}
	
	@Nullable
	protected String getItemSelector() {
		return null;
	}
	
	protected abstract void appendPage(AjaxRequestTarget target, int page);
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(JavaScriptReferenceHeaderItem.forReference(new InfiniteScrollResourceReference()));
		
		String itemSelector;
		if (getItemSelector() != null)
			itemSelector = "'"+JavaScriptEscape.escapeJavaScript(getItemSelector())+"'";
		else
			itemSelector = "undefined";
		String script = String.format("onedev.infiniteScroll.init('%s', %s, %s, %s);", 
				component.getMarkupId(true), getCallbackFunction(explicit("page")), 
				pageSize, itemSelector);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
