package io.onedev.server.web.component.user.gpgkey;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.service.GpgKeyService;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.GpgKey;
import io.onedev.server.model.User;
import io.onedev.server.util.GpgUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.user.UserPage;

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
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                
                GpgKeyService gpgKeyService = OneDev.getInstance(GpgKeyService.class);
                GpgKey gpgKey = (GpgKey) editor.getModelObject();
                gpgKey.setOwner(getUser());
                gpgKey.setKeyId(gpgKey.getKeyIds().get(0));
                
                if (gpgKey.getKeyIds().stream().anyMatch(it->gpgKeyService.findSigningKey(it)!=null)) { 
					editor.error(new Path(new PathNode.Named("content")), _T("This key or one of its subkey is already in use"));
					target.add(form);
                } else {
                	boolean hasErrors = false;
                	for (String emailAddressValue: GpgUtils.getEmailAddresses(gpgKey.getPublicKeys().get(0))) {
                    	EmailAddress emailAddress = OneDev.getInstance(EmailAddressService.class).findByValue(emailAddressValue);
                    	if (emailAddress == null || !emailAddress.isVerified() || !emailAddress.getOwner().equals(getUser())) {
                    		editor.error(new Path(new PathNode.Named("content")), MessageFormat.format(_T("This key is associated with {0}, however it is NOT a verified email address of this user"), emailAddressValue));
                    		target.add(form);
                    		hasErrors = true;
                    		break;
                    	}
                	}
                	if (!hasErrors) {
                        gpgKey.setCreatedAt(new Date());
                        gpgKeyService.create(gpgKey);
                        if (getPage() instanceof UserPage)
							OneDev.getInstance(AuditService.class).audit(null, "added GPG key \"" + GpgUtils.getKeyIDString(gpgKey.getKeyId()) + "\" in account \"" + gpgKey.getOwner().getName() + "\"", null, null);
                        onSave(target);
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
