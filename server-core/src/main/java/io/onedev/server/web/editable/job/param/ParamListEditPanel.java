package io.onedev.server.web.editable.job.param;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.ValidationException;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.ReflectionUtils;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.ci.job.param.ScriptingValues;
import io.onedev.server.ci.job.param.SpecifiedValues;
import io.onedev.server.ci.job.param.ValuesProvider;
import io.onedev.server.ci.job.paramspec.ParamSpec;
import io.onedev.server.ci.job.paramspec.SecretParam;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.ParamSpecProvider;
import io.onedev.server.web.editable.annotation.Password;

@SuppressWarnings("serial")
class ParamListEditPanel extends PropertyEditor<List<Serializable>> {

	private static final Logger logger = LoggerFactory.getLogger(ParamListEditPanel.class);
	
	private final Map<String, JobParam> params = new HashMap<>();
	
	private final String paramSpecProviderMethodName;
	
	private transient Map<String, ParamSpec> paramSpecs;
	
	private transient Serializable defaultParamBean;
	
	public ParamListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		ParamSpecProvider paramSpecProvider = Preconditions.checkNotNull(
				propertyDescriptor.getPropertyGetter().getAnnotation(ParamSpecProvider.class));
		paramSpecProviderMethodName = paramSpecProvider.value();
		
		for (Serializable each: model.getObject()) {
			JobParam param = (JobParam) each;
			params.put(param.getName(), param);
		}
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, ParamSpec> getParamSpecs() {
		if (paramSpecs == null) {
			ComponentContext.push(new ComponentContext(this));
			paramSpecs = new LinkedHashMap<>();
			try {
				for (ParamSpec paramSpec: (List<ParamSpec>) ReflectionUtils.invokeStaticMethod(
						getDescriptor().getBeanClass(), paramSpecProviderMethodName)) {
					paramSpecs.put(paramSpec.getName(), paramSpec);
				}
			} finally {
				ComponentContext.pop();
			}
		}
		return paramSpecs;
	}
	
	private Serializable getDefaultParamBean() {
		if (defaultParamBean == null) {
			try {
				defaultParamBean = JobParam.defineBeanClass(new ArrayList<>(getParamSpecs().values())).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return defaultParamBean;
	}

	private Component newParamsView(Class<?> paramBeanClass) {
		RepeatingView paramsView = new RepeatingView("params");
		paramsView.setDefaultModel(Model.of(paramBeanClass.getName()));
		BeanDescriptor beanDescriptor = new BeanDescriptor(paramBeanClass);
		for (List<PropertyDescriptor> groupProperties: beanDescriptor.getProperties().values()) {
			for (PropertyDescriptor property: groupProperties) {
				WebMarkupContainer item = new WebMarkupContainer(paramsView.newChildId());
				item.add(new Label("name", property.getDisplayName()));

				String required;
				if (property.isPropertyRequired() 
						&& property.getPropertyClass() != boolean.class
						&& property.getPropertyClass() != Boolean.class) {
					required = "*";
				} else {
					required = "&nbsp;";
				}
				
				item.add(new Label("required", required).setEscapeModelStrings(false));
				
				boolean isSecret = property.getPropertyGetter().getAnnotation(Password.class) != null;

				List<String> choices = new ArrayList<>();
				if (isSecret) {
					choices.add(SpecifiedValues.SECRET_DISPLAY_NAME);
					choices.add(ScriptingValues.SECRET_DISPLAY_NAME);
				} else {
					choices.add(SpecifiedValues.DISPLAY_NAME);
					choices.add(ScriptingValues.DISPLAY_NAME);
				}
				DropDownChoice<String> valuesProviderChoice = new DropDownChoice<String>("valuesProvider", new IModel<String>() {
					
					@Override
					public void detach() {
					}

					@Override
					public String getObject() {
						if (item.get("values") instanceof Fragment) 
							return isSecret?SpecifiedValues.SECRET_DISPLAY_NAME:SpecifiedValues.DISPLAY_NAME;
						else 
							return isSecret?ScriptingValues.SECRET_DISPLAY_NAME:ScriptingValues.DISPLAY_NAME;
					}

					@Override
					public void setObject(String object) {
						ValuesProvider valuesProvider;
						if (object.equals(SpecifiedValues.DISPLAY_NAME) || object.endsWith(SpecifiedValues.SECRET_DISPLAY_NAME)) 
							valuesProvider = newSpecifiedValueProvider(property);
						else  
							valuesProvider = new ScriptingValues();
						item.replace(newValuesEditor(property, valuesProvider));
					}
					
				}, choices);
				
				valuesProviderChoice.setNullValid(false);
				
				valuesProviderChoice.add(new AjaxFormComponentUpdatingBehavior("change"){

					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						onPropertyUpdating(target);
						target.add(item.get("values"));
					}
					
				});
				item.add(valuesProviderChoice);
				
				JobParam param = params.get(property.getDisplayName());
				if (param != null)
					item.add(newValuesEditor(property, param.getValuesProvider()));
				else
					item.add(newValuesEditor(property, newSpecifiedValueProvider(property)));
				
				paramsView.add(item);
			}
		}
		
		return paramsView;
	}
	
	@Override
	protected void onBeforeRender() {
		Component paramsView = get("params");
		String paramBeanClassName = (String) paramsView.getDefaultModelObject();
		if (!paramBeanClassName.equals(getDefaultParamBean().getClass().getName()))
			replace(newParamsView(getDefaultParamBean().getClass()));
		super.onBeforeRender();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newParamsView(getDefaultParamBean().getClass()));
		
		add(new INullAcceptingValidator<List<Serializable>>() {

			@SuppressWarnings("deprecation")
			@Override
			public void validate(IValidatable<List<Serializable>> validatable) {
				int index = 0;
				for (Serializable each: validatable.getValue()) {
					JobParam param = (JobParam) each;
					if (param.getValuesProvider() instanceof SpecifiedValues) {
						SpecifiedValues specifiedValues = (SpecifiedValues) param.getValuesProvider();
						try {
							JobParam.validateParamValues(specifiedValues.getValues());
						} catch (ValidationException e) {
							if (!getFlag(FLAG_RENDERING)) {
								RepeatingView paramsView = (RepeatingView) get("params");
								paramsView.get(index).get("values").error(e.getMessage());
							}
						}
					}
					index++;
				}
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	private SpecifiedValues newSpecifiedValueProvider(PropertyDescriptor property) {
		SpecifiedValues specifiedValues = new SpecifiedValues();
		Object typedValue = property.getPropertyValue(getDefaultParamBean());
		ParamSpec paramSpec = getParamSpecs().get(property.getDisplayName());
		Preconditions.checkNotNull(paramSpec);
		List<String> strings = paramSpec.convertToStrings(typedValue);
		List<List<String>> values = new ArrayList<>();
		values.add(strings);
		specifiedValues.setValues(values);
		return specifiedValues;
	}
	
	private Component newSpecifiedValueEditor(String componentId, PropertyDescriptor property, @Nullable List<String> value) {
		WebMarkupContainer item = new WebMarkupContainer(componentId);
		ParamSpec paramSpec = Preconditions.checkNotNull(getParamSpecs().get(property.getDisplayName()));
		
		Serializable paramBean;
		try {
			paramBean = getDefaultParamBean().getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		try {
			if (value != null) 
				property.setPropertyValue(paramBean, paramSpec.convertToObject(value));
		} catch (Exception e) {
			logger.error("Error setting property value", e);
		}
		
		if (property.getPropertyGetter().getAnnotation(Password.class) == null) {
			item.add(PropertyContext.edit("value", paramBean, property.getPropertyName()));
			item.add(new WebMarkupContainer("description").setVisible(false));
		} else { 
			SecretEditBean bean = new SecretEditBean();
			bean.setSecret((String) property.getPropertyValue(paramBean));
			item.add(PropertyContext.edit("value", bean, "secret"));
			item.add(new Label("description", "Secrets can be defined in project setting"));
		}			
		
		item.add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				item.remove();
				String script = String.format(""
						+ "var $item=$('#%s');"
						+ "onedev.server.form.markDirty($item.closest('form'));" 
						+ "$item.remove();",
						item.getMarkupId());
				target.appendJavaScript(script);
				onPropertyUpdating(target);
			}

		});
		
		item.add(new FencedFeedbackPanel("feedback", item));
		
		item.setOutputMarkupId(true);
		return item;
	}
	
	private Component newValuesEditor(PropertyDescriptor property, ValuesProvider valuesProvider) {
		if (valuesProvider instanceof SpecifiedValues) {
			SpecifiedValues specifiedValues = (SpecifiedValues) valuesProvider;
			Fragment fragment = new Fragment("values", "specifiedValuesFrag", this);
			RepeatingView valuesView = new RepeatingView("values");
			fragment.add(valuesView);

			try {
				for (List<String> value: specifiedValues.getValues())
					valuesView.add(newSpecifiedValueEditor(valuesView.newChildId(), property, value));
			} catch (Exception e) {
				logger.error("Error creating value editors", e);
			}
			
			fragment.add(new FencedFeedbackPanel("feedback", fragment));

			fragment.add(new AjaxLink<Void>("add") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					Component newSpecifiedValueEditor = newSpecifiedValueEditor(valuesView.newChildId(), 
							property, null);
					valuesView.add(newSpecifiedValueEditor);
					String script = String.format(""
							+ "var $editor = $('#%s');"
							+ "$editor.before('<li id=\"%s\"></li>');"
							+ "onedev.server.form.markDirty($editor.closest('form'));", 
							getMarkupId(), newSpecifiedValueEditor.getMarkupId());
					target.prependJavaScript(script);
					target.add(newSpecifiedValueEditor);
					
					onPropertyUpdating(target);
				}

			});
			
			fragment.setOutputMarkupId(true);
			return fragment;
		} else {
			return PropertyContext.edit("values", valuesProvider, "scriptName").setOutputMarkupId(true);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Serializable> convertInputToValue() throws ConversionException {
		List<Serializable> value = new ArrayList<>();
		for (Component paramItem: (WebMarkupContainer)get("params")) {
			Label label = (Label) paramItem.get("name");
			JobParam param = new JobParam();
			param.setName((String) label.getDefaultModelObject());
			ParamSpec paramSpec = Preconditions.checkNotNull(getParamSpecs().get(param.getName()));
			param.setSecret(paramSpec instanceof SecretParam);
			Component valuesEditor = paramItem.get("values");
			if (valuesEditor instanceof PropertyEditor) {
				ScriptingValues scriptingValues = new ScriptingValues();
				scriptingValues.setScriptName((String) ((PropertyEditor<Serializable>) valuesEditor).getConvertedInput()); 
				param.setValuesProvider(scriptingValues);
			} else {
				SpecifiedValues specifiedValues = new SpecifiedValues();
				for (Component valueItem: (WebMarkupContainer)valuesEditor.get("values")) {
					Object propertyValue = ((PropertyEditor<Serializable>) valueItem.get("value")).getConvertedInput();
					specifiedValues.getValues().add(paramSpec.convertToStrings(propertyValue));
				}
				param.setValuesProvider(specifiedValues);
			}
			value.add(param);
		}
		return value;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ParamListCssResourceReference()));
		validate();
		if (!getModelObject().equals(getConvertedInput())) {
			String script = String.format("onedev.server.form.markDirty($('#%s').closest('form'));", getMarkupId());
			response.render(OnDomReadyHeaderItem.forScript(script));
		}
	}
	
}
