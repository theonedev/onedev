package io.onedev.server.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.page.base.BasePage;

public abstract class AjaxLazyLoadPanel extends org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel<Component> {

	private static final long serialVersionUID = 1L;

	public AjaxLazyLoadPanel(String id) {
		super(id);
	}

	public AjaxLazyLoadPanel(final String id, final IModel<?> model) {
		super(id, model);
	}

	@Override
	protected void initTimer() {
		if (getPage().getBehaviors(AjaxLazyLoadTimer.class).isEmpty()) {
			var timer = new AjaxLazyLoadTimer() {
				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					AjaxLazyLoadPanel.this.updateAjaxAttributes(attributes);
				}
			};
			getPage().add(timer);
			// Page behaviors are not rendered when this panel is added via Ajax.
			getRequestCycle().find(AjaxRequestTarget.class).ifPresent(timer::restart);
		}
		super.initTimer();
	}

	/**
	 * Customize the page-wide lazy-load timer's Ajax attributes. When multiple panels
	 * share a timer, the panel that installs it supplies the attributes.
	 */
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
	}

	@Override
	public Component getLoadingComponent(String markupId) {
		BasePage page = (BasePage) getPage();
		String icon = page.isDarkMode()? "dark-ajax-indicator.gif": "ajax-indicator.gif";
		return new Label(markupId, "<img alt='Loading...' src='/~img/" + icon + "'/>").setEscapeModelStrings(false);
	}
	
}
