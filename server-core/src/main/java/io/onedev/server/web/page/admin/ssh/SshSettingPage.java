package io.onedev.server.web.page.admin.ssh;

import java.util.Collection;
import java.util.HashSet;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.SshSettings;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;

public class SshSettingPage extends AdministrationPage {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SshSettingPage(PageParameters params) {
        super(params);
        
        if (!isSshEnabled()) {            
            throw new OneException("This page requires Ssh support to be enabled. "
                    + " You need to specify ssh_port parameter in server.properties");
        }
        
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        SshSettings sshSetting = OneDev.getInstance(SettingManager.class).getSshSettings();

        Form<?> form = new Form<Void>("form") {

            @Override
            protected void onSubmit() {
                super.onSubmit();
                OneDev.getInstance(SettingManager.class).saveSshSetting(sshSetting);
                getSession().success("SSH setting has been updated");
                
                setResponsePage(SshSettingPage.class);
            }
            
        };
        Collection<String> excludedProps = new HashSet<>();
        form.add(BeanContext.edit("editor", sshSetting, excludedProps, true));
        
        add(form);
    }
}
