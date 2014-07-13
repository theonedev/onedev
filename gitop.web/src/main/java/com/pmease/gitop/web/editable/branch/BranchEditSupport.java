package com.pmease.gitop.web.editable.branch;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.util.StringUtils;

import com.google.common.base.Preconditions;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.editable.PropertyDescriptorImpl;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.EditSupport;
import com.pmease.commons.wicket.editable.NotDefinedLabel;
import com.pmease.commons.wicket.editable.PropertyContext;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.commons.wicket.editable.PropertyViewer;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.editable.BranchChoice;
import com.pmease.gitop.model.Branch;

@SuppressWarnings("serial")
public class BranchEditSupport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass) {
		return null;
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptorImpl(beanClass, propertyName);
		Method propertyGetter = propertyDescriptor.getPropertyGetter();
        if (propertyGetter.getAnnotation(BranchChoice.class) != null) {
        	if (List.class.isAssignableFrom(propertyGetter.getReturnType()) 
        			&& EditableUtils.getElementClass(propertyGetter.getGenericReturnType()) == Long.class) {
        		return new PropertyContext<List<Long>>(propertyDescriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<Long>> model) {
						return new PropertyViewer(componentId, this) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        List<Long> branchIds = model.getObject();
						        if (branchIds != null && !branchIds.isEmpty()) {
						        	Dao dao = Gitop.getInstance(Dao.class);
						        	List<String> branchNames = new ArrayList<>();
						        	for (Long branchId: branchIds) {
										if (isAffinal(getPropertyGetter()))
						        			branchNames.add(dao.load(Branch.class, branchId).getFullName());
						        		else
						        			branchNames.add(dao.load(Branch.class, branchId).getName());
						        	}
						            return new Label(id, StringUtils.join(branchNames, ", " ));
						        } else {
									return new NotDefinedLabel(id);
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<List<Long>> renderForEdit(String componentId, IModel<List<Long>> model) {
						if (isAffinal(getPropertyGetter()))
			        		return new AffinalBranchMultiChoiceEditor(componentId, this, model);
			        	else
			        		return new BranchMultiChoiceEditor(componentId, this, model);
					}
        			
        		};
        	} else if (propertyGetter.getReturnType() == Long.class) {
        		return new PropertyContext<Long>(propertyDescriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<Long> model) {
						return new PropertyViewer(componentId, this) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        Long branchId = model.getObject();
						        if (branchId != null) {
						        	Branch branch = Gitop.getInstance(Dao.class).load(Branch.class, branchId);
									if (isAffinal(getPropertyGetter()))
						        		return new Label(id, branch.getFullName());
						        	else
						        		return new Label(id, branch.getName());
						        } else {
									return new NotDefinedLabel(id);
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<Long> renderForEdit(String componentId, IModel<Long> model) {
						if (isAffinal(getPropertyGetter()))
			        		return new AffinalBranchSingleChoiceEditor(componentId, this, model);
			        	else
			        		return new BranchSingleChoiceEditor(componentId, this, model);
					}
        			
        		};
        	} else {
        		throw new RuntimeException("Annotation 'BranchChoice' should be applied to property "
        				+ "with type 'Long' or 'List<Long>'.");
        	}
        } else {
            return null;
        }
	}

	private boolean isAffinal(Method propertyGetter) {
    	BranchChoice branchChoice = propertyGetter.getAnnotation(BranchChoice.class);
    	Preconditions.checkNotNull(branchChoice);
    	return branchChoice.value() == BranchChoice.Scope.AFFINAL;
	}
}
