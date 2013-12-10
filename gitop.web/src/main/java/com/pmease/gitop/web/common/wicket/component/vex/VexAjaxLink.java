package com.pmease.gitop.web.common.wicket.component.vex;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public abstract class VexAjaxLink<T> extends AjaxLink<T> {

	public VexAjaxLink(String id) {
		super(id);
	}
	
	public VexAjaxLink(String id, IModel<T> model) {
		super(id, model);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(VexJavaScriptResourceReference.get()));
		response.render(JavaScriptHeaderItem.forScript("vex.defaultOptions.className = 'vex-theme-wireframe'", "vex-theme-options"));
		String markupId = getMarkupId(true);
		response.render(OnDomReadyHeaderItem.forScript(String.format(
				"$('#%s').on('click', function(e){\ne.preventDefault(); \n%s });", 
				markupId,
				"vex.open()\n"
				+ ".append($('#" + getContentMarkupId() + "'))\n"
				+ ".bind('vexClose', function(){\n"
				+ "		$('#" + markupId + "').trigger('vex.closed'); \n"
				+ "});"
				)));
	}

	@Override
	protected AjaxEventBehavior newAjaxEventBehavior(String event) {
		return new AjaxEventBehavior("vex.closed") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled(Component component) {
				return VexAjaxLink.this.isLinkEnabled();
			}

			@Override
			protected void onEvent(AjaxRequestTarget target) {
				onClick(target);
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setAllowDefault(true);
			}
		};
	}
	
	abstract protected String getContentMarkupId();
}
