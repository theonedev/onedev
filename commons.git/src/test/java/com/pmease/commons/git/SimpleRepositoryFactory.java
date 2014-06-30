package com.pmease.commons.git;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

@Singleton
public class SimpleRepositoryFactory implements RepositoryFactory {

	@Override
	public Repository open(File repoDir) {
		try {
			return new FileRepository(repoDir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
