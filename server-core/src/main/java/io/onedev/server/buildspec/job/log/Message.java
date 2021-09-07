package io.onedev.server.buildspec.job.log;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Style style;
	
	private final String text;
	
	public Message(Style style, String text) {
		this.style = style;
		this.text = text;
	}

	public Style getStyle() {
		return style;
	}

	public String getText() {
		return text;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other instanceof Message) {
			Message otherMessage = (Message) other;
			return new EqualsBuilder()
					.append(style, otherMessage.style)
					.append(text, otherMessage.text)
					.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(style).append(text).toHashCode();
	}

}
