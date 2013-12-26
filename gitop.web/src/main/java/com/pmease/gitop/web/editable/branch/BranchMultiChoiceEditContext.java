package com.pmease.gitop.web.editable.branch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.eclipse.jgit.util.StringUtils;

import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.BranchManager;

@SuppressWarnings("serial")
public class BranchMultiChoiceEditContext extends PropertyEditContext {

    public BranchMultiChoiceEditContext(Serializable bean, String propertyName) {
        super(bean, propertyName);
    }

	@Override
    public Object renderForEdit(Object renderParam) {
		return new BranchMultiChoiceEditor((String) renderParam, this);
    }

    @SuppressWarnings("unchecked")
	@Override
    public Object renderForView(Object renderParam) {
        Collection<Long> branchIds = (Collection<Long>) getPropertyValue();
        if (branchIds != null && !branchIds.isEmpty()) {
        	BranchManager branchManager = Gitop.getInstance(BranchManager.class);
        	List<String> branchNames = new ArrayList<>();
        	for (Long branchId: branchIds) {
        		branchNames.add(branchManager.load(branchId).getName());
        	}
            return new Label((String) renderParam, StringUtils.join(branchNames, ", " ));
        } else {
            return new Label((String) renderParam, "<i>Not Defined</i>")
                    .setEscapeModelStrings(false);
        }
    }

}
