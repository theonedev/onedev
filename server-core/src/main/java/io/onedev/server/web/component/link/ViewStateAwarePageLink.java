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
public class ViewStateAwarePageLink<T> extends BookmarkablePageLink<T> {

	private final String scrollTopKey;

	public <C extends Page> ViewStateAwarePageLink(String id, final Class<C> pageClass) {
		this(id, pageClass, new PageParameters());
	}

	public <C extends Page> ViewStateAwarePageLink(String id, final Class<C> pageClass, PageParameters parameters) {
		this(id, pageClass, parameters, null);
	}

	public <C extends Page> ViewStateAwarePageLink(String id, final Class<C> pageClass, PageParameters parameters, String scrollTopKey) {
		super(id, pageClass, parameters);
		this.scrollTopKey = scrollTopKey;
	}

	@Override
	protected CharSequence getOnClickScript(CharSequence url) {
		var script = "onedev.server.viewState.getFromViewAndSetToHistory();";
		if (scrollTopKey != null) {
			script += "" + 
				"localStorage.setItem('" + scrollTopKey + "', Math.floor($(this).closest('.autofit').scrollTop()).toString());";
		}
		return script;
	}
	
}
