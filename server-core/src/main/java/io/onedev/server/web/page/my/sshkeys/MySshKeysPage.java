package io.onedev.server.web.page.my.sshkeys;

import org.apache.wicket.Component;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.my.MyPage;

@SuppressWarnings("serial")
public class MySshKeysPage extends MyPage {
	
	public MySshKeysPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ModalLink("content") {
            
            @Override
            protected Component newContent(String id, ModalPanel modal) {
                return new InsertSshKeyPanel(id, modal);
            }
        });
	}
	
}
