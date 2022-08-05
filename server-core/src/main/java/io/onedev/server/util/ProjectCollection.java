package io.onedev.server.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProjectCollection {

	private final ProjectCache cache;
	
	private final List<Long> ids;
	
	public ProjectCollection(ProjectCache cache, List<Long> ids) {
		this.cache = cache;
		this.ids = ids;
	}

	public ProjectCache getCache() {
		return cache;
	}

	public List<Long> getIds() {
		return ids;
	}
	
	public void sortByPath() {
		Collections.sort(ids, new Comparator<Long>() {

			@Override
			public int compare(Long o1, Long o2) {
				return cache.getPath(o1).compareTo(cache.getPath(o2));
			}
			
		});
	}
	
}
