package com.pmease.commons.wicket.component;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.LoadableDetachableModel;

@SuppressWarnings("serial")
public class FeedbackPanel extends org.apache.wicket.markup.html.panel.FeedbackPanel {
    
    public FeedbackPanel(final String id) {
        super(id);
    }
    
    public FeedbackPanel(final String id, Component component) {
        super(id, new ComponentFeedbackMessageFilter(component));
    }
    
    public FeedbackPanel(final String id, IFeedbackMessageFilter filter) {
        super(id, filter);
    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

            @Override
            public String load() {
                int maxLevel = FeedbackMessage.UNDEFINED;
                for (FeedbackMessage msg: getCurrentMessages()) {
                    if (msg.getLevel() > maxLevel) {
                        maxLevel = msg.getLevel();
                    }
                }
                
                String cssClasses = "alert fade in ";
                if (maxLevel >= FeedbackMessage.ERROR)
                	cssClasses += "alert-danger";
                else if (maxLevel >= FeedbackMessage.WARNING)
                	cssClasses += "alert-warning";
                else if (maxLevel >= FeedbackMessage.SUCCESS)
                	cssClasses += "alert-success";
                else
                	cssClasses += "alert-info";
                
                return cssClasses;
            }
            
        }));
    }
    
    @Override
    protected void onConfigure() {
        super.onConfigure();
        
        setVisible(anyMessage());
    }

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		String script = String.format("$('#%s').prepend(\"<button class='close' aria-hidden='true' data-dismiss='alert' type='button'>×</button>\");", 
				getMarkupId(true));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}