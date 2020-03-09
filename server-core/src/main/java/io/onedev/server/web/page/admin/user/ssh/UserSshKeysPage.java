package io.onedev.server.web.page.admin.user.ssh;

import java.util.List;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.web.OneWebApplication;
import io.onedev.server.web.page.admin.user.UserPage;
import io.onedev.server.web.page.my.sshkeys.SshKeysListPanel;

@SuppressWarnings("serial")
public class UserSshKeysPage extends UserPage {

    public UserSshKeysPage(PageParameters params) {
        super(params);
        
		if (!OneWebApplication.get().isSshEnabled()) {            
            throw new OneException("This page requires Ssh support to be enabled. "
                    + " You need to specify ssh_port parameter in server.properties");
        }
    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        User user = getUser();
        
        LoadableDetachableModel<List<SshKey>> detachableModel = new LoadableDetachableModel<List<SshKey>>() {
            @Override
            protected List<SshKey> load() {
                SshKeyManager sshKeyManager = OneDev.getInstance(SshKeyManager.class);
                return sshKeyManager.loadUserKeys(user);
            }
            
        };

        SshKeysListPanel keyList = new SshKeysListPanel("keyList", detachableModel);
        add(keyList.setOutputMarkupId(true));
    }
}
