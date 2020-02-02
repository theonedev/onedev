package io.onedev.server.web.page.my.sshkeys;

import java.io.StringReader;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.PublicKeyEntryResolver;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import io.onedev.server.OneDev;
import io.onedev.server.git.ssh.SimpleGitSshServer;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class InsertSshKeyPanel extends Panel {

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
                
                sshKey.setOwner(user);
                
                try {
                    StringReader stringReader = new StringReader(sshKey.getContent());
                    List<AuthorizedKeyEntry> entries = AuthorizedKeyEntry.readAuthorizedKeys(stringReader, true);

                    AuthorizedKeyEntry entry = entries.get(0);
                    PublicKey pubEntry = entry.resolvePublicKey(PublicKeyEntryResolver.FAILING);
                    
                    String fingerPrint = KeyUtils.getFingerPrint(SimpleGitSshServer.MD5_DIGESTER, pubEntry);
                    
                    sshKey.setDigest(fingerPrint);
                    sshKey.setTimestamp(LocalDateTime.now());
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                } 
                
                dao.persist(sshKey);
                
                modal.close();
                
                onSave(target);
            }
        });
        
        form.add(new TextField<>("name"));
        form.add(new TextArea<>("content"));
        
        form.setModel(new CompoundPropertyModel<SshKey>(new SshKey()));
        
        add(form);
    }
    
    protected abstract void onSave(AjaxRequestTarget target);
}
