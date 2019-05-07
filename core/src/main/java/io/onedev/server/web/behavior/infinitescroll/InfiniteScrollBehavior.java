package io.onedev.server.web.behavior.infinitescroll;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.unbescape.javascript.JavaScriptEscape;

import io.onedev.server.web.ajaxlistener.DisableGlobalLoadingIndicatorListener;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;

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
		int offset = params.getParameterValue("offset").toInt();
		int count = params.getParameterValue("count").toInt();
		if (offset == 0) {
			WebMarkupContainer container = (WebMarkupContainer) getComponent();
			container.visitChildren(RepeatingView.class, new IVisitor<RepeatingView, Void>() {

				@Override
				public void component(RepeatingView component, IVisit<Void> visit) {
					component.removeAll();
					visit.stop();
				}
				
			});
		}

		appendMore(target, offset, count);

		target.appendJavaScript(String.format("onedev.server.infiniteScroll.onAppended('%s');", 
				getComponent().getMarkupId()));
	}
	
	public void check(AjaxRequestTarget target) {
		target.appendJavaScript(String.format("onedev.server.infiniteScroll.check('%s');", 
				getComponent().getMarkupId()));
	}
	
	@Nullable
	protected String getItemSelector() {
		return null;
	}
	
	protected abstract void appendMore(AjaxRequestTarget target, int offset, int count);
	
	public void refresh(IPartialPageRequestHandler handler) {
		handler.appendJavaScript(String.format("onedev.server.infiniteScroll.refresh('%s');", 
				getComponent().getMarkupId()));
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(JavaScriptReferenceHeaderItem.forReference(new InfiniteScrollResourceReference()));
		
		String itemSelector;
		if (getItemSelector() != null)
			itemSelector = "'"+JavaScriptEscape.escapeJavaScript(getItemSelector())+"'";
		else
			itemSelector = "undefined";
		
		// Run onload script as the container size might be adjusted in window resize event (which 
		// happens before onload). An example is the issue board columns
		String script = String.format("onedev.server.infiniteScroll.onLoad('%s', %s, %s, %s);", 
				component.getMarkupId(true), getCallbackFunction(explicit("offset"), explicit("count")), 
				pageSize, itemSelector);
		response.render(OnLoadHeaderItem.forScript(script));
	}

}
