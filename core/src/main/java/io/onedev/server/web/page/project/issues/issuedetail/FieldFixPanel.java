package io.onedev.server.web.page.project.issues.issuedetail;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueFieldManager;
import io.onedev.server.model.Issue;
import io.onedev.server.util.MultiValueIssueField;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.issues.issuedetail.FieldFixOption.FixType;

@SuppressWarnings("serial")
abstract class FieldFixPanel extends Panel {

	public FieldFixPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		FieldFixOption bean = new FieldFixOption();
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};
		
		String message;
		InputSpec fieldSpec = getIssue().getProject().getIssueWorkflow().getField(getInvalidField().getName());
		if (fieldSpec == null) {
			message = "Field '" + getInvalidField().getName() + "' is undefined. You may change "
					+ "it to another existing field or delete it below";
		} else { 
			message = "Type of field '" + getInvalidField().getName() + "' is inconsistent with "
					+ "its definition. You may change it to another field with same type or "
					+ "delete it below";
		}
		
		form.add(new Label("message", message));
		
		form.add(BeanContext.editBean("editor", bean));
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.add(new AjaxButton("fix") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				IssueFieldManager issueFieldManager = OneDev.getInstance(IssueFieldManager.class);
				if (bean.getFixType() == FixType.DELETE_THIS_FIELD) {
					if (bean.isFixAll())
						issueFieldManager.deleteField(getIssue().getProject(), getInvalidField().getName());
					else
						issueFieldManager.deleteField(getIssue(), getInvalidField().getName());
				} else {
					if (bean.isFixAll())
						issueFieldManager.renameField(getIssue().getProject(), getInvalidField().getName(), bean.getNewField());
					else
						issueFieldManager.renameField(getIssue(), getInvalidField().getName(), bean.getNewField());
				}
				
				onFixed(target);
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.setOutputMarkupId(true);
		add(form);
	}

	abstract Issue getIssue();
	
	abstract MultiValueIssueField getInvalidField();

	abstract void onFixed(AjaxRequestTarget target);
	
	abstract void onCancel(AjaxRequestTarget target);
}
