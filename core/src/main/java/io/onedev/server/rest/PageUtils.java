package io.onedev.server.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

public class PageUtils {

	public static final String PARAM_PER_PAGE = "per_page";
	
	public static final String PARAM_PAGE = "page";
	
	public static int getLastPage(int totalCount, int pageSize) {
		if (totalCount % pageSize == 0)
			return totalCount / pageSize;
		else
			return totalCount / pageSize + 1;
	}

	public static Link[] getNavLinks(UriInfo uriInfo, int totalCount, int pageSize, int currentPage) {
		int lastPage = getLastPage(totalCount, pageSize);
		
		List<Link> links = new ArrayList<>();
		if (currentPage > 1 && lastPage != 0) {
			UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder()
					.queryParam(PARAM_PAGE, 1)
					.queryParam(PARAM_PER_PAGE, pageSize);
			links.add(Link.fromUriBuilder(uriBuilder).rel("first").type("GET").build());
		}
		int prevPage = currentPage - 1;
		if (prevPage > lastPage)
			prevPage = lastPage;
		if (prevPage >= 1) {
			UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder()
					.queryParam(PARAM_PAGE, prevPage)
					.queryParam(PARAM_PER_PAGE, pageSize);
			links.add(Link.fromUriBuilder(uriBuilder).rel("prev").type("GET").build());
		}
		if (currentPage < lastPage && lastPage != 0) { 
			UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder()
					.queryParam(PARAM_PAGE, lastPage)
					.queryParam(PARAM_PER_PAGE, pageSize);
			links.add(Link.fromUriBuilder(uriBuilder).rel("last").type("GET").build());
		}
		int nextPage = currentPage + 1;
		if (nextPage < 1)
			nextPage = 1;
		if (nextPage <= lastPage) {
			UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder()
					.queryParam(PARAM_PAGE, nextPage)
					.queryParam(PARAM_PER_PAGE, pageSize); 
			links.add(Link.fromUriBuilder(uriBuilder).rel("next").type("GET").build());
		}
		
		return links.toArray(new Link[links.size()]);
	}
	
}
