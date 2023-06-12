package io.onedev.server.web.component.issue.create;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import javax.annotation.Nullable;

@SuppressWarnings("serial")
public abstract class CreateIssuePanel extends Panel {

	public CreateIssuePanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		
		NewIssueEditor editor = new NewIssueEditor("editor") {

			@Override
			protected Project getProject() {
				return CreateIssuePanel.this.getProject();
			}

			@Override
			protected Criteria<Issue> getTemplate() {
				return CreateIssuePanel.this.getTemplate();
			}
			
		};
		
		form.add(editor);

		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				Issue issue = editor.getConvertedInput();
				onSave(target, issue);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}
			
		});
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		add(form);
	}

	@Nullable
	protected abstract Criteria<Issue> getTemplate();

	protected abstract void onSave(AjaxRequestTarget target, Issue issue);
	
	protected abstract void onCancel(AjaxRequestTarget target);

	protected abstract Project getProject();
}
