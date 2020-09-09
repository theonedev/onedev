package io.onedev.server.web.page.my.sshkeys;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.user.sshkey.InsertSshKeyPanel;
import io.onedev.server.web.component.user.sshkey.SshKeyListPanel;
import io.onedev.server.web.page.my.MyPage;

@SuppressWarnings("serial")
public class MySshKeysPage extends MyPage {
	
	private SshKeyListPanel keyList;
	
	public MySshKeysPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
        add(new Label("sshKeyNote", "Your SSH keys are managed from " + getLoginUser().getAuthSource()) {
        	
        	@Override
        	protected void onConfigure() {
        		super.onConfigure();
        		setVisible(getLoginUser().isSshKeyExternalManaged());
        	}
        	
        });
        
		add(new ModalLink("newKey") {
            
            @Override
            protected Component newContent(String id, ModalPanel modal) {
                return new InsertSshKeyPanel(id) {

                    @Override
                    protected void onSave(AjaxRequestTarget target) {
                        target.add(keyList);
                        modal.close();
                    }

					@Override
					protected User getUser() {
						return getLoginUser();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}
					
				};
            }
            
            @Override
            protected void onConfigure() {
            	super.onConfigure();
            	setVisible(!getLoginUser().isSshKeyExternalManaged());
            }
		});
            
		keyList = new SshKeyListPanel("keyList", new LoadableDetachableModel<List<SshKey>>() {
			
		    @Override
		    protected List<SshKey> load() {
		        return new ArrayList<>(getLoginUser().getSshKeys());
		    }
		    
		});
		
		add(keyList.setOutputMarkupId(true));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "My SSH Keys");
	}
	
}
