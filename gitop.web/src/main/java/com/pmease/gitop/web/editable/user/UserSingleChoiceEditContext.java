package com.pmease.gitop.web.editable.user;

import java.io.Serializable;

import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;

@SuppressWarnings("serial")
public class UserSingleChoiceEditContext extends PropertyEditContext {

    public UserSingleChoiceEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

	@Override
    public Object renderForEdit(Object renderParam) {
		return new UserSingleChoiceEditor((String) renderParam, this);
    }

    @Override
    public Object renderForView(Object renderParam) {
        Long userId = (Long) getPropertyValue();
        if (userId != null) {
        	User user = Gitop.getInstance(UserManager.class).load(userId);
            return new Label((String) renderParam, user.getName());
        } else {
            return new Label((String) renderParam, "<i>Not Defined</i>")
                    .setEscapeModelStrings(false);
        }
    }

}
