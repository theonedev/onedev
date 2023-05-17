package io.onedev.server.search.entitytext;

import org.apache.lucene.search.Query;

import java.util.Collection;

public class EntityTextQuery {
	
	private final Query contentQuery;
	
	private final Collection<Long> applicableProjectIds;
	
	public EntityTextQuery(Query contentQuery, Collection<Long> applicableProjectIds) {
		this.contentQuery = contentQuery;
		this.applicableProjectIds = applicableProjectIds;
	}

	public Query getContentQuery() {
		return contentQuery;
	}

	public Collection<Long> getApplicableProjectIds() {
		return applicableProjectIds;
	}
}
