package io.onedev.server.web.page.project.builds.detail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.ci.job.JobScheduler;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.behavior.clipboard.CopyClipboardBehavior;
import io.onedev.server.web.component.build.BuildTitleLabel;
import io.onedev.server.web.component.build.side.BuildSidePanel;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.component.build.status.BuildStatusLabel;
import io.onedev.server.web.component.commit.message.CommitMessagePanel;
import io.onedev.server.web.component.contributorpanel.ContributorPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.sideinfo.SideInfoClosed;
import io.onedev.server.web.component.sideinfo.SideInfoOpened;
import io.onedev.server.web.component.sideinfo.SideInfoPanel;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.component.user.contributoravatars.ContributorAvatars;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.issues.list.IssueListPage;
import io.onedev.server.web.util.ConfirmOnClick;
import io.onedev.server.web.util.QueryPosition;
import io.onedev.server.web.util.QueryPositionSupport;

@SuppressWarnings("serial")
public abstract class BuildDetailPage extends ProjectPage {

	public static final String PARAM_BUILD = "build";
	
	protected final IModel<Build> buildModel;
	
	final QueryPosition position;
	
	public BuildDetailPage(PageParameters params) {
		super(params);
		
		buildModel = new LoadableDetachableModel<Build>() {

			@Override
			protected Build load() {
				Long buildNumber = params.get(PARAM_BUILD).toLong();
				Build build = OneDev.getInstance(BuildManager.class).find(getProject(), buildNumber);
				if (build == null)
					throw new EntityNotFoundException("Unable to find build #" + buildNumber + " in project " + getProject());
				return build;
			}

		};
	
		position = QueryPosition.from(params);
	}
	
	protected Build getBuild() {
		return buildModel.getObject();
	}
	
	private WebSocketObserver newBuildObserver(Long buildId) {
		return new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, String observable) {
				handler.add(component);
			}
			
			@Override
			public void onConnectionOpened(IPartialPageRequestHandler handler) {
				handler.add(component);
			}
			
			@Override
			public Collection<String> getObservables() {
				return Sets.newHashSet(Build.getWebSocketObservable(buildId));
			}
			
		};
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer summary = new WebMarkupContainer("summary");
		summary.add(newBuildObserver(getBuild().getId()));
		summary.setOutputMarkupId(true);
		add(summary);
		
		summary.add(new BuildTitleLabel("title", buildModel));
		
		summary.add(new BuildStatusIcon("statusIcon", buildModel));
		summary.add(new BuildStatusLabel("statusLabel", buildModel));
		
		summary.add(new Link<Void>("rebuild") {

			@Override
			public void onClick() {
				OneDev.getInstance(JobScheduler.class).resubmit(getBuild());
				getSession().success("Rebuild request submitted");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().isFinished());
			}
			
		}.add(new ConfirmOnClick("Do you really want to rebuild?")));
		
		summary.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				OneDev.getInstance(JobScheduler.class).cancel(getBuild());
				getSession().success("Cancel request submitted");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getBuild().isFinished());
			}
			
		}.add(new ConfirmOnClick("Do you really want to cancel this build?")));
		
		summary.add(new AjaxLink<Void>("moreInfo") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof SideInfoClosed) {
					SideInfoClosed moreInfoSideClosed = (SideInfoClosed) event.getPayload();
					setVisible(true);
					moreInfoSideClosed.getHandler().add(this);
				}
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				setVisible(false);
				target.add(this);
				send(getPage(), Broadcast.BREADTH, new SideInfoOpened(target));
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		add(new Label("statusMessage", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getBuild().getStatusMessage();
			}
			
		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(newBuildObserver(getBuild().getId()));
				add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						switch (getBuild().getStatus()) {
						case FAILED:
						case IN_ERROR:
						case TIMED_OUT:
						case CANCELLED:
							return "alert-danger";
						case SUCCESSFUL:
							return "alert-success";
						case WAITING:
						case QUEUEING:
						case RUNNING:
							return "alert-warning";
						default:
							throw new OneException("Unexpected build status: " + getBuild().getStatus());
						}
					}
					
				}));
				
				setOutputMarkupPlaceholderTag(true);
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildDetail.onStatusMessageDomReady();"));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().getStatusMessage() != null);
			}
			
		});
		
		CommitDetailPage.State commitState = new CommitDetailPage.State();
		commitState.revision = getBuild().getCommitHash();
		PageParameters params = CommitDetailPage.paramsOf(projectModel.getObject(), commitState);
		
		add(new CommitMessagePanel("commitMessage", projectModel, new AbstractReadOnlyModel<RevCommit>() {

			@Override
			public RevCommit getObject() {
				return getProject().getRevCommit(getBuild().getCommitHash(), true);
			}
			
		}));
		
		RevCommit commit = getProject().getRevCommit(getBuild().getCommitHash(), true);
		add(new ContributorAvatars("commitAvatars", commit.getAuthorIdent(), commit.getCommitterIdent()));
		add(new ContributorPanel("commitNames", commit.getAuthorIdent(), commit.getCommitterIdent()));
		
		Link<Void> hashLink = new ViewStateAwarePageLink<Void>("commitHash", CommitDetailPage.class, params);
		hashLink.add(new Label("label", GitUtils.abbreviateSHA(commit.name())));
		add(hashLink);
		
		add(new WebMarkupContainer("copyCommitHash").add(new CopyClipboardBehavior(Model.of(commit.name()))));

		List<Tab> tabs = new ArrayList<>();

		tabs.add(new BuildTab("Log", BuildLogPage.class) {

			@Override
			protected Component renderOptions(String componentId) {
				BuildLogPage page = (BuildLogPage) getPage();
				return page.renderOptions(componentId);
			}
			
		});
		tabs.add(new BuildTab("Fixed Issues", FixedIssuesPage.class));
		tabs.add(new BuildTab("Changes", BuildChangesPage.class));
		
		List<BuildTabContribution> contributions = new ArrayList<>(OneDev.getExtensions(BuildTabContribution.class));
		contributions.sort(Comparator.comparing(BuildTabContribution::getOrder));
		
		for (BuildTabContribution contribution: contributions)
			tabs.addAll(contribution.getTabs(getBuild()));
		
		add(new Tabbable("buildTabs", tabs).setOutputMarkupId(true));
		
		add(new SideInfoPanel("side") {

			@Override
			protected Component newContent(String componentId) {
				return new BuildSidePanel(componentId) {

					@Override
					protected Build getBuild() {
						return BuildDetailPage.this.getBuild();
					}

					@Override
					protected QueryPositionSupport<Build> getQueryPositionSupport() {
						return new QueryPositionSupport<Build>() {

							@Override
							public QueryPosition getPosition() {
								return position;
							}

							@Override
							public void navTo(AjaxRequestTarget target, Build entity, QueryPosition position) {
								PageParameters params = BuildDetailPage.paramsOf(entity, position);
								setResponsePage(getPageClass(), params);
							}
							
						};
					}

					@Override
					protected Component newDeleteLink(String componentId) {
						Link<Void> deleteLink = new Link<Void>(componentId) {

							@Override
							public void onClick() {
								OneDev.getInstance(BuildManager.class).delete(getBuild());
								PageParameters params = IssueListPage.paramsOf(
										getProject(), 
										QueryPosition.getQuery(position), 
										QueryPosition.getPage(position) + 1); 
								setResponsePage(ProjectBuildsPage.class, params);
							}
							
						};
						deleteLink.add(new ConfirmOnClick("Do you really want to delete this build?"));
						deleteLink.setVisible(SecurityUtils.canAdministrate(getBuild().getProject().getFacade()));
						return deleteLink;
					}
					
				};
			}
			
		});
		
	}
	
	public QueryPosition getPosition() {
		return position;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new BuildDetailResourceReference()));
	}

	@Override
	protected void onDetach() {
		buildModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(Build build, @Nullable QueryPosition position) {
		PageParameters params = ProjectPage.paramsOf(build.getProject());
		params.add(PARAM_BUILD, build.getNumber());
		if (position != null)
			position.fill(params);
		return params;
	}
	
}
