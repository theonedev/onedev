package io.onedev.server.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.page.base.BasePage;

public abstract class AjaxLazyLoadPanel extends org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel {

	private static final long serialVersionUID = 1L;

	public AjaxLazyLoadPanel(String id) {
		super(id);
	}

	public AjaxLazyLoadPanel(final String id, final IModel<?> model) {
		super(id, model);
	}

	@Override
	public Component getLoadingComponent(String markupId) {
		BasePage page = (BasePage) getPage();
		String icon = page.isDarkMode()? "dark-ajax-indicator.gif": "ajax-indicator.gif";
		return new Label(markupId, "<img alt='Loading...' src='/~img/" + icon + "'/>").setEscapeModelStrings(false);
	}
	
}
