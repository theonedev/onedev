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
import org.apache.wicket.util.time.Duration;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.component.build.ParamValuesLabel;
import io.onedev.server.web.component.entity.nav.EntityNavPanel;
import io.onedev.server.web.component.job.JobDefLink;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.build.BuildListPage;
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
				return BuildQuery.parse(getProject(), queryString);
			}

			@Override
			protected Build getEntity() {
				return getBuild();
			}

			@Override
			protected List<Build> query(EntityQuery<Build> query, int offset, int count) {
				return getBuildManager().query(getProject(), query, offset, count);
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
		
		Link<Void> jobLink = new JobDefLink("job", getBuild().getCommitId(), getBuild().getJobName()) {

			@Override
			protected Project getProject() {
				return BuildSidePanel.this.getProject();
			}
			
		};
		jobLink.add(new Label("label", getBuild().getJobName()));
		general.add(jobLink);
		
		UserIdent submitter = UserIdent.of(getBuild().getSubmitter(), getBuild().getSubmitterName());
		general.add(new UserIdentPanel("submitter", submitter, UserIdentPanel.Mode.NAME));

		UserIdent canceller = UserIdent.of(getBuild().getCanceller(), getBuild().getCancellerName());
		general.add(new UserIdentPanel("canceller", canceller, UserIdentPanel.Mode.NAME) {

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
		
		String query = "depends on " + BuildQuery.quote(getBuild().getProject().getName() + "#" + getBuild().getNumber());
		Link<Void> dependentsLink = new BookmarkablePageLink<Void>("dependents", BuildListPage.class, 
				BuildListPage.paramsOf(query, 0, getBuild().getDependents().size()));
		dependentsLink.setVisible(!getBuild().getDependents().isEmpty());
		dependentsLink.add(new Label("label", getBuild().getDependents().size() + " build(s)"));
		
		dependencesContainer.add(dependentsLink);
		
		query = "dependencies of " + BuildQuery.quote(getBuild().getProject().getName() + "#" + getBuild().getNumber());
		Link<Void> dependenciesLink = new BookmarkablePageLink<Void>("dependencies", BuildListPage.class, 
				BuildListPage.paramsOf(query, 0, getBuild().getDependencies().size()));
		dependenciesLink.setVisible(!getBuild().getDependencies().isEmpty());
		dependenciesLink.add(new Label("label", getBuild().getDependencies().size() + " build(s)"));
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
