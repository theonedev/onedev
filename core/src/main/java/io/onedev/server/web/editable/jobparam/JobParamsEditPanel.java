package io.onedev.server.web.editable.jobparam;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.onedev.commons.utils.ReflectionUtils;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.ci.job.param.ScriptingValues;
import io.onedev.server.ci.job.param.SpecifiedValues;
import io.onedev.server.ci.job.param.ValuesProvider;
import io.onedev.server.util.JobUtils;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathElement;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.ParamSpecProvider;

@SuppressWarnings("serial")
public class JobParamsEditPanel extends PropertyEditor<List<Serializable>> {

	private static final Logger logger = LoggerFactory.getLogger(JobParamsEditPanel.class);
	
	private final Map<String, JobParam> params = new HashMap<>();
	
	private final String paramSpecProviderMethodName;
	
	private transient Map<String, InputSpec> paramSpecs;
	
	private transient Serializable defaultParamBean;
	
	public JobParamsEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
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
	private Map<String, InputSpec> getParamSpecs() {
		if (paramSpecs == null) {
			OneContext.push(new OneContext(this));
			paramSpecs = new LinkedHashMap<>();
			try {
				for (InputSpec paramSpec: (List<InputSpec>) ReflectionUtils.invokeStaticMethod(
						getDescriptor().getBeanClass(), paramSpecProviderMethodName)) {
					paramSpecs.put(paramSpec.getName(), paramSpec);
				}
			} finally {
				OneContext.pop();
			}
		}
		return paramSpecs;
	}
	
	private Serializable getDefaultParamBean() {
		if (defaultParamBean == null) {
			try {
				defaultParamBean = JobUtils.defineParamBeanClass(new ArrayList<>(getParamSpecs().values())).newInstance();
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
		for (List<PropertyDescriptor> groupProperties: beanDescriptor.getPropertyDescriptors().values()) {
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
				
				DropDownChoice<String> valuesProviderChoice = new DropDownChoice<String>("valuesProvider", new IModel<String>() {
					
					@Override
					public void detach() {
					}

					@Override
					public String getObject() {
						if (item.get("values") instanceof Fragment)
							return SpecifiedValues.DISPLAY_NAME;
						else
							return ScriptingValues.DISPLAY_NAME;
					}

					@Override
					public void setObject(String object) {
						ValuesProvider valuesProvider;
						if (object.equals(SpecifiedValues.DISPLAY_NAME)) 
							valuesProvider = newSpecifiedValueProvider(property);
						else  
							valuesProvider = new ScriptingValues();
						item.replace(newValuesEditor(property, valuesProvider));
					}
					
				}, Lists.newArrayList(SpecifiedValues.DISPLAY_NAME, ScriptingValues.DISPLAY_NAME));
				
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
							JobParam.validateValues(specifiedValues.getValues());
						} catch (ValidationException e) {
							RepeatingView paramsView = (RepeatingView) get("params");
							paramsView.get(index).get("values").error(e.getMessage());
						}
					}
					index++;
				}
			}
			
		});
	}
	
	private SpecifiedValues newSpecifiedValueProvider(PropertyDescriptor property) {
		SpecifiedValues specifiedValues = new SpecifiedValues();
		Object propertyValue = property.getPropertyValue(getDefaultParamBean());
		InputSpec paramSpec = getParamSpecs().get(property.getDisplayName());
		Preconditions.checkNotNull(paramSpec);
		List<String> strings = paramSpec.convertToStrings(propertyValue);
		specifiedValues.setValues(Lists.newArrayList(JobParam.toValue(strings)));
		return specifiedValues;
	}
	
	private Component newSpecifiedValueEditor(String componentId, PropertyDescriptor property, Optional<String> value) {
		WebMarkupContainer item = new WebMarkupContainer(componentId);
		InputSpec paramSpec = Preconditions.checkNotNull(getParamSpecs().get(property.getDisplayName()));
		Serializable paramBean;
		try {
			paramBean = getDefaultParamBean().getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		try {
			if (value.isPresent()) {
				List<String> strings = JobParam.fromValue(value.orNull());
				property.setPropertyValue(paramBean, paramSpec.convertToObject(strings));
			}
		} catch (Exception e) {
			logger.error("Error setting property value", e);
		}
		item.add(PropertyContext.edit("value", paramBean, property.getPropertyName()));
		item.add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				item.remove();
				String script = String.format("$('#%s').remove();", item.getMarkupId());
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

			for (String value: specifiedValues.getValues())
				valuesView.add(newSpecifiedValueEditor(valuesView.newChildId(), property, Optional.fromNullable(value)));
			
			fragment.add(new FencedFeedbackPanel("feedback", fragment));

			fragment.add(new AjaxLink<Void>("add") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					Component newSpecifiedValueEditor = newSpecifiedValueEditor(valuesView.newChildId(), 
							property, Optional.absent());
					valuesView.add(newSpecifiedValueEditor);
					String script = String.format("$('#%s').before('<li id=\"%s\"></li>')", 
							getMarkupId(), newSpecifiedValueEditor.getMarkupId());
					target.prependJavaScript(script);
					target.add(newSpecifiedValueEditor);
					
					onPropertyUpdating(target);
				}

			});
			
			fragment.setOutputMarkupId(true);
			return fragment;
		} else {
			return PropertyContext.edit("values", valuesProvider, "script").setOutputMarkupId(true);
		}
	}

	@Override
	public ErrorContext getErrorContext(PathElement element) {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected List<Serializable> convertInputToValue() throws ConversionException {
		List<Serializable> value = new ArrayList<>();
		for (Component paramItem: (WebMarkupContainer)get("params")) {
			Label label = (Label) paramItem.get("name");
			JobParam param = new JobParam();
			param.setName((String) label.getDefaultModelObject());
			Component valuesEditor = paramItem.get("values");
			if (valuesEditor instanceof PropertyEditor) {
				ScriptingValues scriptingValues = new ScriptingValues();
				scriptingValues.setScript((String) ((PropertyEditor<Serializable>) valuesEditor).getConvertedInput()); 
				param.setValuesProvider(scriptingValues);
			} else {
				SpecifiedValues specifiedValues = new SpecifiedValues();
				specifiedValues.setValues(new ArrayList<>());
				for (Component valueItem: (WebMarkupContainer)valuesEditor.get("values")) {
					Object propertyValue = ((PropertyEditor<Serializable>) valueItem.get("value")).getConvertedInput(); 
					InputSpec paramSpec = Preconditions.checkNotNull(getParamSpecs().get(param.getName()));
					specifiedValues.getValues().add(JobParam.toValue(paramSpec.convertToStrings(propertyValue)));
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
		response.render(CssHeaderItem.forReference(new JobParamsCssResourceReference()));
	}
	
}
