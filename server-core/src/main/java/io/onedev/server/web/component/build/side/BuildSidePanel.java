package io.onedev.server.web.component.build.side;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.Referenceable;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.build.ParamValuesLabel;
import io.onedev.server.web.component.entity.reference.ReferencePanel;
import io.onedev.server.web.component.job.JobDefLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.pullrequest.RequestStatusBadge;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.builds.BuildListPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;

@SuppressWarnings("serial")
public abstract class BuildSidePanel extends Panel {

	public BuildSidePanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer general = new WebMarkupContainer("general") {

			@Override
			protected void onBeforeRender() {
				User submitter = User.from(getBuild().getSubmitter(), getBuild().getSubmitterName());
				addOrReplace(new UserIdentPanel("submitter", submitter, Mode.NAME));
				User canceller = User.from(getBuild().getCanceller(), getBuild().getCancellerName());
				UserIdentPanel cancellerIdentPanel = new UserIdentPanel("canceller", canceller, Mode.NAME);				
				cancellerIdentPanel.setVisible(getBuild().getStatus() == Build.Status.CANCELLED 
						&& (getBuild().getCanceller() != null || getBuild().getCancellerName() != null));
				addOrReplace(cancellerIdentPanel);
				super.onBeforeRender();
			}
			
		};
		
		general.setOutputMarkupId(true);
		
		add(general);

		String branch = getBuild().getBranch();
		general.add(new Label("branch", branch).setVisible(branch != null));
		
		String tag = getBuild().getTag();
		general.add(new Label("tag", tag).setVisible(tag != null));
		
		CommitDetailPage.State commitState = new CommitDetailPage.State();
		commitState.revision = getBuild().getCommitHash();
		PageParameters params = CommitDetailPage.paramsOf(getProject(), commitState);
		
		Link<Void> hashLink = new ViewStateAwarePageLink<Void>("commit", CommitDetailPage.class, params) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (!SecurityUtils.canReadCode(getProject()))
					tag.setName("span");
			}
			
		};
		hashLink.setEnabled(SecurityUtils.canReadCode(getProject()));
		hashLink.add(new Label("label", GitUtils.abbreviateSHA(getBuild().getCommitHash())));
		general.add(hashLink);
		
		Link<Void> jobLink = new JobDefLink("job", getBuild().getCommitId(), getBuild().getJobName()) {

			@Override
			protected Project getProject() {
				return BuildSidePanel.this.getProject();
			}
			
		};
		jobLink.add(new Label("label", getBuild().getJobName()));
		general.add(jobLink);
		
		general.add(new Label("submitDate", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return DateUtils.formatAge(getBuild().getSubmitDate());
			}
			
		}));
		general.add(new Label("submitReason", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return getBuild().getSubmitReason();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().getSubmitReason() != null);
			}
			
		});
		general.add(new Label("retryDate", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return DateUtils.formatAge(getBuild().getRetryDate());
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().getRetryDate() != null);
			}
			
		});
		
		general.add(new Label("queueingTakes", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return DateUtils.formatDuration(getBuild().getRunningDate().getTime() - getBuild().getPendingDate().getTime());
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().getPendingDate() != null && getBuild().getRunningDate() != null);
			}
			
		});
		
		general.add(new Label("runningTakes", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return DateUtils.formatDuration(getBuild().getFinishDate().getTime() - getBuild().getRunningDate().getTime());
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().getRunningDate() != null && getBuild().getFinishDate() != null);
			}
			
		});
		
		general.add(new WebSocketObserver() {
				
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				handler.add(component);
			}
			
			@Override
			public Collection<String> getObservables() {
				return Sets.newHashSet(Build.getWebSocketObservable(getBuild().getId()));
			}
			
		});
		
		add(new ListView<Input>("params", new LoadableDetachableModel<List<Input>>() {

			@Override
			protected List<Input> load() {
				List<Input> params = new ArrayList<>();
				for (Map.Entry<String, Input> entry: getBuild().getParamInputs().entrySet()) {
					if (getBuild().isParamVisible(entry.getKey())) 
						params.add(entry.getValue());
				}
				return params;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Input> item) {
				Input param = item.getModelObject();
				item.add(new Label("name", param.getName()));
				item.add(new ParamValuesLabel("value", param));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getModelObject().isEmpty());
			}
			
		});
		
		WebMarkupContainer dependencesContainer = new WebMarkupContainer("dependences");
		add(dependencesContainer);
		
		String query = "depends on " + Criteria.quote(getBuild().getProject().getName() + "#" + getBuild().getNumber());
		Link<Void> dependentsLink = new BookmarkablePageLink<Void>("dependents", BuildListPage.class, 
				BuildListPage.paramsOf(query, 0, getBuild().getDependents().size()));
		dependentsLink.setVisible(!getBuild().getDependents().isEmpty());
		dependentsLink.add(new Label("label", getBuild().getDependents().size() + " build(s)"));
		
		dependencesContainer.add(dependentsLink);
		
		query = "dependencies of " + Criteria.quote(getBuild().getProject().getName() + "#" + getBuild().getNumber());
		Link<Void> dependenciesLink = new BookmarkablePageLink<Void>("dependencies", BuildListPage.class, 
				BuildListPage.paramsOf(query, 0, getBuild().getDependencies().size()));
		dependenciesLink.setVisible(!getBuild().getDependencies().isEmpty());
		dependenciesLink.add(new Label("label", getBuild().getDependencies().size() + " build(s)"));
		dependencesContainer.add(dependenciesLink);
		
		WebMarkupContainer comma = new WebMarkupContainer("comma");
		comma.setVisible(dependenciesLink.isVisible() && dependentsLink.isVisible());
		dependencesContainer.add(comma);
		
		dependencesContainer.setVisible(dependentsLink.isVisible() || dependenciesLink.isVisible());
		
		add(new WebMarkupContainer("pullRequest") {

			@Override
			protected void onInitialize() {
				super.onInitialize();

				PullRequest request = getBuild().getRequest();

				if (request != null) {
					Link<Void> link = new ViewStateAwarePageLink<Void>("title", 
							PullRequestActivitiesPage.class, 
							PullRequestActivitiesPage.paramsOf(request));
					link.add(new Label("label", "#" + request.getNumber() + " " + request.getTitle()));
					add(link);
					add(new RequestStatusBadge("status", new AbstractReadOnlyModel<PullRequest>() {
	
						@Override
						public PullRequest getObject() {
							return getBuild().getRequest();
						}
						
					}));
				} else {
					add(new WebMarkupContainer("title").add(new WebMarkupContainer("label")));
					add(new WebMarkupContainer("status"));
				}
			}
			
			@Override
			protected void onConfigure() {
				setVisible(getBuild().getRequest() != null && SecurityUtils.canReadCode(getProject()));
			}
			
		});

		add(new ReferencePanel("reference") {

			@Override
			protected Referenceable getReferenceable() {
				return getBuild();
			}
			
		});
		
		if (SecurityUtils.canManage(getBuild()))
			add(newDeleteLink("delete"));
		else
			add(new WebMarkupContainer("delete").setVisible(false));
		
		setOutputMarkupId(true);
	}

	private Project getProject() {
		return getBuild().getProject();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BuildSideCssResourceReference()));
	}

	protected abstract Build getBuild();

	protected abstract Component newDeleteLink(String componentId);
	
}
