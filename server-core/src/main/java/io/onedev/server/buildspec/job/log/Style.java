package io.onedev.server.buildspec.job.log;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Style implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String FOREGROUND_COLOR_DEFAULT = "fg-default";
	
	public static final String BACKGROUND_COLOR_DEFAULT = "bg-default";
	
	private final String color;
	
	private final String backgroundColor;
	
	private final boolean bold;
	
	public Style(String color, String backgroundColor, boolean bold) {
		this.color = color;
		this.backgroundColor = backgroundColor;
		this.bold = bold;
	}
	
	public String getColor() {
		return color;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public boolean isBold() {
		return bold;
	}

	public boolean isDefault() {
		return color.equals(FOREGROUND_COLOR_DEFAULT) && backgroundColor.equals(BACKGROUND_COLOR_DEFAULT) && !bold;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other instanceof Style) {
			Style otherStyle = (Style) other;
			return new EqualsBuilder()
					.append(color, otherStyle.color)
					.append(backgroundColor, otherStyle.backgroundColor)
					.append(bold, otherStyle.bold)
					.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(color).append(backgroundColor).append(bold).toHashCode();
	}
	
}
