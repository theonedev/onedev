package io.onedev.server.web.component.user.aisetting;

import static io.onedev.server.web.translation.Translation._T;

import javax.inject.Inject;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.User;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.UserService;
import io.onedev.server.web.editable.BeanContext;

public class BehaviorPanel extends GenericPanel<User> {

	@Inject
	private UserService userService;

	@Inject
	private AuditService auditService;

	public BehaviorPanel(String id, IModel<User> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var bean = new BehaviorEditBean();
		bean.setSystemPrompt(getUser().getAiSetting().getSystemPrompt());
		bean.setProactive(getUser().getAiSetting().isProactive());
		bean.setMaxLoopCount(getUser().getAiSetting().getMaxLoopCount());
		var oldAuditContent = VersionedXmlDoc.fromBean(bean).toXML();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				var newAuditContent = VersionedXmlDoc.fromBean(bean).toXML();
				getUser().getAiSetting().setSystemPrompt(bean.getSystemPrompt());
				getUser().getAiSetting().setProactive(bean.isProactive());
				getUser().getAiSetting().setMaxLoopCount(bean.getMaxLoopCount());
				userService.update(getUser(), null);
				auditService.audit(null, "changed AI behavior settings", oldAuditContent, newAuditContent);
				getSession().success(_T("AI behavior settings have been saved"));
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
