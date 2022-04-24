package io.onedev.server.web.component.commandpalette;

import java.io.Serializable;

public class CommandSuggestion implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String label;
	
	private final SuggestionContent content;
	
	public CommandSuggestion(String label, SuggestionContent content) {
		this.label = label;
		this.content = content;
	}

	public String getLabel() {
		return label;
	}

	public SuggestionContent getContent() {
		return content;
	}

}
