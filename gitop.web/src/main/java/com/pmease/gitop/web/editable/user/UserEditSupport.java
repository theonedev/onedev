package com.pmease.gitop.web.editable.user;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.util.JavassistUtils;
import com.pmease.commons.wicket.editable.BeanEditContext;
import com.pmease.commons.wicket.editable.EditSupport;
import com.pmease.commons.wicket.editable.PropertyEditContext;
import com.pmease.gitop.core.editable.UserChoice;

public class UserEditSupport implements EditSupport {

    @Override
    public BeanEditContext getBeanEditContext(Serializable bean) {
        return null;
    }

    @Override
    public PropertyEditContext getPropertyEditContext(Serializable bean, String propertyName) {
        Method propertyGetter = BeanUtils.getGetter(JavassistUtils.unproxy(bean.getClass()), propertyName);
        if (propertyGetter.getAnnotation(UserChoice.class) != null) {
            return new UserSingleChoiceEditContext(bean, propertyName);
        } else {
            return null;
        }
    }

}
