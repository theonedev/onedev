package io.onedev.server.web.editable.build.query;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.BuildQueryBehavior;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.BuildQuery;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public class BuildQueryEditor extends PropertyEditor<String> {
	
	private TextField<String> input;
	
	public BuildQueryEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
    	
		BuildQuery buildQuery = Preconditions.checkNotNull(
				descriptor.getPropertyGetter().getAnnotation(BuildQuery.class));
    	input = new TextField<String>("input", getModel());
        input.add(new BuildQueryBehavior(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				if (getPage() instanceof ProjectPage)
					return ((ProjectPage) getPage()).getProject();
				else
					return null;
			}
    		
    	}, buildQuery.withCurrentUserCriteria(), buildQuery.withUnfinishedCriteria()));
        
		input.setLabel(Model.of(getDescriptor().getDisplayName()));
        
        add(input);
		input.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

}
