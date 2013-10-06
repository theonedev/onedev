package com.pmease.gitop.web.common.form;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.model.AbstractReadOnlyModel;

public class FeedbackPanel extends org.apache.wicket.markup.html.panel.FeedbackPanel {
    
    private static final long serialVersionUID = 1L;

    public FeedbackPanel(final String id) {
        super(id);
    }
    
    public FeedbackPanel(final String id, Component component) {
        super(id);
        this.setFilter(new ComponentFeedbackMessageFilter(component));
    }
    
    public FeedbackPanel(final String id, IFeedbackMessageFilter filter) {
        super(id, filter);
    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        this.setOutputMarkupId(true);
        add(AttributeModifier.append("class", new AbstractReadOnlyModel<String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                return getCssClass();
            }
            
        }));
    }
    @Override
    protected void onConfigure() {
        super.onConfigure();
        
        this.setVisibilityAllowed(this.anyMessage());
    }
    
    public String getCssClass() {
        List<FeedbackMessage> msgs = getCurrentMessages();

        FeedbackMessage maxLevelMsg = null;
        for (FeedbackMessage msg : msgs) {
            if (maxLevelMsg == null || msg.getLevel() > maxLevelMsg.getLevel()) {
                maxLevelMsg = msg;
            }
        }
        
        if (maxLevelMsg == null) {
            return "";
        } else {
            return "has-" + maxLevelMsg.getLevelAsString().toLowerCase();
        }
    }
}