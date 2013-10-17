package com.pmease.gitop.web.page.test;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.web.page.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	public static boolean ready = false;
	
	public TestPage() {
		if (!ready)
			redirectWithInterception(TestPage2.class);
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();

		final EditContext editContext = EditableUtils.getContext(new Project());
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				editContext.validate();
				if (!editContext.hasValidationError()) {
				}
			}
			
		};
		
		form.add((Component) editContext.renderForEdit("editor"));
		
		add(form);
		
		add(new AjaxLink<Void>("reset") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				ready = false;
				setResponsePage(new TestPage());
			}
			
		});
	}
	
	@Override
	protected String getPageTitle() {
		return "Test page used by Robin";
	}

}
