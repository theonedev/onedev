package com.pmease.gitop.web.editable.directory;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class DirectoryMultiChoiceEditContext extends PropertyEditContext {

    public DirectoryMultiChoiceEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

	@Override
    public Object renderForEdit(Object renderParam) {
		return new DirectoryChoiceEditor((String) renderParam, this, true);
    }

    @SuppressWarnings("unchecked")
	@Override
    public Object renderForView(Object renderParam) {
        List<String> directories = (List<String>) getPropertyValue();
        if (directories != null && !directories.isEmpty()) {
            return new Label((String) renderParam, StringUtils.join(directories, ", "));
        } else {
            return new Label((String) renderParam, "<i>Not Defined</i>")
                    .setEscapeModelStrings(false);
        }
    }

}
