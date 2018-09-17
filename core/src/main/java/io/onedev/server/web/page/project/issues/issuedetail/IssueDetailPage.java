package io.onedev.server.web.page.project.issues.issuedetail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
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
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.CodeCommentRelationInfoManager;
import io.onedev.server.manager.CommitInfoManager;
import io.onedev.server.manager.IssueActionManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.IssueVoteManager;
import io.onedev.server.manager.IssueWatchManager;
import io.onedev.server.manager.UserInfoManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueVote;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.model.support.issue.IssueConstants;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.model.support.issue.workflow.IssueWorkflow;
import io.onedev.server.model.support.issue.workflow.TransitionSpec;
import io.onedev.server.model.support.issue.workflow.transitiontrigger.PressButtonTrigger;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.dateinput.DateInput;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.entitynav.EntityNavPanel;
import io.onedev.server.web.component.entitywatches.EntityWatchesPanel;
import io.onedev.server.web.component.issuestate.IssueStateLabel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.milestone.MilestoneProgressBar;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.projectcomment.CommentInput;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.PageTabLink;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.component.userlist.UserListLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.page.project.issues.issuedetail.activities.IssueActivitiesPage;
import io.onedev.server.web.page.project.issues.issuedetail.fixbuilds.FixBuildsPage;
import io.onedev.server.web.page.project.issues.issuedetail.fixcommits.FixCommitsPage;
import io.onedev.server.web.page.project.issues.issuedetail.reviewrequests.ReviewRequestsPage;
import io.onedev.server.web.page.project.issues.issuelist.IssueListPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneDetailPage;
import io.onedev.server.web.page.project.issues.newissue.NewIssuePage;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.util.ConfirmOnClick;
import io.onedev.server.web.util.ProjectAttachmentSupport;
import io.onedev.server.web.util.QueryPosition;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
public abstract class IssueDetailPage extends ProjectPage implements InputContext {

	private static final int MAX_DISPLAY_AVATARS = 20;
	
	public static final String PARAM_ISSUE = "issue";
	
	private static final String ACTION_OPTIONS_ID = "actionOptions";
	
	private static final String TITLE_ID = "title";
	
	private final IModel<Issue> issueModel;
	
	private final QueryPosition position;
	
	private final IModel<Collection<ObjectId>> fixCommitsModel = new LoadableDetachableModel<Collection<ObjectId>>() {

		@Override
		protected Collection<ObjectId> load() {
			CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class); 
			return commitInfoManager.getFixCommits(getProject(), getIssue().getNumber());
		}
		
	};
	
	private final IModel<Collection<Long>> pullRequestIdsModel = new LoadableDetachableModel<Collection<Long>>() {

		@Override
		protected Collection<Long> load() {
			CodeCommentRelationInfoManager codeCommentRelationInfoManager = OneDev.getInstance(CodeCommentRelationInfoManager.class); 
			Collection<Long> pullRequestIds = new HashSet<>();
			for (ObjectId commit: fixCommitsModel.getObject()) 
				pullRequestIds.addAll(codeCommentRelationInfoManager.getPullRequestIds(getProject(), commit));		
			return pullRequestIds;
		}
		
	};
	
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
	
		position = QueryPosition.from(params);
	}
	
	protected Issue getIssue() {
		return issueModel.getObject();
	}
	
	private Fragment newTitleEditor() {
		Fragment titleEditor = new Fragment(TITLE_ID, "titleEditFrag", this);
		Form<?> form = new Form<Void>("form");
		TextField<String> titleInput = new TextField<String>("title", Model.of(getIssue().getTitle()));
		titleInput.setRequired(true);
		titleInput.setLabel(Model.of("Title"));
		
		form.add(titleInput);
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				OneDev.getInstance(IssueActionManager.class).changeTitle(getIssue(), titleInput.getModelObject(), SecurityUtils.getUser());
				
				Fragment titleViewer = newTitleViewer();
				titleEditor.replaceWith(titleViewer);
				target.add(titleViewer);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(titleEditor);
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment titleViewer = newTitleViewer();
				titleEditor.replaceWith(titleViewer);
				target.add(titleViewer);
			}
			
		});		
		
		titleEditor.add(form);
		
		titleEditor.add(new NotificationPanel("feedback", form));
		titleEditor.setOutputMarkupId(true);
		
		return titleEditor;
	}
	
	private Fragment newTitleViewer() {
		Fragment titleViewer = new Fragment(TITLE_ID, "titleViewFrag", this);
		titleViewer.add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "#" + getIssue().getNumber() + " - " + getIssue().getTitle();
			}
			
		}));
		
		titleViewer.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment titleEditor = newTitleEditor();
				titleViewer.replaceWith(titleEditor);
				target.add(titleEditor);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(SecurityUtils.canModify(getIssue()));
			}
			
		});
		
		titleViewer.setOutputMarkupId(true);
		
		return titleViewer;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newTitleViewer());
	
		add(new IssueStateLabel("state", issueModel));
		
		RepeatingView transitionsView = new RepeatingView("transitions");

		List<TransitionSpec> transitions = getProject().getIssueWorkflow().getTransitionSpecs();
		Collections.sort(transitions, new Comparator<TransitionSpec>() {

			@Override
			public int compare(TransitionSpec o1, TransitionSpec o2) {
				IssueWorkflow workflow = getProject().getIssueWorkflow();
				return workflow.getStateSpecIndex(o1.getToState()) - workflow.getStateSpecIndex(o2.getToState());
			}
			
		});
		for (TransitionSpec transition: transitions) {
			if (transition.canTransite(getIssue()) && transition.getTrigger() instanceof PressButtonTrigger) {
				PressButtonTrigger trigger = (PressButtonTrigger) transition.getTrigger();
				if (trigger.isAuthorized()) {
					AjaxLink<Void> link = new AjaxLink<Void>(transitionsView.newChildId()) {

						private String comment;
						
						@Override
						public void onClick(AjaxRequestTarget target) {
							Fragment fragment = new Fragment(ACTION_OPTIONS_ID, "transitionFrag", IssueDetailPage.this);
							Class<?> fieldBeanClass = IssueUtils.defineBeanClass(getProject());
							Serializable fieldBean = getIssue().getFieldBean(fieldBeanClass, true);

							Form<?> form = new Form<Void>("form") {

								@Override
								protected void onError() {
									super.onError();
									RequestCycle.get().find(AjaxRequestTarget.class).add(this);
								}
								
							};
							
							Collection<String> propertyNames = IssueUtils.getPropertyNames(fieldBeanClass, trigger.getPromptFields());
							BeanEditor editor = BeanContext.editBean("fields", fieldBean, propertyNames, false); 
							form.add(editor);
							
							form.add(new CommentInput("comment", new PropertyModel<String>(this, "comment"), false) {

								@Override
								protected AttachmentSupport getAttachmentSupport() {
									return new ProjectAttachmentSupport(getProject(), getIssue().getUUID());
								}

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

									getIssue().removeFields(transition.getRemoveFields());
									Map<String, Object> fieldValues = IssueUtils.getFieldValues(fieldBean, trigger.getPromptFields());
									getIssueChangeManager().changeState(getIssue(), transition.getToState(), fieldValues, comment, SecurityUtils.getUser());
								
									setResponsePage(IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(getIssue(), position));
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
					link.add(new Label("label", trigger.getButtonLabel()));
					transitionsView.add(link);
				}
			}
		}
		
		add(transitionsView);

		List<String> criterias = new ArrayList<>();
		if (getIssue().getMilestone() != null)
			criterias.add(IssueQuery.quote(IssueConstants.FIELD_MILESTONE) + " is " + IssueQuery.quote(getIssue().getMilestoneName()));
		for (Map.Entry<String, IssueField> entry: getIssue().getFields().entrySet()) {
			List<String> strings = entry.getValue().getValues();
			if (strings.isEmpty()) {
				criterias.add(IssueQuery.quote(entry.getKey()) + " is empty");
			} else { 
				InputSpec field = getProject().getIssueWorkflow().getFieldSpec(entry.getKey());
				if (field instanceof ChoiceInput && ((ChoiceInput)field).isAllowMultiple()) {
					for (String string: strings)
						criterias.add(IssueQuery.quote(entry.getKey()) + " contains " + IssueQuery.quote(string));
				} else if (!(field instanceof DateInput)) { 
					criterias.add(IssueQuery.quote(entry.getKey()) + " is " + IssueQuery.quote(strings.iterator().next()));
				}
			}
		}

		String query;
		if (!criterias.isEmpty())
			query = StringUtils.join(criterias, " and ");
		else
			query = null;
		
		add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, NewIssuePage.paramsOf(getProject(), query)));
		
		newEmptyActionOptions(null);
		
		List<Tab> tabs = new ArrayList<>();
		tabs.add(new IssueTab("Activities", IssueActivitiesPage.class));
		
		if (SecurityUtils.canReadCode(getProject().getFacade())) {
			if (!getFixCommits().isEmpty())		
				tabs.add(new IssueTab("Fix Commits", FixCommitsPage.class));
			if (!getPullRequestIds().isEmpty())
				tabs.add(new IssueTab("Pull Requests", ReviewRequestsPage.class));
		}
		if (!getFixCommits().isEmpty()) // Do not calculate fix builds now as it might be slow
			tabs.add(new IssueTab("Fix Builds", FixBuildsPage.class));
		
		add(new Tabbable("issueTabs", tabs).setOutputMarkupId(true));
		
		RequestCycle.get().getListeners().add(new IRequestCycleListener() {
			
			@Override
			public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url) {
			}
			
			@Override
			public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onExceptionRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler, Exception exception) {
			}
			
			@Override
			public IRequestHandler onException(RequestCycle cycle, Exception ex) {
				return null;
			}
			
			@Override
			public void onEndRequest(RequestCycle cycle) {
				if (SecurityUtils.getUser() != null) 
					OneDev.getInstance(UserInfoManager.class).visitIssue(SecurityUtils.getUser(), getIssue());
			}
			
			@Override
			public void onDetach(RequestCycle cycle) {
			}
			
			@Override
			public void onBeginRequest(RequestCycle cycle) {
			}
			
		});	

		add(new EntityNavPanel<Issue>("issueNav") {

			@Override
			protected EntityQuery<Issue> parse(String queryString) {
				return IssueQuery.parse(getProject(), queryString, true);
			}

			@Override
			protected Issue getEntity() {
				return getIssue();
			}

			@Override
			protected QueryPosition getPosition() {
				return position;
			}

			@Override
			protected void navTo(Issue issue, QueryPosition position) {
				PageParameters params = IssueDetailPage.paramsOf(issue, position);
				setResponsePage(getPageClass(), params);
			}

			@Override
			protected List<Issue> query(EntityQuery<Issue> query, int offset, int count) {
				return getIssueManager().query(getProject(), getLoginUser(), query, offset, count);
			}
			
		});
		
		add(newFieldsContainer());
		add(newMilestoneContainer());
		add(newVotesContainer());
		
		add(new EntityWatchesPanel("watches") {

			@Override
			protected void onSaveWatch(EntityWatch watch) {
				OneDev.getInstance(IssueWatchManager.class).save((IssueWatch) watch);
			}

			@Override
			protected void onDeleteWatch(EntityWatch watch) {
				OneDev.getInstance(IssueWatchManager.class).delete((IssueWatch) watch);
			}

			@Override
			protected AbstractEntity getEntity() {
				return getIssue();
			}
			
		});
		
		Link<Void> deleteLink = new Link<Void>("delete") {

			@Override
			public void onClick() {
				OneDev.getInstance(IssueManager.class).delete(getIssue());
				setResponsePage(IssueListPage.class, IssueListPage.paramsOf(getProject()));
			}
			
		};
		deleteLink.add(new ConfirmOnClick("Do you really want to delete this issue?"));
		deleteLink.setVisible(SecurityUtils.canAdministrate(getIssue().getProject().getFacade()));
		add(deleteLink);		
	}
	
	private Component newFieldsContainer() {
		IModel<List<IssueField>> fieldsModel = new LoadableDetachableModel<List<IssueField>>() {

			@Override
			protected List<IssueField> load() {
				List<IssueField> fields = new ArrayList<>();
				for (IssueField field: getIssue().getFields().values()) {
					if (getIssue().isFieldVisible(field.getName()))
						fields.add(field);
				}
				return fields;
			}
			
		};		
		WebMarkupContainer fieldsContainer = new WebMarkupContainer("fields") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!fieldsModel.getObject().isEmpty());
			}
			
		};
		fieldsContainer.setOutputMarkupId(true);
		
		fieldsContainer.add(new ListView<IssueField>("fields", fieldsModel) {

			@Override
			protected void populateItem(ListItem<IssueField> item) {
				IssueField field = item.getModelObject();
				item.add(new Label("name", field.getName()));
				item.add(new FieldValuesPanel("values") {

					@Override
					protected Issue getIssue() {
						return IssueDetailPage.this.getIssue();
					}

					@Override
					protected IssueField getField() {
						return item.getModelObject();
					}
					
				}.setRenderBodyOnly(true));
			}
			
		});
		
		fieldsContainer.add(new ModalLink("edit") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "fieldEditFrag", IssueDetailPage.this);
				Form<?> form = new Form<Void>("form");

				Class<?> fieldBeanClass = IssueUtils.defineBeanClass(getProject());
				Serializable fieldBean = getIssue().getFieldBean(fieldBeanClass, true); 

				Collection<String> propertyNames = IssueUtils.getPropertyNames(fieldBeanClass, getIssue().getFieldNames());
				form.add(BeanContext.editBean("editor", fieldBean, propertyNames, false));
				
				form.add(new AjaxButton("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						
						Map<String, Object> fieldValues = IssueUtils.getFieldValues(fieldBean, getIssue().getFieldNames());
						OneDev.getInstance(IssueActionManager.class).changeFields(getIssue(), fieldValues, SecurityUtils.getUser());
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
				User user = getLoginUser();
				String initialState = getProject().getIssueWorkflow().getInitialStateSpec().getName();
				setVisible(SecurityUtils.canWriteCode(getIssue().getProject().getFacade())
						|| user != null && user.equals(getIssue().getSubmitter()) && getIssue().getState().equals(initialState));
			}
			
		});		
		
		return fieldsContainer;
	}
	
	private Component newMilestoneContainer() {
		Fragment fragment = new Fragment("milestone", "milestoneViewFrag", this);
		if (getIssue().getMilestone() != null) {
			Link<Void> link = new BookmarkablePageLink<Void>("link", MilestoneDetailPage.class, MilestoneDetailPage.paramsOf(getIssue().getMilestone(), null));
			link.add(new Label("label", getIssue().getMilestone().getName()));
			fragment.add(new MilestoneProgressBar("progress", new AbstractReadOnlyModel<Milestone>() {

				@Override
				public Milestone getObject() {
					return getIssue().getMilestone();
				}
				
			}));
			fragment.add(link);
		} else {
			WebMarkupContainer link = new WebMarkupContainer("link") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
			};
			link.add(new Label("label", "<i>No milestone</i>").setEscapeModelStrings(false));
			fragment.add(new WebMarkupContainer("progress").setVisible(false));
			fragment.add(link);
		}

		fragment.add(new AjaxLink<Void>("edit") {

			private String milestoneName = getIssue().getMilestoneName();
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment =  new Fragment("milestone", "milestoneEditFrag", IssueDetailPage.this);
				Form<?> form = new Form<Void>("form");
				
				List<String> milestones = getProject().getMilestones().stream().map(it->it.getName()).collect(Collectors.toList());
				StringSingleChoice choice = new StringSingleChoice("milestone", 
						new PropertyModel<String>(this, "milestoneName"), milestones) {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						getSettings().setPlaceholder("No milestone");
					}
					
				};
				choice.setRequired(false);
				form.add(choice);

				form.add(new AjaxButton("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						Milestone milestone = getProject().getMilestone(milestoneName);
						getIssueChangeManager().changeMilestone(getIssue(), milestone, SecurityUtils.getUser());
						Component container = newMilestoneContainer();
						getPage().replace(container);
						target.add(container);
					}

				});
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Component container = newMilestoneContainer();
						getPage().replace(container);
						target.add(container);
					}
					
				});
				fragment.add(form);
				fragment.setOutputMarkupId(true);
				getPage().replace(fragment);
				target.add(fragment);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canWriteCode(getIssue().getProject().getFacade()));
			}
			
		});
		fragment.setOutputMarkupId(true);
		return fragment;
	}
	
	private List<IssueVote> getSortedVotes() {
		List<IssueVote> votes = new ArrayList<>(getIssue().getVotes());
		Collections.sort(votes, new Comparator<IssueVote>() {

			@Override
			public int compare(IssueVote o1, IssueVote o2) {
				return o2.getId().compareTo(o1.getId());
			}
			
		});
		return votes;
	}
	
	private Component newVotesContainer() {
		WebMarkupContainer votesContainer = new WebMarkupContainer("votes");
		votesContainer.setOutputMarkupId(true);

		votesContainer.add(new Label("count", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return String.valueOf(getIssue().getVoteCount());
			}
			
		}));

		votesContainer.add(new ListView<IssueVote>("voters", new LoadableDetachableModel<List<IssueVote>>() {

			@Override
			protected List<IssueVote> load() {
				List<IssueVote> votes = getSortedVotes();
				if (votes.size() > MAX_DISPLAY_AVATARS)
					votes = votes.subList(0, MAX_DISPLAY_AVATARS);
				return votes;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<IssueVote> item) {
				User user = item.getModelObject().getUser();
				item.add(new AvatarLink("voter", user, user.getDisplayName()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getIssue().getVotes().isEmpty());
			}
			
		});
		
		votesContainer.add(new UserListLink("more") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getIssue().getVotes().size() > MAX_DISPLAY_AVATARS);
			}

			@Override
			protected List<User> getUsers() {
				List<IssueVote> votes = getSortedVotes();
				if (votes.size() > MAX_DISPLAY_AVATARS)
					votes = votes.subList(MAX_DISPLAY_AVATARS, votes.size());
				else
					votes = new ArrayList<>();
				return votes.stream().map(it->it.getUser()).collect(Collectors.toList());
			}
					
		});
		
		AjaxLink<Void> voteLink = new AjaxLink<Void>("vote") {

			private IssueVote getVote(User user) {
				for (IssueVote vote: getIssue().getVotes()) {
					if (user.equals(vote.getUser())) 
						return vote;
				}
				return null;
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				if (getLoginUser() != null) {
					IssueVote vote = getVote(getLoginUser());
					if (vote == null) {
						vote = new IssueVote();
						vote.setIssue(getIssue());
						vote.setUser(getLoginUser());
						vote.setDate(new Date());
						OneDev.getInstance(IssueVoteManager.class).save(vote);
						getIssue().getVotes().add(vote);
					} else {
						getIssue().getVotes().remove(vote);
						OneDev.getInstance(IssueVoteManager.class).delete(vote);
					}
					target.add(votesContainer);
				} else {
					throw new RestartResponseAtInterceptPageException(LoginPage.class);
				}
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("label", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						if (getLoginUser() != null) {
							if (getVote(getLoginUser()) != null)
								return "Unvote";
							else
								return "Vote";
						} else {
							return "Login to vote";
						}
					}
					
				}));
			}
			
		};
		votesContainer.add(voteLink);
		
		return votesContainer;
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

	public QueryPosition getPosition() {
		return position;
	}
	
	private IssueActionManager getIssueChangeManager() {
		return OneDev.getInstance(IssueActionManager.class);
	}

	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new IssueDetailResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.issueDetail.onDomReady();"));
	}

	@Override
	protected void onDetach() {
		issueModel.detach();
		fixCommitsModel.detach();
		pullRequestIdsModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(Issue issue, @Nullable QueryPosition position) {
		PageParameters params = ProjectPage.paramsOf(issue.getProject());
		params.add(PARAM_ISSUE, issue.getNumber());
		if (position != null)
			position.fill(params);
		return params;
	}

	@Override
	public List<String> getInputNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputSpec getInputSpec(String inputName) {
		return getProject().getIssueWorkflow().getFieldSpec(inputName);
	}
	
	@Override
	public boolean isReservedName(String inputName) {
		throw new UnsupportedOperationException();
	}
	
	protected Collection<ObjectId> getFixCommits() {
		return fixCommitsModel.getObject();
	}
	
	protected Collection<Long> getPullRequestIds() {
		return pullRequestIdsModel.getObject();
	}
	
	private class IssueTab extends PageTab {

		public IssueTab(String title, Class<? extends Page> pageClass) {
			super(Model.of(title), pageClass);
		}
		
		@Override
		public Component render(String componentId) {
			return new PageTabLink(componentId, this) {

				@Override
				protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
					return new ViewStateAwarePageLink<Void>(linkId, pageClass, paramsOf(getIssue(), position));
				}
				
			};
		}
		
	}
	
}
