package com.pmease.gitplex.web.editable.branch;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.util.StringUtils;

import com.google.common.base.Preconditions;
import com.pmease.commons.editable.DefaultPropertyDescriptor;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.EditSupport;
import com.pmease.commons.wicket.editable.NotDefinedLabel;
import com.pmease.commons.wicket.editable.PropertyContext;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.commons.wicket.editable.PropertyViewer;
import com.pmease.gitplex.core.editable.BranchChoice;
import com.pmease.gitplex.core.model.RepoAndBranch;

@SuppressWarnings("serial")
public class BranchEditSupport implements EditSupport {

	@Override
	public BeanContext<?> getBeanEditContext(Class<?> beanClass) {
		return null;
	}

	@Override
	public PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName) {
		PropertyDescriptor propertyDescriptor = new DefaultPropertyDescriptor(beanClass, propertyName);
		Method propertyGetter = propertyDescriptor.getPropertyGetter();
        if (propertyGetter.getAnnotation(BranchChoice.class) != null) {
        	if (List.class.isAssignableFrom(propertyGetter.getReturnType()) 
        			&& EditableUtils.getElementClass(propertyGetter.getGenericReturnType()) == Long.class) {
        		return new PropertyContext<List<RepoAndBranch>>(propertyDescriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<List<RepoAndBranch>> model) {
						return new PropertyViewer(componentId, this) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        List<RepoAndBranch> repoAndBranches = model.getObject();
						        if (repoAndBranches != null && !repoAndBranches.isEmpty()) {
						        	List<String> branches = new ArrayList<>();
						        	for (RepoAndBranch each: repoAndBranches) {
										if (isAffinal(getPropertyGetter()) || isGlobal(getPropertyGetter()))
											branches.add(each.getFQN());
						        		else
						        			branches.add(each.getBranch());
						        	}
						            return new Label(id, StringUtils.join(branches, ", " ));
						        } else {
									return new NotDefinedLabel(id);
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<List<RepoAndBranch>> renderForEdit(String componentId, IModel<List<RepoAndBranch>> model) {
						if (isAffinal(getPropertyGetter()))
			        		return new AffinalBranchMultiChoiceEditor(componentId, this, model);
						else if (isGlobal(getPropertyGetter()))
							return new GlobalBranchMultiChoiceEditor(componentId, this, model);
			        	else
			        		return new LocalBranchMultiChoiceEditor(componentId, this, model);
					}
        			
        		};
        	} else if (propertyGetter.getReturnType() == Long.class) {
        		return new PropertyContext<RepoAndBranch>(propertyDescriptor) {

					@Override
					public PropertyViewer renderForView(String componentId, final IModel<RepoAndBranch> model) {
						return new PropertyViewer(componentId, this) {

							@Override
							protected Component newContent(String id, PropertyDescriptor propertyDescriptor) {
						        RepoAndBranch repoAndBranch = model.getObject();
						        if (repoAndBranch != null) {
									if (isAffinal(getPropertyGetter()) || isGlobal(getPropertyGetter()))
						        		return new Label(id, repoAndBranch.getFQN());
						        	else
						        		return new Label(id, repoAndBranch.getBranch());
						        } else {
									return new NotDefinedLabel(id);
						        }
							}
							
						};
					}

					@Override
					public PropertyEditor<RepoAndBranch> renderForEdit(String componentId, IModel<RepoAndBranch> model) {
						if (isAffinal(getPropertyGetter()))
			        		return new AffinalBranchSingleChoiceEditor(componentId, this, model);
						else if (isGlobal(getPropertyGetter()))
							return new GlobalBranchSingleChoiceEditor(componentId, this, model);
			        	else
			        		return new LocalBranchSingleChoiceEditor(componentId, this, model);
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

	private boolean isGlobal(Method propertyGetter) {
    	BranchChoice branchChoice = propertyGetter.getAnnotation(BranchChoice.class);
    	Preconditions.checkNotNull(branchChoice);
    	return branchChoice.value() == BranchChoice.Scope.GLOBAL;
	}

	private boolean isAffinal(Method propertyGetter) {
    	BranchChoice branchChoice = propertyGetter.getAnnotation(BranchChoice.class);
    	Preconditions.checkNotNull(branchChoice);
    	return branchChoice.value() == BranchChoice.Scope.AFFINAL;
	}
}
