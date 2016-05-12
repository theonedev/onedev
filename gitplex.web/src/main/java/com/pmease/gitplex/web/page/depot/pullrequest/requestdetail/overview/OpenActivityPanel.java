package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.hibernate.StaleObjectStateException;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.behavior.markdown.AttachmentSupport;
import com.pmease.commons.wicket.component.markdownviewer.MarkdownViewer;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.AccountLink;
import com.pmease.gitplex.web.component.comment.DepotAttachmentSupport;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.util.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
class OpenActivityPanel extends AbstractActivityPanel {

	private static final String BODY_ID = "body";
	
	private static final String FORM_ID = "form";
	
	public OpenActivityPanel(String id, RenderableActivity activity) {
		super(id, activity);
	}
	
	private Fragment renderForView() {
		final Fragment fragment = new Fragment(BODY_ID, "viewFrag", this);

		final NotificationPanel feedback = new NotificationPanel("feedback", fragment);
		feedback.setOutputMarkupPlaceholderTag(true);
		fragment.add(feedback);
		String description = requestModel.getObject().getDescription();
		if (StringUtils.isNotBlank(description)) {
			final AtomicLong lastVersionRef = new AtomicLong(requestModel.getObject().getVersion());
			fragment.add(new MarkdownViewer("description", new IModel<String>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return requestModel.getObject().getDescription();
				}

				@Override
				public void setObject(String object) {
					AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
					Preconditions.checkNotNull(target);
					PullRequest request = requestModel.getObject();
					try {
						if (request.getVersion() != lastVersionRef.get())
							throw new StaleObjectStateException(PullRequest.class.getName(), request.getId());
						request.setDescription(object);
						GitPlex.getInstance(Dao.class).persist(request);				
						target.add(feedback); // clear the feedback
					} catch (StaleObjectStateException e) {
						fragment.warn("Some one changed the content you are editing. The content has now been "
								+ "reloaded, please try again.");
						target.add(fragment);
					}
					lastVersionRef.set(request.getVersion());
				}
				
			}, SecurityUtils.canModify(requestModel.getObject())));
		} else {
			fragment.add(new Label("description", "<i>No description</i>").setEscapeModelStrings(false));
		}
		fragment.setOutputMarkupId(true);
		
		return fragment;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final WebMarkupContainer head = new WebMarkupContainer("head");
		head.setOutputMarkupId(true);
		add(head);
		
		head.add(new AccountLink("user", userModel.getObject()));
		head.add(new Label("age", DateUtils.formatAge(activity.getDate())));
		
		head.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment(BODY_ID, "editFrag", OpenActivityPanel.this);
				
				final Form<?> form = new Form<Void>(FORM_ID);
				form.setOutputMarkupId(true);
				fragment.add(form);
				
				final NotificationPanel feedback = new NotificationPanel("feedback", form);
				feedback.setOutputMarkupPlaceholderTag(true);
				form.add(feedback);
				
				long lastVersion = requestModel.getObject().getVersion();
				CommentInput input = new CommentInput("input", Model.of(requestModel.getObject().getDescription())) {

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new DepotAttachmentSupport(requestModel.getObject().getTargetDepot());
					}
					
				};
				form.add(input);
				
				form.add(new AjaxButton("save") {

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(feedback);
					}

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						try {
							PullRequest request = requestModel.getObject();
							if (request.getVersion() != lastVersion)
								throw new StaleObjectStateException(PullRequest.class.getName(), request.getId());
							request.setDescription(input.getModelObject());
							GitPlex.getInstance(Dao.class).persist(request);
	
							Fragment fragment = renderForView();
							OpenActivityPanel.this.replace(fragment);
							target.add(fragment);
							target.add(head);
						} catch (StaleObjectStateException e) {
							error("Some one changed the content you are editing. Reload the page and try again.");
							target.add(feedback);
						}
					}
					
				});
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(form));
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						Fragment fragment = renderForView();
						OpenActivityPanel.this.replace(fragment);
						target.add(fragment);
						target.add(head);
					}
					
				});
				
				OpenActivityPanel.this.replace(fragment);
				
				target.add(fragment);
				target.add(head);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(SecurityUtils.canModify(requestModel.getObject()) 
						&& OpenActivityPanel.this.get(BODY_ID).get(FORM_ID) == null);
			}

		});
		
		add(renderForView());
	}

}
