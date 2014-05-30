package com.pmease.gitop.web.editable.directory;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.wicket.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class DirectorySingleChoiceEditContext extends PropertyEditContext {

    public DirectorySingleChoiceEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

	@Override
    public Component renderForEdit(String componentId) {
		return new DirectoryChoiceEditor(componentId, this, false);
    }

    @Override
    public Component renderForView(String componentId) {
        String directory = (String) getPropertyValue();
        if (directory != null) {
            return new Label(componentId, directory);
        } else {
            return new Label(componentId, "<i>Not Defined</i>")
                    .setEscapeModelStrings(false);
        }
    }

}
