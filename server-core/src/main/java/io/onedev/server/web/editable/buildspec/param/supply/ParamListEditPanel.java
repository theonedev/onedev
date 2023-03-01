package io.onedev.server.web.editable.buildspec.param.supply;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
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
import com.google.common.collect.Lists;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.param.ParamUtils;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.param.spec.SecretParam;
import io.onedev.server.buildspec.param.supply.Ignore;
import io.onedev.server.buildspec.param.supply.ParamSupply;
import io.onedev.server.buildspec.param.supply.PassthroughValues;
import io.onedev.server.buildspec.param.supply.ScriptingValues;
import io.onedev.server.buildspec.param.supply.SpecifiedValues;
import io.onedev.server.buildspec.param.supply.ValuesProvider;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.behavior.InterpolativeAssistBehavior;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.JobSecretEditBean;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.annotation.ParamSpecProvider;
import io.onedev.server.annotation.Password;
import io.onedev.server.annotation.VariableOption;
import io.onedev.server.web.editable.buildspec.job.trigger.JobTriggerEditPanel;
import io.onedev.server.web.editable.string.StringPropertyEditor;

@SuppressWarnings("serial")
class ParamListEditPanel extends PropertyEditor<List<Serializable>> {

	private static final Logger logger = LoggerFactory.getLogger(ParamListEditPanel.class);
	
	private final Map<String, ParamSupply> params = new HashMap<>();
	
	private final String paramSpecProviderMethodName;
	
	private final boolean withBuildVersion;
	
	private final boolean withDynamicVariables;
	
	private transient Map<String, ParamSpec> paramSpecs;
	
	private transient Serializable defaultParamBean;
	
	public ParamListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		ParamSpecProvider paramSpecProvider = Preconditions.checkNotNull(
				propertyDescriptor.getPropertyGetter().getAnnotation(ParamSpecProvider.class));
		paramSpecProviderMethodName = paramSpecProvider.value();
		
		for (Serializable each: model.getObject()) {
			ParamSupply param = (ParamSupply) each;
			params.put(param.getName(), param);
		}
		
		VariableOption variableOption = propertyDescriptor.getPropertyGetter().getAnnotation(VariableOption.class);
		if (variableOption != null) {
			withBuildVersion = variableOption.withBuildVersion();
			withDynamicVariables = variableOption.withDynamicVariables();
		} else {
			withBuildVersion = true;
			withDynamicVariables = true;
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
				defaultParamBean = ParamUtils.defineBeanClass(new ArrayList<>(getParamSpecs().values()))
						.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
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
				WebMarkupContainer container = new WebMarkupContainer(paramsView.newChildId());
				container.add(new Label("name", property.getDisplayName()));

				ParamSupply param = params.get(property.getDisplayName());
				if (param != null) {
					container.add(newValuesEditor("values", property, param.getValuesProvider()));
					container.setDefaultModel(Model.of(param.getValuesProvider().getClass()));
				} else {
					container.add(newValuesEditor("values", property, newSpecifiedValuesProvider(property)));
					container.setDefaultModel(Model.of(SpecifiedValues.class));
				}

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
					choices.add(SpecifiedValues.SECRET_DISPLAY_NAME);
					if (findParent(JobTriggerEditPanel.class) == null)
						choices.add(PassthroughValues.DISPLAY_NAME);
					choices.add(ScriptingValues.SECRET_DISPLAY_NAME);
					choices.add(Ignore.DISPLAY_NAME);
				} else {
					choices.add(SpecifiedValues.DISPLAY_NAME);
					if (findParent(JobTriggerEditPanel.class) == null)
						choices.add(PassthroughValues.DISPLAY_NAME);
					choices.add(ScriptingValues.DISPLAY_NAME);
					choices.add(Ignore.DISPLAY_NAME);
				}
				DropDownChoice<String> valuesProviderChoice = new DropDownChoice<String>("valuesProvider", new IModel<String>() {
					
					@Override
					public void detach() {
					}

					@Override
					public String getObject() {
						Class<?> valuesProviderClass = (Class<?>) container.getDefaultModelObject();
						if (valuesProviderClass == SpecifiedValues.class)
							return isSecret?SpecifiedValues.SECRET_DISPLAY_NAME:SpecifiedValues.DISPLAY_NAME;
						else if (valuesProviderClass == ScriptingValues.class)
							return isSecret?ScriptingValues.SECRET_DISPLAY_NAME:ScriptingValues.DISPLAY_NAME;
						else if (valuesProviderClass == PassthroughValues.class)
							return PassthroughValues.DISPLAY_NAME;
						else
							return Ignore.DISPLAY_NAME;
					}

					@Override
					public void setObject(String object) {
						ValuesProvider valuesProvider;
						if (object.equals(SpecifiedValues.DISPLAY_NAME) || object.equals(SpecifiedValues.SECRET_DISPLAY_NAME))  
							valuesProvider = newSpecifiedValuesProvider(property);
						else if (object.equals(ScriptingValues.DISPLAY_NAME) || object.equals(ScriptingValues.SECRET_DISPLAY_NAME))
							valuesProvider = new ScriptingValues();
						else if (object.equals(PassthroughValues.DISPLAY_NAME))
							valuesProvider = new PassthroughValues();
						else
							valuesProvider = new Ignore();
						container.replace(newValuesEditor("values", property, valuesProvider));
						container.setDefaultModelObject(valuesProvider.getClass());
					}
										
				}, choices);
				
				valuesProviderChoice.setNullValid(false);
				
				valuesProviderChoice.add(new AjaxFormComponentUpdatingBehavior("change"){

					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						onPropertyUpdating(target);
						target.add(container);
					}
					
				});
				container.add(valuesProviderChoice);
				container.add(new Label("description", property.getDescription()).setEscapeModelStrings(false));
				container.add(new FencedFeedbackPanel("feedback", container));
				container.setOutputMarkupId(true);
				paramsView.add(container);
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
					ParamSupply param = (ParamSupply) each;
					if (param.getValuesProvider() instanceof SpecifiedValues) {
						SpecifiedValues specifiedValues = (SpecifiedValues) param.getValuesProvider();
						try {
							ParamUtils.validateParamValues(specifiedValues.getValues());
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
	
	private SpecifiedValues newSpecifiedValuesProvider(PropertyDescriptor property) {
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
			paramBean = getDefaultParamBean().getClass().getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		
		try {
			if (value != null) 
				property.setPropertyValue(paramBean, paramSpec.convertToObject(value));
		} catch (Exception e) {
			logger.error("Error setting property value", e);
		}
		
		if (property.getPropertyGetter().getAnnotation(Password.class) == null) {
			PropertyEditor<?> propertyEditor = PropertyContext.edit("value", paramBean, property.getPropertyName());
			item.add(propertyEditor);
			if (propertyEditor instanceof StringPropertyEditor) {
				((StringPropertyEditor) propertyEditor).setInputAssist(new InterpolativeAssistBehavior() {

					@Override
					protected List<InputSuggestion> suggestVariables(String matchWith) {
						return BuildSpec.suggestVariables(matchWith, withBuildVersion, withDynamicVariables, false);
					}
					
					@Override
					protected List<InputSuggestion> suggestLiterals(String matchWith) {
						return Lists.newArrayList();
					}
					
				});
			}
		} else { 
			JobSecretEditBean bean = new JobSecretEditBean();
			bean.setSecret((String) property.getPropertyValue(paramBean));
			item.add(PropertyContext.edit("value", bean, "secret"));
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
	
	private Component newValuesEditor(String componentId, PropertyDescriptor property, ValuesProvider valuesProvider) {
		if (valuesProvider instanceof SpecifiedValues) {
			SpecifiedValues specifiedValues = (SpecifiedValues) valuesProvider;
			Fragment fragment = new Fragment(componentId, "specifiedValuesFrag", this);
			RepeatingView valuesView = new RepeatingView("values");
			fragment.add(valuesView);
			
			String matrixDescription;
			if (getDescriptor().getBeanClass() == Job.class) {
				matrixDescription = "Add additional values to run matrix job. The job will run for each possible "
						+ "combination of different values across all parameters";				
			} else {
				matrixDescription = "Add additional values to run matrix step. The step will run for each possible "
						+ "combination of different values across all parameters";				
			}
			
			fragment.add(new Label("matrixDescription", matrixDescription));

			try {
				for (List<String> value: specifiedValues.getValues())
					valuesView.add(newSpecifiedValueEditor(valuesView.newChildId(), property, value));
			} catch (Exception e) {
				logger.error("Error creating value editors", e);
			}
			
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
		} else if (valuesProvider instanceof ScriptingValues) {
			return PropertyContext.edit(componentId, valuesProvider, "scriptName").setOutputMarkupId(true);
		} else if (valuesProvider instanceof PassthroughValues) {
			return PropertyContext.edit(componentId, valuesProvider, "paramName").setOutputMarkupId(true);
		} else {
			return new WebMarkupContainer(componentId);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Serializable> convertInputToValue() throws ConversionException {
		List<Serializable> value = new ArrayList<>();
		for (Component paramContainer: (WebMarkupContainer)get("params")) {
			Label label = (Label) paramContainer.get("name");
			ParamSupply param = new ParamSupply();
			param.setName((String) label.getDefaultModelObject());
			ParamSpec paramSpec = Preconditions.checkNotNull(getParamSpecs().get(param.getName()));
			param.setSecret(paramSpec instanceof SecretParam);
			Class<?> valuesProviderClass = (Class<?>) paramContainer.getDefaultModelObject();
			Component valuesEditor = paramContainer.get("values");
			if (valuesProviderClass == ScriptingValues.class) {
				ScriptingValues scriptingValues = new ScriptingValues();
				scriptingValues.setScriptName((String) ((PropertyEditor<Serializable>) valuesEditor).getConvertedInput()); 
				param.setValuesProvider(scriptingValues);
			} else if (valuesProviderClass == SpecifiedValues.class) {
				SpecifiedValues specifiedValues = new SpecifiedValues();
				for (Component valueContainer: (WebMarkupContainer)valuesEditor.get("values")) {
					Object propertyValue = ((PropertyEditor<Serializable>) valueContainer.get("value")).getConvertedInput();
					specifiedValues.getValues().add(paramSpec.convertToStrings(propertyValue));
				}
				param.setValuesProvider(specifiedValues);
			} else if (valuesProviderClass == PassthroughValues.class) {
				PassthroughValues passthroughValues = new PassthroughValues();
				passthroughValues.setParamName((String) ((PropertyEditor<Serializable>) valuesEditor).getConvertedInput()); 
				param.setValuesProvider(passthroughValues);
			} else {
				param.setValuesProvider(new Ignore());
			}
			value.add(param);
		}
		return value;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		validate();
		if (!getModelObject().equals(getConvertedInput())) {
			String script = String.format("onedev.server.form.markDirty($('#%s').closest('form'));", getMarkupId());
			response.render(OnDomReadyHeaderItem.forScript(script));
		}
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}
	
}
