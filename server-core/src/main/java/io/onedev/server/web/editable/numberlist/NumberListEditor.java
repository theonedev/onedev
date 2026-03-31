package io.onedev.server.web.editable.numberlist;

import static io.onedev.server.util.ReflectionUtils.getCollectionElementClass;
import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

public class NumberListEditor extends PropertyEditor<List<Number>> {

	private TextField<String> input;

	private final Class<?> elementClass;

	public NumberListEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<Number>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
		elementClass = getCollectionElementClass(
				propertyDescriptor.getPropertyGetter().getGenericReturnType());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<Number> numbers = getModelObject();
		String value = null;
		if (numbers != null && !numbers.isEmpty()) {
			value = numbers.stream()
					.map(Object::toString)
					.collect(Collectors.joining(" "));
		}
		input = new TextField<>("input", Model.of(value));
		add(input);
		input.setLabel(Model.of(_T(getDescriptor().getDisplayName())));

		input.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}

		});

		input.add(newPlaceholderModifier());
	}

	@Override
	protected List<Number> convertInputToValue() throws ConversionException {
		List<Number> numbers = new ArrayList<>();
		String text = input.getConvertedInput();
		if (StringUtils.isNotBlank(text)) {
			for (String token : StringUtils.splitAndTrim(text, " ")) {
				try {
					if (elementClass == Long.class)
						numbers.add(Long.valueOf(token));
					else
						numbers.add(Integer.valueOf(token));
				} catch (NumberFormatException e) {
					throw new ConversionException("Invalid number: " + token);
				}
			}
		}
		return numbers;
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
