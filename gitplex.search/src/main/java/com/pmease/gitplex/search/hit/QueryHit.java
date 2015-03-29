package com.pmease.gitplex.search.hit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;

public abstract class QueryHit implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final String blobPath;
	
	public QueryHit(String blobPath) {
		this.blobPath = blobPath;
	}

	public String getBlobPath() {
		return blobPath;
	}
	
	public abstract int getLineNo();

	public abstract Component render(String componentId);
	
	public static List<PathHits> groupByPath(List<QueryHit> hits) {
		Map<String, PathHits> hitsByPath = new HashMap<>();

		for (QueryHit hit: hits) {
			PathHits pathHits = hitsByPath.get(hit.getBlobPath());
			if (pathHits == null) {
				pathHits = new PathHits(hit.getBlobPath(), new ArrayList<QueryHit>());
				hitsByPath.put(hit.getBlobPath(), pathHits);
			}
			pathHits.getHits().add(hit);
		}
		
		List<PathHits> groupedHits = new ArrayList<>(hitsByPath.values());
		
		Collections.sort(groupedHits, new Comparator<PathHits>() {

			@Override
			public int compare(PathHits hits1, PathHits hits2) {
				return hits1.getBlobPath().compareTo(hits2.getBlobPath());
			}
			
		});
		
		return groupedHits;
	}
	
}
