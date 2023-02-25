package io.onedev.server.model.support.issue.field;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.ValidationException;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.buildspecmodel.inputspec.InputContext;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.buildspecmodel.inputspec.SecretInput;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.SecretField;
import io.onedev.server.model.support.issue.field.supply.FieldSupply;
import io.onedev.server.model.support.issue.field.supply.SpecifiedValue;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;

public class FieldUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(FieldUtils.class);
	
	private static final MetaDataKey<Class<? extends Serializable>> FIELD_BEAN_CLASS_KEY = 
			new MetaDataKey<Class<? extends Serializable>>() {

		private static final long serialVersionUID = 1L;
		
	};
	
	public static final String FIELD_BEAN_CLASS_NAME = "IssueFieldBean";
	
	public static void clearFields(Serializable fieldBean) {
		for (List<PropertyDescriptor> groupProperties: new BeanDescriptor(fieldBean.getClass()).getProperties().values()) {
			for (PropertyDescriptor property: groupProperties) 
				property.setPropertyValue(fieldBean, null);
		}
	}
	
	public static Class<? extends Serializable> getFieldBeanClass() {
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle != null) {
			Class<? extends Serializable> fieldBeanClass = requestCycle.getMetaData(FIELD_BEAN_CLASS_KEY);
			if (fieldBeanClass == null) {
				fieldBeanClass = defineFieldBeanClass();
				requestCycle.setMetaData(FIELD_BEAN_CLASS_KEY, fieldBeanClass);
			}
			return fieldBeanClass;
		} else {
			return defineFieldBeanClass();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Class<? extends Serializable> defineFieldBeanClass() {
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		return (Class<? extends Serializable>) FieldSpec.defineClass(FIELD_BEAN_CLASS_NAME, 
				"Issue Fields", issueSetting.getFieldSpecs());
	}
	
	public static Collection<String> getEditablePropertyNames(Project project, Class<?> fieldBeanClass, Collection<String> fieldNames) {
		BeanDescriptor descriptor = new BeanDescriptor(fieldBeanClass);
		return fieldNames.stream()
				.filter(it->SecurityUtils.canEditIssueField(project, it))
				.map(it->getPropertyName(descriptor, it))
				.filter(it->it!=null)
				.collect(Collectors.toList());
	}

	public static Collection<String> getEditableFields(Project project, Collection<String> fieldNames) {
		return fieldNames.stream()
				.filter(it->SecurityUtils.canEditIssueField(project, it))
				.collect(Collectors.toList());
	}
	
	@Nullable
	public static String getPropertyName(BeanDescriptor descriptor, String fieldName) {
		for (List<PropertyDescriptor> groupProperties: descriptor.getProperties().values()) {
			for (PropertyDescriptor property: groupProperties) {
				if (fieldName.equals(property.getDisplayName())) 
					return property.getPropertyName();
			}
		}
		return null;
	}
	
	public static Map<String, Object> getFieldValues(ComponentContext context, Serializable fieldBean, Collection<String> fieldNames) {
		ComponentContext.push(context);
		try {
			Map<String, Object> fieldValues = new HashMap<>();
			BeanDescriptor beanDescriptor = new BeanDescriptor(fieldBean.getClass());
			for (List<PropertyDescriptor> groupProperties: beanDescriptor.getProperties().values()) {
				for (PropertyDescriptor property: groupProperties) {
					if (fieldNames.contains(property.getDisplayName()))
						fieldValues.put(property.getDisplayName(), property.getPropertyValue(fieldBean));
				}
			}
			
			return fieldValues;
		} finally {
			ComponentContext.pop();
		}
	}
	
	private static void validateFieldValue(FieldSpec fieldSpec, String fieldName, List<String> fieldValue) {
		try {
			fieldSpec.convertToObject(fieldValue);
		} catch (Exception e) {
			String displayValue;
			if (fieldSpec instanceof SecretField)
				displayValue = SecretInput.MASK;
			else
				displayValue = fieldValue.toString();
			if (e.getMessage() == null)
				logger.error("Error validating field value", e);
			throw new ValidationException("Error validating value '" + displayValue + "' of field '" 
					+ fieldName + "': " + e.getMessage());
		}
	}

	private static void validateFieldNames(Collection<String> fieldSpecNames, Collection<String> fieldNames) {
		for (String fieldSpecName: fieldSpecNames) {
			if (!fieldNames.contains(fieldSpecName))
				throw new ValidationException("Missing issue field: " + fieldSpecName);
		}
		for (String fieldName: fieldNames) {
			if (!fieldSpecNames.contains(fieldName))
				throw new ValidationException("Unknown issue field: " + fieldName);
		}
	}
	
	public static void validateFieldMap(Map<String, FieldSpec> fieldSpecMap, Map<String, List<String>> fieldMap) {
		validateFieldNames(fieldSpecMap.keySet(), fieldMap.keySet());
		for (Map.Entry<String, List<String>> entry: fieldMap.entrySet()) {
			if (entry.getValue() != null) {
				FieldSpec fieldSpec = Preconditions.checkNotNull(fieldSpecMap.get(entry.getKey()));
				validateFieldValue(fieldSpec, entry.getKey(), entry.getValue());
			}
		}
	}
	
	public static void validateFields(Map<String, FieldSpec> fieldSpecs, List<FieldSupply> fields) {
		Map<String, List<String>> fieldMap = new HashMap<>();
		for (FieldSupply field: fields) {
			List<String> values;
			if (field.getValueProvider() instanceof SpecifiedValue)
				values = field.getValueProvider().getValue();
			else
				values = null;
			if (fieldMap.put(field.getName(), values) != null)
				throw new ValidationException("Duplicate field: " + field.getName());
		}
		validateFieldMap(fieldSpecs, fieldMap);
	}

	public static boolean isFieldVisible(BeanDescriptor beanDescriptor, Serializable fieldBean, String fieldName) {
		String propertyName = getPropertyName(beanDescriptor, fieldName);
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(fieldBean.getClass(), propertyName);
		return propertyDescriptor.isPropertyVisible(newPropertyComponentContexts(beanDescriptor, fieldBean), beanDescriptor);
	}
	
	public static Map<String, ComponentContext> newPropertyComponentContexts(BeanDescriptor beanDescriptor, Serializable fieldBean) {
		Map<String, ComponentContext> componentContexts = new HashMap<>();

		ComponentContext componentContext = new ComponentContext(newContextComponent(beanDescriptor, fieldBean));
		for (List<PropertyDescriptor> group: beanDescriptor.getProperties().values()) {
			for (PropertyDescriptor property: group) 
				componentContexts.put(property.getPropertyName(), componentContext);
		}
		
		return componentContexts;
	}
	
	private static Component newContextComponent(BeanDescriptor beanDescriptor, Serializable fieldBean) {
		class FakeComponent extends MarkupContainer implements InputContext, EditContext {

			private static final long serialVersionUID = 1L;

			public FakeComponent() {
				super("component");
			}

			@Override
			public Object getInputValue(String name) {
				return beanDescriptor.getProperty(name).getPropertyValue(fieldBean);
			}

			@Override
			public List<String> getInputNames() {
				return getIssueSetting().getFieldNames();
			}

			private GlobalIssueSetting getIssueSetting() {
				return OneDev.getInstance(SettingManager.class).getIssueSetting();
			}
			
			@Override
			public InputSpec getInputSpec(String inputName) {
				return getIssueSetting().getFieldSpec(inputName);
			}

		}
		return new FakeComponent();
	}
	
	public static ComponentContext newBeanComponentContext(BeanDescriptor beanDescriptor, Serializable fieldBean) {
		return new ComponentContext(newContextComponent(beanDescriptor, fieldBean)) {

			private static final long serialVersionUID = 1L;

			@Override
			public ComponentContext getChildContext(String childName) {
				return new ComponentContext(newContextComponent(beanDescriptor, fieldBean));
			}
			
		};
	}

}
