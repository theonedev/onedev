package com.pmease.gitop.web.common.wicket.bootstrap;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

/**
 * Add html message support
 */
@SuppressWarnings("serial")
public class Alert extends de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Alert {

	private Component message;
	
	/**
     * Constructor.
     *
     * @param id      the wicket component id.
     * @param message the alert message
     */
    public Alert(String id, IModel<String> message) {
    	super(id, message);
    }

    /**
     * Constructor.
     *
     * @param id      the wicket component id.
     * @param message the alert message
     * @param header  the title of the alert message
     */
    public Alert(String id, IModel<String> message, IModel<String> header) {
        super(id, message, header);
    }
    
    @Override
	protected Component createMessage(final String markupId, final IModel<String> message) {
    	this.message = super.createMessage(markupId, message);
    	return this.message;
    }
    
    /**
     * Whether or not escape message model strings
     * 
     * @param b
     * @return
     */
    public Alert withHtmlMessage(boolean b) {
    	if (b) {
    		this.message.setEscapeModelStrings(false);
    	}
    	
    	return this;
    }
}