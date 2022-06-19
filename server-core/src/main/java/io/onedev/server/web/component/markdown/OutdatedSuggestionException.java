package io.onedev.server.web.component.markdown;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.support.Mark;

public class OutdatedSuggestionException extends ExplicitException {

	private static final long serialVersionUID = 1L;
	
	private final Mark mark;
	
	public OutdatedSuggestionException(Mark mark) {
		super("Suggestion is outdated");
		this.mark = mark;
	}

	public Mark getMark() {
		return mark;
	}
	
}

