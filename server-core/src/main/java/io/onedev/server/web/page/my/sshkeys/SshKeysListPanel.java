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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.model.SshKey;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.web.OneWebApplication;

@SuppressWarnings("serial")
public class SshKeysListPanel extends Panel {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public SshKeysListPanel(String id, IModel<List<SshKey>> model) {
        super(id);
        
        if (!OneWebApplication.get().isSshEnabled()) {            
            throw new OneException("This page requires Ssh support to be enabled. "
                    + " You need to specify ssh_port parameter in server.properties");
        }
        
        WebMarkupContainer keyList = new WebMarkupContainer("keyList");
        
        keyList.add(new ListView<SshKey>("keys", model)
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
        });
        
        add(keyList);
    }
}
