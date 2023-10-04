package io.onedev.server.web.component.project.info;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.replica.ProjectReplica;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.search.entity.project.ProjectQueryLexer;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ParsedEmailAddress;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.component.entity.labels.EntityLabelsPanel;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.project.forkoption.ForkOptionPanel;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.onedev.server.replica.ProjectReplica.Type.REDUNDANT;
import static java.util.Comparator.comparingInt;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

@SuppressWarnings("serial")
public abstract class ProjectInfoPanel extends Panel {

	private static final Logger logger = LoggerFactory.getLogger(ProjectInfoPanel.class);
	
	private final IModel<Project> projectModel;
	
	private final IModel<Map<String, ProjectReplica>> replicasModel = new LoadableDetachableModel<>() {
		@Override
		protected Map<String, ProjectReplica> load() {
			return getProjectManager().getReplicas(getProject().getId());
		}
	};
	
	public ProjectInfoPanel(String id, IModel<Project> projectModel) {
		super(id);
		this.projectModel = projectModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new BookmarkablePageLink<Void>("pathAndId", ProjectDashboardPage.class, 
				ProjectDashboardPage.paramsOf(getProject())) {

			@Override
			public IModel<?> getBody() {
				return Model.of(getProject().getPath() + " (id: " + getProject().getId() + ")");
			}
			
		});
		
		add(new EntityLabelsPanel<>("labels", projectModel));
		
		WebMarkupContainer forkInfo = new WebMarkupContainer("forkInfo");
		forkInfo.setVisible(getProject().isCodeManagement());
		add(forkInfo);
		
		String query = ProjectQuery.getRuleName(ProjectQueryLexer.ForksOf) + " " 
				+ Criteria.quote(getProject().getPath());
		PageParameters params = ProjectListPage.paramsOf(query, 0, getProject().getForks().size());
		Link<Void> forksLink = new BookmarkablePageLink<Void>("forks", ProjectListPage.class, params) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (!isEnabled())
					tag.setName("span");
			}
			
		};
		forksLink.add(new Label("label", getProject().getForks().size() + " forks"));
		forksLink.setVisible(getProject().isCodeManagement());
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
		forkNow.setVisible(SecurityUtils.getUser() != null 
				&& SecurityUtils.canReadCode(getProject()) 
				&& activeServer != null);
		forkInfo.add(forkNow);
        
        SettingManager settingManager = OneDev.getInstance(SettingManager.class);
        if (settingManager.getServiceDeskSetting() != null
        		&& settingManager.getMailService() != null 
        		&& settingManager.getMailService().getInboxMonitor() != null
        		&& getProject().isIssueManagement()) {
        	
        	String subAddressed;
        	
			ParsedEmailAddress checkAddress = ParsedEmailAddress.parse(settingManager.getMailService().getSystemAddress());
			if (getProject().getServiceDeskName() != null)
				subAddressed = checkAddress.getSubAddressed(getProject().getServiceDeskName());
			else
				subAddressed = checkAddress.getSubAddressed(getProject().getPath());
        	
        	add(new WebMarkupContainer("serviceDesk") {

				@Override
				protected void onInitialize() {
					super.onInitialize();
					add(new Label("label", subAddressed));
				}

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.put("href", "mailto:" + subAddressed);
				}
        		
        	});
        } else {
        	add(new WebMarkupContainer("serviceDesk").setVisible(false));
        }
		
		if (getProject().getDescription() != null)
			add(new MarkdownViewer("description", Model.of(getProject().getDescription()), null));
		else 
			add(new WebMarkupContainer("description").setVisible(false));
				
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
				for (var server: getClusterManager().getServerAddresses()) 
					orders.put(server, index++);					
				return replicas.stream()
						.filter(it -> orders.containsKey(it.getKey()) && (it.getValue().getType() != REDUNDANT || it.getKey().equals(activeServer)))
						.sorted(comparingInt(o -> orders.get(o.getKey())))
						.collect(Collectors.toList());
			}
		}) {

			@Override
			protected void populateItem(ListItem<Map.Entry<String, ProjectReplica>> item) {
				var server = item.getModelObject().getKey();
				var replica = item.getModelObject().getValue();
				var escapedServer = escapeHtml5(server + " (" + getClusterManager().getServerName(server) + ")");
				String serverInfo;
				if (server.equals(activeServer)) 
					serverInfo = escapedServer + " <span class='badge badge-sm badge-info ml-1'>active</span>";
				else if (replica.getVersion() == latestVersion)
					serverInfo = escapedServer + " <span class='badge badge-sm badge-success ml-1'>up to date</span>";
				else
					serverInfo = escapedServer + " <span class='badge badge-sm badge-warning ml-1'>outdated</span>";
				item.add(new Label("server", serverInfo).setEscapeModelStrings(false));
				
				var projectId = getProject().getId();
				item.add(new AjaxLink<Void>("sync") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						getClusterManager().submitToServer(server, () -> {
							try {
								getProjectManager().requestToSyncReplica(projectId, activeServer);
							} catch (Exception e) {
								logger.error("Error requestig to sync replica of project with id '" + projectId + "'", e);
							}
							return null;
						});
						Session.get().success("Sync requested. Please check status after a while");
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canManage(getProject()) 
								&& item.getModelObject().getValue().getVersion() < latestVersion);
					}
				});

			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(latestVersion != -1 && OneDev.getInstance(ClusterManager.class).isClusteringSupported());
			}
		});
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	private ClusterManager getClusterManager() {
		return OneDev.getInstance(ClusterManager.class);
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
