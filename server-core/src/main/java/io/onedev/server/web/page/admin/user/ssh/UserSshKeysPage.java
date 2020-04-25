package io.onedev.server.web.page.admin.user.ssh;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.model.support.administration.authenticator.ldap.LdapAuthenticator;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.admin.user.UserPage;
import io.onedev.server.web.page.my.sshkeys.InsertSshKeyPanel;
import io.onedev.server.web.page.my.sshkeys.SshKeysListPanel;

@SuppressWarnings("serial")
public class UserSshKeysPage extends UserPage {

    public UserSshKeysPage(PageParameters params) {
        super(params);
        
		if (!isSshEnabled()) {            
            throw new OneException("This page requires Ssh support to be enabled. "
                    + " You need to specify ssh_port parameter in server.properties");
        }
    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        LoadableDetachableModel<List<SshKey>> detachableModel = new LoadableDetachableModel<List<SshKey>>() {
        	
            @Override
            protected List<SshKey> load() {
            	return new ArrayList<>(getUser().getSshKeys());
            }
            
        };

        SshKeysListPanel keyList = new SshKeysListPanel("keyList", detachableModel);
        
        add(new ModalLink("newKey") {
            
            @Override
            protected Component newContent(String id, ModalPanel modal) {
                return new InsertSshKeyPanel(id, modal) {

                    @Override
                    protected void onSave(AjaxRequestTarget target) {
                        target.add(keyList);
                    }

					@Override
					protected User getUser() {
						return UserSshKeysPage.this.getUser();
					}
				};
            }
            
            @Override
            protected void onConfigure() {
            	super.onConfigure();
            	setVisible(determineNewSSHKeyVisibility());
            }
            

        });
        
        add(new WebMarkupContainer("sshKeyNote") {
        	@Override
        	protected void onConfigure() {
        		super.onConfigure();
        		setVisible(determineSSHKeyNoteVisibility());
        	}
        });
        
        add(keyList.setOutputMarkupId(true));
    }
    
    private boolean determineSSHKeyNoteVisibility() {
    	return !determineNewSSHKeyVisibility();
    }
    
    private boolean determineNewSSHKeyVisibility() {
    	if (!getUser().isExternalManaged()) {
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
}
