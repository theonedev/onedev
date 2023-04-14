package io.onedev.server.ee.dashboard;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.model.support.Widget;

interface WidgetEditCallback extends Serializable {
	
	void onSave(AjaxRequestTarget target, WidgetPanel widgetPanel);
	
	void onCopy(AjaxRequestTarget target, Widget widget);
	
	void onDelete(AjaxRequestTarget target, WidgetPanel widgetPanel);
	
}