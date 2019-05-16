package io.onedev.server.ci.job.param;

import java.util.List;

public class SpecifiedValues implements ValuesProvider {

	private static final long serialVersionUID = 1L;

	public static final String DISPLAY_NAME = "Use specified values";
	
	private List<List<String>> values;

	@Override
	public List<List<String>> getValues() {
		return values;
	}

	public void setValues(List<List<String>> values) {
		this.values = values;
	}

}
