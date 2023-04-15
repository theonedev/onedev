package io.onedev.server.web.page.admin.usermanagement.sshkeys;

import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.user.sshkey.InsertSshKeyPanel;
import io.onedev.server.web.component.user.sshkey.SshKeyListPanel;
import io.onedev.server.web.page.admin.usermanagement.UserPage;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class UserSshKeysPage extends UserPage {

    public UserSshKeysPage(PageParameters params) {
        super(params);
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

        SshKeyListPanel keyList = new SshKeyListPanel("keyList", detachableModel);
        
        add(new ModalLink("newKey") {
            
            @Override
            protected Component newContent(String id, ModalPanel modal) {
                return new InsertSshKeyPanel(id) {

					@Override
					protected User getUser() {
						return UserSshKeysPage.this.getUser();
					}

                    @Override
                    protected void onSave(AjaxRequestTarget target) {
                        target.add(keyList);
                        modal.close();
                    }

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}
					
				};
            }

        });
        
        add(keyList.setOutputMarkupId(true));
    }

}
