package io.onedev.server.util;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import io.onedev.server.OneDev;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.inputspec.InputSpec;

public class IssueField implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(IssueField.class);

	private final String name;
	
	@XStreamOmitField
	private final String type;
	
	private final List<String> values;
	
	public IssueField(String name, String type, List<String> values) {
		this.name = name;
		this.type = type;
		this.values = values;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public List<String> getValues() {
		return values;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other instanceof IssueField) {
			IssueField otherField = (IssueField) other;
			return new EqualsBuilder()
					.append(getName(), otherField.getName())
					.append(getType(), otherField.getType())
					.append(getValues(), otherField.getValues())
					.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(getName())
				.append(getType())
				.append(getValues())
				.toHashCode();
	}

	@Nullable
	public Object getValue(Project project) {
		InputSpec fieldSpec = OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldSpec(name);
		if (fieldSpec != null) {
			try {
				if (!getValues().isEmpty())
					return fieldSpec.convertToObject(getValues());
			} catch (Exception e) {
				logger.error("Error converting field values to object: " + name, e);
			}
		}
		return null;
	}
	
}
