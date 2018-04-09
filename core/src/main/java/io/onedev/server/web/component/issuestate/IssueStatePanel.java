package io.onedev.server.web.component.issuestate;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;

@SuppressWarnings("serial")
public abstract class IssueStatePanel extends GenericPanel<Issue> {

	public IssueStatePanel(String id, IModel<Issue> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("name", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getIssue().getState();
			}
			
		}));
		add(new ModalLink("undefined") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "fixUndefinedFrag", IssueStatePanel.this);
				StateFixOption bean = new StateFixOption();
				
				Form<?> form = new Form<Void>("form") {

					@Override
					protected void onError() {
						super.onError();
						RequestCycle.get().find(AjaxRequestTarget.class).add(this);
					}
					
				};
				form.add(new Label("state", getIssue().getState()));
				
				form.add(new AjaxLink<Void>("close") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				
				form.add(BeanContext.editBean("editor", bean));
				form.add(new AjaxButton("fix") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						IssueManager issueManager = OneDev.getInstance(IssueManager.class);
						if (bean.isFixAll()) {
							issueManager.renameState(getIssue().getProject(), getIssue().getState(), bean.getNewState());
						} else {
							getIssue().setState(bean.getNewState());
							issueManager.save(getIssue());
						}
						
						onFixed(target);
					}
					
				});
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				form.setOutputMarkupId(true);
				fragment.add(form);
				return fragment;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getIssue().getProject().getIssueWorkflow().getState(getIssue().getState()) == null);
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueStateResourceReference()));
	}

	private Issue getIssue() {
		return getModelObject();
	}
	
	protected abstract void onFixed(AjaxRequestTarget target);
}
