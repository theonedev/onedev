package io.onedev.server.web.page.my.sshkeys;

import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.model.support.administration.authenticator.ldap.LdapAuthenticator;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.my.MyPage;

@SuppressWarnings("serial")
public class MySshKeysPage extends MyPage {
	
	public MySshKeysPage(PageParameters params) {
		super(params);
		
		if (!isSshEnabled()) {            
            throw new OneException("This page requires SSH support to be enabled. "
                    + " You need to specify ssh_port parameter in server.properties");
        }
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		User user = getLoginUser();
		
		LoadableDetachableModel<List<SshKey>> detachableModel = new LoadableDetachableModel<List<SshKey>>() {
		    @Override
		    protected List<SshKey> load() {
		        SshKeyManager sshKeyManager = OneDev.getInstance(SshKeyManager.class);
		        return sshKeyManager.loadUserKeys(user);
		    }
		    
		};

		SshKeysListPanel keyList = new SshKeysListPanel("keyList", detachableModel);
		
		add(new ModalLink("newKey") {
            
            @Override
            protected Component newContent(String id, ModalPanel modal) {
                return new InsertSshKeyPanel(id, modal, user) {

                    @Override
                    protected void onSave(AjaxRequestTarget target) {
                        target.add(keyList);
                    }};
            }
            
            @Override
            public boolean isVisible() {
            	if (!user.isExternalManaged()) {
            		return true;
            	}
            	
            	Authenticator auth = OneDev.getInstance(SettingManager.class).getAuthenticator();
        		
            	if (auth == null || !(auth instanceof LdapAuthenticator)) {
            		return true;
            	}
            	
            	if (((LdapAuthenticator) auth).getUserSSHPublicKey() != null) {
            		return false;
            	}
            	
            	return true;
            }
        });
		
		add(keyList.setOutputMarkupId(true));
		
	}
	
}
