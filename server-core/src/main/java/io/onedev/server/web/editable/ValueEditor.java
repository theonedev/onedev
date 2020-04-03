package io.onedev.server.web.editable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;

@SuppressWarnings("serial")
public abstract class ValueEditor<T> extends FormComponentPanel<T> {

	public ValueEditor(String id, IModel<T> model) {
		super(id, model);
		setConvertedInput(model.getObject());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new EditableResourceReference()));
	}
	
	public void clearErrors() {
		visitComponentsPostOrder(this, new IVisitor<Component, Void>() {
			
			@Override
			public void component(Component formComponent, IVisit<Void> visit) {
				formComponent.getFeedbackMessages().clear();
			}
			
		});
	}

	protected abstract T convertInputToValue() throws ConversionException;
	
	@Override
	public void convertInput() {
		try {
			setConvertedInput(convertInputToValue());
		} catch (ConversionException e) {
			error(newValidationError(e));
		}
	}
	
	public final void error(Path path, String errorMessage) {
		PathNode node = path.takeNode();
		if (node != null)
			error(node, path, errorMessage);
		else
			error(errorMessage);
	}

	public void error(PathNode propertyNode, Path pathInProperty, String errorMessage) {
		error(Path.describe(propertyNode, pathInProperty) + ": " + errorMessage);
	}
	
}
