package io.onedev.server.web.component.issue.side;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueVoteManager;
import io.onedev.server.entitymanager.IssueWatchManager;
import io.onedev.server.entityreference.Referenceable;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueVote;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.match.MatchScoreProvider;
import io.onedev.server.util.match.MatchScoreUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.AttachAjaxIndicatorListener;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.entity.reference.ReferencePanel;
import io.onedev.server.web.component.entity.watches.EntityWatchesPanel;
import io.onedev.server.web.component.issue.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.component.issue.statestats.StateStatsBar;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.milestone.MilestoneStatusLabel;
import io.onedev.server.web.component.milestone.choice.AbstractMilestoneChoiceProvider;
import io.onedev.server.web.component.milestone.choice.MilestoneChoiceResourceReference;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.server.web.component.select2.SelectToAddChoice;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.component.user.list.SimpleUserListLink;
import io.onedev.server.web.page.project.issues.milestones.MilestoneIssuesPage;
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
		addOrReplace(newMilestonesContainer());
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
		return new ListView<Input>("fields", fieldsModel) {

			@Override
			protected void populateItem(ListItem<Input> item) {
				Input field = item.getModelObject();
				item.add(new Label("name", field.getName()));
				item.add(new FieldValuesPanel("values", Mode.NAME) {

					@Override
					protected AttachAjaxIndicatorListener getInplaceEditAjaxIndicator() {
						return new AttachAjaxIndicatorListener(false);
					}

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
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!fieldsModel.getObject().isEmpty());
			}
			
		};
	}
	
	private Component newMilestonesContainer() {
		WebMarkupContainer container = new WebMarkupContainer("milestones") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getIssue().getSchedules().isEmpty() || SecurityUtils.canScheduleIssues(getProject()));
			}
			
		};
		
		container.add(new ListView<Milestone>("milestones", new AbstractReadOnlyModel<List<Milestone>>() {

			@Override
			public List<Milestone> getObject() {
				return getIssue().getMilestones().stream()
						.sorted(new Milestone.DatesAndStatusComparator())
						.collect(Collectors.toList()); 
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Milestone> item) {
				Milestone milestone = item.getModelObject();

				Link<Void> link = new BookmarkablePageLink<Void>("link", MilestoneIssuesPage.class, 
						MilestoneIssuesPage.paramsOf(getIssue().getProject(), milestone, null));
				link.add(new Label("label", milestone.getName()));
				item.add(link);
				
				item.add(new StateStatsBar("progress", new AbstractReadOnlyModel<Map<String, Integer>>() {

					@Override
					public Map<String, Integer> getObject() {
						return item.getModelObject().getStateStats(getIssue().getProject());
					}
					
				}) {

					@Override
					protected Link<Void> newStateLink(String componentId, String state) {
						String query = new IssueQuery(new StateCriteria(state)).toString();
						PageParameters params = MilestoneIssuesPage.paramsOf(getIssue().getProject(), 
								item.getModelObject(), query);
						return new ViewStateAwarePageLink<Void>(componentId, MilestoneIssuesPage.class, params);
					}
					
				});
				item.add(new MilestoneStatusLabel("status", new AbstractReadOnlyModel<Milestone>() {

					@Override
					public Milestone getObject() {
						return item.getModelObject();
					}
					
				}));
				
				item.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						if (!getIssue().isNew()) {
							attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to "
									+ "remove the issue from milestone '" + item.getModelObject().getName() + "'?"));
						}
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						getIssueChangeManager().removeFromMilestone(getIssue(), item.getModelObject());
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canScheduleIssues(getIssue().getProject()));
					}
					
				});
			}
			
		});
		
		container.add(new SelectToAddChoice<Milestone>("add", new AbstractMilestoneChoiceProvider() {
			
			@Override
			public void query(String term, int page, Response<Milestone> response) {
				List<Milestone> milestones = getProject().getSortedHierarchyMilestones();
				milestones.removeAll(getIssue().getMilestones());
				
				milestones = MatchScoreUtils.filterAndSort(
						milestones, new MatchScoreProvider<Milestone>() {

					@Override
					public double getMatchScore(Milestone object) {
						return MatchScoreUtils.getMatchScore(object.getName(), term);
					}
					
				});
				new ResponseFiller<Milestone>(response).fill(milestones, page, WebConstants.PAGE_SIZE);
			}
			
		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder("Add to milestone...");
				getSettings().setFormatResult("onedev.server.milestoneChoiceFormatter.formatResult");
				getSettings().setFormatSelection("onedev.server.milestoneChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("onedev.server.milestoneChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canScheduleIssues(getIssue().getProject()));
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(JavaScriptHeaderItem.forReference(new MilestoneChoiceResourceReference()));
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Milestone milestone) {
				getIssueChangeManager().addToMilestone(getIssue(), milestone);
			}

		});		
		
		return container;
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
