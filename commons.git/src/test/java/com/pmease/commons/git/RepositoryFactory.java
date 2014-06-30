package com.pmease.commons.git;

import java.io.File;

import org.eclipse.jgit.lib.Repository;

import com.google.inject.ImplementedBy;

@ImplementedBy(SimpleRepositoryFactory.class)
public interface RepositoryFactory {
	Repository open(File repoDir);
}
