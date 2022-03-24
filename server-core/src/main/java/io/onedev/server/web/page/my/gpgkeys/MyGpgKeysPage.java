package io.onedev.server.web.page.my.gpgkeys;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.GpgKey;
import io.onedev.server.model.User;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.user.gpgkey.GpgKeyListPanel;
import io.onedev.server.web.component.user.gpgkey.InsertGpgKeyPanel;
import io.onedev.server.web.page.my.MyPage;

@SuppressWarnings("serial")
public class MyGpgKeysPage extends MyPage {
	
	private GpgKeyListPanel keyList;
	
	public MyGpgKeysPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ModalLink("newKey") {
            
            @Override
			protected String getModalCssClass() {
            	return "modal-lg";
			}

			@Override
            protected Component newContent(String id, ModalPanel modal) {
                return new InsertGpgKeyPanel(id) {

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
            
		});
            
		keyList = new GpgKeyListPanel("keyList", new LoadableDetachableModel<List<GpgKey>>() {
			
		    @Override
		    protected List<GpgKey> load() {
		    	return getLoginUser().getGpgKeys();
		    }
		    
		});
		
		add(keyList.setOutputMarkupId(true));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "My GPG Keys");
	}
	
}
