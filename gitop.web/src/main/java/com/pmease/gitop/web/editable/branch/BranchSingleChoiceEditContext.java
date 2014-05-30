package com.pmease.gitop.web.editable.branch;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.editable.PropertyEditContext;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Branch;

@SuppressWarnings("serial")
public class BranchSingleChoiceEditContext extends PropertyEditContext {

    public BranchSingleChoiceEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

	@Override
    public Component renderForEdit(String componentId) {
		return new BranchSingleChoiceEditor(componentId, this);
    }

    @Override
    public Component renderForView(String componentId) {
        Long branchId = (Long)getPropertyValue();
        if (branchId != null) {
        	Branch branch = Gitop.getInstance(Dao.class).load(Branch.class, branchId);
            return new Label(componentId, branch.getName());
        } else {
            return new Label(componentId, "<i>Not Defined</i>")
                    .setEscapeModelStrings(false);
        }
    }

}
