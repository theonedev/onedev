package io.onedev.server.web.page.my.sshkeys;

import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import io.onedev.server.OneDev;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.my.MyPage;

@SuppressWarnings("serial")
public class MySshKeysPage extends MyPage {
	
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
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
		    final SimpleExpression eq = Restrictions.eq("owner", user);

            @Override
            protected List<SshKey> load() {
                Dao dao = OneDev.getInstance(Dao.class);
                EntityCriteria<SshKey> entityCriteria = EntityCriteria.of(SshKey.class).add(eq);
                return dao.query(entityCriteria);
            }
		    
		};
		
		keyList.add(new ListView<SshKey>("keys", detachableModel)
		 {
		        public void populateItem(final ListItem<SshKey> item)
		        {
		                final SshKey sshKey = item.getModelObject();
		                item.add(new Label("name", sshKey.getName()));
		                item.add(new Label("owner", sshKey.getOwner().getName()));
		                item.add(new Label("digest", sshKey.getDigest()));                          
		                item.add(new Label("timestamp", sshKey.getTimestamp().format(formatter)));                          
		                item.add(new AjaxLink<Void>("delete") {
		                    @Override
		                    public void onClick(AjaxRequestTarget target) {
		                       Dao dao = OneDev.getInstance(Dao.class);
		                       dao.remove(sshKey);
		                       target.add(keyList);
		                    }
		                    
		                    @Override
		                    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		                        super.updateAjaxAttributes(attributes);
		                        AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
		                            
		                            @Override
		                            public CharSequence getPrecondition(Component component) {
		                                return "return confirm(\"Are you sure you want to delete key \'" 
		                                        + sshKey.getName() + "\'?\")";
		                            }
		                        };
		                        attributes.getAjaxCallListeners().add(myAjaxCallListener);
		                    }
		                });
		        }
		 }).setOutputMarkupId(true);
		
		add(keyList);
	}
	
}
