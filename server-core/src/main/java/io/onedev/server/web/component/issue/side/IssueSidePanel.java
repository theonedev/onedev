package io.onedev.server.web.component.issue.side;

import static io.onedev.server.security.SecurityUtils.canAccessIssue;
import static io.onedev.server.security.SecurityUtils.canManageIssues;
import static io.onedev.server.util.EmailAddressUtils.describe;
import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.internet.InternetAddress;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.Input;
import io.onedev.server.service.IssueChangeService;
import io.onedev.server.service.IssueVoteService;
import io.onedev.server.service.IssueWatchService;
import io.onedev.server.entityreference.EntityReference;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueVote;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Similarities;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.AttachAjaxIndicatorListener;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.entity.reference.EntityReferencePanel;
import io.onedev.server.web.component.entity.watches.EntityWatchesPanel;
import io.onedev.server.web.component.issue.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.component.issue.statestats.StateStatsBar;
import io.onedev.server.web.component.iteration.IterationStatusLabel;
import io.onedev.server.web.component.iteration.choice.AbstractIterationChoiceProvider;
import io.onedev.server.web.component.iteration.choice.IterationChoiceResourceReference;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.server.web.component.select2.SelectToActChoice;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.component.user.list.SimpleUserListLink;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.issues.iteration.IterationIssuesPage;
import io.onedev.server.web.page.security.LoginPage;

public abstract class IssueSidePanel extends Panel {

	private static final int MAX_DISPLAY_AVATARS = 20;
		
	private boolean confidential;
	
	private Component watchesContainer;
	
	public IssueSidePanel(String id) {
		super(id);
		confidential = getIssue().isConfidential();
	}

	@Override
	protected void onBeforeRender() {
		addOrReplace(newFieldsContainer());
		addOrReplace(newConfidentialContainer());
		addOrReplace(newIterationsContainer());
		addOrReplace(newVotesContainer());
		
		addOrReplace(watchesContainer = new EntityWatchesPanel("watches") {

			@Override
			protected void onSaveWatch(EntityWatch watch) {
				OneDev.getInstance(IssueWatchService.class).createOrUpdate((IssueWatch) watch);
			}

			@Override
			protected void onDeleteWatch(EntityWatch watch) {
				OneDev.getInstance(IssueWatchService.class).delete((IssueWatch) watch);
			}

			@Override
			protected AbstractEntity getEntity() {
				return getIssue();
			}

			@Override
			protected boolean isAuthorized(User user) {
				return canAccessIssue(user.asSubject(), getIssue());
			}
			
		});
		addOrReplace(newExternalParticipantsContainer());
		
		addOrReplace(new EntityReferencePanel("reference") {

			@Override
			protected EntityReference getReference() {
				return getIssue().getReference();
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

		add(new ChangeObserver() {
			
			@Override
			public Collection<String> findObservables() {
				return Lists.newArrayList(Issue.getDetailChangeObservable(getIssue().getId()));
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
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				
				String precondition = "" +
						"if (onedev.server.form.confirmLeave())" +
						"	return true;" +
						"if ($(this).is(':checkbox'))" +
						"  this.checked = !this.checked;" +
						"return false;";
				attributes.getAjaxCallListeners().add(new AjaxCallListener().onPrecondition(precondition));
			}

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				var user = SecurityUtils.getUser();
				OneDev.getInstance(IssueChangeService.class).changeConfidential(user, getIssue(), confidential);
				setResponsePage(getPage());
			}
			
		});
		confidentialInput.setVisible(SecurityUtils.canModifyIssue(getIssue()));
		
		return confidentialInput;
	}
		
	private Component newIterationsContainer() {
		WebMarkupContainer container = new WebMarkupContainer("iterations") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getIssue().getSchedules().isEmpty() || SecurityUtils.canScheduleIssues(getProject()));
			}
			
		};
		
		container.add(new ListView<Iteration>("iterations", new AbstractReadOnlyModel<List<Iteration>>() {

			@Override
			public List<Iteration> getObject() {
				return getIssue().getIterations().stream()
						.sorted(new Iteration.DatesAndStatusComparator())
						.collect(Collectors.toList()); 
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Iteration> item) {
				Iteration iteration = item.getModelObject();

				Link<Void> link = new BookmarkablePageLink<Void>("link", IterationIssuesPage.class, 
						IterationIssuesPage.paramsOf(getIssue().getProject(), iteration, null));
				link.add(new Label("label", iteration.getName()));
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
						PageParameters params = IterationIssuesPage.paramsOf(getIssue().getProject(), 
								item.getModelObject(), query);
						return new ViewStateAwarePageLink<Void>(componentId, IterationIssuesPage.class, params);
					}
					
				});
				item.add(new IterationStatusLabel("status", new AbstractReadOnlyModel<Iteration>() {

					@Override
					public Iteration getObject() {
						return item.getModelObject();
					}
					
				}).add(AttributeAppender.append("class", "badge-sm")));
				
				item.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						if (!getIssue().isNew()) {
							attributes.getAjaxCallListeners().add(new ConfirmClickListener(MessageFormat.format(_T("Do you really want to remove the issue from iteration \"{0}\"?"), item.getModelObject().getName())));
						}
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						var user = SecurityUtils.getUser();
						getIssueChangeService().removeSchedule(user, getIssue(), item.getModelObject());
						notifyIssueChange(target, getIssue());
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canScheduleIssues(getIssue().getProject()));
					}
					
				});
			}
			
		});
		
		container.add(new SelectToActChoice<Iteration>("add", new AbstractIterationChoiceProvider() {
			
			@Override
			public void query(String term, int page, Response<Iteration> response) {
				List<Iteration> iterations = getProject().getSortedHierarchyIterations();
				iterations.removeAll(getIssue().getIterations());
				
				iterations = new Similarities<Iteration>(iterations) {

					@Override
					public double getSimilarScore(Iteration object) {
						return Similarities.getSimilarScore(object.getName(), term);
					}
					
				};
				new ResponseFiller<>(response).fill(iterations, page, WebConstants.PAGE_SIZE);
			}
			
		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder(_T("Add to iteration..."));
				getSettings().setFormatResult("onedev.server.iterationChoiceFormatter.formatResult");
				getSettings().setFormatSelection("onedev.server.iterationChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("onedev.server.iterationChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canScheduleIssues(getIssue().getProject()));
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(JavaScriptHeaderItem.forReference(new IterationChoiceResourceReference()));
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Iteration iteration) {
				getIssueChangeService().addSchedule(SecurityUtils.getUser(), getIssue(), iteration);
				notifyIssueChange(target, getIssue());
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

		container.add(new ListView<>("voters", new LoadableDetachableModel<List<IssueVote>>() {

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
				if (SecurityUtils.getAuthUser() != null) {
					IssueVote vote = getVote(SecurityUtils.getAuthUser());
					if (vote == null) {
						vote = new IssueVote();
						vote.setIssue(getIssue());
						vote.setUser(SecurityUtils.getAuthUser());
						vote.setDate(new Date());
						OneDev.getInstance(IssueVoteService.class).create(vote);
						getIssue().getVotes().add(vote);
						target.add(watchesContainer);
					} else {
						getIssue().getVotes().remove(vote);
						OneDev.getInstance(IssueVoteService.class).delete(vote);
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
						if (SecurityUtils.getAuthUser() != null) {
							if (getVote(SecurityUtils.getAuthUser()) != null)
								return _T("Unvote");
							else
								return _T("Vote");
						} else {
							return _T("Login to vote");
						}
					}
					
				}));
			}
			
		};
		container.add(voteLink);
		
		return container;
	}

	private Component newExternalParticipantsContainer() {
		WebMarkupContainer container = new WebMarkupContainer("externalParticipants") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getIssue().getExternalParticipants().isEmpty());
			}
			
		};
		container.setOutputMarkupId(true);

		container.add(new ListView<>("externalParticipants", new LoadableDetachableModel<List<InternetAddress>>() {

			@Override
			protected List<InternetAddress> load() {
				var addresses = new ArrayList<>(getIssue().getExternalParticipants());
				addresses.sort((o1, o2) -> describe(o1, canManageIssues(getProject())).compareTo(describe(o2, canManageIssues(getProject()))));
				return addresses;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<InternetAddress> item) {
				item.add(new Label("label", describe(item.getModelObject(), canManageIssues(getProject()))));
				item.add(new AjaxLink<Void>("delete") {
					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener("'" + describe(item.getModelObject(), canManageIssues(getProject())) + "' will not be able to participate in this issue. Do you want to continue?"));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canManageIssues(getIssue().getProject()));
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						OneDev.getInstance(TransactionService.class).run(() -> {
							getIssue().getExternalParticipants().remove(item.getModelObject());
						});		
						target.add(container);
					}
				});
			}
			
		});

		return container;
	}
	
	private Project getProject() {
		return getIssue().getProject();
	}
	
	private IssueChangeService getIssueChangeService() {
		return OneDev.getInstance(IssueChangeService.class);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueSideCssResourceReference()));
	}

	protected abstract Issue getIssue();

	protected abstract Component newDeleteLink(String componentId);

	private void notifyIssueChange(IPartialPageRequestHandler handler, Issue issue) {
		((BasePage)getPage()).notifyObservablesChange(handler, issue.getChangeObservables(true));
	}
	
}
