package io.onedev.server.web.page.project.setting.servicedesk;

import static io.onedev.server.model.Project.PROP_SERVICE_DESK_EMAIL_ADDRESS;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;
import io.onedev.server.web.page.project.setting.general.GeneralProjectSettingPage;

public class ServiceDeskSettingPage extends ProjectSettingPage {

	private BeanEditor editor;
	
	public ServiceDeskSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var oldAuditContent = VersionedXmlDoc.fromBean(getProject().getServiceDeskEmailAddress()).toXML();
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				var project = getProject();
				if (project.getServiceDeskEmailAddress() != null) {
					Project projectWithSameServiceDeskEmailAddress = getProjectService().findByServiceDeskEmailAddress(project.getServiceDeskEmailAddress());
					if (projectWithSameServiceDeskEmailAddress != null && !projectWithSameServiceDeskEmailAddress.equals(getProject())) {
						editor.error(new Path(new PathNode.Named(PROP_SERVICE_DESK_EMAIL_ADDRESS)),
								"This address has already been used by another project");
					} 
				} 
				if (editor.isValid()) {
					var newAuditContent = VersionedXmlDoc.fromBean(getProject().getServiceDeskEmailAddress()).toXML();
					getProjectService().update(getProject());
					auditService.audit(getProject(), "changed service desk email address", oldAuditContent, newAuditContent);
					setResponsePage(ServiceDeskSettingPage.class, ServiceDeskSettingPage.paramsOf(getProject()));
					Session.get().success("Service desk settings updated");
				}
			}
			
		};
		form.add(editor = BeanContext.editModel("editor", new IModel<>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getProject();
			}

			@Override
			public void setObject(Serializable object) {
				editor.getDescriptor().copyProperties(object, getProject());
			}

		}, Sets.newHashSet(PROP_SERVICE_DESK_EMAIL_ADDRESS), false));
		
		add(form);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>Service Desk Settings</span>").setEscapeModelStrings(false);
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (SecurityUtils.canManageProject(project)) {
			if (project.isIssueManagement())
				return new ViewStateAwarePageLink<Void>(componentId, ServiceDeskSettingPage.class, paramsOf(project.getId()));
			else
				return new ViewStateAwarePageLink<Void>(componentId, GeneralProjectSettingPage.class, paramsOf(project.getId()));
		} else {
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectPage.paramsOf(project.getId()));
		}
	}
	
}
