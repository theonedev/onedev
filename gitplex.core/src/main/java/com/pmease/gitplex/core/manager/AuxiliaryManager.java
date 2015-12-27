package com.pmease.gitplex.core.manager;

import java.util.Map;
import java.util.Set;

import com.pmease.commons.git.NameAndEmail;
import com.pmease.gitplex.core.model.Repository;

public interface AuxiliaryManager {
	
	void check(Repository repository, String refName);
	
	byte[] getPaths(Repository repository);
	
	Set<NameAndEmail> getContributors(Repository repository);
	
	Map<String, Map<NameAndEmail, Long>> getContributors(Repository repository, Set<String> files);

}
