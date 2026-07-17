package io.onedev.server.web.page.project.overview;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.replica.ProjectReplica;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.search.entity.project.ProjectQueryLexer;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.PackService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.ParsedEmailAddress;
import io.onedev.server.util.ProjectBuildStatusStat;
import io.onedev.server.util.ProjectIssueStateStat;
import io.onedev.server.util.ProjectPackTypeStat;
import io.onedev.server.util.ProjectPullRequestStatusStat;
import io.onedev.server.util.ProjectWorkspaceStatusStat;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.entity.labels.EntityLabelsPanel;
import io.onedev.server.web.component.issue.statestats.StateStatsBar;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.project.ProjectAvatar;
import io.onedev.server.web.component.project.forkoption.ForkOptionPanel;
import io.onedev.server.web.component.project.list.ProjectListPanel;
import io.onedev.server.web.component.project.stats.build.BuildStatsPanel;
import io.onedev.server.web.component.project.stats.code.CodeStatsPanel;
import io.onedev.server.web.component.project.stats.code.LanguageStatsPanel;
import io.onedev.server.web.component.project.stats.issue.IssueStatsPanel;
import io.onedev.server.web.component.project.stats.pack.PackStatsPanel;
import io.onedev.server.web.component.project.stats.pullrequest.PullRequestStatsPanel;
import io.onedev.server.web.component.project.stats.workspace.WorkspaceStatsPanel;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.iteration.IterationIssuesPage;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.web.util.paginghistory.ParamPagingHistorySupport;
import io.onedev.server.workspace.WorkspaceService;
import io.onedev.server.xodus.CommitInfoService;

import com.google.common.collect.Lists;

public class ProjectOverviewPage extends ProjectPage {

	private static final Logger logger = LoggerFactory.getLogger(ProjectOverviewPage.class);

	private static final String PARAM_CHILD_PAGE = "child-page";

	private static final String PARAM_CHILD_QUERY = "child-query";

	private String childrenQuery;

	private Component childrenList;

	private final IModel<Map<String, ProjectReplica>> replicasModel = new LoadableDetachableModel<>() {

		@Override
		protected Map<String, ProjectReplica> load() {
			return getProjectService().getReplicas(getProject().getId());
		}

	};

	private final IModel<Map<Integer, Long>> issueStatsModel = new LoadableDetachableModel<>() {

		@Override
		protected Map<Integer, Long> load() {
			Map<Integer, Long> stateCounts = new LinkedHashMap<>();
			GlobalIssueSetting issueSetting = getSettingService().getIssueSetting();
			for (ProjectIssueStateStat stats : getIssueService().queryStateStats(
					SecurityUtils.getSubject(), Lists.newArrayList(getProject()))) {
				if (stats.getStateOrdinal() >= 0
						&& stats.getStateOrdinal() < issueSetting.getStateSpecs().size()) {
					stateCounts.put(stats.getStateOrdinal(), stats.getStateCount());
				}
			}
			return stateCounts;
		}

	};

	private final IModel<Map<Build.Status, Long>> buildStatsModel = new LoadableDetachableModel<>() {

		@Override
		protected Map<Build.Status, Long> load() {
			Map<Build.Status, Long> statusCounts = new LinkedHashMap<>();
			for (ProjectBuildStatusStat stats : getBuildService().queryStatusStats(Lists.newArrayList(getProject())))
				statusCounts.put(stats.getBuildStatus(), stats.getStatusCount());
			return statusCounts;
		}

	};

	private final IModel<Map<PullRequest.Status, Long>> pullRequestStatsModel = new LoadableDetachableModel<>() {

		@Override
		protected Map<PullRequest.Status, Long> load() {
			Map<PullRequest.Status, Long> statusCounts = new LinkedHashMap<>();
			for (ProjectPullRequestStatusStat stats : getPullRequestService().queryStatusStats(Lists.newArrayList(getProject())))
				statusCounts.put(stats.getPullRequestStatus(), stats.getStatusCount());
			return statusCounts;
		}

	};

	private final IModel<Map<Workspace.Status, Long>> workspaceStatsModel = new LoadableDetachableModel<>() {

		@Override
		protected Map<Workspace.Status, Long> load() {
			Map<Workspace.Status, Long> statusCounts = new LinkedHashMap<>();
			for (ProjectWorkspaceStatusStat stats : getWorkspaceService().queryStatusStats(Lists.newArrayList(getProject())))
				statusCounts.put(stats.getWorkspaceStatus(), stats.getStatusCount());
			return statusCounts;
		}

	};

	private final IModel<Map<String, Long>> packStatsModel = new LoadableDetachableModel<>() {

		@Override
		protected Map<String, Long> load() {
			Map<String, Long> typeCounts = new LinkedHashMap<>();
			for (ProjectPackTypeStat stats : getPackService().queryTypeStats(Lists.newArrayList(getProject())))
				typeCounts.put(stats.getType(), stats.getTypeCount());
			return typeCounts;
		}

	};

	private final IModel<Iteration> nextIterationModel = new LoadableDetachableModel<>() {

		@Override
		protected Iteration load() {
			return getProject().getSortedHierarchyIterations().stream()
					.filter(it -> !it.isClosed())
					.findFirst()
					.orElse(null);
		}

	};

	public ProjectOverviewPage(PageParameters params) {
		super(params);
		childrenQuery = params.get(PARAM_CHILD_QUERY).toOptionalString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var summary = newSummary("summary");
		add(summary);
		summary.add(newEntityStats("entityStats"));
		summary.add(newReplicas("replicas"));
		add(newLanguageStats("languageStats"));
		add(newNextIteration("nextIteration"));

		WebMarkupContainer children = new WebMarkupContainer("children") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProjectService().hasChildren(getProject().getId())
						|| SecurityUtils.canCreateChildren(getProject()));
			}

		};
		add(children);
		children.add(childrenList = new ProjectListPanel("body", new IModel<>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return childrenQuery;
			}

			@Override
			public void setObject(String object) {
				childrenQuery = object;
				PageParameters params = getPageParameters();
				params.set(PARAM_CHILD_QUERY, childrenQuery);
				params.remove(PARAM_CHILD_PAGE);
				CharSequence url = RequestCycle.get().urlFor(ProjectOverviewPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), childrenQuery);
			}

		}, 0) {

			@Override
			protected Project getParentProject() {
				return getProject();
			}

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new ParamPagingHistorySupport() {

					@Override
					public PageParameters newPageParameters(int currentPage) {
						return paramsOf(getProject(), childrenQuery, currentPage + 1);
					}

					@Override
					public int getCurrentPage() {
						return getPageParameters().get(PARAM_CHILD_PAGE).toInt(1) - 1;
					}

				};
			}

		});
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		childrenQuery = (String) data;
		getPageParameters().set(PARAM_CHILD_QUERY, childrenQuery);
		target.add(childrenList);
	}

	public static PageParameters paramsOf(Project project, @Nullable String childQuery, int childPage) {
		PageParameters params = paramsOf(project);
		if (childQuery != null)
			params.add(PARAM_CHILD_QUERY, childQuery);
		if (childPage != 0)
			params.add(PARAM_CHILD_PAGE, childPage);
		return params;
	}

	private WebMarkupContainer newSummary(String componentId) {
		WebMarkupContainer container = new WebMarkupContainer(componentId);

		container.add(new ProjectAvatar("avatar", getProject().getId()));
		container.add(new Label("path", getProject().getPath()));
		container.add(new EntityLabelsPanel<>("labels", projectModel));
		container.add(new Label("id", "id: " + getProject().getId()));
		container.add(new Label("key", " · key: " + getProject().getKey())
				.setVisible(getProject().getKey() != null));

		if (getProject().getForkedFrom() != null) {
			container.add(new BookmarkablePageLink<Void>("forkedFrom", ProjectOverviewPage.class,
					ProjectOverviewPage.paramsOf(getProject().getForkedFrom())) {

				@Override
				public IModel<?> getBody() {
					return Model.of(getProject().getForkedFrom().getPath());
				}

			});
		} else {
			container.add(new WebMarkupContainer("forkedFrom").setVisible(false));
		}

		WebMarkupContainer forkInfo = new WebMarkupContainer("forkInfo");
		forkInfo.setVisible(getProject().isCodeManagement());
		container.add(forkInfo);

		String query = ProjectQuery.getRuleName(ProjectQueryLexer.ForksOf) + " "
				+ Criteria.quote(getProject().getPath());
		PageParameters params = ProjectListPage.paramsOf(query, 0, getProject().getForks().size());
		Link<Void> forksLink = new BookmarkablePageLink<>("forks", ProjectListPage.class, params);
		forksLink.add(new Label("label", MessageFormat.format(_T("{0} forks"),
				String.valueOf(getProject().getForks().size()))));
		forkInfo.add(forksLink);

		ModalLink forkNow = new ModalLink("forkNow") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new ForkOptionPanel(id, projectModel) {

					@Override
					protected void onClose(AjaxRequestTarget target) {
						modal.close();
					}

				};
			}

		};
		var activeServer = getProject().getActiveServer(false);
		forkNow.setVisible(SecurityUtils.getAuthUser() != null
				&& SecurityUtils.canReadCode(getProject())
				&& activeServer != null);
		forkInfo.add(forkNow);

		SettingService settingService = getSettingService();
		if (settingService.getServiceDeskSetting() != null
				&& settingService.getMailConnector() != null
				&& settingService.getMailConnector().getInboxMonitor(false) != null
				&& getProject().isIssueManagement()) {

			String serviceDeskEmailAddress;
			if (getProject().getServiceDeskEmailAddress() != null) {
				serviceDeskEmailAddress = getProject().getServiceDeskEmailAddress();
			} else {
				ParsedEmailAddress systemAddress = ParsedEmailAddress.parse(
						settingService.getMailConnector().getSystemAddress());
				serviceDeskEmailAddress = systemAddress.getSubaddress(getProject().getPath());
			}

			container.add(new WebMarkupContainer("serviceDesk") {

				@Override
				protected void onInitialize() {
					super.onInitialize();
					add(new Label("label", serviceDeskEmailAddress));
				}

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.put("href", "mailto:" + serviceDeskEmailAddress);
				}

			});
		} else {
			container.add(new WebMarkupContainer("serviceDesk").setVisible(false));
		}

		if (getProject().getDescription() != null)
			container.add(new MultilineLabel("description", Model.of(getProject().getDescription())));
		else
			container.add(new WebMarkupContainer("description").setVisible(false));

		return container;
	}

	private Component newReplicas(String componentId) {
		var activeServer = getProject().getActiveServer(false);
		long latestVersion;
		if (activeServer != null) {
			var replica = replicasModel.getObject().get(activeServer);
			if (replica != null)
				latestVersion = replica.getVersion();
			else
				latestVersion = -1;
		} else {
			latestVersion = -1;
		}

		long latestVersionFinal = latestVersion;
		String activeServerFinal = activeServer;

		Fragment fragment = new Fragment(componentId, "replicasFrag", this) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				Component replicas = get("replicas");
				replicas.configure();
				setVisible(replicas.determineVisibility());
			}

		};

		fragment.add(new ListView<Map.Entry<String, ProjectReplica>>("replicas", new LoadableDetachableModel<>() {

			@Override
			protected List<Map.Entry<String, ProjectReplica>> load() {
				var replicas = new ArrayList<>(replicasModel.getObject().entrySet());
				var orders = new HashMap<String, Integer>();
				var index = 0;
				for (var server : getClusterService().getServerAddresses())
					orders.put(server, index++);
				return replicas.stream()
						.filter(it -> orders.containsKey(it.getKey())
								&& (it.getValue().getType() != ProjectReplica.Type.REDUNDANT
								|| it.getKey().equals(activeServerFinal)))
						.sorted(Comparator.comparingInt(o -> orders.get(o.getKey())))
						.collect(Collectors.toList());
			}

		}) {

			@Override
			protected void populateItem(ListItem<Map.Entry<String, ProjectReplica>> item) {
				var server = item.getModelObject().getKey();
				var replica = item.getModelObject().getValue();
				var escapedServer = HtmlEscape.escapeHtml5(server + " (" + getClusterService().getServerName(server) + ")");
				String serverInfo;
				if (server.equals(activeServerFinal))
					serverInfo = escapedServer + " <span class='badge badge-sm badge-info ml-1'>" + _T("active replica") + "</span>";
				else if (replica.getVersion() == latestVersionFinal)
					serverInfo = escapedServer + " <span class='badge badge-sm badge-success ml-1'>" + _T("up to date replica") + "</span>";
				else
					serverInfo = escapedServer + " <span class='badge badge-sm badge-warning ml-1'>" + _T("outdated replica") + "</span>";
				item.add(new Label("server", serverInfo).setEscapeModelStrings(false));

				var projectId = getProject().getId();
				item.add(new AjaxLink<Void>("sync") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						getClusterService().submitToServer(server, () -> {
							try {
								getProjectService().requestToSyncReplica(projectId, activeServerFinal);
							} catch (Throwable e) {
								logger.error(MessageFormat.format(
										"Error requestig to sync replica of project with id \"{0}\"",
										String.valueOf(projectId)), e);
							}
							return null;
						});
						Session.get().success(_T("Sync requested. Please check status after a while"));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canManageProject(getProject())
								&& item.getModelObject().getValue().getVersion() < latestVersionFinal);
					}

				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManageProject(getProject())
						&& latestVersionFinal != -1
						&& getClusterService().isClusteringSupported());
			}

		});

		return fragment;
	}

	private Component newEntityStats(String componentId) {
		Fragment fragment = new Fragment(componentId, "entityStatsFrag", this) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				boolean visible = false;
				for (String id : new String[] {"codeStats", "pullRequestStats", "workspaceStats",
						"issueStats", "buildStats", "packStats"}) {
					Component child = get(id);
					child.configure();
					if (child.determineVisibility()) {
						visible = true;
						break;
					}
				}
				setVisible(visible);
			}

		};

		fragment.add(new CodeStatsPanel("codeStats", projectModel) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject().getActiveServer(false) != null
						&& getProject().isCodeManagement()
						&& SecurityUtils.canReadCode(getProject())
						&& isVisible());
			}

		});
		fragment.add(new PullRequestStatsPanel("pullRequestStats", projectModel, pullRequestStatsModel) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject().getActiveServer(false) != null
						&& getProject().isCodeManagement()
						&& SecurityUtils.canReadCode(getProject())
						&& isVisible());
			}

		});
		fragment.add(new WorkspaceStatsPanel("workspaceStats", projectModel, workspaceStatsModel) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject().getActiveServer(false) != null
						&& getProject().isCodeManagement()
						&& SecurityUtils.canWriteCode(getProject())
						&& !getProject().getHierarchyWorkspaceSpecs().isEmpty()
						&& isVisible());
			}

		});
		fragment.add(new IssueStatsPanel("issueStats", projectModel, issueStatsModel) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject().getActiveServer(false) != null
						&& getProject().isIssueManagement()
						&& isVisible());
			}

		});
		fragment.add(new BuildStatsPanel("buildStats", projectModel, buildStatsModel) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject().getActiveServer(false) != null
						&& getProject().isCodeManagement()
						&& isVisible());
			}

		});
		fragment.add(new PackStatsPanel("packStats", projectModel, packStatsModel) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject().getActiveServer(false) != null
						&& getProject().isPackManagement()
						&& SecurityUtils.canReadPack(getProject())
						&& isVisible());
			}

		});

		return fragment;
	}

	private Component newLanguageStats(String componentId) {
		Fragment fragment = new Fragment(componentId, "languageStatsFrag", this) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				if (getProject().getActiveServer(false) == null
						|| !getProject().isCodeManagement()
						|| !SecurityUtils.canReadCode(getProject())) {
					setVisible(false);
				} else {
					Component statsBar = get("statsBar");
					statsBar.configure();
					setVisible(statsBar.determineVisibility());
				}
			}

		};

		fragment.add(new Label("title", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				int sloc = OneDev.getInstance(CommitInfoService.class)
						.getLineStats(getProject().getId()).values()
						.stream().mapToInt(Integer::intValue).sum();
				return MessageFormat.format(_T("{0} Lines"),
						NumberFormat.getIntegerInstance().format(sloc));
			}

		}));
		fragment.add(new LanguageStatsPanel("statsBar", projectModel));
		return fragment;
	}

	private Component newNextIteration(String componentId) {
		Fragment fragment = new Fragment(componentId, "nextIterationFrag", this) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject().isIssueManagement() && nextIterationModel.getObject() != null);
			}

		};

		Iteration iteration = nextIterationModel.getObject();
		Link<Void> titleLink;
		if (iteration != null) {
			titleLink = new BookmarkablePageLink<>("title", IterationIssuesPage.class,
					IterationIssuesPage.paramsOf(getProject(), iteration, null));
		} else {
			titleLink = new BookmarkablePageLink<>("title", IterationIssuesPage.class,
					new PageParameters());
			titleLink.setVisible(false);
		}
		titleLink.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				Iteration iteration = nextIterationModel.getObject();
				return MessageFormat.format(_T("Next Iteration ({0})"),
						iteration != null ? iteration.getName() : "");
			}

		}));
		fragment.add(titleLink);

		fragment.add(new StateStatsBar("statsBar", new LoadableDetachableModel<>() {

			@Override
			protected Map<String, Integer> load() {
				Iteration iteration = nextIterationModel.getObject();
				if (iteration != null)
					return iteration.getStateStats(getProject());
				else
					return new LinkedHashMap<>();
			}

		}) {

			@Override
			protected Link<Void> newStateLink(String componentId, String state) {
				Iteration iteration = nextIterationModel.getObject();
				String query = new IssueQuery(new StateCriteria(state, IssueQueryLexer.Is)).toString();
				PageParameters params = IterationIssuesPage.paramsOf(getProject(), iteration, query);
				return new ViewStateAwarePageLink<>(componentId, IterationIssuesPage.class, params);
			}

		});

		return fragment;
	}

	private SettingService getSettingService() {
		return OneDev.getInstance(SettingService.class);
	}

	private ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}

	private IssueService getIssueService() {
		return OneDev.getInstance(IssueService.class);
	}

	private BuildService getBuildService() {
		return OneDev.getInstance(BuildService.class);
	}

	private PullRequestService getPullRequestService() {
		return OneDev.getInstance(PullRequestService.class);
	}

	private WorkspaceService getWorkspaceService() {
		return OneDev.getInstance(WorkspaceService.class);
	}

	private PackService getPackService() {
		return OneDev.getInstance(PackService.class);
	}

	@Override
	protected void onDetach() {
		replicasModel.detach();
		issueStatsModel.detach();
		buildStatsModel.detach();
		pullRequestStatsModel.detach();
		workspaceStatsModel.detach();
		packStatsModel.detach();
		nextIterationModel.detach();
		super.onDetach();
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Overview"));
	}

	@Override
	protected String getPageTitle() {
		return _T("Overview") + " - " + getProject().getPath();
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		return new ViewStateAwarePageLink<>(componentId, ProjectOverviewPage.class,
				ProjectOverviewPage.paramsOf(project.getId()));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectOverviewCssResourceReference()));
	}

}
