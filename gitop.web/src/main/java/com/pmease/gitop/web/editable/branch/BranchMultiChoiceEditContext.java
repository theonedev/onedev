package com.pmease.gitop.web.editable.branch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.eclipse.jgit.util.StringUtils;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.editable.PropertyEditContext;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Branch;

@SuppressWarnings("serial")
public class BranchMultiChoiceEditContext extends PropertyEditContext {

    public BranchMultiChoiceEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

	@Override
    public Component renderForEdit(String componentId) {
		return new BranchMultiChoiceEditor(componentId, this);
    }

    @SuppressWarnings("unchecked")
	@Override
    public Component renderForView(String componentId) {
        Collection<Long> branchIds = (Collection<Long>) getPropertyValue();
        if (branchIds != null && !branchIds.isEmpty()) {
        	Dao dao = Gitop.getInstance(Dao.class);
        	List<String> branchNames = new ArrayList<>();
        	for (Long branchId: branchIds) {
        		branchNames.add(dao.load(Branch.class, branchId).getName());
        	}
            return new Label(componentId, StringUtils.join(branchNames, ", " ));
        } else {
            return new Label(componentId, "<i>Not Defined</i>")
                    .setEscapeModelStrings(false);
        }
    }

}
