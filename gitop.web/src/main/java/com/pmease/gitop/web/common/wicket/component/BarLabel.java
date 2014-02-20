package com.pmease.gitop.web.common.wicket.component;

import java.util.Locale;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.base.Strings;
import com.pmease.gitop.web.common.datatype.DataTypes;

@SuppressWarnings("serial")
public class BarLabel extends Label {

	private final IModel<Double> percentModel;
	private final IModel<String> colorModel;
	
	public BarLabel(String id, IModel<Double> percentModel) {
		this(id, percentModel, Model.of(""));
	}
	
	public BarLabel(String id, IModel<Double> percentModel, IModel<String> colorModel) {
		super(id);
		
		this.percentModel = percentModel;
		this.colorModel = colorModel;
	}

	@Override
	protected void onComponentTag(final ComponentTag tag) {
		super.onComponentTag(tag);

		StringBuffer style = new StringBuffer();
		Double percent = percentModel.getObject();
		style.append("width: ")
			.append(DataTypes.PERCENT.asString(percent, "0.00000%", Locale.US));

		String color = colorModel.getObject();
		if (!Strings.isNullOrEmpty(color)) {
			style.append(";background:").append(color);
		}

		tag.put("style", style);
	}
	
	@Override
	public void onDetach() {
		if (percentModel != null) {
			percentModel.detach();
		}
		
		if (colorModel != null) {
			colorModel.detach();
		}
		
		super.onDetach();
	}
}
