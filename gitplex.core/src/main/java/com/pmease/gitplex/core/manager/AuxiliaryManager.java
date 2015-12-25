package com.pmease.gitplex.core.manager;

import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.commons.git.Commit;
import com.pmease.gitplex.core.model.Repository;

public interface AuxiliaryManager {
	
	void check(Repository repository, String refName);
	
	Map<String, Commit> getCommits(Repository repository, Set<String> commitHashes); 
	
	Set<PersonIdent> getAuthors(Repository repository);
	
	Set<PersonIdent> getCommitters(Repository repository);
	
	Set<PersonIdent> getAuthorsModified(Repository repository, String file);
	
	Set<PersonIdent> getCommittersModified(Repository repository, String file);

}
