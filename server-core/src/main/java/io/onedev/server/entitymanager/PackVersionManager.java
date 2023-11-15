package io.onedev.server.entitymanager;

import io.onedev.server.model.PackBlob;
import io.onedev.server.model.PackVersion;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface PackVersionManager extends EntityManager<PackVersion> {
	
	List<PackVersion> query(Project project, String type, @Nullable String fuzzyQuery,
                            int firstResult, int maxResults);
	
	List<String> queryTags(Project project, String type, @Nullable String lastTag, int count);
	
	int count(Project project, String type, @Nullable String query);

	@Nullable
    PackVersion findByName(Project project, String type, String name);

	@Nullable
	PackVersion findByDataHash(Project project, String type, String dataHash);

	void deleteByName(Project project, String type, String name);
	
	void deleteByDataHash(Project project, String type, String dataHash);
	
    void createOrUpdate(PackVersion packVersion, Collection<PackBlob> packBlobs);

}
