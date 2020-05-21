package io.onedev.server.web.page.project.pullrequests.detail.operationconfirm;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestUpdateManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;

@SuppressWarnings("serial")
public abstract class OperationConfirmPanel extends Panel {

	private final ModalPanel modal;
	
	private Form<?> form;
	
	private Long latestUpdateId;
	
	public OperationConfirmPanel(String componentId, ModalPanel modal, Long latestUpdateId) {
		super(componentId);
		this.modal = modal;
		this.latestUpdateId = latestUpdateId;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		add(form);
		
		form.add(new Label("title", getTitle()));
		
		form.add(new FencedFeedbackPanel("feedback", form) {

			@Override
			protected Component newMessageDisplayComponent(String id, FeedbackMessage message) {
				return super.newMessageDisplayComponent(id, message).setEscapeModelStrings(false);
			}
			
		});
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				modal.close();
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
				modal.close();
			}
			
		});
		form.add(new AjaxButton("ok") {

			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				PullRequestUpdate latestUpdate = getLatestUpdate();
				PullRequest request = latestUpdate.getRequest();
				if (latestUpdate.equals(request.getLatestUpdate())) {
					if (operate()) {
						modal.close();
					} else {
						form.error("Can not perform this operation now");
						target.add(form);
					}
				} else {
					PullRequestChangesPage.State state = new PullRequestChangesPage.State();
					state.oldCommitHash = latestUpdate.getHeadCommitHash();
					state.newCommitHash = request.getLatestUpdate().getHeadCommitHash();
					
					CharSequence url = RequestCycle.get().urlFor(PullRequestChangesPage.class, 
							PullRequestChangesPage.paramsOf(request, state));
					
					form.warn(String.format("There are <a href='%s'>new changes</a> in this pull request", url));					
					
					target.add(form);
					latestUpdateId = request.getLatestUpdate().getId();
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}
			
		});
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new OperationConfirmCssResourceReference()));
	}

	protected abstract boolean operate();
	
	protected final Form<?> getForm() {
		return form;
	}
	
	protected final PullRequestUpdate getLatestUpdate() {
		return OneDev.getInstance(PullRequestUpdateManager.class).load(latestUpdateId);
	}
	
	protected abstract String getTitle();
	
}
