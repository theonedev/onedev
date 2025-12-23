package io.onedev.server.web.component.user.aisetting;

import static io.onedev.server.web.translation.Translation._T;

import javax.inject.Inject;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.model.User;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.UserService;

public class SystemPromptPanel extends GenericPanel<User> {

    @Inject
    private UserService userService;

    @Inject
    private AuditService auditService;

    public SystemPromptPanel(String id, IModel<User> model) {
        super(id, model);
    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();

		String systemPrompt = getUser().getAiSetting().getSystemPrompt();

		var editor = new TextArea<String>("systemPrompt", Model.of(systemPrompt));
		editor.setConvertEmptyInputStringToNull(true);

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				var newSystemPrompt = editor.getModelObject();
                getUser().getAiSetting().setSystemPrompt(newSystemPrompt);
                userService.update(getUser(), null);
				auditService.audit(null, "changed AI system prompt", systemPrompt, newSystemPrompt);				
				getSession().success(_T("AI system prompt has been saved"));
				setResponsePage(getPage().getClass(), getPage().getPageParameters());
			}
			
		};
		form.add(editor);
		
		add(form);
    }

	private User getUser() {
		return getModelObject();
	}

}
