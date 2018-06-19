package io.onedev.server.web.page.project.issues.issueboards;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.query.IssueCriteria;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.newissue.NewIssueEditor;

@SuppressWarnings("serial")
abstract class NewCardPanel extends Panel {

	public NewCardPanel(String id) {
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
				return NewCardPanel.this.getProject();
			}

			@Override
			protected IssueCriteria getTemplate() {
				return NewCardPanel.this.getTemplate();
			}
			
		};
		
		form.add(editor);

		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				Issue issue = editor.getConvertedInput();
				OneDev.getInstance(IssueManager.class).open(issue);
				if (getTemplate().matches(issue)) {
					onAdded(target, issue);
				} else {
					new ModalPanel(target) {
						
						@Override
						protected Component newContent(String id) {
							return new NewCardNotMatchingPanel(id) {

								@Override
								protected Issue getIssue() {
									return issue;
								}

								@Override
								protected void onClose(AjaxRequestTarget target) {
									close();
								}
							};
						}
					};
				}
				onClose(target);
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
				onClose(target);
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		add(form);
	}

	protected abstract IssueCriteria getTemplate();
	
	protected abstract void onAdded(AjaxRequestTarget target, Issue issue);
	
	protected abstract void onClose(AjaxRequestTarget target);

	protected abstract Project getProject();
}
