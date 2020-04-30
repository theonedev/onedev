package io.onedev.server.web.editable.issue.fieldsupply;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.model.support.issue.fieldspec.SecretField;
import io.onedev.server.model.support.issue.fieldsupply.FieldSupply;
import io.onedev.server.model.support.issue.fieldsupply.Ignore;
import io.onedev.server.model.support.issue.fieldsupply.ScriptingValue;
import io.onedev.server.model.support.issue.fieldsupply.SpecifiedValue;
import io.onedev.server.model.support.issue.fieldsupply.ValueProvider;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.JobSecretEditBean;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.FieldNamesProvider;
import io.onedev.server.web.editable.annotation.Password;

@SuppressWarnings("serial")
class FieldListEditPanel extends PropertyEditor<List<Serializable>> {

	private static final Logger logger = LoggerFactory.getLogger(FieldListEditPanel.class);
	
	private final String fieldNamesProviderMethodName;
	
	private final Map<String, FieldSupply> fields = new HashMap<>();
	
	private transient Serializable defaultFieldBean;
	
	private transient Map<String, FieldSpec> fieldSpecs;
	
	public FieldListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		FieldNamesProvider fieldNamesProvider = Preconditions.checkNotNull(
				propertyDescriptor.getPropertyGetter().getAnnotation(FieldNamesProvider.class));
		fieldNamesProviderMethodName = fieldNamesProvider.value();
		
		for (Serializable each: model.getObject()) {
			FieldSupply param = (FieldSupply) each;
			fields.put(param.getName(), param);
		}
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, FieldSpec> getFieldSpecs() {
		if (fieldSpecs == null) {
			fieldSpecs = new HashMap<>();
			Collection<String> fieldNames = (Collection<String>) ReflectionUtils.invokeStaticMethod(
					getDescriptor().getBeanClass(), fieldNamesProviderMethodName);
			fieldSpecs = getIssueSetting().getFieldSpecMap(fieldNames);
		}
		return fieldSpecs;
	}
	
	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	private Serializable getDefaultFieldBean() {
		if (defaultFieldBean == null) {
			Class<?> fieldBeanClass = IssueUtils.defineFieldBeanClass(Project.get());
			try {
				defaultFieldBean = (Serializable) fieldBeanClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return defaultFieldBean;
	}

	private Component newFieldsView(Class<?> fieldBeanClass) {
		RepeatingView fieldsView = new RepeatingView("fields");
		fieldsView.setDefaultModel(Model.of(fieldBeanClass.getName()));
		BeanDescriptor beanDescriptor = new BeanDescriptor(fieldBeanClass);
		for (List<PropertyDescriptor> groupProperties: beanDescriptor.getProperties().values()) {
			for (PropertyDescriptor property: groupProperties) {
				if (getFieldSpecs().containsKey(property.getDisplayName())) {
					WebMarkupContainer container = new WebMarkupContainer(fieldsView.newChildId());
					FieldSupply field = fields.get(property.getDisplayName());
					if (field != null) {
						container.add(newValueEditor("value", property, field.getValueProvider()));
						container.setDefaultModel(Model.of(field.getValueProvider().getClass()));
					} else {
						container.add(newValueEditor("value", property, newSpecifiedValueProvider(property)));
						container.setDefaultModel(Model.of(SpecifiedValue.class));
					}
					
					container.add(new Label("name", property.getDisplayName()));

					String required;
					if (property.isPropertyRequired() 
							&& property.getPropertyClass() != boolean.class
							&& property.getPropertyClass() != Boolean.class) {
						required = "*";
					} else {
						required = "&nbsp;";
					}
					
					container.add(new Label("required", required).setEscapeModelStrings(false));
					
					boolean isSecret = property.getPropertyGetter().getAnnotation(Password.class) != null;

					List<String> choices = new ArrayList<>();
					if (isSecret) {
						choices.add(SpecifiedValue.SECRET_DISPLAY_NAME);
						choices.add(ScriptingValue.SECRET_DISPLAY_NAME);
						choices.add(Ignore.DISPLAY_NAME);
					} else {
						choices.add(SpecifiedValue.DISPLAY_NAME);
						choices.add(ScriptingValue.DISPLAY_NAME);
						choices.add(Ignore.DISPLAY_NAME);
					}
					DropDownChoice<String> valueProviderChoice = new DropDownChoice<String>("valueProvider", new IModel<String>() {
						
						@Override
						public void detach() {
						}

						@Override
						public String getObject() {
							Class<?> valueProviderClass = (Class<?>) container.getDefaultModelObject();
							if (valueProviderClass == SpecifiedValue.class)
								return isSecret?SpecifiedValue.SECRET_DISPLAY_NAME:SpecifiedValue.DISPLAY_NAME;
							else if (valueProviderClass == ScriptingValue.class)
								return isSecret?ScriptingValue.SECRET_DISPLAY_NAME:ScriptingValue.DISPLAY_NAME;
							else
								return Ignore.DISPLAY_NAME;
						}

						@Override
						public void setObject(String object) {
							ValueProvider valueProvider;
							if (object.equals(SpecifiedValue.DISPLAY_NAME) || object.equals(SpecifiedValue.SECRET_DISPLAY_NAME))  
								valueProvider = newSpecifiedValueProvider(property);
							else if (object.equals(ScriptingValue.DISPLAY_NAME) || object.equals(ScriptingValue.SECRET_DISPLAY_NAME))
								valueProvider = new ScriptingValue();
							else
								valueProvider = new Ignore();
							container.replace(newValueEditor("value", property, valueProvider));
							container.setDefaultModelObject(valueProvider.getClass());
						}
						
					}, choices);
					
					valueProviderChoice.setNullValid(false);
					
					valueProviderChoice.add(new AjaxFormComponentUpdatingBehavior("change"){

						@Override
						protected void onUpdate(AjaxRequestTarget target) {
							onPropertyUpdating(target);
							target.add(container);
						}
						
					});
					container.add(valueProviderChoice);
					
					container.add(new Label("description", property.getDescription()).setEscapeModelStrings(false));
					container.add(new FencedFeedbackPanel("feedback", container));
					container.setOutputMarkupId(true);
					fieldsView.add(container);
				}
			}
		}
		
		return fieldsView;
	}
	
	@Override
	protected void onBeforeRender() {
		Component fieldsView = get("fields");
		String fieldBeanClassName = (String) fieldsView.getDefaultModelObject();
		if (!fieldBeanClassName.equals(getDefaultFieldBean().getClass().getName()))
			replace(newFieldsView(getDefaultFieldBean().getClass()));
		super.onBeforeRender();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newFieldsView(getDefaultFieldBean().getClass()));
		
		setOutputMarkupId(true);
	}
	
	private SpecifiedValue newSpecifiedValueProvider(PropertyDescriptor property) {
		SpecifiedValue specifiedValue = new SpecifiedValue();
		Object typedValue = property.getPropertyValue(getDefaultFieldBean());
		FieldSpec fieldSpec = getFieldSpecs().get(property.getDisplayName());
		Preconditions.checkNotNull(fieldSpec);
		specifiedValue.setValue(fieldSpec.convertToStrings(typedValue));
		return specifiedValue;
	}
	
	private Component newValueEditor(String componentId, PropertyDescriptor property, ValueProvider valueProvider) {
		if (valueProvider instanceof SpecifiedValue) {
			SpecifiedValue specifiedValue = (SpecifiedValue) valueProvider;
			List<String> value = specifiedValue.getValue();
			FieldSpec fieldSpec = Preconditions.checkNotNull(getFieldSpecs().get(property.getDisplayName()));
			
			Serializable fieldBean;
			try {
				fieldBean = getDefaultFieldBean().getClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			
			try {
				if (value != null) 
					property.setPropertyValue(fieldBean, fieldSpec.convertToObject(value));
			} catch (Exception e) {
				logger.error("Error setting property value", e);
			}
			
			if (property.getPropertyGetter().getAnnotation(Password.class) == null) {
				return PropertyContext.edit("value", fieldBean, property.getPropertyName());
			} else { 
				JobSecretEditBean bean = new JobSecretEditBean();
				bean.setSecret((String) property.getPropertyValue(fieldBean));
				return PropertyContext.edit("value", bean, "secret");
			}	
		} else if (valueProvider instanceof ScriptingValue) {
			return PropertyContext.edit("value", valueProvider, "scriptName");
		} else {
			return new WebMarkupContainer("value");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Serializable> convertInputToValue() throws ConversionException {
		List<Serializable> value = new ArrayList<>();
		for (Component container: (WebMarkupContainer)get("fields")) {
			Label label = (Label) container.get("name");
			FieldSupply field = new FieldSupply();
			field.setName((String) label.getDefaultModelObject());
			FieldSpec fieldSpec = Preconditions.checkNotNull(getFieldSpecs().get(field.getName()));
			field.setSecret(fieldSpec instanceof SecretField);
			if (container.get("value") instanceof PropertyEditor) {
				PropertyEditor<Serializable> propertyEditor = (PropertyEditor<Serializable>) container.get("value");
				Class<?> valueProviderClass = (Class<?>) container.getDefaultModelObject();
				if (valueProviderClass == SpecifiedValue.class) {
					SpecifiedValue specifiedValue = new SpecifiedValue();
					Object propertyValue = propertyEditor.getConvertedInput();
					specifiedValue.setValue(fieldSpec.convertToStrings(propertyValue));
					field.setValueProvider(specifiedValue);
				} else {
					ScriptingValue scriptingValue = new ScriptingValue();
					scriptingValue.setScriptName((String) propertyEditor.getConvertedInput()); 
					field.setValueProvider(scriptingValue);
				} 
			} else {
				field.setValueProvider(new Ignore());
			}
			value.add(field);
		}
		return value;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new FieldListCssResourceReference()));
		validate();
		if (!getModelObject().equals(getConvertedInput())) {
			String script = String.format("onedev.server.form.markDirty($('#%s').closest('form'));", getMarkupId());
			response.render(OnDomReadyHeaderItem.forScript(script));
		}
	}
	
}
