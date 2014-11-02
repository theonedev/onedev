package com.pmease.commons.wicket.component.history;

import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public abstract class HistoryAwarePanel extends Panel {

	public HistoryAwarePanel(String id) {
		super(id);
	}

	public HistoryAwarePanel(String id, IModel<?> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newContent("content"));
		
		setRenderBodyOnly(true);
	}
	
	protected abstract Component newContent(String id);

	public abstract void onPopState(AjaxRequestTarget target, Serializable state);
	
	public void pushState(AjaxRequestTarget target, String url, Serializable state) {
		HistoryState historyState = new HistoryState(getPageRelativePath(), state);
		String encodedHistoryState = new String(Base64.encodeBase64(SerializationUtils.serialize(historyState)));
		target.appendJavaScript(String.format("History.pushState({state:'%s'}, '', '%s');", encodedHistoryState, url));
	}

}
