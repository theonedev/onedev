package io.onedev.server.web.page.my.sshkeys;

import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.model.support.administration.authenticator.ldap.LdapAuthenticator;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.web.component.modal.confirm.ConfirmModal;

@SuppressWarnings("serial")
public class SshKeysListPanel extends Panel {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public SshKeysListPanel(String id, IModel<List<SshKey>> model) {
        super(id);
        
        WebMarkupContainer keyList = new WebMarkupContainer("keyList");
        
        keyList.add(new ListView<SshKey>("keys", model)
        {
            public void populateItem(final ListItem<SshKey> item)
            {
                final SshKey sshKey = item.getModelObject();
                item.add(new Label("name", sshKey.getName()));
                item.add(new Label("digest", sshKey.getDigest()));                          
                item.add(new Label("timestamp", sshKey.getTimestamp().format(formatter)));                          
                item.add(new AjaxLink<Void>("delete") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        
                        new ConfirmModal(target) {

                            @Override
                            protected void onConfirm(AjaxRequestTarget target) {
                                Dao dao = OneDev.getInstance(Dao.class);
                                dao.remove(sshKey);
                                target.add(keyList);
                            }

                            @Override
                            protected String getConfirmInput() {
                                return null;
                            }

                            @Override
                            protected String getConfirmMessage() {
                                return "Are you sure you want to delete key '" + sshKey.getName() + "'?";
                            }
                        };
                    }
                    
                    @Override
                    protected void onConfigure() {
                    	super.onConfigure();
                    	setVisible(determineDeleteSSHKeyVisibility());
                    }
                    
                    public boolean determineDeleteSSHKeyVisibility() {
                    	if (!item.getModelObject().getOwner().isExternalManaged()) {
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
            }
        }).setOutputMarkupId(true);
        
        Label noRecordsMsg = new Label("msg", new ResourceModel("datatable.no-records-found")) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(model.getObject().size() == 0);
            }
        };
        
        keyList.add(noRecordsMsg);

        add(keyList);
        
    }
}
