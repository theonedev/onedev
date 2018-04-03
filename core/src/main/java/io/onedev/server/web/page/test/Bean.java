package io.onedev.server.web.page.test;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.common.collect.Lists;

import io.onedev.server.util.editable.annotation.ChoiceProvider;
import io.onedev.server.util.editable.annotation.Editable;

@Editable
public class Bean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private List<String> states;

	@Editable(order=100)
	@NotNull
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200)
	@ChoiceProvider("getStateChoices")
	@Size(min=1, message="At least one state must be defined")
	public List<String> getStates() {
		return states;
	}

	public void setStates(List<String> states) {
		this.states = states;
	}

	@SuppressWarnings("unused")
	private static List<String> getStateChoices() {
		return Lists.newArrayList("Open", "Closed");
	}
}
