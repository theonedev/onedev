package com.pmease.commons.wicket.component.select2;

import java.util.List;

import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

@SuppressWarnings("serial")
public abstract class ListChoiceProvider<T> extends ChoiceProvider<T> {

	private final int pageSize;
	
	public ListChoiceProvider(int pageSize) {
		this.pageSize = pageSize;
	}
	
	@Override
	public void query(String term, int page, Response<T> response) {
		List<T> filtered = filterList(term);
		int first = page * pageSize;
		if (first + pageSize < filtered.size()) {
			response.addAll(filtered.subList(first, first + pageSize));
			response.setHasMore(true);
		} else if (first + pageSize == filtered.size()) {
			response.addAll(filtered.subList(first, first + pageSize));
			response.setHasMore(false);
		} else {
			response.addAll(filtered.subList(first, filtered.size()));
			response.setHasMore(false);
		}
	}

	protected abstract List<T> filterList(String term);
}
