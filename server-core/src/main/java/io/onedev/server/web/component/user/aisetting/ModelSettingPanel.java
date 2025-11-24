package io.onedev.server.web.component.user.aisetting;

import static io.onedev.server.web.translation.Translation._T;

import javax.inject.Inject;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.User;
import io.onedev.server.model.support.AiModelSetting;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.UserService;
import io.onedev.server.web.editable.BeanContext;

public class ModelSettingPanel extends GenericPanel<User> {

    @Inject
    private UserService userService;

    @Inject
    private AuditService auditService;

    public ModelSettingPanel(String id, IModel<User> model) {
        super(id, model);
    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();

		AiModelSetting setting = getUser().getAiSetting().getModelSetting();
		var oldAuditContent = VersionedXmlDoc.fromBean(setting).toXML();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				var newAuditContent = VersionedXmlDoc.fromBean(setting).toXML();
                getUser().getAiSetting().setModelSetting(setting);
                userService.update(getUser(), null);
				auditService.audit(null, "changed AI model settings", oldAuditContent, newAuditContent);				
				getSession().success(_T("AI model settings have been saved"));
				setResponsePage(getPage().getClass(), getPage().getPageParameters());
			}
			
		};
		form.add(BeanContext.edit("editor", setting));
		
		add(form);
    }

	private User getUser() {
		return getModelObject();
	}

}
