package com.pmease.gitop.web.editable.directory;

import java.io.Serializable;

import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class DirectorySingleChoiceEditContext extends PropertyEditContext {

    public DirectorySingleChoiceEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

	@Override
    public Object renderForEdit(Object renderParam) {
		return new DirectoryChoiceEditor((String) renderParam, this, false);
    }

    @Override
    public Object renderForView(Object renderParam) {
        String directory = (String) getPropertyValue();
        if (directory != null) {
            return new Label((String) renderParam, directory);
        } else {
            return new Label((String) renderParam, "<i>Not Defined</i>")
                    .setEscapeModelStrings(false);
        }
    }

}
