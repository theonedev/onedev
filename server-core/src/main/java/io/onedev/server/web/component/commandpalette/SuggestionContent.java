package io.onedev.server.web.component.commandpalette;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

public class SuggestionContent implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String url;
	
	private final String searchBase;
	
	public SuggestionContent(@Nullable String url, @Nullable String searchBase) {
		this.url = url;
		this.searchBase = searchBase;
	}

	@Nullable
	public String getUrl() {
		return url;
	}

	@Nullable
	public String getSearchBase() {
		return searchBase;
	}

	public SuggestionContent mergeWith(SuggestionContent content) {
		return new SuggestionContent(url!=null?url:content.url, 
				searchBase!=null?searchBase:content.searchBase);
	}
	
}
