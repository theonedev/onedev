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
import io.onedev.server.util.EditContext;
import io.onedev.server.util.MultiValueIssueField;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.issues.issuedetail.FieldValueFixOption.FixType;

@SuppressWarnings("serial")
abstract class FieldValueFixPanel extends Panel implements EditContext {

	public FieldValueFixPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		FieldValueFixOption bean = new FieldValueFixOption();
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};

		form.add(new Label("fieldName", getField().getName()));
		form.add(new Label("fieldValue", getInvalidValue()));
		
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
				if (bean.getFixType() == FixType.DELETE_THIS_VALUE) {
					if (bean.isFixAll())
						issueFieldManager.deleteFieldValue(getIssue().getProject(), getField().getName(), getInvalidValue());
					else
						issueFieldManager.deleteFieldValue(getIssue(), getField().getName(), getInvalidValue());
				} else {
					if (bean.isFixAll())
						issueFieldManager.renameFieldValue(getIssue().getProject(), getField().getName(), getInvalidValue(), bean.getNewValue());
					else
						issueFieldManager.renameFieldValue(getIssue(), getField().getName(), getInvalidValue(), bean.getNewValue());
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
	
	abstract MultiValueIssueField getField();
	
	abstract String getInvalidValue();

	abstract void onFixed(AjaxRequestTarget target);
	
	abstract void onCancel(AjaxRequestTarget target);

}
