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
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.IssueVoteManager;
import io.onedev.server.entitymanager.IssueWatchManager;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entityreference.Referenceable;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueVote;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.LinkSide;
import io.onedev.server.util.Similarities;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.AttachAjaxIndicatorListener;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.entity.reference.ReferencePanel;
import io.onedev.server.web.component.entity.watches.EntityWatchesPanel;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.issue.IssueStateBadge;
import io.onedev.server.web.component.issue.choice.IssueAddChoice;
import io.onedev.server.web.component.issue.choice.IssueChoiceProvider;
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
import io.onedev.server.web.editable.InplacePropertyEditLink;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneIssuesPage;
import io.onedev.server.web.page.simple.security.LoginPage;

@SuppressWarnings("serial")
public abstract class IssueSidePanel extends Panel {

	private static final int MAX_DISPLAY_AVATARS = 20;
	
	private boolean confidential;
	
	public IssueSidePanel(String id) {
		super(id);
		confidential = getIssue().isConfidential();
	}

	@Override
	protected void onBeforeRender() {
		addOrReplace(newFieldsContainer());
		addOrReplace(newConfidentialContainer());
		addOrReplace(newMilestonesContainer());
		addOrReplace(newLinksContainer());
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

			@Override
			protected boolean isAuthorized(User user) {
				return SecurityUtils.canAccess(user.asSubject(), getIssue());
			}
			
		});
		
		addOrReplace(new ReferencePanel("reference") {

			@Override
			protected Referenceable getReferenceable() {
				return getIssue();
			}
			
		});
		
		String initialState = OneDev.getInstance(SettingManager.class).getIssueSetting().getInitialStateSpec().getName();
		if (SecurityUtils.canManageIssues(getProject()) 
				|| getIssue().getState().equals(initialState) && getIssue().getSubmitter().equals(SecurityUtils.getUser())) {
			addOrReplace(newDeleteLink("delete"));		
		} else {
			addOrReplace(new WebMarkupContainer("delete").setVisible(false));
		}
		
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
				item.add(new FieldValuesPanel("values", Mode.NAME, false) {

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
	
	private Component newConfidentialContainer() {
		CheckBox confidentialInput = new CheckBox("confidential", new PropertyModel<Boolean>(this, "confidential"));
		confidentialInput.add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				OneDev.getInstance(IssueChangeManager.class).changeConfidential(getIssue(), confidential);
				setResponsePage(getPage());
			}
			
		});
		confidentialInput.setVisible(SecurityUtils.canModify(getIssue()));
		
		return confidentialInput;
	}
	
	private Component newLinksContainer() {
		return new ListView<LinkSide>("links", new LoadableDetachableModel<List<LinkSide>>() {

			@Override
			protected List<LinkSide> load() {
				List<LinkSide> links = new ArrayList<>();
				List<LinkSpec> specs = new ArrayList<>(OneDev.getInstance(LinkSpecManager.class).queryAndSort());
				
				for (LinkSpec spec: specs) {
					if (SecurityUtils.canEditIssueLink(getProject(), spec) 
							|| getIssue().getLinks().stream().anyMatch(it->it.getSpec().equals(spec))) {
						if (spec.getOpposite() != null) {
							IssueQuery query = spec.getOpposite().getParsedIssueQuery(getProject());
							if (query.matches(getIssue()))
								links.add(new LinkSide(spec, false));
							query = spec.getParsedIssueQuery(getProject());
							if (query.matches(getIssue()))
								links.add(new LinkSide(spec, true));
						} else {
							IssueQuery query = spec.getParsedIssueQuery(getProject());
							if (query.matches(getIssue()))
								links.add(new LinkSide(spec, false));
						}
					}
				}
				return links;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<LinkSide> item) {
				LinkSide side = item.getModelObject();
				
				if (side.isOpposite() && side.getSpec().getOpposite().isMultiple() 
						|| !side.isOpposite() && side.getSpec().isMultiple()) {
					item.add(newMultipleLinks(item.getModel()));
				} else {
					item.add(newSingleLink(item.getModel()));
				}
			}
			
			private Fragment newMultipleLinks(IModel<LinkSide> model) {
				Fragment fragment = new Fragment("content", "multipleLinksFrag", IssueSidePanel.this);
				LinkSide side = model.getObject();
				LinkSpec spec = side.getSpec();
				boolean opposite = side.isOpposite();
				
				boolean canEditIssueLink = SecurityUtils.canEditIssueLink(getProject(), spec);
				
				String name = spec.getName(opposite);
				fragment.add(new Label("name", name));
				
				RepeatingView linkedIssuesView = new RepeatingView("linkedIssues");
				for (Issue linkedIssue: getIssue().findLinkedIssues(spec, opposite)) {
					LinkDeleteListener deleteListener;
					if (canEditIssueLink 
							&& (linkedIssue.getProject().equals(getProject()) || SecurityUtils.canEditIssueLink(linkedIssue.getProject(), spec))) { 
						deleteListener = new LinkDeleteListener() {
	
							@Override
							void onDelete(AjaxRequestTarget target, Issue linkedIssue) {
								getIssueChangeManager().removeLink(model.getObject().getSpec(), getIssue(), 
										linkedIssue, opposite);
							}
							
						};
					} else {
						deleteListener = null;
					}
					linkedIssuesView.add(newLinkedIssueContainer(linkedIssuesView.newChildId(), 
							linkedIssue, deleteListener));
				}
				fragment.add(linkedIssuesView);

				fragment.add(new IssueAddChoice("add", new IssueChoiceProvider() {

					@Override
					protected Project getProject() {
						return getIssue().getProject();
					}
					
					@Override
					protected EntityQuery<Issue> getScope() {
						LinkSpec spec = model.getObject().getSpec();
						if (opposite) 
							return spec.getOpposite().getParsedIssueQuery(getProject());
						else 
							return spec.getParsedIssueQuery(getProject());
					}
					
				}) {

					@Override
					protected void onSelect(AjaxRequestTarget target, Issue selection) {
						LinkSpec spec = model.getObject().getSpec();
						if (getIssue().equals(selection)) {
							getSession().warn("Can not link to self");
						} else if (getIssue().findLinkedIssues(spec, opposite).contains(selection)) { 
							getSession().warn("Issue already added");
						} else if (!selection.getProject().equals(getProject()) 
								&& !SecurityUtils.canEditIssueLink(selection.getProject(), spec)) {
							getSession().warn("Not authorized to link issue in project '" + selection.getProject() + "'");
						} else {
							getIssueChangeManager().addLink(spec, getIssue(), selection, opposite);
						}
					}

					@Override
					protected String getPlaceholder() {
						return "Add " + name.toLowerCase();
					}
					
				}.setVisible(canEditIssueLink));
				
				return fragment;
			}
			
			private Fragment newSingleLink(IModel<LinkSide> model) {
				Fragment fragment = new Fragment("content", "singleLinkFrag", IssueSidePanel.this);
				LinkSide side = model.getObject();
				fragment.add(new Label("name", side.getSpec().getName(side.isOpposite())));
				
				SingleLinkBean bean = new SingleLinkBean();
				
				Issue prevLinkedIssue = getIssue().findLinkedIssue(side.getSpec(), side.isOpposite());
				if (prevLinkedIssue != null)
					bean.setIssueId(prevLinkedIssue.getId());
				
				Long prevLinkedIssueId = bean.getIssueId();
				
				boolean authorized = SecurityUtils.canEditIssueLink(getProject(), side.getSpec()) 
						&& (prevLinkedIssue == null 
								|| prevLinkedIssue.getProject().equals(getProject()) 
								|| SecurityUtils.canEditIssueLink(prevLinkedIssue.getProject(), side.getSpec()));
				
				fragment.add(new InplacePropertyEditLink("edit", new AlignPlacement(100, 0, 100, 0)) {

					@Override
					protected Serializable getBean() {
						return bean;
					}

					@Override
					protected String getPropertyName() {
						return "issueId";
					}

					@Override
					protected Project getProject() {
						return getIssue().getProject();
					}

					@Override
					protected IssueQuery getIssueQuery() {
						LinkSide side = model.getObject();
						if (side.isOpposite()) 
							return side.getSpec().getOpposite().getParsedIssueQuery(getProject());
						else 
							return side.getSpec().getParsedIssueQuery(getProject());
					}

					@Override
					protected void onUpdated(IPartialPageRequestHandler handler, Serializable bean,
							String propertyName) {
						LinkSide side = model.getObject();
						SingleLinkBean singleLinkBean = (SingleLinkBean) bean;
						Issue linkedIssue = null;
						if (singleLinkBean.getIssueId() != null) 
							linkedIssue = getIssueManager().load(singleLinkBean.getIssueId());
						if (getIssue().equals(linkedIssue)) {
							getSession().warn("Can not link to self");
							singleLinkBean.setIssueId(prevLinkedIssueId);
						} else if (linkedIssue != null && !linkedIssue.getProject().equals(getProject()) 
								&& !SecurityUtils.canEditIssueLink(linkedIssue.getProject(), side.getSpec())) {
							getSession().warn("Not authorized to link issue in project '" + linkedIssue.getProject() + "'");
							singleLinkBean.setIssueId(prevLinkedIssueId);
						} else {
							getIssueChangeManager().changeLink(side.getSpec(), getIssue(), 
									linkedIssue, side.isOpposite());
						}
					}

				}.setVisible(authorized));
				
				if (prevLinkedIssue != null) 
					fragment.add(newLinkedIssueContainer("body", prevLinkedIssue, null));
				else 
					fragment.add(new Label("body", "<i>Not specified</i>").setEscapeModelStrings(false));
				
				return fragment;
			}
			
		};
	}
	
	private Component newLinkedIssueContainer(String componentId, Issue linkedIssue, 
			@Nullable LinkDeleteListener deleteListener) {
		if (SecurityUtils.canAccess(linkedIssue)) {
			Long linkedIssueId = linkedIssue.getId();
			Fragment fragment = new Fragment(componentId, "linkedIssueFrag", this);
			Link<Void> link = new BookmarkablePageLink<Void>("number", IssueActivitiesPage.class, 
					IssueActivitiesPage.paramsOf(linkedIssue));
			if (linkedIssue.getNumberScope().equals(getIssue().getNumberScope()))
				link.add(new Label("label", "#" + linkedIssue.getNumber()));
			else
				link.add(new Label("label", linkedIssue.getFQN().toString()));
			fragment.add(link);
			
			fragment.add(new AjaxLink<Void>("delete") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new ConfirmClickListener(
							"Do you really want to remove this?"));
				}

				@Override
				public void onClick(AjaxRequestTarget target) {
					Issue linkedIssue = getIssueManager().load(linkedIssueId);
					deleteListener.onDelete(target, linkedIssue);
				}
				
			}.setVisible(deleteListener != null));
			
			fragment.add(new IssueStateBadge("state", new LoadableDetachableModel<Issue>() {

				@Override
				protected Issue load() {
					return getIssueManager().load(linkedIssueId);
				}
				
			}));
			
			link = new BookmarkablePageLink<Void>("title", IssueActivitiesPage.class, 
					IssueActivitiesPage.paramsOf(linkedIssue));
			link.add(new Label("label", linkedIssue.getTitle()));
			fragment.add(link);
			
			return fragment;
		} else {
			Fragment fragment = new Fragment(componentId, "unauthorizedLinkedIssueFrag", IssueSidePanel.this);
			if (getProject().equals(linkedIssue.getProject()))
				fragment.add(new Label("number", "#" + linkedIssue.getNumber()));
			else
				fragment.add(new Label("number", linkedIssue.getProject().getPath() + "#" + linkedIssue.getNumber()));
			return fragment;
		}
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
						String query = new IssueQuery(new StateCriteria(state, IssueQueryLexer.Is)).toString();
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
						getIssueChangeManager().removeSchedule(getIssue(), item.getModelObject());
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
				
				milestones = new Similarities<Milestone>(milestones) {

					@Override
					public double getSimilarScore(Milestone object) {
						return Similarities.getSimilarScore(object.getName(), term);
					}
					
				};
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
				getIssueChangeManager().addSchedule(getIssue(), milestone);
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
	
	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueSideCssResourceReference()));
	}

	protected abstract Issue getIssue();

	protected abstract Component newDeleteLink(String componentId);

	private static abstract class LinkDeleteListener implements Serializable {
		
		abstract void onDelete(AjaxRequestTarget target, Issue linkedIssue);
		
	}
}
