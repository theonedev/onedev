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

public class DispatchSettingsPage extends AdministrationPage {

	@Inject
	private SettingService settingService;

	public DispatchSettingsPage(PageParameters params) {
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
				auditService.audit(null, "changed AI dispatch settings", oldAuditContent, newAuditContent);
				getSession().success(_T("AI dispatch settings have been saved"));

				setResponsePage(DispatchSettingsPage.class);
			}

		};
		form.add(BeanContext.edit("editor", aiSetting, Set.of(
				AiSetting.PROP_DISPATCH_ENABLED,
				AiSetting.PROP_CLAUDE_DISPATCH_SETTING,
				AiSetting.PROP_COPILOT_DISPATCH_SETTING,
				AiSetting.PROP_COPILOT_API_SETTING,
				AiSetting.PROP_CODEX_DISPATCH_SETTING,
				AiSetting.PROP_MAX_DISPATCH_SESSIONS,
				AiSetting.PROP_DISPATCH_TIMEOUT_MINUTES), false));

		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Dispatch Settings"));
	}

}
