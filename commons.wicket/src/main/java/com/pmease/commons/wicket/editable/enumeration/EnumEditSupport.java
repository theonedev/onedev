package com.pmease.commons.wicket.editable.enumeration;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.util.JavassistUtils;
import com.pmease.commons.wicket.editable.BeanEditContext;
import com.pmease.commons.wicket.editable.EditSupport;
import com.pmease.commons.wicket.editable.PropertyEditContext;

public class EnumEditSupport implements EditSupport {

    @Override
    public BeanEditContext getBeanEditContext(Serializable bean) {
        return null;
    }

    @Override
    public PropertyEditContext getPropertyEditContext(Serializable bean, String propertyName) {
        Method propertyGetter = BeanUtils.getGetter(JavassistUtils.unproxy(bean.getClass()), propertyName);
        if (Enum.class.isAssignableFrom(propertyGetter.getReturnType())) {
            return new EnumPropertyEditContext(bean, propertyName);
        } else {
            return null;
        }
    }

}
