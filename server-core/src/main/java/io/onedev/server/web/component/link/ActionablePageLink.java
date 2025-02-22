package io.onedev.server.web.component.link;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.behavior.CtrlAwareOnClickAjaxBehavior;

public abstract class ActionablePageLink extends AbstractLink {

	private final Class<? extends Page> pageClass;

	private PageParameters params;

	public ActionablePageLink(String id, Class<? extends Page> pageClass, PageParameters params) {
		super(id);
		this.pageClass = pageClass;
		this.params = params;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new CtrlAwareOnClickAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				onClick(target);
			}

		});

		setOutputMarkupId(true);
	}

	public PageParameters getPageParams() {
		return params;
	}

	public Class<? extends Page> getPageClass() {
		return pageClass;
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.put("href", RequestCycle.get().urlFor(pageClass, params));
	}

	protected void onClick(AjaxRequestTarget target) {
		doBeforeNav(target);
		setResponsePage(pageClass, params);
	}

	protected abstract void doBeforeNav(AjaxRequestTarget target);
	
}
