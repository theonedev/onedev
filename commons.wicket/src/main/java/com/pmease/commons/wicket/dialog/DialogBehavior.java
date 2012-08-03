package com.pmease.commons.wicket.dialog;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

@SuppressWarnings("serial")
public abstract class DialogBehavior extends AbstractDefaultAjaxBehavior {

	public DialogBehavior() {
	}
	
	@Override
	protected void onBind() {
		super.onBind();
		getComponent().setOutputMarkupId(true);
	}

	@Override
	protected void respond(AjaxRequestTarget target) {
		Dialog dialog = newDialog(getDialogId());
		getComponent().getParent().addOrReplace(dialog);
		dialog.setOutputMarkupId(true);
		dialog.setMarkupId(getDialogMarkupId());
		dialog.setMarkup(Markup.of(String.format("<div wicket:id='%s'></div>", getDialogMarkupId())));
		
		target.add(dialog);
		
		String scriptTemplate = 
				"$('#%s')" +
				".modal({" +
				"	backdrop:'static'" +
				"})" +
				".css({" +
				"	'width': '%s', " +
				"	'margin-left': function(){" +
				"		return -($(this).width() / 2);" +
				"	}" +
				"});"; 
		target.appendJavaScript(String.format(scriptTemplate, getDialogMarkupId(), dialog.width()));
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		String script = String.format("$('#%s').after(\"<div id='%s'/>\")", 
				getComponent().getMarkupId(), getDialogMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	private String getDialogMarkupId() {
		return getComponent().getMarkupId() + "-dialog";
	}
	
	private String getDialogId() {
		return getComponent().getId() + "-dialog";
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		String scriptTemplate = 
				"if ($('#%s').hasClass('modal')) {" +
				"  $('#%s').modal('show');" +
				"} else {" +
				"  var callback=%s; callback();" +
				"}";
		String script = String.format(scriptTemplate, getDialogMarkupId(), getDialogMarkupId(), getCallbackFunction());
		tag.put("onclick", script);
		
		if (tag.getName().equals("a"))
			tag.put("href", "#");
	}
	
	protected abstract Dialog newDialog(String id);
}
