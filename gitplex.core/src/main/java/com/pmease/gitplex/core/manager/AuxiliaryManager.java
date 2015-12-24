package com.pmease.gitplex.core.manager;

import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.gitplex.core.model.Repository;

public interface AuxiliaryManager {
	
	void check(Repository repository, String refName);
	
	Map<String, Set<String>> getParents(Set<String> commitHashes); 
	
	Set<PersonIdent> getAuthors();
	
	Set<PersonIdent> getCommitters();
	
	Set<PersonIdent> getAuthorsModified(String file);
	
	Set<PersonIdent> getCommittersModified(String file);

}
