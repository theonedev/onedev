package io.onedev.server.web.component.pullrequest.build;

import static io.onedev.server.model.Build.*;
import static io.onedev.server.search.entity.build.BuildQuery.getRuleName;
import static io.onedev.server.search.entity.build.BuildQueryLexer.And;
import static io.onedev.server.search.entity.build.BuildQueryLexer.Is;
import static io.onedev.server.util.criteria.Criteria.quote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Sets;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.build.simplelist.SimpleBuildListPanel;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;

@SuppressWarnings("serial")
public abstract class PullRequestJobsPanel extends Panel {

	public PullRequestJobsPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<JobBuildInfo>("jobs", new LoadableDetachableModel<List<JobBuildInfo>>() {

			@Override
			protected List<JobBuildInfo> load() {
				List<JobBuildInfo> listOfJobBuildInfo = new ArrayList<>();
				PullRequest request = getPullRequest();
				Map<String, List<Build>> map = new HashMap<>();
				for (Build build: request.getCurrentBuilds()) {
					String jobName = build.getJobName();
					List<Build> list = map.get(jobName);
					if (list == null) {
						list = new ArrayList<>();
						map.put(jobName, list);
					}
					list.add(build);
				}
				for (Map.Entry<String, List<Build>> entry: map.entrySet()) {
					String jobName = entry.getKey();
					if (SecurityUtils.canAccess(getPullRequest().getTargetProject(), jobName)) {
						List<Build> builds = new ArrayList<>(entry.getValue());
						Collections.sort(builds);
						boolean required = getPullRequest().getRequiredJobs().contains(jobName);
						listOfJobBuildInfo.add(new JobBuildInfo(jobName, required, builds));
					}
				}
				Collections.sort(listOfJobBuildInfo, new Comparator<JobBuildInfo>() {

					@Override
					public int compare(JobBuildInfo o1, JobBuildInfo o2) {
						return o1.getBuilds().iterator().next().getId().compareTo(o2.getBuilds().iterator().next().getId());
					}
					
				});
				return listOfJobBuildInfo;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<JobBuildInfo> item) {
				JobBuildInfo jobBuilds = item.getModelObject();

				String jobName = jobBuilds.getJobName();
				Status status = Status.getOverallStatus(jobBuilds.getBuilds()
						.stream()
						.map(it->it.getStatus())
						.collect(Collectors.toSet()));
				
				WebMarkupContainer link = new DropdownLink("job") {

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						return new SimpleBuildListPanel(id, new LoadableDetachableModel<List<Build>>() {

							@Override
							protected List<Build> load() {
								return item.getModelObject().getBuilds();
							}
							
						}) {
							
							@Override
							protected Component newListLink(String componentId) {
								MergePreview mergePreview = getPullRequest().getMergePreview();
								if (mergePreview != null && mergePreview.getMergeCommitHash() != null) {
									String query = "" 
											+ quote(NAME_PULL_REQUEST) + " " + getRuleName(Is) + " " + quote("#" + getPullRequest().getNumber())
											+ " " + getRuleName(And) + " "
											+ quote(NAME_COMMIT) + " " + getRuleName(Is) + " " + quote(mergePreview.getMergeCommitHash())
											+ " " + getRuleName(And) + " "
											+ quote(NAME_JOB) + " " + getRuleName(Is) + " " + quote(jobName);
									return new BookmarkablePageLink<Void>(componentId, ProjectBuildsPage.class, 
											ProjectBuildsPage.paramsOf(getPullRequest().getTargetProject(), query, 0));
								} else {
									return super.newListLink(componentId);
								}
							}
							
						};
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						String title;
						if (status != null) {
							if (status != Status.SUCCESSFUL)
								title = "Some builds are "; 
							else
								title = "Builds are "; 
							title += status.getDisplayName().toLowerCase() + ", click for details";
						} else {
							title = "No builds";
						}
						tag.put("title", title);
					}
					
				};
								
				link.add(new BuildStatusIcon("status", Model.of(status)));
				item.add(link);

				if (jobBuilds.isRequired()) 
					link.add(new Label("name", HtmlEscape.escapeHtml5(jobName) + " <span class='text-danger'>*</span>").setEscapeModelStrings(false));
				else
					link.add(new Label("name", jobName));
				item.add(link);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getModelObject().isEmpty());
			}
			
		});
		
		add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				handler.add(component);
			}
			
			@Override
			public Collection<String> getObservables() {
				return Sets.newHashSet(PullRequest.getWebSocketObservable(getPullRequest().getId()));
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new PullRequestJobsCssResourceReference()));
	}

	protected abstract PullRequest getPullRequest();

	private static class JobBuildInfo {
		
		private final String jobName;

		private final boolean required;
		
		private final List<Build> builds;
		
		public JobBuildInfo(String jobName, boolean required, List<Build> builds) {
			this.jobName = jobName;
			this.required = required;
			this.builds = builds;
		}

		public String getJobName() {
			return jobName;
		}

		public boolean isRequired() {
			return required;
		}

		public List<Build> getBuilds() {
			return builds;
		}
		
	}
	
}
