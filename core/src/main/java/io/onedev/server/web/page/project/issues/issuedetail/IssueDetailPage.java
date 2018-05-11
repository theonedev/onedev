package io.onedev.server.web.page.project.issues.issuedetail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueCommentManager;
import io.onedev.server.manager.IssueFieldManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.IssueRelationManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueRelation;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.workflow.IssueWorkflow;
import io.onedev.server.model.support.issue.workflow.StateTransition;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.PromptedField;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.component.IssueStateLabel;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.comment.ProjectAttachmentSupport;
import io.onedev.server.web.component.issuechoice.SelectToAddIssue;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.page.project.issues.issuedetail.activity.CommentedActivity;
import io.onedev.server.web.page.project.issues.issuedetail.activity.IssueActivity;
import io.onedev.server.web.page.project.issues.issuedetail.activity.IssueCommentDeleted;
import io.onedev.server.web.page.project.issues.issuedetail.activity.OpenedActivity;
import io.onedev.server.web.page.project.issues.issuelist.IssueListPage;
import io.onedev.server.web.page.project.issues.newissue.NewIssuePage;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.util.ConfirmOnClick;
import io.onedev.server.web.util.ajaxlistener.ConfirmListener;

@SuppressWarnings("serial")
public class IssueDetailPage extends ProjectPage implements InputContext {

	public static final String PARAM_ISSUE = "issue";
	
	private static final String ACTION_OPTIONS_ID = "actionOptions";
	
	private final IModel<Issue> issueModel;
	
	private boolean editingTitle;
	
	private RepeatingView activitiesView;
	
	public IssueDetailPage(PageParameters params) {
		super(params);
		
		issueModel = new LoadableDetachableModel<Issue>() {

			@Override
			protected Issue load() {
				Long issueNumber = params.get(PARAM_ISSUE).toLong();
				Issue issue = OneDev.getInstance(IssueManager.class).find(getProject(), issueNumber);
				if (issue == null)
					throw new EntityNotFoundException("Unable to find issue #" + issueNumber + " in project " + getProject());
				return issue;
			}

		};
	}
	
	private Issue getIssue() {
		return issueModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer titleContainer = new WebMarkupContainer("title");
		titleContainer.setOutputMarkupId(true);
		add(titleContainer);
		
		titleContainer.add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "#" + getIssue().getNumber() + " - " + getIssue().getTitle();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!editingTitle);
			}
			
		});
		
		titleContainer.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				editingTitle = true;
				
				target.add(titleContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(!editingTitle && SecurityUtils.canModify(getIssue()));
			}
			
		});

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(editingTitle);
			}
			
		};
		titleContainer.add(form);
		
		form.add(new TextField<String>("title", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				if (StringUtils.isNotBlank(getIssue().getTitle()))
					return getIssue().getTitle();
				else
					return "";
			}

			@Override
			public void setObject(String object) {
				getIssue().setTitle(object);
			}
			
		}));
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				if (StringUtils.isNotBlank(getIssue().getTitle())) {
					OneDev.getInstance(Dao.class).persist(getIssue());
					editingTitle = false;
				}

				target.add(titleContainer);
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				editingTitle = false;
				target.add(titleContainer);
			}
			
		});
		
		add(new IssueStateLabel("state", issueModel));
		
		RepeatingView transitionsView = new RepeatingView("transitions");

		List<StateTransition> transitions = getProject().getIssueWorkflow().getStateTransitions();
		Collections.sort(transitions, new Comparator<StateTransition>() {

			@Override
			public int compare(StateTransition o1, StateTransition o2) {
				IssueWorkflow workflow = getProject().getIssueWorkflow();
				return workflow.getStateIndex(o1.getToState()) - workflow.getStateIndex(o2.getToState());
			}
			
		});
		for (StateTransition transition: transitions) {
			if (transition.getFromStates().contains(getIssue().getState()) 
					&& transition.getOnAction().getButton() != null 
					&& getLoginUser() != null
					&& transition.getOnAction().getButton().getAuthorized().matches(getProject(), getLoginUser())) {
				boolean applicable = false;
				if (transition.getPrerequisite() == null) {
					applicable = true;
				} else {
					PromptedField field = getIssue().getPromptedFields().get(transition.getPrerequisite().getFieldName());
					List<String> fieldValues;
					if (field != null)
						fieldValues = field.getValues();
					else
						fieldValues = new ArrayList<>();
					if (transition.getPrerequisite().getValueSpecification().matches(fieldValues))
						applicable = true;
				}
				if (applicable) {
					AjaxLink<Void> link = new AjaxLink<Void>(transitionsView.newChildId()) {

						private String comment;
						
						@Override
						public void onClick(AjaxRequestTarget target) {
							Fragment fragment = new Fragment(ACTION_OPTIONS_ID, "transitionFrag", IssueDetailPage.this);
							Serializable fieldBean = getIssueFieldManager().readFields(getIssue());
							Set<String> excludedFields = getIssueFieldManager().getExcludedFields(getIssue().getProject(), transition.getToState());

							Form<?> form = new Form<Void>("form") {

								@Override
								protected void onError() {
									super.onError();
									RequestCycle.get().find(AjaxRequestTarget.class).add(this);
								}
								
							};
							
							BeanEditor editor = BeanContext.editBean("fields", fieldBean, excludedFields); 
							form.add(editor);
							
							form.add(new CommentInput("comment", new PropertyModel<String>(this, "comment"), false) {

								@Override
								protected Project getProject() {
									return getIssue().getProject();
								}
								
								@Override
								protected List<AttributeModifier> getInputModifiers() {
									return Lists.newArrayList(AttributeModifier.replace("placeholder", "Leave a comment"));
								}
								
							});
							
							form.add(new AjaxButton("save") {

								@Override
								protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
									super.onSubmit(target, form);
									getIssue().setState(transition.getToState());
									Set<String> promptedFields = new HashSet<>(getIssue().getPromptedFields().keySet());
									
									for (PropertyContext<?> propertyContext: editor.getPropertyContexts()) { 
										if (!propertyContext.isExcluded()) {
											if (propertyContext.isPropertyVisible(editor.getOneContext(propertyContext.getPropertyName()), editor.getBeanDescriptor()))
												promptedFields.add(propertyContext.getDisplayName());
											else
												promptedFields.remove(propertyContext.getDisplayName());
										}
									}
									getIssueManager().save(getIssue(), fieldBean, promptedFields);
									setResponsePage(IssueDetailPage.class, IssueDetailPage.paramsOf(getIssue()));
								}
								
							});
							
							form.add(new AjaxLink<Void>("cancel") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									newEmptyActionOptions(target);
								}
								
							});
							fragment.add(form);
							
							fragment.setOutputMarkupId(true);
							IssueDetailPage.this.replace(fragment);
							target.add(fragment);
						}
						
					};
					link.add(new Label("label", transition.getOnAction().getButton().getName()));
					transitionsView.add(link);
				}
			}
		}
		
		add(transitionsView);
		
		add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, NewIssuePage.paramsOf(getProject())) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canRead(getProject()));
			}
			
		});
		
		newEmptyActionOptions(null);
		
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
					return IssueDetailPage.this.getProject();
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
			
			form = new Form<Void>("form");
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
				Fragment fragment = new Fragment(id, "fieldEditFrag", IssueDetailPage.this);
				Form<?> form = new Form<Void>("form");

				Serializable fieldBean = getIssueFieldManager().readFields(issue); 
				
				Map<String, PropertyDescriptor> propertyDescriptors = 
						new BeanDescriptor(fieldBean.getClass()).getMapOfDisplayNameToPropertyDescriptor();
				
				Set<String> excludedFields = new HashSet<>();
				for (InputSpec fieldSpec: getProject().getIssueWorkflow().getFields()) {
					if (!issue.getPromptedFields().containsKey(fieldSpec.getName()))
						excludedFields.add(propertyDescriptors.get(fieldSpec.getName()).getPropertyName());
				}

				form.add(BeanContext.editBean("editor", fieldBean, excludedFields));
				
				form.add(new AjaxButton("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						new BeanDescriptor(Issue.class).copyProperties(issue, getIssue());
						getIssueFieldManager().writeFields(issue, fieldBean, issue.getPromptedFields().keySet());
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

		WebMarkupContainer relations = new WebMarkupContainer("relations") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getIssue().getRelationsByCurrent().isEmpty() || SecurityUtils.canWrite(getProject()));
			}
			
		};
		
		relations.setOutputMarkupId(true);
		add(relations);
		
		IModel<List<Issue>> relationsModel = new LoadableDetachableModel<List<Issue>>() {

			@Override
			protected List<Issue> load() {
				Set<Issue> set = new HashSet<>();
				for (IssueRelation relation: getIssue().getRelationsByCurrent())
					set.add(relation.getOther());
				for (IssueRelation relation: getIssue().getRelationsByOther())
					set.add(relation.getCurrent());
				List<Issue> list = new ArrayList<>(set);
				Collections.sort(list, new Comparator<Issue>() {

					@Override
					public int compare(Issue o1, Issue o2) {
						return (int) (o2.getNumber() - o1.getNumber());
					}
					
				});
				return list;
			}
			
		};
		
		relations.add(new ListView<Issue>("relations", relationsModel) {

			@Override
			protected void populateItem(ListItem<Issue> item) {
				Issue issue = item.getModelObject();
				Link<Void> link = new BookmarkablePageLink<Void>("link", IssueDetailPage.class, IssueDetailPage.paramsOf(issue));
				link.add(new Label("label", "#" + issue.getNumber()));
				item.add(link);
				
				item.add(new IssueStateLabel("state", item.getModel()));
				
				item.add(new AjaxLink<Void>("remove") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmListener("Really want to remove this relation?"));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						IssueRelationManager issueRelationManager = OneDev.getInstance(IssueRelationManager.class);
						IssueRelation relation = issueRelationManager.find(getIssue(), item.getModelObject());
						if (relation != null) {
							issueRelationManager.delete(relation);
							getIssue().getRelationsByCurrent().remove(relation);
						}
						
						relation = issueRelationManager.find(item.getModelObject(), getIssue());
						if (relation != null) {
							issueRelationManager.delete(relation);
							getIssue().getRelationsByOther().remove(relation);
						}
						
						relationsModel.detach();
						
						target.add(relations);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canWrite(getProject()));
					}
					
				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getModelObject().isEmpty());
			}
			
		});
		
		relations.add(new NotificationPanel("feedback", relations));
		
		add(new SelectToAddIssue("addRelation", new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}) {
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().setPlaceholder("Add related issue...");
			}

			@Override
			protected void onSelect(AjaxRequestTarget target, Issue selection) {
				if (selection.equals(getIssue())) {
					relations.error("Can not add self as related issue");
				} else {
					IssueRelationManager issueRelationManager = OneDev.getInstance(IssueRelationManager.class);
					IssueRelation relationByCurrent = issueRelationManager.find(getIssue(), selection);
					IssueRelation relationByOther = issueRelationManager.find(selection, getIssue());
					if (relationByCurrent == null && relationByOther == null) {
						relationByCurrent = new IssueRelation();
						relationByCurrent.setCurrent(getIssue());
						relationByCurrent.setOther(selection);
						getIssue().getRelationsByCurrent().add(relationByCurrent);
						issueRelationManager.save(relationByCurrent);
					}
				}
				target.add(relations);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canWrite(getProject()));
			}
			
		});
		
		Link<Void> deleteLink = new Link<Void>("delete") {

			@Override
			public void onClick() {
				getIssueManager().delete(getIssue());
				setResponsePage(IssueListPage.class, IssueListPage.paramsOf(getProject()));
			}
			
		};
		deleteLink.add(new ConfirmOnClick("Do you really want to delete this issue?"));
		deleteLink.setVisible(SecurityUtils.canModify(getIssue()));
		add(deleteLink);
	}
	
	private void newEmptyActionOptions(@Nullable AjaxRequestTarget target) {
		WebMarkupContainer actionOptions = new WebMarkupContainer(ACTION_OPTIONS_ID);
		actionOptions.setOutputMarkupPlaceholderTag(true);
		actionOptions.setVisible(false);
		if (target != null) {
			replace(actionOptions);
			target.add(actionOptions);
		} else {
			add(actionOptions);
		}
	}

	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}

	private IssueFieldManager getIssueFieldManager() {
		return OneDev.getInstance(IssueFieldManager.class);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueDetailResourceReference()));
	}

	@Override
	protected void onDetach() {
		issueModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(Issue issue) {
		PageParameters params = ProjectPage.paramsOf(issue.getProject());
		params.set(PARAM_ISSUE, issue.getNumber());
		return params;
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
		
		WebMarkupContainer avatarColumn = new WebMarkupContainer("avatar");
		row.add(avatarColumn);
		
		avatarColumn.add(new AvatarLink("avatar", 
				User.getForDisplay(getIssue().getSubmitter(), getIssue().getSubmitterName())));

		return row;
	}
	
	private List<IssueActivity> getActivities() {
		List<IssueActivity> activities = new ArrayList<>();

		activities.add(new OpenedActivity(getIssue()));

		for (IssueComment comment: getIssue().getComments()) { 
			activities.add(new CommentedActivity(comment));
		}
		
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
	
	@Override
	public List<String> getInputNames() {
		return getProject().getIssueWorkflow().getInputNames();
	}

	@Override
	public InputSpec getInput(String inputName) {
		return getProject().getIssueWorkflow().getInput(inputName);
	}
	
	@Override
	public boolean isReservedName(String inputName) {
		throw new UnsupportedOperationException();
	}
	
}
