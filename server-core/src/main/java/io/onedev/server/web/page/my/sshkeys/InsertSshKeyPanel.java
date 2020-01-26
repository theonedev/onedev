package io.onedev.server.web.page.my.sshkeys;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public class InsertSshKeyPanel extends Panel {

    private ModalPanel modal;

    public InsertSshKeyPanel(String id, ModalPanel modal) {
        super(id);
        this.modal = modal;
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

        add(new AjaxLink<Void>("cancel") {
            
            @Override
            public void onClick(AjaxRequestTarget target) {
                modal.close();
            }
        });
    }
}
