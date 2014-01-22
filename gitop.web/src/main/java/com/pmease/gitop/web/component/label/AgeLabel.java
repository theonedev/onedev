package com.pmease.gitop.web.component.label;

import java.util.Date;

import org.apache.tools.ant.util.DateUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.Constants;

@SuppressWarnings("serial")
public class AgeLabel extends Label {

	public AgeLabel(String id, final IModel<Date> model) {
		super(id, new AgeModel(model));
	}
	
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.put("title", DateUtils.format(getDate(), Constants.DATETIME_FORMAT));
		tag.put("data-toggle", "tooltip");
	}
	
	private Date getDate() {
		AgeModel model = (AgeModel) getDefaultModel();
		return model.getDate();
	}
}
