package com.pmease.gitop.web.editable.branch;

import java.io.Serializable;

import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Branch;

@SuppressWarnings("serial")
public class BranchSingleChoiceEditContext extends PropertyEditContext {

    public BranchSingleChoiceEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

	@Override
    public Object renderForEdit(Object renderParam) {
		return new BranchSingleChoiceEditor((String) renderParam, this);
    }

    @Override
    public Object renderForView(Object renderParam) {
        Long branchId = (Long)getPropertyValue();
        if (branchId != null) {
        	Branch branch = Gitop.getInstance(Dao.class).load(Branch.class, branchId);
            return new Label((String) renderParam, branch.getName());
        } else {
            return new Label((String) renderParam, "<i>Not Defined</i>")
                    .setEscapeModelStrings(false);
        }
    }

}
