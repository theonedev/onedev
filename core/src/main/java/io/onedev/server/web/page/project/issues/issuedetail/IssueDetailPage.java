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
import java.util.Set;
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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
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
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.exception.OneException;
import io.onedev.server.manager.IssueChangeManager;
import io.onedev.server.manager.IssueFieldUnaryManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.IssueVoteManager;
import io.onedev.server.manager.IssueWatchManager;
import io.onedev.server.manager.VisitManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueVote;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.model.support.issue.WatchStatus;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.model.support.issue.workflow.IssueWorkflow;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.model.support.issue.workflow.TransitionSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.dateinput.DateInput;
import io.onedev.server.web.component.IssueStateLabel;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.comment.ProjectAttachmentSupport;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.PageTabLink;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.component.watchstatus.WatchStatusLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.page.project.issues.issuedetail.activities.IssueActivitiesPage;
import io.onedev.server.web.page.project.issues.issuelist.IssueListPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneDetailPage;
import io.onedev.server.web.page.project.issues.newissue.NewIssuePage;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.util.ConfirmOnClick;
import io.onedev.server.web.util.QueryPosition;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
public abstract class IssueDetailPage extends ProjectPage implements InputContext {

	private static final int MAX_DISPLAY_AVATARS = 20;
	
	public static final String PARAM_ISSUE = "issue";
	
	private static final String ACTION_OPTIONS_ID = "actionOptions";
	
	private static final String TITLE_ID = "title";
	
	private static final String NAV_ID = "issueNav";
	
	private final IModel<Issue> issueModel;
	
	private final QueryPosition position;
	
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
				
				String prevTitle = getIssue().getTitle();
				getIssue().setTitle(titleInput.getModelObject());
				OneDev.getInstance(IssueChangeManager.class).changeTitle(getIssue(), prevTitle);
				
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
			if (transition.getFromStates().contains(getIssue().getState()) 
					&& transition.getOnAction().getButton() != null 
					&& getLoginUser() != null
					&& transition.getOnAction().getButton().getAuthorized().matches(getProject(), getLoginUser())) {
				boolean applicable = false;
				if (transition.getPrerequisite() == null) {
					applicable = true;
				} else {
					IssueField field = getIssue().getEffectiveFields().get(transition.getPrerequisite().getInputName());
					List<String> fieldValues;
					if (field != null)
						fieldValues = field.getValues();
					else
						fieldValues = new ArrayList<>();
					if (transition.getPrerequisite().matches(fieldValues))
						applicable = true;
				}
				if (applicable) {
					AjaxLink<Void> link = new AjaxLink<Void>(transitionsView.newChildId()) {

						private String comment;
						
						@Override
						public void onClick(AjaxRequestTarget target) {
							Fragment fragment = new Fragment(ACTION_OPTIONS_ID, "transitionFrag", IssueDetailPage.this);
							Serializable fieldBean = getIssueFieldUnaryManager().readFields(getIssue());
							Set<String> excludedFields = getIssueFieldUnaryManager().getExcludedFields(getIssue(), transition.getToState());

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

									Map<String, IssueField> prevFields = getIssue().getEffectiveFields();
									Collection<String> promptedFields = getIssue().getPromptedFields();
									StateSpec toStateSpec = getProject().getIssueWorkflow().getStateSpec(transition.getToState());
									if (toStateSpec == null)
										throw new OneException("Unable to find state spec: " + transition.getToState());
									promptedFields.addAll(toStateSpec.getFields());
								
									String prevState = getIssue().getState();
									getIssue().setState(transition.getToState());
									getIssueChangeManager().changeState(getIssue(), fieldBean, comment, prevState, prevFields, promptedFields);
								
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
					link.add(new Label("label", transition.getOnAction().getButton().getName()));
					transitionsView.add(link);
				}
			}
		}
		
		add(transitionsView);

		List<String> criterias = new ArrayList<>();
		if (getIssue().getMilestone() != null)
			criterias.add(IssueQuery.quote(Issue.MILESTONE) + " is " + IssueQuery.quote(getIssue().getMilestoneName()));
		for (Map.Entry<String, IssueField> entry: getIssue().getEffectiveFields().entrySet()) {
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
		
		add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, NewIssuePage.paramsOf(getProject(), query)) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canRead(getProject()));
			}
			
		});
		
		newEmptyActionOptions(null);
		
		List<Tab> tabs = new ArrayList<>();
		tabs.add(new IssueTab("Activities", IssueActivitiesPage.class));
		
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
				if (SecurityUtils.getUser() != null) { 
					OneDev.getInstance(VisitManager.class).visitIssue(SecurityUtils.getUser(), getIssue());
				}
			}
			
			@Override
			public void onDetach(RequestCycle cycle) {
			}
			
			@Override
			public void onBeginRequest(RequestCycle cycle) {
			}
			
		});	

		add(newNavContainer());
		add(newFieldsContainer());
		add(newMilestoneContainer());
		add(newVotesContainer());
		add(newWatchesContainer());
		
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
	
	private Component newNavContainer() {
		if (position != null) {
			Fragment fragment = new Fragment(NAV_ID, "issueNavFrag", this);
			fragment.add(new Link<Void>("prev") {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setEnabled(position.getOffset()>0);
				}

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					if (position.getOffset() <= 0)
						tag.put("disabled", "disabled");
				}

				@Override
				public void onClick() {
					IssueQuery query = IssueQuery.parse(getProject(), position.getQuery(), true);
					int count = position.getCount();
					int offset = position.getOffset() - 1;
					List<Issue> issues = getIssueManager().query(getProject(), query, offset, 1);
					if (!issues.isEmpty()) {
						if (!query.getCriteria().matches(getIssue()))
							count--;
						QueryPosition prevPosition = new QueryPosition(position.getQuery(), count, offset);
						PageParameters params = IssueDetailPage.paramsOf(issues.get(0), prevPosition);
						setResponsePage(getPageClass(), params);
					} else {
						WebSession.get().warn("No more issues");
					}
				}
				
			});
			fragment.add(new Link<Void>("next") {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setEnabled(position.getOffset()<position.getCount()-1);
				}

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					if (position.getOffset() >= position.getCount()-1)
						tag.put("disabled", "disabled");
				}

				@Override
				public void onClick() {
					IssueQuery query = IssueQuery.parse(getProject(), position.getQuery(), true);
					int offset = position.getOffset();
					int count = position.getCount();
					if (query.getCriteria().matches(getIssue())) 
						offset++;
					else
						count--;
					
					List<Issue> issues = getIssueManager().query(getProject(), query, offset, 1);
					if (!issues.isEmpty()) {
						QueryPosition nextPosition = new QueryPosition(position.getQuery(), count, offset);
						PageParameters params = IssueDetailPage.paramsOf(issues.get(0), nextPosition);
						setResponsePage(getPageClass(), params);
					} else {
						WebSession.get().warn("No more issues");
					}
				}
				
			});
			
			fragment.add(new Label("current", "issue " + (position.getOffset()+1) + " of " + position.getCount()));
			
			return fragment;
		} else {
			return new WebMarkupContainer(NAV_ID).setVisible(false);
		}
	}
	
	private Component newFieldsContainer() {
		WebMarkupContainer fieldsContainer = new WebMarkupContainer("fields") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getIssue().getEffectiveFields().isEmpty());
			}
			
		};
		fieldsContainer.setOutputMarkupId(true);
		
		fieldsContainer.add(new ListView<IssueField>("fields", new LoadableDetachableModel<List<IssueField>>() {

			private boolean isVisible(IssueField field, Set<String> checkedFieldNames) {
				if (!checkedFieldNames.contains(field.getName())) {
					checkedFieldNames.add(field.getName());
					
					IssueWorkflow workflow = getProject().getIssueWorkflow();
					InputSpec fieldSpec = workflow.getFieldSpec(field.getName());
					if (fieldSpec != null) {
						if (fieldSpec.getShowCondition() != null) {
							IssueField dependentField = getIssue().getEffectiveFields().get(fieldSpec.getShowCondition().getInputName());
							if (dependentField != null) {
								if (!isVisible(dependentField, checkedFieldNames))
									return false;
								String value;
								if (!dependentField.getValues().isEmpty())
									value = dependentField.getValues().iterator().next();
								else
									value = null;
								return fieldSpec.getShowCondition().getValueMatcher().matches(value);
							} else {
								return false;
							}
						} else {
							return true;
						}
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
			
			@Override
			protected List<IssueField> load() {
				List<IssueField> fields = new ArrayList<>();
				for (IssueField field: getIssue().getEffectiveFields().values()) {
					if (isVisible(field, new HashSet<>()))
						fields.add(field);
				}
				return fields;
			}
			
		}) {

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
					
				});
			}
			
		});
		
		fieldsContainer.add(new ModalLink("edit") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "fieldEditFrag", IssueDetailPage.this);
				Form<?> form = new Form<Void>("form");

				Serializable fieldBean = OneDev.getInstance(IssueFieldUnaryManager.class).readFields(getIssue()); 
				Map<String, IssueField> prevFields = getIssue().getEffectiveFields();
				
				Map<String, PropertyDescriptor> propertyDescriptors = 
						new BeanDescriptor(fieldBean.getClass()).getMapOfDisplayNameToPropertyDescriptor();
				
				Set<String> excludedFields = new HashSet<>();
				for (InputSpec fieldSpec: getProject().getIssueWorkflow().getFieldSpecs()) {
					if (!getIssue().getEffectiveFields().containsKey(fieldSpec.getName()))
						excludedFields.add(propertyDescriptors.get(fieldSpec.getName()).getPropertyName());
				}

				form.add(BeanContext.editBean("editor", fieldBean, excludedFields));
				
				form.add(new AjaxButton("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);

						OneDev.getInstance(IssueChangeManager.class).changeFields(getIssue(), fieldBean, prevFields, 
								getIssue().getPromptedFields());
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
		
		return fieldsContainer;
	}
	
	private Component newMilestoneContainer() {
		Fragment fragment = new Fragment("milestone", "milestoneViewFrag", this);
		if (getIssue().getMilestone() != null) {
			Link<Void> link = new BookmarkablePageLink<Void>("link", MilestoneDetailPage.class, MilestoneDetailPage.paramsOf(getIssue().getMilestone(), null));
			link.add(new Label("label", getIssue().getMilestone().getName()));
			fragment.add(link);
		} else {
			WebMarkupContainer link = new WebMarkupContainer("link") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
			};
			link.add(new Label("label", "<i>Unspecified</i>").setEscapeModelStrings(false));
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
						getSettings().setPlaceholder("Unspecified");
					}
					
				};
				choice.setRequired(false);
				form.add(choice);

				form.add(new AjaxButton("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						String prevMilestoneName = getIssue().getMilestoneName();
						Milestone milestone = getProject().getMilestone(milestoneName);
						getIssue().setMilestone(milestone);
						getIssueChangeManager().changeMilestone(getIssue(), prevMilestoneName);
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
				setVisible(SecurityUtils.canModify(getIssue()));
			}
			
		});
		fragment.setOutputMarkupId(true);
		return fragment;
	}
	
	private Component newVotesContainer() {
		WebMarkupContainer votesContainer = new WebMarkupContainer("votes");
		votesContainer.setOutputMarkupId(true);

		votesContainer.add(new Label("count", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return String.valueOf(getIssue().getNumOfVotes());
			}
			
		}));

		votesContainer.add(new ListView<IssueVote>("voters", new LoadableDetachableModel<List<IssueVote>>() {

			@Override
			protected List<IssueVote> load() {
				List<IssueVote> votes = new ArrayList<>(getIssue().getVotes());
				Collections.sort(votes, new Comparator<IssueVote>() {

					@Override
					public int compare(IssueVote o1, IssueVote o2) {
						return o2.getId().compareTo(o1.getId());
					}
					
				});
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
		
		votesContainer.add(new WebMarkupContainer("more") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getIssue().getVotes().size() > MAX_DISPLAY_AVATARS);
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
	
	private List<IssueWatch> getEffectWatches() {
		List<IssueWatch> watches = new ArrayList<>();
		for (IssueWatch watch: getIssue().getWatches()) {
			if (watch.isWatching())
				watches.add(watch);
		}
		Collections.sort(watches, new Comparator<IssueWatch>() {

			@Override
			public int compare(IssueWatch o1, IssueWatch o2) {
				return o2.getId().compareTo(o1.getId());
			}
			
		});
		return watches;
	}
	
	private Component newWatchesContainer() {
		WebMarkupContainer watchesContainer = new WebMarkupContainer("watches");
		watchesContainer.setOutputMarkupId(true);

		watchesContainer.add(new Label("count", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return String.valueOf(getEffectWatches().size());
			}
			
		}));
		
		watchesContainer.add(new ListView<IssueWatch>("watchers", new LoadableDetachableModel<List<IssueWatch>>() {

			@Override
			protected List<IssueWatch> load() {
				List<IssueWatch> watches = getEffectWatches();
				if (watches.size() > MAX_DISPLAY_AVATARS)
					watches = watches.subList(0, MAX_DISPLAY_AVATARS);
				return watches;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<IssueWatch> item) {
				User user = item.getModelObject().getUser();
				item.add(new AvatarLink("watcher", user, user.getDisplayName()));
			}
			
		});
		
		watchesContainer.add(new WebMarkupContainer("more") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getEffectWatches().size() > MAX_DISPLAY_AVATARS);
			}
			
		});
		
		watchesContainer.add(new WatchStatusLink("watch") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getLoginUser() != null);
			}

			@Override
			protected WatchStatus getWatchStatus() {
				IssueWatch issueWatch = getIssue().getWatch(getLoginUser());
				if (issueWatch != null && !issueWatch.isWatching())
					return WatchStatus.DO_NOT_WATCH;
				else if (issueWatch != null && issueWatch.isWatching())
					return WatchStatus.WATCH;
				else
					return WatchStatus.DEFAULT;
			}

			@Override
			protected void onWatchStatusChange(AjaxRequestTarget target, WatchStatus watchStatus) {
				if (watchStatus == WatchStatus.DO_NOT_WATCH) {
					IssueWatch issueWatch = getIssue().getWatch(getLoginUser());
					if (issueWatch == null) {
						issueWatch = new IssueWatch();
						issueWatch.setIssue(getIssue());
						issueWatch.setUser(getLoginUser());
						getIssue().getWatches().add(issueWatch);
					}
					issueWatch.setWatching(false);
					OneDev.getInstance(IssueWatchManager.class).save(issueWatch);
				} else if (watchStatus == WatchStatus.WATCH) {
					IssueWatch issueWatch = getIssue().getWatch(getLoginUser());
					if (issueWatch == null) {
						issueWatch = new IssueWatch();
						issueWatch.setIssue(getIssue());
						issueWatch.setUser(getLoginUser());
						getIssue().getWatches().add(issueWatch);
					}
					issueWatch.setWatching(true);
					OneDev.getInstance(IssueWatchManager.class).save(issueWatch);
				} else {
					IssueWatch issueWatch = getIssue().getWatch(getLoginUser());
					if (issueWatch != null) {
						getIssue().getWatches().remove(issueWatch);
						OneDev.getInstance(IssueWatchManager.class).delete(issueWatch);
					}
				}
				target.add(watchesContainer);
			}
			
		});

		return watchesContainer;
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

	private IssueChangeManager getIssueChangeManager() {
		return OneDev.getInstance(IssueChangeManager.class);
	}

	private IssueFieldUnaryManager getIssueFieldUnaryManager() {
		return OneDev.getInstance(IssueFieldUnaryManager.class);
	}
	
	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueDetailResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.issueDetail.onDomReady();"));
	}

	@Override
	protected void onDetach() {
		issueModel.detach();
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
		return getProject().getIssueWorkflow().getFieldNames();
	}

	@Override
	public InputSpec getInputSpec(String inputName) {
		return getProject().getIssueWorkflow().getFieldSpec(inputName);
	}
	
	@Override
	public boolean isReservedName(String inputName) {
		throw new UnsupportedOperationException();
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
