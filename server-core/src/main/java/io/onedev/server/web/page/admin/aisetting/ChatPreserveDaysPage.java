package io.onedev.server.web.page.admin.aisetting;

import static io.onedev.server.web.translation.Translation._T;

import java.util.Set;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.support.administration.AiSetting;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;

public class ChatPreserveDaysPage extends AdministrationPage {

	@Inject
	private SettingService settingService;

	public ChatPreserveDaysPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		AiSetting aiSetting = settingService.getAiSetting();
		var oldAuditContent = VersionedXmlDoc.fromBean(aiSetting).toXML();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				var newAuditContent = VersionedXmlDoc.fromBean(aiSetting).toXML();
				settingService.saveAiSetting(aiSetting);
				auditService.audit(null, "changed chat preserve days setting", oldAuditContent, newAuditContent);				
				getSession().success(_T("Chat preserve days setting has been saved"));
				
				setResponsePage(ChatPreserveDaysPage.class);
			}
			
		};
		form.add(BeanContext.edit("editor", aiSetting, Set.of(AiSetting.PROP_CHAT_PRESERVE_DAYS), false));
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Chat Preserve Days"));
	}

}

