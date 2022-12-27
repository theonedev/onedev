package io.onedev.server.model.support.code;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Numeric;

import java.io.Serializable;

@Editable
public class GitPackConfig implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String windowMemory;
	
	private String packSizeLimit;
	
	private String threads;
	
	private String window;

	@Editable(order = 100, placeholder = "Use default", description = "Optionally specify value of git config " +
			"<code>pack.windowMemory</code> for the repository")
	public String getWindowMemory() {
		return windowMemory;
	}

	public void setWindowMemory(String windowMemory) {
		this.windowMemory = windowMemory;
	}

	@Editable(order = 200, placeholder = "Use default", description = "Optionally specify value of git config " +
			"<code>pack.packSizeLimit</code> for the repository")
	public String getPackSizeLimit() {
		return packSizeLimit;
	}

	public void setPackSizeLimit(String packSizeLimit) {
		this.packSizeLimit = packSizeLimit;
	}

	@Editable(order = 300, placeholder = "Use default", description = "Optionally specify value of git config " +
			"<code>pack.threads</code> for the repository")
	@Numeric
	public String getThreads() {
		return threads;
	}

	public void setThreads(String threads) {
		this.threads = threads;
	}

	@Editable(order = 400, placeholder = "Use default", description = "Optionally specify value of git config " +
			"<code>pack.window</code> for the repository")
	@Numeric
	public String getWindow() {
		return window;
	}

	public void setWindow(String window) {
		this.window = window;
	}
}
