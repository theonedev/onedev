package io.onedev.server.web.page.my.sshkeys;

import java.time.LocalDateTime;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.util.convert.IConverter;

import io.onedev.server.OneDev;
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

        Form<SshKey> form = new Form<SshKey>("form") {
            @Override
            protected void onSubmit() {
                super.onSubmit();
                
                SshKey sshKey = getModelObject();
                Dao dao = OneDev.getInstance(Dao.class);
                
                sshKey.setOwner(user);
                sshKey.setTimestamp(LocalDateTime.now());
                
                dao.persist(sshKey);
            }
        };
        
        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        
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
                modal.close();
                onSave(target);
            }
            
            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);
                target.add(feedbackPanel);
            }
        });
        
        TextArea<String> textArea = new TextArea<String>("content");

        form.add(new TextField<>("name").setRequired(true));
        form.add(textArea.setRequired(true));
        form.add(new HiddenField<String>("digest"){
            @SuppressWarnings("unchecked")
            @Override
            public <C> IConverter<C> getConverter(Class<C> type) {
                return (IConverter<C>) new SshConverter(textArea);
            }
        });
        
        form.add(feedbackPanel);
        
        form.setModel(new CompoundPropertyModel<SshKey>(new SshKey()));
        
        add(form);
    }
    
    protected abstract void onSave(AjaxRequestTarget target);
}
