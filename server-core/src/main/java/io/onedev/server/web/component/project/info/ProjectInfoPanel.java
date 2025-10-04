package io.onedev.server.web.component.project.info;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.Project;
import io.onedev.server.replica.ProjectReplica;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.search.entity.project.ProjectQueryLexer;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ParsedEmailAddress;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.entity.labels.EntityLabelsPanel;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.project.forkoption.ForkOptionPanel;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;

public abstract class ProjectInfoPanel extends Panel {

	private static final Logger logger = LoggerFactory.getLogger(ProjectInfoPanel.class);
	
	private final IModel<Project> projectModel;
	
	private final IModel<Map<String, ProjectReplica>> replicasModel = new LoadableDetachableModel<>() {
		@Override
		protected Map<String, ProjectReplica> load() {
			return getProjectService().getReplicas(getProject().getId());
		}
	};
	
	public ProjectInfoPanel(String id, IModel<Project> projectModel) {
		super(id);
		this.projectModel = projectModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("path", getProject().getPath()));
		add(new EntityLabelsPanel<>("labels", projectModel));

		add(new Label("id", "id: " + getProject().getId()));
		add(new Label("key", ", key: " + getProject().getKey())
				.setVisible(getProject().getKey() != null));
		if (getProject().getForkedFrom() != null) {
			add(new BookmarkablePageLink<Void>("forkedFrom", ProjectDashboardPage.class,
					ProjectDashboardPage.paramsOf(getProject().getForkedFrom())) {

				@Override
				public IModel<?> getBody() {
					return Model.of(getProject().getForkedFrom().getPath());
				}

			});
		} else {
			add(new WebMarkupContainer("forkedFrom").setVisible(false));
		}

		WebMarkupContainer forkInfo = new WebMarkupContainer("forkInfo");
		forkInfo.setVisible(getProject().isCodeManagement());
		add(forkInfo);
		
		String query = ProjectQuery.getRuleName(ProjectQueryLexer.ForksOf) + " " 
				+ Criteria.quote(getProject().getPath());
		PageParameters params = ProjectListPage.paramsOf(query, 0, getProject().getForks().size());
		Link<Void> forksLink = new BookmarkablePageLink<Void>("forks", ProjectListPage.class, params);
		forksLink.add(new Label("label", MessageFormat.format(_T("{0} forks"), getProject().getForks().size())));
		forkInfo.add(forksLink);
		
		ModalLink forkNow = new ModalLink("forkNow") {
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				super.onClick(target);
				onPromptForkOption(target);
			}

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
        
        SettingService settingService = OneDev.getInstance(SettingService.class);
        if (settingService.getServiceDeskSetting() != null
        		&& settingService.getMailConnector() != null 
        		&& settingService.getMailConnector().getInboxMonitor(false) != null
        		&& getProject().isIssueManagement()) {
        	
			String serviceDeskEmailAddress;
			if (getProject().getServiceDeskEmailAddress() != null) {
				serviceDeskEmailAddress = getProject().getServiceDeskEmailAddress();
			} else {
				ParsedEmailAddress systemAddress = ParsedEmailAddress.parse(settingService.getMailConnector().getSystemAddress());
				serviceDeskEmailAddress = systemAddress.getSubaddress(getProject().getPath());
			}
			
        	add(new WebMarkupContainer("serviceDesk") {

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
        	add(new WebMarkupContainer("serviceDesk").setVisible(false));
        }
		
		if (getProject().getDescription() != null)
			add(new MultilineLabel("description", Model.of(getProject().getDescription())));
		else 
			add(new WebMarkupContainer("description").setVisible(false));
				
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
		add(new ListView<Map.Entry<String, ProjectReplica>>("replicas", new LoadableDetachableModel<>() {
			@Override
			protected List<Map.Entry<String, ProjectReplica>> load() {
				var replicas = new ArrayList<>(replicasModel.getObject().entrySet());
				var orders = new HashMap<String, Integer>();
				var index = 0;
				for (var server: getClusterService().getServerAddresses()) 
					orders.put(server, index++);					
				return replicas.stream()
						.filter(it -> orders.containsKey(it.getKey()) && (it.getValue().getType() != ProjectReplica.Type.REDUNDANT || it.getKey().equals(activeServer)))
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
				if (server.equals(activeServer)) 
					serverInfo = escapedServer + " <span class='badge badge-sm badge-info ml-1'>" + _T("active") + "</span>";
				else if (replica.getVersion() == latestVersion)
					serverInfo = escapedServer + " <span class='badge badge-sm badge-success ml-1'>" + _T("up to date") + "</span>";
				else
					serverInfo = escapedServer + " <span class='badge badge-sm badge-warning ml-1'>" + _T("outdated") + "</span>";
				item.add(new Label("server", serverInfo).setEscapeModelStrings(false));
				
				var projectId = getProject().getId();
				item.add(new AjaxLink<Void>("sync") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						getClusterService().submitToServer(server, () -> {
							try {
								getProjectService().requestToSyncReplica(projectId, activeServer);
							} catch (Exception e) {
								logger.error(MessageFormat.format("Error requestig to sync replica of project with id \"{0}\"", projectId), e);
							}
							return null;
						});
						Session.get().success(_T("Sync requested. Please check status after a while"));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canManageProject(getProject()) 
								&& item.getModelObject().getValue().getVersion() < latestVersion);
					}
				});

			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(latestVersion != -1 && OneDev.getInstance(ClusterService.class).isClusteringSupported());
			}
		});
	}
	
	private ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}
	
	private ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		replicasModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectInfoCssResourceReference()));
	}

	protected abstract void onPromptForkOption(AjaxRequestTarget target);
	
}
