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

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
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
import org.apache.wicket.model.PropertyModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueChangeManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.IssueVoteManager;
import io.onedev.server.manager.IssueWatchManager;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueVote;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IssueField;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.component.entity.nav.EntityNavPanel;
import io.onedev.server.web.component.entity.watches.EntityWatchesPanel;
import io.onedev.server.web.component.issue.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.component.milestone.progress.MilestoneProgressBar;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.component.user.ident.UserIdentPanel.Mode;
import io.onedev.server.web.component.user.list.UserListLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.issues.milestones.MilestoneDetailPage;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.util.QueryPositionSupport;
import io.onedev.server.web.util.ajaxlistener.AppendLoadingIndicatorListener;

@SuppressWarnings("serial")
public abstract class IssueInfoPanel extends Panel {

	private static final int MAX_DISPLAY_AVATARS = 20;
	
	public IssueInfoPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
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
			protected List<Issue> query(EntityQuery<Issue> query, int offset, int count) {
				return getIssueManager().query(getProject(), SecurityUtils.getUser(), query, offset, count);
			}

			@Override
			protected QueryPositionSupport<Issue> getQueryPositionSupport() {
				return IssueInfoPanel.this.getQueryPositionSupport();
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
		
		add(newDeleteLink("delete"));
		
		setOutputMarkupId(true);
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
		Fragment fragment = new Fragment("fields", "fieldsViewFrag", this) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!fieldsModel.getObject().isEmpty());
			}
			
		};
		
		fragment.setOutputMarkupId(true);
		
		fragment.add(new ListView<IssueField>("fields", fieldsModel) {

			@Override
			protected void populateItem(ListItem<IssueField> item) {
				IssueField field = item.getModelObject();
				item.add(new Label("name", field.getName()));
				item.add(new FieldValuesPanel("values") {

					@Override
					protected Issue getIssue() {
						return IssueInfoPanel.this.getIssue();
					}

					@Override
					protected IssueField getField() {
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
				setVisible(SecurityUtils.canWriteCode(getIssue().getProject().getFacade())
						|| user != null && user.equals(getIssue().getSubmitter()) && getIssue().getState().equals(initialState));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("fields", "fieldsEditFrag", IssueInfoPanel.this);
				Form<?> form = new Form<Void>("form");

				Class<?> fieldBeanClass = IssueUtils.defineBeanClass(getProject());
				Serializable fieldBean = getIssue().getFieldBean(fieldBeanClass, true); 

				Collection<String> propertyNames = IssueUtils.getPropertyNames(getIssue().getProject(), 
						fieldBeanClass, getIssue().getFieldNames());
				BeanEditor beanEditor = BeanContext.editBean("editor", fieldBean, propertyNames, false); 
				form.add(beanEditor);
				
				form.add(new AjaxButton("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);

						Map<String, Object> fieldValues = IssueUtils.getFieldValues(beanEditor.getOneContext(), fieldBean, getIssue().getFieldNames());
						OneDev.getInstance(IssueChangeManager.class).changeFields(getIssue(), fieldValues, SecurityUtils.getUser());
						Component fieldsContainer = newFieldsContainer();
						IssueInfoPanel.this.replace(fieldsContainer);
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
						IssueInfoPanel.this.replace(fieldsContainer);
						target.add(fieldsContainer);
					}
					
				});
				fragment.add(form);
				fragment.setOutputMarkupId(true);
				
				IssueInfoPanel.this.replace(fragment);
				target.add(fragment);
			}
			
		});		
		
		return fragment;
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
				Fragment fragment =  new Fragment("milestone", "milestoneEditFrag", IssueInfoPanel.this);
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
						IssueInfoPanel.this.replace(container);
						target.add(container);
					}

				});
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Component container = newMilestoneContainer();
						IssueInfoPanel.this.replace(container);
						target.add(container);
					}
					
				});
				fragment.add(form);
				fragment.setOutputMarkupId(true);
				IssueInfoPanel.this.replace(fragment);
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
				item.add(new UserIdentPanel("voter", UserIdent.of(UserFacade.of(user)), Mode.AVATAR));
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
		votesContainer.add(voteLink);
		
		return votesContainer;
	}
	
	private Project getProject() {
		return getIssue().getProject();
	}
	
	private IssueChangeManager getIssueChangeManager() {
		return OneDev.getInstance(IssueChangeManager.class);
	}

	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueInfoCssResourceReference()));
	}

	protected abstract Issue getIssue();

	@Nullable
	protected abstract QueryPositionSupport<Issue> getQueryPositionSupport();
	
	protected abstract Component newDeleteLink(String componentId);
}
