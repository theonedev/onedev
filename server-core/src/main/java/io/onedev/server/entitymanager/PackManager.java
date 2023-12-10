package io.onedev.server.entitymanager;

import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectPackStats;
import io.onedev.server.util.criteria.Criteria;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface PackManager extends EntityManager<Pack> {

	List<Pack> query(@Nullable Project project, EntityQuery<Pack> packQuery, int firstResult, int maxResults);

	int count(@Nullable Project project, Criteria<Pack> packCriteria);

	List<Pack> queryPrevComparables(Pack compareWith, String fuzzyQuery, int count);
	
	List<String> queryTags(Project project, String type, @Nullable String lastTag, int count);
	
	List<String> queryProps(Project project, String propName, String matchWith, int count);
	
	List<ProjectPackStats> queryStats(Collection<Project> projects);
	
	@Nullable
    Pack findByTag(Project project, String type, String tag);
	
	@Nullable
	Pack findByGWithoutAV(Project project, String type, String groupId);

	List<Pack> queryByGAWithV(Project project, String type, String groupId, String artifactId);

	@Nullable
	Pack findByGAV(Project project, String type, String groupId, String artifactId, String version);
	
	void deleteByTag(Project project, String type, String tag);
	
    void createOrUpdate(Pack pack, Collection<PackBlob> packBlobs);

	void createOrUpdate(Pack pack);
	
	void delete(Collection<Pack> packs);

}
