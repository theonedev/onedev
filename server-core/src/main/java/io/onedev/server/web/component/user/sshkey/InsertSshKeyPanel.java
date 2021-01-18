package io.onedev.server.web.component.user.sshkey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Date;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.security.CipherUtils;
import io.onedev.server.ssh.SshKeyUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

@SuppressWarnings("serial")
public abstract class InsertSshKeyPanel extends Panel {

    public InsertSshKeyPanel(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        add(new AjaxLink<Void>("close") {

            @Override
            public void onClick(AjaxRequestTarget target) {
            	onCancel(target);
            }
            
        });
        
        Form<?> form = new Form<Void>("form");
        
        BeanEditor editor = BeanContext.edit("editor", new SshKey());
        form.add(editor);
        
        form.add(new AjaxButton("add") {
        	
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> myform) {
                super.onSubmit(target, myform);
                
                SshKeyManager sshKeyManager = OneDev.getInstance(SshKeyManager.class);
                SshKey sshKey = (SshKey) editor.getModelObject();
                
                try {
                    PublicKey pubEntry = SshKeyUtils.decodeSshPublicKey(sshKey.getContent());
                    String fingerPrint = KeyUtils.getFingerPrint(CipherUtils.DIGEST_FORMAT, pubEntry);
                    sshKey.setDigest(fingerPrint);
                } catch (IOException | GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
                
                if (sshKeyManager.findByDigest(sshKey.getDigest()) != null) {
					editor.error(new Path(new PathNode.Named("content")), "This key is already in use");
					target.add(form);
                } else {
                    sshKey.setOwner(getUser());
                    sshKey.setDate(new Date());
                    sshKeyManager.save(sshKey);
                    onSave(target);
                }
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
        		onCancel(target);
            }
            
        });
        
        add(form.setOutputMarkupId(true));
    }
    
    protected abstract User getUser();
    
    protected abstract void onSave(AjaxRequestTarget target);
    
    protected abstract void onCancel(AjaxRequestTarget target);

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SshKeyCssResourceReference()));
	}
    
}
