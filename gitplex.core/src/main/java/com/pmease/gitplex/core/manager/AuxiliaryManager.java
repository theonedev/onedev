package com.pmease.gitplex.core.manager;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pmease.commons.git.NameAndEmail;
import com.pmease.gitplex.core.model.Repository;

public interface AuxiliaryManager {
	
	void check(Repository repository, String refName);
	
	List<String> getFiles(Repository repository);
	
	List<NameAndEmail> getContributors(Repository repository);
	
	Map<String, Map<NameAndEmail, Long>> getContributions(Repository repository, Set<String> files);

}
