package io.onedev.server.web.page.admin.sshserverkey;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.SshSetting;
import io.onedev.server.ssh.SshKeyUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.util.ConfirmClickModifier;

public class SshServerKeyPage extends AdministrationPage {

    public SshServerKeyPage(PageParameters params) {
        super(params);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        SshSetting sshSetting = OneDev.getInstance(SettingManager.class).getSshSetting();

        Form<?> form = new Form<Void>("form");
		form.add(new Button("save") {
			
			@Override
			public void onSubmit() {
				super.onSubmit();
				OneDev.getInstance(SettingManager.class).saveSshSetting(sshSetting);
				getSession().success("SSH settings have been saved and SSH server restarted");
				setResponsePage(SshServerKeyPage.class);
			}
			
		}.add(new ConfirmClickModifier("This will restart SSH server. Do you want to continue?")));
		
        form.add(BeanContext.edit("editor", sshSetting));
        
        form.add(new Link<Void>("rekey") {

			@Override
			public void onClick() {
				sshSetting.setPemPrivateKey(SshKeyUtils.generatePEMPrivateKey());
				OneDev.getInstance(SettingManager.class).saveSshSetting(sshSetting);
				setResponsePage(SshServerKeyPage.class);
	            getSession().success("Private key regenerated and SSH server restarted");
			}
        	
        }.add(new ConfirmClickModifier("This will restart SSH server. Do you want to continue?")));
		
        form.setOutputMarkupId(true);
        add(form);
    }

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "SSH Server Key");
	}
    
}
