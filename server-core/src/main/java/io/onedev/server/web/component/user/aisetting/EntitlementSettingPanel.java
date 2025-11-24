package io.onedev.server.web.component.user.aisetting;

import static io.onedev.server.web.translation.Translation._T;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.GroupEntitlement;
import io.onedev.server.model.ProjectEntitlement;
import io.onedev.server.model.User;
import io.onedev.server.model.UserEntitlement;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.GroupEntitlementService;
import io.onedev.server.service.GroupService;
import io.onedev.server.service.ProjectEntitlementService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.UserEntitlementService;
import io.onedev.server.service.UserService;
import io.onedev.server.web.editable.BeanContext;

public class EntitlementSettingPanel extends GenericPanel<User> {

    @Inject
    private UserService userService;

    @Inject
    private AuditService auditService;

    @Inject
    private TransactionService transactionService;

    @Inject
    private ProjectEntitlementService projectEntitlementService;

    @Inject
    private GroupEntitlementService groupEntitlementService;

    @Inject
    private UserEntitlementService userEntitlementService;

    @Inject
    private ProjectService projectService;

    @Inject
    private GroupService groupService;

    public EntitlementSettingPanel(String id, IModel<User> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        var bean = new EntitlementEditBean();
        bean.setEntitleToAll(getUser().getAiSetting().isEntitleToAll());
        bean.setEntitledProjects(getUser().getProjectEntitlements().stream()
                .map(it->it.getProject().getPath())
                .sorted()
                .collect(Collectors.toList()));
        bean.setEntitledGroups(getUser().getGroupEntitlements().stream()
                .map(it->it.getGroup().getName())
                .sorted()
                .collect(Collectors.toList()));
        bean.setEntitledUsers(getUser().getUserEntitlements().stream()
                .map(it->it.getUser().getName())
                .sorted()
                .collect(Collectors.toList()));

		var oldAuditContent = VersionedXmlDoc.fromBean(bean).toXML();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

                transactionService.run(() -> {
                    var newAuditContent = VersionedXmlDoc.fromBean(bean).toXML();
                    getUser().getAiSetting().setEntitleToAll(bean.isEntitleToAll());

                    var projectEntitlements = bean.getEntitledProjects().stream().map(it->{
                        var entitlement = new ProjectEntitlement();
                        entitlement.setProject(projectService.findByPath(it));
                        entitlement.setAi(getUser());
                        return entitlement;
                    }).collect(Collectors.toList());

                    var groupEntitlements = bean.getEntitledGroups().stream().map(it->{
                        var entitlement = new GroupEntitlement();
                        entitlement.setGroup(groupService.find(it));
                        entitlement.setAi(getUser());
                        return entitlement;
                    }).collect(Collectors.toList());

                    var userEntitlements = bean.getEntitledUsers().stream().map(it->{
                        var entitlement = new UserEntitlement();
                        entitlement.setUser(userService.findByName(it));
                        entitlement.setAI(getUser());
                        return entitlement;
                    }).collect(Collectors.toList());

                    userService.update(getUser(), null);
                    projectEntitlementService.syncEntitlements(getUser(), projectEntitlements);
                    groupEntitlementService.syncEntitlements(getUser(), groupEntitlements);
                    userEntitlementService.syncEntitlements(getUser(), userEntitlements);

                    auditService.audit(null, "changed AI entitlement settings", oldAuditContent, newAuditContent);				
                });

				getSession().success(_T("AI entitlement settings have been saved"));
				setResponsePage(getPage().getClass(), getPage().getPageParameters());
			}
			
		};
		form.add(BeanContext.edit("editor", bean));
		
		add(form);
    }

    private User getUser() {
        return getModelObject();
    }
    
}
