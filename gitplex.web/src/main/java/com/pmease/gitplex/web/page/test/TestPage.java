package com.pmease.gitplex.web.page.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.behavior.inputassist.AssistItem;
import com.pmease.commons.wicket.behavior.inputassist.ErrorMark;
import com.pmease.commons.wicket.behavior.inputassist.InputAssist;
import com.pmease.commons.wicket.behavior.inputassist.InputAssistBehavior;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private String query;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<Void> form = new Form<Void>("form");
		add(form);
		
		form.add(new TextField<String>("query", new IModel<String>(){

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return query;
			}

			@Override
			public void setObject(String object) {
				query = object;
			}
			
		}).add(new InputAssistBehavior() {

			@Override
			protected InputAssist assist(String input, int cursor) {
				List<AssistItem> assistItems = new ArrayList<>();
				for (int i=0; i<20; i++) {
					String newInput = input + " hello " + i*10;
					assistItems.add(new AssistItem(newInput, newInput.length()));
					newInput = input + " world " + i*10;
					assistItems.add(new AssistItem(newInput, newInput.length()));
				}
				List<ErrorMark> inputErrors = new ArrayList<>();
				inputErrors.add(new ErrorMark(0, 1));
				return new InputAssist(inputErrors, assistItems);
			}

		}));
		
		form.add(new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				System.out.println(query);
			}
			
		});
	}

}
