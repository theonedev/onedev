package io.onedev.server.web.page.my.sshkeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.time.LocalDateTime;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.git.ssh.SshKeyUtils;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

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
        
        Form<?> form = new Form<Void>("form");
        
        BeanEditor editor = BeanContext.edit("editor", new SshKey());
        form.add(editor);
        form.add(new AjaxSubmitLink("add") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> myform) {
                super.onSubmit(target, myform);
                SshKeyManager sshKeyManager = OneDev.getInstance(SshKeyManager.class);
                SshKey sshKey = (SshKey) editor.getModelObject();
                
                try {
                    PublicKey pubEntry = SshKeyUtils.decodeSshPublicKey(sshKey.getContent());
                    String fingerPrint = KeyUtils.getFingerPrint(SshKeyUtils.MD5_DIGESTER, pubEntry);
                    sshKey.setDigest(fingerPrint);
                } catch (IOException | GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
                
                sshKey.setOwner(user);
                sshKey.setTimestamp(LocalDateTime.now());
                
                sshKeyManager.save(sshKey);
                
                modal.close();
                onSave(target);
            }
            
            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);
                target.add(form);
            }
        });
        
        form.add(new AjaxLink<Void>("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                modal.close();
            }
        });
        
        add(form.setOutputMarkupId(true));
    }
    
    protected abstract void onSave(AjaxRequestTarget target);
}
