package io.onedev.server.web.component.issue.side;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.onedev.server.web.component.entity.reference.ReferencePanel;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueVoteManager;
import io.onedev.server.entitymanager.IssueWatchManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueVote;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.util.Referenceable;
import io.onedev.server.web.ajaxlistener.AppendLoadingIndicatorListener;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.entity.watches.EntityWatchesPanel;
import io.onedev.server.web.component.issue.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.component.issue.statestats.StateStatsBar;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.milestone.MilestoneStatusLabel;
import io.onedev.server.web.component.milestone.choice.MilestoneSingleChoice;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.component.user.list.SimpleUserListLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.issues.milestones.MilestoneDetailPage;
import io.onedev.server.web.page.simple.security.LoginPage;

@SuppressWarnings("serial")
public abstract class IssueSidePanel extends Panel {

	private static final int MAX_DISPLAY_AVATARS = 20;
	
	public IssueSidePanel(String id) {
		super(id);
	}

	@Override
	protected void onBeforeRender() {
		addOrReplace(newFieldsContainer());
		addOrReplace(newMilestoneContainer());
		addOrReplace(newVotesContainer());
		
		addOrReplace(new EntityWatchesPanel("watches") {

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
		
		addOrReplace(new ReferencePanel("reference") {

			@Override
			protected Referenceable getReferenceable() {
				return getIssue();
			}
			
		});
		
		if (SecurityUtils.canManageIssues(getProject()))
			addOrReplace(newDeleteLink("delete"));		
		else
			addOrReplace(new WebMarkupContainer("delete").setVisible(false));
		
		super.onBeforeRender();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				handler.add(IssueSidePanel.this);
			}
			
			@Override
			public Collection<String> getObservables() {
				return Lists.newArrayList(Issue.getWebSocketObservable(getIssue().getId()));
			}
			
		});
		
		setOutputMarkupId(true);
	}

	private Component newFieldsContainer() {
		IModel<List<Input>> fieldsModel = new LoadableDetachableModel<List<Input>>() {

			@Override
			protected List<Input> load() {
				List<Input> fields = new ArrayList<>();
				for (Input field: getIssue().getFieldInputs().values()) {
					if (getIssue().isFieldVisible(field.getName()))
						fields.add(field);
				}
				return fields;
			}
			
		};		
		Fragment fragment = new Fragment("fields", "fieldsViewFrag", this) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!fieldsModel.getObject().isEmpty());
			}
			
		};
		fragment.setOutputMarkupId(true);
		
		fragment.add(new ListView<Input>("fields", fieldsModel) {

			@Override
			protected void populateItem(ListItem<Input> item) {
				Input field = item.getModelObject();
				item.add(new Label("name", field.getName()));
				item.add(new FieldValuesPanel("values", Mode.NAME) {

					@Override
					protected Issue getIssue() {
						return IssueSidePanel.this.getIssue();
					}

					@Override
					protected Input getField() {
						return item.getModelObject();
					}
					
				}.setRenderBodyOnly(true));
			}
			
		});
		
		fragment.add(new AjaxLink<Void>("edit") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new AppendLoadingIndicatorListener(false));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				User user = SecurityUtils.getUser();
				String initialState = OneDev.getInstance(SettingManager.class).getIssueSetting().getInitialStateSpec().getName();
				if (SecurityUtils.canManageIssues(getProject())) {
					setVisible(true);
				} else {
					GlobalIssueSetting setting = OneDev.getInstance(SettingManager.class).getIssueSetting();
					boolean hasEditableFields = false;
					for (String fieldName: setting.getFieldNames()) {
						if (SecurityUtils.canEditIssueField(getProject(), fieldName)) {
							hasEditableFields = true;
							break;
						}
					}
					setVisible(hasEditableFields 
							&& user != null 
							&& user.equals(getIssue().getSubmitter()) 
							&& getIssue().getState().equals(initialState));
				}
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("fields", "fieldsEditFrag", IssueSidePanel.this);
				Form<?> form = new Form<Void>("form");

				Class<?> fieldBeanClass = IssueUtils.defineFieldBeanClass(getProject());
				Serializable fieldBean = getIssue().getFieldBean(fieldBeanClass, true); 

				Collection<String> propertyNames = IssueUtils.getPropertyNames(getIssue().getProject(), 
						fieldBeanClass, getIssue().getFieldNames());
				BeanEditor beanEditor = BeanContext.edit("editor", fieldBean, propertyNames, false); 
				form.add(beanEditor);
				
				form.add(new AjaxButton("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);

						Map<String, Object> fieldValues = IssueUtils.getFieldValues(beanEditor.newComponentContext(), fieldBean, getIssue().getFieldNames());
						OneDev.getInstance(IssueChangeManager.class).changeFields(getIssue(), fieldValues);
						Component fieldsContainer = newFieldsContainer();
						IssueSidePanel.this.replace(fieldsContainer);
						target.add(fieldsContainer);
					}

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(fragment);
					}
					
				});
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Component fieldsContainer = newFieldsContainer();
						IssueSidePanel.this.replace(fieldsContainer);
						target.add(fieldsContainer);
					}
					
				});
				fragment.add(form);
				fragment.setOutputMarkupId(true);
				
				IssueSidePanel.this.replace(fragment);
				target.add(fragment);
			}
			
		});		
		
		return fragment;
	}
	
	private Component newMilestoneContainer() {
		Fragment fragment = new Fragment("milestone", "milestoneViewFrag", this);
		if (getIssue().getMilestone() != null) {
			Link<Void> link = new BookmarkablePageLink<Void>("link", MilestoneDetailPage.class, 
					MilestoneDetailPage.paramsOf(getIssue().getMilestone(), null));
			link.add(new Label("label", getIssue().getMilestone().getName()));
			fragment.add(new StateStatsBar("progress", new AbstractReadOnlyModel<Map<String, Integer>>() {

				@Override
				public Map<String, Integer> getObject() {
					return getIssue().getMilestone().getStateStats();
				}
				
			}) {

				@Override
				protected Link<Void> newStateLink(String componentId, String state) {
					String query = new IssueQuery(new StateCriteria(state)).toString();
					PageParameters params = MilestoneDetailPage.paramsOf(getIssue().getMilestone(), query);
					return new ViewStateAwarePageLink<Void>(componentId, MilestoneDetailPage.class, params);
				}
				
			});
			fragment.add(link);
			fragment.add(new MilestoneStatusLabel("status", new AbstractReadOnlyModel<Milestone>() {

				@Override
				public Milestone getObject() {
					return getIssue().getMilestone();
				}
				
			}));
		} else {
			WebMarkupContainer link = new WebMarkupContainer("link") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
			};
			link.add(new Label("label", "<i>No milestone</i>").setEscapeModelStrings(false));
			fragment.add(new WebMarkupContainer("status").setVisible(false));
			fragment.add(new WebMarkupContainer("progress").setVisible(false));
			fragment.add(link);
		}

		fragment.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment =  new Fragment("milestone", "milestoneEditFrag", IssueSidePanel.this);
				Form<?> form = new Form<Void>("form");
				
				MilestoneSingleChoice choice = new MilestoneSingleChoice("milestone", 
						Model.of(getIssue().getMilestone()), 
						new LoadableDetachableModel<Collection<Milestone>>() {

					@Override
					protected Collection<Milestone> load() {
						return getProject().getSortedMilestones();
					}
					
				});
				choice.setRequired(false);
				
				form.add(choice);

				form.add(new AjaxButton("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						Milestone milestone = choice.getModelObject();
						getIssueChangeManager().changeMilestone(getIssue(), milestone);
						Component container = newMilestoneContainer();
						IssueSidePanel.this.replace(container);
						target.add(container);
					}

				});
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Component container = newMilestoneContainer();
						IssueSidePanel.this.replace(container);
						target.add(container);
					}
					
				});
				fragment.add(form);
				fragment.setOutputMarkupId(true);
				IssueSidePanel.this.replace(fragment);
				target.add(fragment);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canScheduleIssues(getIssue().getProject()));
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
		WebMarkupContainer container = new WebMarkupContainer("votes");
		container.setOutputMarkupId(true);

		container.add(new Label("count", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return String.valueOf(getIssue().getVoteCount());
			}
			
		}));

		container.add(new ListView<IssueVote>("voters", new LoadableDetachableModel<List<IssueVote>>() {

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
				item.add(new UserIdentPanel("voter", user, Mode.AVATAR));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getIssue().getVotes().isEmpty());
			}
			
		});
		
		container.add(new SimpleUserListLink("more") {

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
				if (SecurityUtils.getUser() != null) {
					IssueVote vote = getVote(SecurityUtils.getUser());
					if (vote == null) {
						vote = new IssueVote();
						vote.setIssue(getIssue());
						vote.setUser(SecurityUtils.getUser());
						vote.setDate(new Date());
						OneDev.getInstance(IssueVoteManager.class).save(vote);
						getIssue().getVotes().add(vote);
					} else {
						getIssue().getVotes().remove(vote);
						OneDev.getInstance(IssueVoteManager.class).delete(vote);
					}
					target.add(container);
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
						if (SecurityUtils.getUser() != null) {
							if (getVote(SecurityUtils.getUser()) != null)
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
		container.add(voteLink);
		
		return container;
	}
	
	private Project getProject() {
		return getIssue().getProject();
	}
	
	private IssueChangeManager getIssueChangeManager() {
		return OneDev.getInstance(IssueChangeManager.class);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueSideCssResourceReference()));
	}

	protected abstract Issue getIssue();

	protected abstract Component newDeleteLink(String componentId);
	
}
