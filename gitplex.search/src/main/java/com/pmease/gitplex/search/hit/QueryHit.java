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
	
	public static List<MatchedBlob> groupByBlob(List<QueryHit> hits) {
		Map<String, MatchedBlob> hitsByBlob = new HashMap<>();

		for (QueryHit hit: hits) {
			MatchedBlob blob = hitsByBlob.get(hit.getBlobPath());
			if (blob == null) {
				blob = new MatchedBlob(hit.getBlobPath(), new ArrayList<QueryHit>());
				hitsByBlob.put(hit.getBlobPath(), blob);
			}
			if (!(hit instanceof FileHit))
				blob.getHits().add(hit);
		}
		
		List<MatchedBlob> matchedBlobs = new ArrayList<>(hitsByBlob.values());
		
		Collections.sort(matchedBlobs, new Comparator<MatchedBlob>() {

			@Override
			public int compare(MatchedBlob hits1, MatchedBlob hits2) {
				return hits1.getBlobPath().compareTo(hits2.getBlobPath());
			}
			
		});
		
		return matchedBlobs;
	}
	
}
