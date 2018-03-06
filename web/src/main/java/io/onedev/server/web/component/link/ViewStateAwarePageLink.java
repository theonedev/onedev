package io.onedev.server.web.component.link;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * This class stores view state (scroll position, cursor etc) before leaving current page, so that 
 * the view state can be restored when we go back to current page  
 * 
 * @author robin
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public class ViewStateAwarePageLink<T> extends BookmarkablePageLink<T> {

	public <C extends Page> ViewStateAwarePageLink(String id, final Class<C> pageClass) {
		super(id, pageClass);
	}

	public <C extends Page> ViewStateAwarePageLink(String id, final Class<C> pageClass, PageParameters parameters) {
		super(id, pageClass, parameters);
	}

	@Override
	protected CharSequence getOnClickScript(CharSequence url) {
		return "onedev.server.viewState.getFromViewAndSetToHistory();";
	}
	
}
