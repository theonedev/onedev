package io.onedev.server.web.page.admin.ssh;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.SshSetting;
import io.onedev.server.ssh.SshKeyUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class SshSettingPage extends AdministrationPage {

    public SshSettingPage(PageParameters params) {
        super(params);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        SshSetting sshSetting = OneDev.getInstance(SettingManager.class).getSshSetting();

        Form<?> form = new Form<Void>("form") {

			private static final long serialVersionUID = 1L;

			@Override
            protected void onSubmit() {
                super.onSubmit();
                OneDev.getInstance(SettingManager.class).saveSshSetting(sshSetting);
                getSession().success("SSH setting has been saved");
                
                setResponsePage(SshSettingPage.class);
            }
            
        };
        form.add(BeanContext.edit("editor", sshSetting));
        
        form.add(new AjaxLink<Void>("rekey") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				form.visitChildren(TextArea.class, new IVisitor<TextArea<String>, Void>() {

					@Override
					public void component(TextArea<String> component, IVisit<Void> visit) {
						component.setModelObject(SshKeyUtils.generatePEMPrivateKey());
						visit.stop();
					}
					
				});
				target.add(form);
				target.appendJavaScript(String.format("onedev.server.form.markDirty($('#%s'));", form.getMarkupId()));
	            getSession().success("Private key regenerated");
			}
        	
        });
        form.setOutputMarkupId(true);
        add(form);
    }

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "SSH Setting");
	}
    
}
