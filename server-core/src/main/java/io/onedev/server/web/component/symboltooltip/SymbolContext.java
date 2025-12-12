package io.onedev.server.web.component.symboltooltip;

import java.io.Serializable;
import java.util.List;

public class SymbolContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String blobPath;

	private final List<String> contextLines;

	public SymbolContext(String blobPath, List<String> contextLines) {
		this.blobPath = blobPath;
		this.contextLines = contextLines;
	}

	public String getBlobPath() {
		return blobPath;
	}

	public List<String> getContextLines() {
		return contextLines;
	}

}
