package io.onedev.server.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.apache.shiro.subject.Subject;

import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectPackTypeStat;
import io.onedev.server.util.criteria.Criteria;

public interface PackService extends EntityService<Pack> {

	List<Pack> query(Subject subject, @Nullable Project project, EntityQuery<Pack> packQuery, 
					 boolean loadLabelsAndBlobs, int firstResult, int maxResults);

	int count(Subject subject, @Nullable Project project, Criteria<Pack> packCriteria);

	List<Pack> queryPrevComparables(Pack compareWith, String fuzzyQuery, int count);
	
	List<String> queryVersions(Project project, String type, String name, 
							   @Nullable String lastVersion, int count);
	
	List<String> queryProps(Project project, String propName, String matchWith, int count);
	
	List<ProjectPackTypeStat> queryTypeStats(Collection<Project> projects);
	
	@Nullable
	Pack findByNameAndVersion(Project project, String type, String name, String version);

	List<Pack> query(Project project, String type, @Nullable Boolean prerelease);
	
	List<Pack> queryByName(Project project, String type, String name, 
						   @Nullable Comparator<Pack> sortComparator);
	
	List<Pack> queryLatests(Project project, String type, @Nullable String nameQuery, 
							boolean includePrerelease, int firstResult, int maxResults);

	int countNames(Project project, String type, @Nullable String nameQuery, 
				   boolean includePrerelease);

	List<String> queryNames(Project project, String type, @Nullable String nameQuery, 
							boolean includePrerelease, int firstResult, int maxResults);
	
	Map<String, List<Pack>> loadPacks(List<String> names, boolean includePrerelease, 
									  @Nullable Comparator<Pack> sortComparator);
	
	void deleteByNameAndVersion(Project project, String type, String name, String version);

	void createOrUpdate(Pack pack, @Nullable Collection<PackBlob> packBlobs, boolean postPublishEvent);
	
	void delete(Collection<Pack> packs);

}
