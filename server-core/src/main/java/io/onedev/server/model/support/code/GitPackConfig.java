package io.onedev.server.model.support.code;

import io.onedev.server.rest.annotation.Api;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Numeric;

import javax.annotation.Nullable;
import java.io.Serializable;

@Editable
public class GitPackConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	@Api(description = "May be null")
	private String windowMemory;

	@Api(description = "May be null")
	private String packSizeLimit;

	@Api(description = "May be null")
	private String threads;

	@Api(description = "May be null")
	private String window;

	@Editable(order = 100, placeholder = "Use default", description = "Optionally specify value of git config " +
			"<code>pack.windowMemory</code> for the repository")
	@Nullable
	public String getWindowMemory() {
		return windowMemory;
	}

	public void setWindowMemory(@Nullable String windowMemory) {
		this.windowMemory = windowMemory;
	}

	@Editable(order = 200, placeholder = "Use default", description = "Optionally specify value of git config " +
			"<code>pack.packSizeLimit</code> for the repository")
	@Nullable
	public String getPackSizeLimit() {
		return packSizeLimit;
	}

	public void setPackSizeLimit(@Nullable String packSizeLimit) {
		this.packSizeLimit = packSizeLimit;
	}

	@Editable(order = 300, placeholder = "Use default", description = "Optionally specify value of git config " +
			"<code>pack.threads</code> for the repository")
	@Numeric
	@Nullable
	public String getThreads() {
		return threads;
	}

	public void setThreads(@Nullable String threads) {
		this.threads = threads;
	}

	@Editable(order = 400, placeholder = "Use default", description = "Optionally specify value of git config " +
			"<code>pack.window</code> for the repository")
	@Numeric
	@Nullable
	public String getWindow() {
		return window;
	}

	public void setWindow(@Nullable String window) {
		this.window = window;
	}
}
