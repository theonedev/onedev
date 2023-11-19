package io.onedev.server.entitymanager;

import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface PackManager extends EntityManager<Pack> {

	List<Pack> query(@Nullable Project project, EntityQuery<Pack> packQuery, int firstResult, int maxResults);

	int count(@Nullable Project project, Criteria<Pack> packCriteria);
	
	List<Pack> query(Project project, String type, @Nullable String fuzzyQuery,
                     int firstResult, int maxResults);
	
	List<String> queryTags(Project project, String type, @Nullable String lastTag, int count);

	List<String> queryVersions(Project project, String matchWith, int count);
	
	int count(Project project, String type, @Nullable String query);

	@Nullable
    Pack find(Project project, String type, String version);

	void delete(Project project, String type, String version);
	
    void createOrUpdate(Pack pack, Collection<PackBlob> packBlobs);

	void delete(Collection<Pack> packs);
	
}
