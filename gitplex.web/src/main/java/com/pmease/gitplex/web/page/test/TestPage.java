package com.pmease.gitplex.web.page.test;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

import com.google.common.collect.Lists;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private String choice = "a";
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new DropDownChoice<String>("choice", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return choice;
			}

			@Override
			public void setObject(String object) {
				choice = object;
			}
			
		}, Lists.newArrayList("a", "b")).add(new OnChangeAjaxBehavior() {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				System.out.println(choice);
			}
			
		}));		
	}

}
