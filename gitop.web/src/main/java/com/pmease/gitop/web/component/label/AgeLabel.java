package com.pmease.gitop.web.component.label;

import java.util.Date;

import org.apache.tools.ant.util.DateUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.Constants;

@SuppressWarnings("serial")
public class AgeLabel extends Label {

	public AgeLabel(String id, final IModel<Date> model) {
		super(id, new AgeModel(model));
		
		add(AttributeModifier.replace("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Date date = model.getObject();
				return DateUtils.format(date, Constants.DATETIME_FULL_FORMAT);
			}
		}));
	}
}
