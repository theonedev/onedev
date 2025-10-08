package io.onedev.server.model.support.widget;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;

public class TabState implements Serializable {

	private String query;

	private int page;

	@Nullable
	public String getQuery() {
		return query;
	}

	public void setQuery(@Nullable String query) {
		this.query = query;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

}
