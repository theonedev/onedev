package com.pmease.gitop.web.editable.directory;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.wicket.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class DirectoryMultiChoiceEditContext extends PropertyEditContext {

    public DirectoryMultiChoiceEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

	@Override
    public Component renderForEdit(String componentId) {
		return new DirectoryChoiceEditor(componentId, this, true);
    }

    @SuppressWarnings("unchecked")
	@Override
    public Component renderForView(String componentId) {
        List<String> directories = (List<String>) getPropertyValue();
        if (directories != null && !directories.isEmpty()) {
            return new Label(componentId, StringUtils.join(directories, ", "));
        } else {
            return new Label(componentId, "<i>Not Defined</i>")
                    .setEscapeModelStrings(false);
        }
    }

}
