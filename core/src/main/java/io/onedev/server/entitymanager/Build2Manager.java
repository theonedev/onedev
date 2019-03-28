package io.onedev.server.entitymanager;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.Build2;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

public interface Build2Manager extends EntityManager<Build2> {
	
    @Nullable
    Build2 find(Project project, long number);
    
	@Nullable
	Build2 find(Project project, String commitHash, String jobName, Map<String, String> params); 
	
	void create(Build2 build);
	
	@Nullable
	Build2 find(String runInstanceId);
	
	List<Build2> queryUnfinished();
	
}
