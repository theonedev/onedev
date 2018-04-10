package io.onedev.server.web.page.test;

import java.io.Serializable;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;

import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Color;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.Script;

@Editable
public class Bean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String script;
	
	private String color;
	
	private String state;

	@Editable(order=50)
	@Script
	@NotEmpty
	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	@Editable(order=100)
	@Color
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	@Editable(order=200)
	@ChoiceProvider("getStateChoices")
	@NotEmpty
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getStateChoices() {
		return Lists.newArrayList("a", "b", "c");
	}
}
