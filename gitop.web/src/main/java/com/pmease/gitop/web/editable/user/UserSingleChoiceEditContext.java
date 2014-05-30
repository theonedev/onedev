package com.pmease.gitop.web.editable.user;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.editable.PropertyEditContext;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.User;

@SuppressWarnings("serial")
public class UserSingleChoiceEditContext extends PropertyEditContext {

    public UserSingleChoiceEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

	@Override
    public Component renderForEdit(String componentId) {
		return new UserSingleChoiceEditor(componentId, this);
    }

    @Override
    public Component renderForView(String componentId) {
        Long userId = (Long) getPropertyValue();
        if (userId != null) {
        	User user = Gitop.getInstance(Dao.class).load(User.class, userId);
            return new Label(componentId, user.getName());
        } else {
            return new Label(componentId, "<i>Not Defined</i>")
                    .setEscapeModelStrings(false);
        }
    }

}
