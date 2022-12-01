package io.onedev.server.web.component.project.info;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLabel;
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

@SuppressWarnings("serial")
public abstract class ProjectInfoPanel extends Panel {

	private final IModel<Project> projectModel;
	
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
		
		add(new EntityLabelsPanel<ProjectLabel>("labels", projectModel));
		
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
		forkNow.setVisible(SecurityUtils.canReadCode(getProject()) 
				&& SecurityUtils.canCreateProjects() 
				&& getProject().getStorageServerUUID(false) != null);
		forkInfo.add(forkNow);
        
        SettingManager settingManager = OneDev.getInstance(SettingManager.class);
        if (settingManager.getServiceDeskSetting() != null
        		&& settingManager.getMailSetting() != null 
        		&& settingManager.getMailSetting().getCheckSetting() != null
        		&& getProject().isIssueManagement()) {
        	
        	String subAddressed;
        	
			ParsedEmailAddress checkAddress = ParsedEmailAddress.parse(settingManager.getMailSetting().getCheckSetting().getCheckAddress());
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
		
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectInfoCssResourceReference()));
	}

	protected abstract void onPromptForkOption(AjaxRequestTarget target);
	
}
