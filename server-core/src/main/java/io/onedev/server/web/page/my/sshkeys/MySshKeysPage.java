package io.onedev.server.web.page.my.sshkeys;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
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
		
		User user = getLoginUser();
		WebMarkupContainer keyList = new WebMarkupContainer("keyList");
		
		add(new ModalLink("content") {
            
            @Override
            protected Component newContent(String id, ModalPanel modal) {
                return new InsertSshKeyPanel(id, modal, user) {

                    @Override
                    protected void onSave(AjaxRequestTarget target) {
                        target.add(keyList);
                    }};
            }
        });
		
	
		
		LoadableDetachableModel<List<SshKey>> detachableModel = new LoadableDetachableModel<List<SshKey>>() {

            @Override
            protected List<SshKey> load() {
                return new ArrayList<>(user.getSshKeys());
            }
		    
		};
		
		keyList.add(new ListView<SshKey>("keys", detachableModel)
		 {
		        public void populateItem(final ListItem<SshKey> item)
		        {
		                final SshKey sshKey = item.getModelObject();
		                item.add(new Label("name", sshKey.getName()));
		                item.add(new Label("owner", sshKey.getOwner().getName()));
		                item.add(new AjaxLink<Void>("delete") {
		                    @Override
		                    public void onClick(AjaxRequestTarget target) {
		                       user.getSshKeys().remove(sshKey);
		                       OneDev.getInstance(UserManager.class).save(user);
		                       target.add(keyList);
		                    }
		                });
		        }
		 }).setOutputMarkupId(true);
		
		add(keyList);
	}
	
}
