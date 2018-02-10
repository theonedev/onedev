package com.turbodev.server.web.behavior.infinitescroll;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.turbodev.server.web.behavior.AbstractPostAjaxBehavior;

public abstract class InfiniteScrollBehavior extends AbstractPostAjaxBehavior {

	private static final long serialVersionUID = 1L;

	private final int pageSize;
	
	public InfiniteScrollBehavior(int pageSize) {
		this.pageSize = pageSize;
	}
	
	@Override
	protected void respond(AjaxRequestTarget target) {
		IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
		int page = params.getParameterValue("page").toInt();
		target.prependJavaScript(String.format("$('#%s .loading-indicator').remove();", 
				getComponent().getMarkupId()));
		
		appendPage(target, page);

		target.appendJavaScript(String.format("turbodev.infiniteScroll.check('%s');", 
				getComponent().getMarkupId()));
	}
	
	protected abstract void appendPage(AjaxRequestTarget target, int page);
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(JavaScriptReferenceHeaderItem.forReference(new InfiniteScrollResourceReference()));
		
		CharSequence url = RequestCycle.get().urlFor(new PackageResourceReference(
				InfiniteScrollBehavior.class, "ajax-indicator.gif"), new PageParameters());
		String script = String.format("turbodev.infiniteScroll.init('%s', %s, '%s', %s);", 
				component.getMarkupId(true), getCallbackFunction(explicit("page")), url, pageSize);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
