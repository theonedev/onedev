package io.onedev.server.model.support.inputspec;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.java.JavaEscape;

import com.google.common.collect.Lists;

import io.onedev.server.model.support.inputspec.showcondition.ShowCondition;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public abstract class InputSpec implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(InputSpec.class);
	
	public static final String BOOLEAN = "Checkbox";

	public static final String TEXT = "Text";
	
	public static final String DATE = "Date";
	
	public static final String SECRET = "Secret";
	
	public static final String NUMBER = "Number";
	
	public static final String COMMIT = "Commit";
	
	public static final String ENUMERATION = "Enumeration";
	
	public static final String USER = "User";
	
	public static final String GROUP = "Group";
	
	public static final String ISSUE = "Issue";
	
	public static final String BUILD = "Build";

	public static final String PULLREQUEST = "Pull request";
	
	private String name;

	private String description;

	private boolean allowMultiple;
	
	private boolean allowEmpty;
	
	private ShowCondition showCondition;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isAllowMultiple() {
		return allowMultiple;
	}

	public void setAllowMultiple(boolean allowMultiple) {
		this.allowMultiple = allowMultiple;
	}

	public ShowCondition getShowCondition() {
		return showCondition;
	}

	public void setShowCondition(ShowCondition showCondition) {
		this.showCondition = showCondition;
	}
	
	public boolean isAllowEmpty() {
		return allowEmpty;
	}

	public void setAllowEmpty(boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
	}
	
	public List<String> getPossibleValues() {
		return Lists.newArrayList();
	}

	public static String escape(String string) {
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

	public void appendField(StringBuffer buffer, int index, String type) {
		buffer.append("    private Optional<" + type + "> input" + index + ";\n");
		buffer.append("\n");
	}
	
	public void appendChoiceProvider(StringBuffer buffer, int index, String annotation) {
		buffer.append("    " + annotation + "(\"getInput" + index + "Choices\")\n");		
	}
	
	public void appendCommonAnnotations(StringBuffer buffer, int index) {
		if (description != null) {
			buffer.append("    @Editable(name=\"" + escape(name) + "\", description=\"" + 
					escape(description) + "\", order=" + index + ")\n");
		} else {
			buffer.append("    @Editable(name=\"" + escape(name) + 
					"\", order=" + index + ")\n");
		}
		if (showCondition != null) 
			buffer.append("    @ShowCondition(\"isInput" + index + "Visible\")\n");
	}

	private void wrapWithChildContext(StringBuffer buffer, int index, String statement) {
		buffer.append("            ComponentContext context = ComponentContext.get();\n");
		buffer.append("            if (context != null) {\n");
		buffer.append("                ComponentContext childContext = context.getChildContext(\"input" + index + "\");\n");
		buffer.append("                if (childContext != null) {\n");
		buffer.append("                    ComponentContext.push(childContext);\n");
		buffer.append("                    try {\n");
		buffer.append("                        " + statement + "\n");
		buffer.append("                    } finally {\n");
		buffer.append("                        ComponentContext.pop();\n");
		buffer.append("                    }\n");
		buffer.append("                } else {\n");
		buffer.append("                    " + statement + "\n");
		buffer.append("                }\n");
		buffer.append("            } else {\n");
		buffer.append("                " + statement + "\n");
		buffer.append("            }\n");
	}
	
	public void appendMethods(StringBuffer buffer, int index, String type, 
			@Nullable Serializable choiceProvider, @Nullable Serializable defaultValueProvider) {
		String literalBytes = getLiteral(SerializationUtils.serialize(defaultValueProvider));
		buffer.append("    public " + type + " getInput" + index + "() {\n");
		buffer.append("        if (input" + index + "!=null) {\n");
		buffer.append("            return input" + index + ".orNull();\n");
		buffer.append("        } else {\n");
		if (defaultValueProvider != null) {
			wrapWithChildContext(buffer, index, "return SerializationUtils.deserialize(" + literalBytes + ").getDefaultValue();");
		} else {
			buffer.append("        return null;\n");
		}
		buffer.append("        }\n");
		buffer.append("    }\n");
		buffer.append("\n");
		
		buffer.append("    public void setInput" + index + "(" + type + " value) {\n");
		buffer.append("        this.input" + index + "=Optional.fromNullable(value);\n");
		buffer.append("    }\n");
		buffer.append("\n");
		
		if (showCondition != null) {
			buffer.append("    private static boolean isInput" + index + "Visible() {\n");
			literalBytes = getLiteral(SerializationUtils.serialize(showCondition));
			buffer.append("        return SerializationUtils.deserialize(" + literalBytes + ").isVisible();\n");
			buffer.append("    }\n");
			buffer.append("\n");
		}
 
		if (choiceProvider != null) {
			buffer.append("    private static List getInput" + index + "Choices() {\n");
			literalBytes = getLiteral(SerializationUtils.serialize(choiceProvider));
			if (choiceProvider instanceof io.onedev.server.model.support.inputspec.choiceinput.choiceprovider.ChoiceProvider) {
				buffer.append("        return new ArrayList(SerializationUtils.deserialize(" + literalBytes + ").getChoices(false).keySet());\n");
			} else {
				buffer.append("        return SerializationUtils.deserialize(" + literalBytes + ").getChoices(false);\n");
			}
			buffer.append("    }\n");
			buffer.append("\n");
		}
	}
	
	public static Class<?> defineClass(String className, String description, Collection<? extends InputSpec> inputs) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("import org.apache.commons.lang3.SerializationUtils;\n");
		buffer.append("import com.google.common.base.Optional;\n");
		buffer.append("import io.onedev.server.web.editable.annotation.*;\n");
		buffer.append("import io.onedev.server.util.validation.annotation.*;\n");
		buffer.append("import io.onedev.util.*;\n");
		buffer.append("import io.onedev.server.util.*;\n");
		buffer.append("import io.onedev.server.util.facade.*;\n");
		buffer.append("import java.util.*;\n");
		buffer.append("import javax.validation.constraints.*;\n");
		buffer.append("import org.hibernate.validator.constraints.*;\n");
		buffer.append("\n");
		buffer.append("@Editable(name=").append("\"").append(description).append("\")\n");
		buffer.append("class " + className + " implements java.io.Serializable {\n");
		buffer.append("\n");
		buffer.append("    private static final long serialVersionUID = 1L;\n");
		buffer.append("\n");
		Map<String, Integer> indexes = new HashMap<>();
		int index = 1;
		for (InputSpec input: inputs)
			indexes.put(input.getName(), index++);
		for (InputSpec input: inputs)
			buffer.append(input.getPropertyDef(indexes));

		buffer.append("}\n");
		buffer.append("return " + className + ";\n");
		
		logger.trace("Class definition script:\n" + buffer.toString());
		
		return (Class<?>) GroovyUtils.evalScript(buffer.toString(), new HashMap<>());
	}

	public abstract List<String> convertToStrings(@Nullable Object object);

	/**
	 * Convert list of strings to object
	 * 
	 * @param strings
	 * 			list of strings
	 * @return
	 * 			converted object
	 */
	@Nullable
	public abstract Object convertToObject(List<String> strings);
	
	public long getOrdinal(String fieldValue) {
		return -1;
	}
	
	public String getType() {
		return EditableUtils.getDisplayName(getClass());		
	}

	public boolean checkListElements(Object value, Class<?> elementClass) {
		if (value instanceof List) {
			for (Object element: (List<?>)value) {
				if (element == null || element.getClass() != elementClass)
					return false;
			}
			return true;
		} else {
			return false;
		}
	}
		
}
