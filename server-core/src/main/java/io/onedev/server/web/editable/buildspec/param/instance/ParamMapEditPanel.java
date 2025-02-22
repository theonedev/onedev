package io.onedev.server.web.editable.buildspec.param.instance;

import com.google.common.collect.Lists;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.ParamSpecProvider;
import io.onedev.server.annotation.Password;
import io.onedev.server.annotation.VariableOption;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamUtils;
import io.onedev.server.buildspec.param.instance.*;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.param.spec.SecretParam;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.behavior.InterpolativeAssistBehavior;
import io.onedev.server.web.editable.*;
import io.onedev.server.web.editable.buildspec.job.trigger.JobTriggerEditPanel;
import io.onedev.server.web.editable.string.StringPropertyEditor;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.feedback.FencedFeedbackPanel;
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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

class ParamMapEditPanel extends PropertyEditor<List<Serializable>> {

	private static final Logger logger = LoggerFactory.getLogger(ParamMapEditPanel.class);
	
	private final Map<String, ParamInstance> params = new HashMap<>();
	
	private final String paramSpecProviderMethodName;
	
	private final boolean withBuildVersion;
	
	private final boolean withDynamicVariables;
	
	private transient Map<String, ParamSpec> paramSpecs;
	
	private transient Serializable defaultParamBean;
	
	public ParamMapEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		ParamSpecProvider paramSpecProvider = checkNotNull(
				propertyDescriptor.getPropertyGetter().getAnnotation(ParamSpecProvider.class));
		paramSpecProviderMethodName = paramSpecProvider.value();
		
		for (Serializable each: model.getObject()) {
			ParamInstance param = (ParamInstance) each;
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
			BeanDescriptor beanDescriptor = new BeanDescriptor(defaultParamBean.getClass());
			for (List<PropertyDescriptor> groupProperties: beanDescriptor.getProperties().values()) {
				for (PropertyDescriptor property : groupProperties) 
					property.setPropertyValue(defaultParamBean, null);
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

				ParamInstance param = params.get(property.getDisplayName());
				if (param != null) {
					container.add(newValueEditor("value", property, param.getValueProvider()));
					container.setDefaultModel(Model.of(param.getValueProvider().getClass()));
				} else {
					container.add(newValueEditor("value", property, new SpecifiedValue()));
					container.setDefaultModel(Model.of(SpecifiedValue.class));
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
					choices.add(SpecifiedValue.SECRET_DISPLAY_NAME);
					if (findParent(JobTriggerEditPanel.class) == null)
						choices.add(PassthroughValue.DISPLAY_NAME);
					choices.add(ScriptingValue.SECRET_DISPLAY_NAME);
					choices.add(IgnoreValue.DISPLAY_NAME);
				} else {
					choices.add(SpecifiedValue.DISPLAY_NAME);
					if (findParent(JobTriggerEditPanel.class) == null)
						choices.add(PassthroughValue.DISPLAY_NAME);
					choices.add(ScriptingValue.DISPLAY_NAME);
					choices.add(IgnoreValue.DISPLAY_NAME);
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
						else if (valueProviderClass == PassthroughValue.class)
							return PassthroughValue.DISPLAY_NAME;
						else
							return IgnoreValue.DISPLAY_NAME;
					}

					@Override
					public void setObject(String object) {
						ValueProvider valueProvider;
						if (object.equals(SpecifiedValue.DISPLAY_NAME) || object.equals(SpecifiedValue.SECRET_DISPLAY_NAME))  
							valueProvider = new SpecifiedValue();
						else if (object.equals(ScriptingValue.DISPLAY_NAME) || object.equals(ScriptingValue.SECRET_DISPLAY_NAME))
							valueProvider = new ScriptingValue();
						else if (object.equals(PassthroughValue.DISPLAY_NAME))
							valueProvider = new PassthroughValue();
						else
							valueProvider = new IgnoreValue();
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
		add(validatable -> {
			// add an empty validator to force calling convertInputToValue
		});
		setOutputMarkupId(true);
	}
	
	private Component newSpecifiedValueEditor(String componentId, PropertyDescriptor property, 
											  List<String> value) {
		var paramSpec = checkNotNull(getParamSpecs().get(property.getDisplayName()));
		
		Serializable paramBean;
		try {
			paramBean = getDefaultParamBean().getClass().getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}

		try {
			property.setPropertyValue(paramBean, paramSpec.convertToObject(value));
		} catch (Exception e) {
			logger.error("Error setting property value", e);
		}
		
		if (property.getPropertyGetter().getAnnotation(Password.class) == null) {
			PropertyEditor<?> propertyEditor = PropertyContext.edit(componentId, paramBean, property.getPropertyName());
			
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
			return propertyEditor;
		} else { 
			JobSecretEditBean bean = new JobSecretEditBean();
			bean.setSecret((String) property.getPropertyValue(paramBean));
			return PropertyContext.edit(componentId, bean, "secret");
		}			
	}
	
	private Component newValueEditor(String componentId, PropertyDescriptor property, ValueProvider valueProvider) {
		if (valueProvider instanceof SpecifiedValue) {
			return newSpecifiedValueEditor(componentId, property, ((SpecifiedValue) valueProvider).getValue());
		} else if (valueProvider instanceof ScriptingValue) {
			return PropertyContext.edit(componentId, valueProvider, "scriptName").setOutputMarkupId(true);
		} else if (valueProvider instanceof PassthroughValue) {
			return PropertyContext.edit(componentId, valueProvider, "paramName").setOutputMarkupId(true);
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
			ParamInstance param = new ParamInstance();
			param.setName((String) label.getDefaultModelObject());
			ParamSpec paramSpec = checkNotNull(getParamSpecs().get(param.getName()));
			param.setSecret(paramSpec instanceof SecretParam);
			Class<?> valueProviderClass = (Class<?>) paramContainer.getDefaultModelObject();
			Component valueEditor = paramContainer.get("value");
			if (valueProviderClass == ScriptingValue.class) {
				ScriptingValue scriptingValue = new ScriptingValue();
				scriptingValue.setScriptName((String) ((PropertyEditor<Serializable>) valueEditor).getConvertedInput()); 
				param.setValueProvider(scriptingValue);
			} else if (valueProviderClass == SpecifiedValue.class) {
				SpecifiedValue specifiedValue = new SpecifiedValue();
				Object propertyValue = ((PropertyEditor<Serializable>) valueEditor).getConvertedInput();
				specifiedValue.setValue(paramSpec.convertToStrings(propertyValue));
				param.setValueProvider(specifiedValue);
			} else if (valueProviderClass == PassthroughValue.class) {
				PassthroughValue passthroughValue = new PassthroughValue();
				passthroughValue.setParamName((String) ((PropertyEditor<Serializable>) valueEditor).getConvertedInput()); 
				param.setValueProvider(passthroughValue);
			} else {
				param.setValueProvider(new IgnoreValue());
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
