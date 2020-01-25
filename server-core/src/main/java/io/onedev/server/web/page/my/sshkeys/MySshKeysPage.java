package io.onedev.server.web.page.my.sshkeys;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import io.onedev.server.model.User;
import io.onedev.server.web.page.my.MyPage;

@SuppressWarnings("serial")
public class MySshKeysPage extends MyPage {
	
	public MySshKeysPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("content", new AbstractReadOnlyModel<User>() {

			@Override
			public User getObject() {
				return getLoginUser();
			}
			
		}));
	}
	
}
