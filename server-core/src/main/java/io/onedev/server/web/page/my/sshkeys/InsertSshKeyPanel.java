package io.onedev.server.web.page.my.sshkeys;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import io.onedev.server.OneDev;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public class InsertSshKeyPanel extends Panel {

    private ModalPanel modal;
    private User user;

    public InsertSshKeyPanel(String id, ModalPanel modal, User user) {
        super(id);
        this.modal = modal;
        this.user = user;
    }
    

    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        add(new AjaxLink<Void>("close") {

            @Override
            public void onClick(AjaxRequestTarget target) {
              modal.close();
            }
        });

        Form<SshKey> form = new Form<>("form");
        
        form.add(new AjaxLink<Void>("cancel") {
            
            @Override
            public void onClick(AjaxRequestTarget target) {
                modal.close();
            }
        });
        
        form.add(new AjaxSubmitLink("add") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> myform) {
                super.onSubmit(target, myform);
                                
                SshKey sshKey = form.getModelObject();
                
                Dao dao = OneDev.getInstance(Dao.class);
                
                sshKey.setDigest("digesto");
                sshKey.setOwner(user);
                
                dao.persist(sshKey);
                
                System.out.println("Number of keys: " + dao.count(SshKey.class));
                
                modal.close();
            }
        });
        
        form.add(new TextField<>("name"));
        form.add(new TextArea<>("content"));
        
        form.setModel(new CompoundPropertyModel<SshKey>(new SshKey()));
        
        add(form);
    }
}
