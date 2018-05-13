package io.onedev.server.web.page.project.issues.issuedetail.overview;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueChangeManager;
import io.onedev.server.manager.IssueCommentManager;
import io.onedev.server.manager.IssueFieldManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.PromptedField;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.comment.ProjectAttachmentSupport;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.page.project.issues.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.page.project.issues.issuedetail.IssueDetailPage;
import io.onedev.server.web.page.project.issues.issuedetail.overview.activity.CommentedActivity;
import io.onedev.server.web.page.project.issues.issuedetail.overview.activity.IssueActivity;
import io.onedev.server.web.page.project.issues.issuedetail.overview.activity.IssueCommentDeleted;
import io.onedev.server.web.page.project.issues.issuedetail.overview.activity.OpenedActivity;
import io.onedev.server.web.page.project.issues.issuelist.IssueListPage;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.util.ConfirmOnClick;

@SuppressWarnings("serial")
public class IssueOverviewPage extends IssueDetailPage {

	private RepeatingView activitiesView;
	
	public IssueOverviewPage(PageParameters params) {
		super(params);
	}

	private List<IssueActivity> getActivities() {
		List<IssueActivity> activities = new ArrayList<>();

		activities.add(new OpenedActivity(getIssue()));

		for (IssueComment comment: getIssue().getComments())  
			activities.add(new CommentedActivity(comment));
		
		activities.sort((o1, o2) -> {
			if (o1.getDate().getTime()<o2.getDate().getTime())
				return -1;
			else if (o1.getDate().getTime()>o2.getDate().getTime())
				return 1;
			else
				return 1;
		});
		
		return activities;
	}
	
	private Component newActivityRow(String id, IssueActivity activity) {
		WebMarkupContainer row = new WebMarkupContainer(id, Model.of(activity)) {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				
				if (event.getPayload() instanceof IssueCommentDeleted) {
					IssueCommentDeleted commentRemoved = (IssueCommentDeleted) event.getPayload();
					remove();
					commentRemoved.getHandler().appendJavaScript(String.format("$('#%s').remove();", getMarkupId()));
				} 
			}
			
		};
		row.setOutputMarkupId(true);
		String anchor = activity.getAnchor();
		if (anchor != null)
			row.setMarkupId(anchor);
		
		if (row.get("content") == null) 
			row.add(activity.render("content"));
		
		row.add(new AvatarLink("avatar", User.getForDisplay(getIssue().getSubmitter(), getIssue().getSubmitterName())));

		row.add(AttributeAppender.append("class", activity.getClass().getSimpleName()));
		return row;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(activitiesView = new RepeatingView("activities"));
		activitiesView.setOutputMarkupId(true);
		
		Issue issue = getIssue();

		for (IssueActivity activity: getActivities()) {
			if (issue.isVisitedAfter(activity.getDate())) {
				activitiesView.add(newActivityRow(activitiesView.newChildId(), activity));
			} else {
				Component row = newActivityRow(activitiesView.newChildId(), activity);
				row.add(AttributeAppender.append("class", "new"));
				activitiesView.add(row);
			}
		}
		
		add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, String observable) {
				updateActivities(handler);
			}
			
			@Override
			public void onConnectionOpened(IPartialPageRequestHandler handler) {
				updateActivities(handler);
			}
			
			private void updateActivities(IPartialPageRequestHandler handler) {
				@SuppressWarnings("deprecation")
				Component prevActivityRow = activitiesView.get(activitiesView.size()-1);
				IssueActivity lastActivity = (IssueActivity) prevActivityRow.getDefaultModelObject();
				List<IssueActivity> newActivities = new ArrayList<>();
				for (IssueActivity activity: getActivities()) {
					if (activity.getDate().getTime() > lastActivity.getDate().getTime())
						newActivities.add(activity);
				}

				for (IssueActivity activity: newActivities) {
					Component newActivityRow = newActivityRow(activitiesView.newChildId(), activity); 
					newActivityRow.add(AttributeAppender.append("class", "new"));
					activitiesView.add(newActivityRow);
					
					String script = String.format("$(\"<tr id='%s'></tr>\").insertAfter('#%s');", 
							newActivityRow.getMarkupId(), prevActivityRow.getMarkupId());
					handler.prependJavaScript(script);
					handler.add(newActivityRow);
					prevActivityRow = newActivityRow;
				}
			}
			
			@Override
			public Collection<String> getObservables() {
				return Lists.newArrayList(Issue.getWebSocketObservable(getIssue().getId()));
			}
			
		});
		
		if (getLoginUser() != null) {
			Fragment fragment = new Fragment("addComment", "addCommentFrag", this);
			fragment.setOutputMarkupId(true);
			String autosaveKey = "autosave:addIssueComment:" + issue.getId();
			CommentInput input = new CommentInput("comment", Model.of(""), false) {

				@Override
				protected AttachmentSupport getAttachmentSupport() {
					return new ProjectAttachmentSupport(getProject(), getIssue().getUUID());
				}

				@Override
				protected Project getProject() {
					return IssueOverviewPage.this.getProject();
				}
				
				@Override
				protected String getAutosaveKey() {
					return autosaveKey;
				}
				
				@Override
				protected List<AttributeModifier> getInputModifiers() {
					return Lists.newArrayList(AttributeModifier.replace("placeholder", "Leave a comment"));
				}
				
			};
			input.setRequired(true).setLabel(Model.of("Comment"));
			
			Form<?> form = new Form<Void>("form");
			form.add(new NotificationPanel("feedback", form));
			form.add(input);
			form.add(new AjaxSubmitLink("save") {

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					super.onSubmit(target, form);

					IssueComment comment = new IssueComment();
					comment.setContent(input.getModelObject());
					comment.setUser(getLoginUser());
					comment.setDate(new Date());
					comment.setIssue(getIssue());
					OneDev.getInstance(IssueCommentManager.class).save(comment);
					
					input.setModelObject("");

					target.add(fragment);
					
					@SuppressWarnings("deprecation")
					Component lastActivityRow = activitiesView.get(activitiesView.size()-1);
					Component newActivityRow = newActivityRow(activitiesView.newChildId(), new CommentedActivity(comment)); 
					activitiesView.add(newActivityRow);
					
					String script = String.format("$(\"<tr id='%s'></tr>\").insertAfter('#%s');", 
							newActivityRow.getMarkupId(), lastActivityRow.getMarkupId());
					target.prependJavaScript(script);
					target.add(newActivityRow);
					target.appendJavaScript(String.format("localStorage.removeItem('%s');", autosaveKey));
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					super.onError(target, form);
					target.add(form);
					target.appendJavaScript(String.format("localStorage.removeItem('%s');", autosaveKey));
				}
				
			});
			form.setOutputMarkupId(true);
			fragment.add(form);
			add(fragment);
		} else {
			Fragment fragment = new Fragment("addComment", "loginToCommentFrag", this);
			fragment.add(new Link<Void>("login") {

				@Override
				public void onClick() {
					throw new RestartResponseAtInterceptPageException(LoginPage.class);
				}
				
			});
			add(fragment);
		}

		WebMarkupContainer fieldsContainer = new WebMarkupContainer("fields") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getIssue().getFields().isEmpty());
			}
			
		};
		fieldsContainer.setOutputMarkupId(true);
		
		fieldsContainer.add(new ListView<PromptedField>("fields", new LoadableDetachableModel<List<PromptedField>>() {

			@Override
			protected List<PromptedField> load() {
				return new ArrayList<>(getIssue().getPromptedFields().values());
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<PromptedField> item) {
				PromptedField field = item.getModelObject();
				item.add(new Label("name", field.getName()));
				item.add(new FieldValuesPanel("values", item.getModel()));
			}
			
		});
		
		fieldsContainer.add(new ModalLink("editFields") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "fieldEditFrag", IssueOverviewPage.this);
				Form<?> form = new Form<Void>("form");

				Serializable fieldBean = OneDev.getInstance(IssueFieldManager.class).readFields(getIssue()); 
				Map<String, PromptedField> prevFields = getIssue().getPromptedFields();
				
				Map<String, PropertyDescriptor> propertyDescriptors = 
						new BeanDescriptor(fieldBean.getClass()).getMapOfDisplayNameToPropertyDescriptor();
				
				Set<String> excludedFields = new HashSet<>();
				for (InputSpec fieldSpec: getProject().getIssueWorkflow().getFields()) {
					if (!getIssue().getPromptedFields().containsKey(fieldSpec.getName()))
						excludedFields.add(propertyDescriptors.get(fieldSpec.getName()).getPropertyName());
				}

				form.add(BeanContext.editBean("editor", fieldBean, excludedFields));
				
				form.add(new AjaxButton("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						OneDev.getInstance(IssueChangeManager.class).changeFields(
								getIssue(), fieldBean, prevFields, getIssue().getPromptedFields().keySet());
						modal.close();
						target.add(fieldsContainer);
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
						modal.close();
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
				setVisible(SecurityUtils.canModify(getIssue()));
			}
			
		});
		
		add(fieldsContainer);
		
		Link<Void> deleteLink = new Link<Void>("delete") {

			@Override
			public void onClick() {
				OneDev.getInstance(IssueManager.class).delete(getIssue());
				setResponsePage(IssueListPage.class, IssueListPage.paramsOf(getProject()));
			}
			
		};
		deleteLink.add(new ConfirmOnClick("Do you really want to delete this issue?"));
		deleteLink.setVisible(SecurityUtils.canModify(getIssue()));
		add(deleteLink);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueOverviewResourceReference()));
	}

}
