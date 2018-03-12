package io.onedev.server.util.input;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.Size;

import org.apache.commons.lang.SerializationUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.java.JavaEscape;

import com.google.common.collect.Lists;

import io.onedev.server.util.GroovyUtils;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.NameOfEmptyValue;
import io.onedev.server.util.input.showcondition.ShowCondition;

@Editable
public abstract class Input implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(Input.class);
	
	public static final String BOOLEAN = "Boolean";

	public static final String TEXT = "Text";
	
	public static final String DATE = "Date";
	
	public static final String PASSWORD = "Password";
	
	public static final String NUMBER = "Number";
	
	public static final String CHOICE = "Choice";
	
	public static final String MULTI_CHOICE = "Multi-choice";
	
	public static final String USER_CHOICE = "User choice";
	
	public static final String USER_MULTI_CHOICE = "User multi-choice";
	
	public static final String GROUP_CHOICE = "Group choice";
	
	public static final String GROUP_MULTI_CHOICE = "Group multi-choice";
	
	private String name;

	private List<String> scenarios;
	
	private String description;
	
	private boolean allowEmpty;
	
	private String nameOfEmptyValue;
	
	private ShowCondition showCondition;

	@Editable(order=10, description="Name of the custom input")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(name="resource.input.scenariosName", order=20, description="resource.input.scenariosDescription")
	@ChoiceProvider("getScenarioChoices")
	@Size(min=1, max=100)
	public List<String> getScenarios() {
		return scenarios;
	}

	public void setScenarios(List<String> scenarios) {
		this.scenarios = scenarios;
	}

	@Editable(order=30, description="resource.input.description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Editable(order=40, name="Show Conditionally", description="resource.input.showCondition")
	@NameOfEmptyValue("Always")
	public ShowCondition getShowCondition() {
		return showCondition;
	}

	public void setShowCondition(ShowCondition showCondition) {
		this.showCondition = showCondition;
	}
	
	@Editable(order=50, name="Allow Empty Value", description="resource.input.allowEmpty")
	public boolean isAllowEmpty() {
		return allowEmpty;
	}

	public void setAllowEmpty(boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
	}
	
	@Editable(order=60)
	@io.onedev.server.util.editable.annotation.ShowCondition("isNameOfEmptyValueVisible")
	@NotEmpty
	public String getNameOfEmptyValue() {
		return nameOfEmptyValue;
	}

	public void setNameOfEmptyValue(String nameOfEmptyValue) {
		this.nameOfEmptyValue = nameOfEmptyValue;
	}
	
	@SuppressWarnings("unused")
	private static boolean isNameOfEmptyValueVisible() {
		return (boolean) OneContext.get().getEditContext().getOnScreenValue("allowEmpty");
	}
	
	public List<String> getPossibleValues() {
		return Lists.newArrayList();
	}

	protected String escape(String string) {
		String escaped = JavaEscape.escapeJava(string);
		// escape $ character since it has special meaning in groovy string
		escaped = escaped.replace("$", "\\$");

		return escaped;
	}
	
	public abstract String getPropertyDef(Map<String, Integer> indexes);
	
	protected String getLiteral(byte[] bytes) {
		StringBuffer buffer = new StringBuffer("[");
		for (byte eachByte: bytes) {
			buffer.append(String.format("%d", eachByte)).append(",");
		}
		buffer.append("] as byte[]");
		return buffer.toString();
	}

	protected void appendField(StringBuffer buffer, int index, String type) {
		buffer.append("    private " + type + " input" + index + ";\n");
		buffer.append("\n");
	}
	
	protected void appendAnnotations(StringBuffer buffer, int index, String notEmptyAnnotation, 
			@Nullable String choiceAnnotation, boolean hasDefaultValueProvider) {
		if (description != null) {
			buffer.append("    @Editable(name=\"" + escape(name) + "\", description=\"" + 
					escape(description) + "\", order=" + index + ")\n");
		} else {
			buffer.append("    @Editable(name=\"" + escape(name) + 
					"\", order=" + index + ")\n");
		}
		if (!allowEmpty)
			buffer.append("    @" + notEmptyAnnotation + "\n");
		if (showCondition != null) 
			buffer.append("    @ShowCondition(\"isInput" + index + "Visible\")\n");
		if (choiceAnnotation != null) 
			buffer.append("    @" + choiceAnnotation + "(\"getInput" + index + "Choices\")\n");
		if (hasDefaultValueProvider)
			buffer.append("    @DefaultValueProvider(\"getDefaultInput" + index + "\")\n");
	}
	
	protected void appendMethods(StringBuffer buffer, int index, String type, 
			@Nullable Serializable choiceProvider, @Nullable Serializable defaultValueProvider) {
		buffer.append("    public " + type + " getInput" + index + "() {\n");
		buffer.append("        return input" + index + ";\n");
		buffer.append("    }\n");
		buffer.append("\n");
		
		buffer.append("    public void setInput" + index + "(" + type + " value) {\n");
		buffer.append("        this.input" + index + "=value;\n");
		buffer.append("    }\n");
		buffer.append("\n");
		
		if (showCondition != null) {
			buffer.append("    private static boolean isInput" + index + "Visible() {\n");
			String literalBytes = getLiteral(SerializationUtils.serialize(showCondition));
			buffer.append("        return SerializationUtils.deserialize(" + literalBytes + ").isVisible();\n");
			buffer.append("    }\n");
			buffer.append("\n");
		}

		if (choiceProvider != null) {
			buffer.append("    private static List getInput" + index + "Choices() {\n");
			String literalBytes = getLiteral(SerializationUtils.serialize(choiceProvider));
			buffer.append("        return SerializationUtils.deserialize(" + literalBytes + ").getChoices(false);\n");
			buffer.append("    }\n");
			buffer.append("\n");
		}
		
		if (defaultValueProvider != null) {
			buffer.append("    private static " + type + " getDefaultInput" + index + "() {\n");
			String literalBytes = getLiteral(SerializationUtils.serialize(defaultValueProvider));
			buffer.append("        return SerializationUtils.deserialize(" + literalBytes + ").getDefaultValue();\n");
			buffer.append("    }\n");
			buffer.append("\n");
		}
	}
	
	public boolean isUsingInput(String inputName) {
		if (showCondition != null && inputName.equals(showCondition.getInputName())) {
			return true;
		} else {
			return false;
		}
	}

	public void onInputRename(String oldName, String newName) {
		if (showCondition != null && oldName.equals(showCondition.getInputName())) {
			showCondition.setInputName(newName);
		}
	}
	
	@SuppressWarnings("unused")
	private static List<String> getScenarioChoices() {
		return OneContext.get().getInputContext().getScenarios();
	}
	
	public static Class<?> defineClass(String className, List<Input> inputs) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("import org.apache.commons.lang3.SerializationUtils;\n");
		buffer.append("import io.onedev.server.util.editable.annotation.*;\n");
		buffer.append("import io.onedev.util.*;\n");
		buffer.append("import io.onedev.server.util.*;\n");
		buffer.append("import io.onedev.server.util.facade.*;\n");
		buffer.append("import java.util.*;\n");
		buffer.append("import javax.validation.constraints.*;\n");
		buffer.append("import org.hibernate.validator.constraints.*;\n");
		buffer.append("\n");
		buffer.append("@Editable\n");
		buffer.append("class " + className + " implements java.io.Serializable {\n");
		buffer.append("\n");
		buffer.append("    private static final long serialVersionUID = 1L;\n");
		buffer.append("\n");
		Map<String, Integer> indexes = new HashMap<>();
		int index = 1;
		for (Input input: inputs)
			indexes.put(input.getName(), index++);
		for (Input input: inputs)
			buffer.append(input.getPropertyDef(indexes));

		buffer.append("}\n");
		buffer.append("return " + className + ";\n");
		
		logger.trace("Class definition script:\n" + buffer.toString());
		
		return (Class<?>) GroovyUtils.evalScript(buffer.toString(), new HashMap<>());
	}

	@Nullable
	public abstract String toString(@Nullable Object object);
	
	@Nullable
	public abstract Object toObject(@Nullable String string);
	
}
