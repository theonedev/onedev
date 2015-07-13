package com.pmease.gitplex.web.component;

import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitplex.web.DateUtils;

@SuppressWarnings("serial")
public class AgeLabel extends Label {

	private final IModel<Date> model;
	
	public AgeLabel(String id, final IModel<Date> model) {
		super(id);
		
		this.model = model;
		
		setDefaultModel(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return DateUtils.formatAge(model.getObject());
			}
			
		});
	}

	@Override
	protected void onDetach() {
		model.detach();
		
		super.onDetach();
	}

}
