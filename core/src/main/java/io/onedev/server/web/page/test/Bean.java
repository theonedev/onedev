package io.onedev.server.web.page.test;

import java.io.Serializable;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.Script;

@Editable
public class Bean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String script;

	@Editable
	@Script
	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}
	
}
