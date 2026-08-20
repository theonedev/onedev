package io.onedev.server.web.page.admin.aisetting;

import static io.onedev.server.web.translation.Translation._T;

import java.util.Set;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.model.support.administration.AiSetting;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.PropertyDescriptor;

public class ChatPromptEditPanel extends Panel {

	@Inject
	private SettingService settingService;

	private final String propertyName;

	private final String defaultValue;

	private final AiSetting aiSetting;

	private String oldAuditContent;

	public ChatPromptEditPanel(String id, String propertyName, String defaultValue) {
		super(id);
		this.propertyName = propertyName;
		this.defaultValue = defaultValue;
		aiSetting = settingService.getAiSetting();
	}

	private String getPrompt() {
		return (String) new PropertyDescriptor(AiSetting.class, propertyName).getPropertyValue(aiSetting);
	}

	private void setPrompt(String prompt) {
		new PropertyDescriptor(AiSetting.class, propertyName).setPropertyValue(aiSetting, prompt);
	}

	private void savePrompt(String action) {
		var newAuditContent = getPrompt();
		OneDev.getInstance(SettingService.class).saveAiSetting(aiSetting);
		OneDev.getInstance(AuditService.class).audit(null, action + " chat prompt \"" + propertyName + "\"", oldAuditContent, newAuditContent);
		oldAuditContent = newAuditContent;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		// Hold the setting in a field so that the editor and the save/revert handlers keep 
		// referencing the same instance after page serialization
		oldAuditContent = getPrompt();

		Form<?> form = new Form<Void>("form");
		form.add(BeanContext.edit("editor", aiSetting, Set.of(propertyName), false));

		form.add(new Button("save") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				savePrompt("changed");
				getSession().success(_T("Chat prompt has been saved"));
				setResponsePage(ChatPromptsPage.class);
			}

		});

		form.add(new AjaxLink<Void>("revert") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to revert to default prompt?")));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				setPrompt(defaultValue);
				savePrompt("reverted");
				getSession().success(_T("Chat prompt has been reverted to default"));
				setResponsePage(ChatPromptsPage.class);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				configure();
				if (!isEnabled())
					tag.put("disabled", "disabled");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(!getPrompt().trim().equals(defaultValue.trim()));
			}

		});

		add(form);
	}

}
