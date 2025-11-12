package io.onedev.server.web.component.symboltooltip;

import java.io.Serializable;
import java.util.List;

public class SymbolContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String blobPath;

	private final String symbolLine;

	private final List<String> linesBeforeSymbolLine;

	private final List<String> linesAfterSymbolLine;

	private final List<String> linesAtStart;

	public SymbolContext(String fileName, String symbolLine, 
            List<String> linesBeforeSymbolLine, List<String> linesAfterSymbolLine, List<String> linesAtStart) {
		this.blobPath = fileName;
		this.symbolLine = symbolLine;
		this.linesBeforeSymbolLine = linesBeforeSymbolLine;
		this.linesAfterSymbolLine = linesAfterSymbolLine;
		this.linesAtStart = linesAtStart;
	}

	public String getBlobPath() {
		return blobPath;
	}

	public String getSymbolLine() {
		return symbolLine;
	}

	public List<String> getLinesBeforeSymbolLine() {
		return linesBeforeSymbolLine;
	}

	public List<String> getLinesAfterSymbolLine() {
		return linesAfterSymbolLine;
	}

	public List<String> getLinesAtStart() {
		return linesAtStart;
	}

}
