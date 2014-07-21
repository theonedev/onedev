package com.pmease.gitplex.web.component.label;

import java.util.Date;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.web.util.DateUtils;

public class AgeModel extends AbstractReadOnlyModel<String> {
	private static final long serialVersionUID = 1L;
	
	private IModel<Date> dateModel;
	
	public AgeModel(IModel<Date> dateModel) {
		this.dateModel = dateModel;
	}
	
	@Override
	public String getObject() {
		Date date = dateModel.getObject();
		return DateUtils.formatAge(date);
	}
	
	@Override
	public void detach() {
		if (dateModel != null) {
			dateModel.detach();
		}
		
		super.detach();
	}
	
	public Date getDate() {
		return dateModel.getObject();
	}
}