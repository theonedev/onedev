package io.onedev.server.web.component.select2;

import java.util.List;

public class ResponseFiller<T> {
	
	private Response<T> response;
	
	public ResponseFiller(Response<T> response) {
		this.response = response;
	}
	
	public void fill(List<T> values, int currentPage, int pageSize) {
		int from = currentPage * pageSize;
		int to = from + pageSize;
		
		if (to > values.size()) {
			to = values.size();
		} 
		if (from > to) {
			from = to;
		}
		response.addAll(values.subList(from, to));
		response.setHasMore(to<values.size());
	}
	
}
