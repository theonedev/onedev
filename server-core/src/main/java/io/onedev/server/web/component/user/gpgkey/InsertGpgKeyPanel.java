package io.onedev.server.web.component.user.gpgkey;

import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.GpgKeyManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.GpgKey;
import io.onedev.server.model.User;
import io.onedev.server.util.GpgUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.my.MyPage;

@SuppressWarnings("serial")
public abstract class InsertGpgKeyPanel extends Panel {

    public InsertGpgKeyPanel(String id) {
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
        
        BeanEditor editor = BeanContext.edit("editor", new GpgKey());
        form.add(editor);
        
        form.add(new AjaxButton("add") {
        	
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> myform) {
                super.onSubmit(target, myform);
                
                GpgKeyManager gpgKeyManager = OneDev.getInstance(GpgKeyManager.class);
                GpgKey gpgKey = (GpgKey) editor.getModelObject();
                gpgKey.setKeyId(gpgKey.getKeyIds().get(0));
                
                if (gpgKey.getKeyIds().stream().anyMatch(it->gpgKeyManager.findSignatureVerificationKey(it)!=null)) { 
					editor.error(new Path(new PathNode.Named("content")), "This key or one of its subkey is already in use");
					target.add(form);
                } else {
                	String emailAddressValue = GpgUtils.getEmailAddress(gpgKey.getPublicKeys().get(0));
                	EmailAddress emailAddress = OneDev.getInstance(EmailAddressManager.class).findByValue(emailAddressValue);
                	if (emailAddress != null && emailAddress.isVerified() && emailAddress.getOwner().equals(getUser())) {
                		gpgKey.setEmailAddress(emailAddress);
                        gpgKey.setCreatedAt(new Date());
                        gpgKeyManager.save(gpgKey);
                        onSave(target);
                	} else {
                		String who = (getPage() instanceof MyPage)? "yours": "the user";
                		editor.error(new Path(new PathNode.Named("content")), "This key is associated with " + emailAddressValue 
                				+ ", however it is NOT a verified email address of " + who);
                		target.add(form);
                	}
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
		response.render(CssHeaderItem.forReference(new GpgKeyCssResourceReference()));
	}
    
}
