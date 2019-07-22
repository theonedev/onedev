package io.onedev.server.web.component.build.side;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.SecretInput;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.behavior.clipboard.CopyClipboardBehavior;
import io.onedev.server.web.component.entity.nav.EntityNavPanel;
import io.onedev.server.web.component.job.JobDefLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.util.QueryPositionSupport;

@SuppressWarnings("serial")
public abstract class BuildSidePanel extends Panel {

	public BuildSidePanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new EntityNavPanel<Build>("buildNav") {

			@Override
			protected EntityQuery<Build> parse(String queryString) {
				return BuildQuery.parse(getProject(), queryString, true);
			}

			@Override
			protected Build getEntity() {
				return getBuild();
			}

			@Override
			protected List<Build> query(EntityQuery<Build> query, int offset, int count) {
				return getBuildManager().query(getProject(), SecurityUtils.getUser(), query, offset, count);
			}

			@Override
			protected QueryPositionSupport<Build> getQueryPositionSupport() {
				return BuildSidePanel.this.getQueryPositionSupport();
			}
			
		});
		
		WebMarkupContainer general = new WebMarkupContainer("general");
		if (!getBuild().isFinished()) {
			general.add(new AjaxSelfUpdatingTimerBehavior(Duration.ONE_SECOND) {

				@Override
				protected void onPostProcessTarget(AjaxRequestTarget target) {
					super.onPostProcessTarget(target);
					if (getBuild().isFinished())
						stop(target);
				}
				
			});
		}
		general.setOutputMarkupId(true);
		add(general);
		
		RevCommit commit = getProject().getRevCommit(getBuild().getCommitHash(), true);
		CommitDetailPage.State commitState = new CommitDetailPage.State();
		commitState.revision = commit.name();
		PageParameters pageParams = CommitDetailPage.paramsOf(getProject(), commitState);
		
		Link<Void> hashLink = new ViewStateAwarePageLink<Void>("commitHash", CommitDetailPage.class, pageParams);
		hashLink.add(new Label("label", GitUtils.abbreviateSHA(commit.name())));
		general.add(hashLink);
		
		general.add(new WebMarkupContainer("copyCommitHash").add(new CopyClipboardBehavior(Model.of(commit.name()))));
		
		Link<Void> jobLink = new JobDefLink("job", getProject(), getBuild().getCommitId(), getBuild().getJobName());
		jobLink.add(new Label("label", getBuild().getJobName()));
		general.add(jobLink);
		
		UserIdent submitter = UserIdent.of(UserFacade.of(getBuild().getSubmitter()), getBuild().getSubmitterName());
		general.add(new UserIdentPanel("submitter", submitter, UserIdentPanel.Mode.AVATAR_AND_NAME));

		UserIdent canceller = UserIdent.of(UserFacade.of(getBuild().getCanceller()), getBuild().getCancellerName());
		general.add(new UserIdentPanel("canceller", canceller, UserIdentPanel.Mode.AVATAR_AND_NAME) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().getCanceller() != null || getBuild().getCancellerName() != null);
			}
			
		});
		
		general.add(new Label("submitDate", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return DateUtils.formatAge(getBuild().getSubmitDate());
			}
			
		}));
		
		general.add(new Label("waitingInQueue", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				long duration;
				if (getBuild().getRunningDate() != null)
					duration = getBuild().getRunningDate().getTime() - getBuild().getPendingDate().getTime();
				else
					duration = System.currentTimeMillis() - getBuild().getPendingDate().getTime(); 
				if (duration < 0)
					duration = 0;
				return DateUtils.formatDuration(duration);
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().getPendingDate() != null 
						&& (!getBuild().isFinished() || getBuild().getRunningDate() != null));
			}
			
		});
		
		general.add(new Label("runningTakes", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				long duration;
				if (getBuild().getFinishDate() != null)
					duration = getBuild().getFinishDate().getTime() - getBuild().getRunningDate().getTime();
				else
					duration = System.currentTimeMillis() - getBuild().getRunningDate().getTime(); 
				if (duration < 0)
					duration = 0;
				return DateUtils.formatDuration(duration);
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().getRunningDate() != null);
			}
			
		});
		
		add(new ListView<BuildParam>("params", new LoadableDetachableModel<List<BuildParam>>() {

			@Override
			protected List<BuildParam> load() {
				List<BuildParam> params = new ArrayList<>();
				for (Map.Entry<String, Input> entry: getBuild().getParamInputs().entrySet()) {
					if (getBuild().isParamVisible(entry.getKey())) {
						if (entry.getValue().getValues().size() > 1) {
							int i = 1;
							for (String value: entry.getValue().getValues()) {
								BuildParam param = new BuildParam();
								param.setName(entry.getKey() + "_" + (i++));
								param.setType(entry.getValue().getType());
								param.setValue(value);
								params.add(param);
							}
						} else {
							BuildParam param = new BuildParam();
							param.setName(entry.getKey());
							param.setType(entry.getValue().getType());
							if (entry.getValue().getValues().size() == 1)
								param.setValue(entry.getValue().getValues().iterator().next());
							params.add(param);
						}
					}
				}
				return params;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<BuildParam> item) {
				BuildParam param = item.getModelObject();
				item.add(new Label("name", param.getName()));
				if (param.getType().equals(InputSpec.SECRET))
					item.add(new Label("value", SecretInput.MASK));
				else
					item.add(new Label("value", param.getValue()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getBuild().getParams().isEmpty());
			}
			
		});
		
		WebMarkupContainer dependencesContainer = new WebMarkupContainer("dependences");
		add(dependencesContainer);
		
		Link<Void> dependentsLink = new BookmarkablePageLink<Void>("dependents", ProjectBuildsPage.class, 
				ProjectBuildsPage.paramsOf(getProject(), "depends on " + BuildQuery.quote("#" + getBuild().getNumber()), 0));
		dependentsLink.setVisible(!getBuild().getDependents().isEmpty());
		
		if (getBuild().getDependents().size() > 1)
			dependentsLink.add(new Label("label", getBuild().getDependents().size() + " builds"));
		else
			dependentsLink.add(new Label("label", getBuild().getDependents().size() + " build"));
		
		dependencesContainer.add(dependentsLink);
		
		Link<Void> dependenciesLink = new BookmarkablePageLink<Void>("dependencies", ProjectBuildsPage.class, 
				ProjectBuildsPage.paramsOf(getProject(), "dependencies of " + BuildQuery.quote("#" + getBuild().getNumber()), 0));
		dependenciesLink.setVisible(!getBuild().getDependencies().isEmpty());
		if (getBuild().getDependencies().size() > 1)
			dependenciesLink.add(new Label("label", getBuild().getDependencies().size() + " builds"));
		else
			dependenciesLink.add(new Label("label", getBuild().getDependencies().size() + " build"));
		dependencesContainer.add(dependenciesLink);
		
		WebMarkupContainer comma = new WebMarkupContainer("comma");
		comma.setVisible(dependenciesLink.isVisible() && dependentsLink.isVisible());
		dependencesContainer.add(comma);
		
		dependencesContainer.setVisible(dependentsLink.isVisible() || dependenciesLink.isVisible());
		
		add(newDeleteLink("delete"));
		
		setOutputMarkupId(true);
	}

	private Project getProject() {
		return getBuild().getProject();
	}
	
	private BuildManager getBuildManager() {
		return OneDev.getInstance(BuildManager.class);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BuildSideCssResourceReference()));
	}

	protected abstract Build getBuild();

	@Nullable
	protected abstract QueryPositionSupport<Build> getQueryPositionSupport();
	
	protected abstract Component newDeleteLink(String componentId);
	
}
