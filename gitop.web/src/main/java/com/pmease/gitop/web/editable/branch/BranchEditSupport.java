package com.pmease.gitop.web.editable.branch;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.pmease.commons.editable.BeanEditContext;
import com.pmease.commons.editable.EditSupport;
import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.util.JavassistUtils;
import com.pmease.gitop.core.editable.BranchChoice;

public class BranchEditSupport implements EditSupport {

    @Override
    public BeanEditContext getBeanEditContext(Serializable bean) {
        return null;
    }

    @Override
    public PropertyEditContext getPropertyEditContext(Serializable bean, String propertyName) {
        Method propertyGetter = BeanUtils.getGetter(JavassistUtils.unproxy(bean.getClass()), propertyName);
        if (propertyGetter.getAnnotation(BranchChoice.class) != null) {
            return new BranchSingleChoiceEditContext(bean, propertyName);
        } else {
            return null;
        }
    }

    @Override
    public int getPriorty() {
        return 1;
    }

}
