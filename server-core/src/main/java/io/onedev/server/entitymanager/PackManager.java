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
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public interface PackManager extends EntityManager<Pack> {

	List<Pack> query(@Nullable Project project, EntityQuery<Pack> packQuery, int firstResult, int maxResults);

	int count(@Nullable Project project, Criteria<Pack> packCriteria);

	List<Pack> queryPrevComparables(Pack compareWith, String fuzzyQuery, int count);
	
	List<String> queryTags(Project project, String type, @Nullable String lastTag, int count);
	
	List<String> queryProps(Project project, String propName, String matchWith, int count);
	
	List<ProjectPackStats> queryStats(Collection<Project> projects);
	
	@Nullable
    Pack findByTag(Project project, String type, String tag);
	
	Pack findByNameAndVersion(Project project, String type, String name, String version);
	
	@Nullable
	Pack findByGWithoutAV(Project project, String type, String groupId);

	List<Pack> queryByGAWithV(Project project, String type, String groupId, String artifactId);
	
	List<Pack> queryByName(Project project, String type, String name, 
						   @Nullable Comparator<Pack> sortComparator);
	
	List<Pack> queryLatests(Project project, String type, String nameQuery, int firstResult, int maxResults);

	int countNames(Project project, String type, @Nullable String nameQuery, 
				   @Nullable String excludeVersionQuery);

	List<String> queryNames(Project project, String type, @Nullable String nameQuery, 
							@Nullable String excludeVersionQuery, int firstResult, int maxResults);
	
	Map<String, List<Pack>> loadPacks(List<String> names, @Nullable String exludeVersionQuery, 
									  @Nullable Comparator<Pack> sortComparator);
	
	@Nullable
	Pack findByGAV(Project project, String type, String groupId, String artifactId, String version);
	
	void deleteByTag(Project project, String type, String tag);

	void createOrUpdate(Pack pack, @Nullable Collection<PackBlob> packBlobs, boolean postPublishEvent);
	
	void delete(Collection<Pack> packs);

}
