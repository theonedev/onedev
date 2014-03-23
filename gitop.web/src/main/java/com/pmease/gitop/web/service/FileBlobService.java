package com.pmease.gitop.web.service;

import org.eclipse.jgit.lib.ObjectStream;

import com.pmease.gitop.model.Repository;

public interface FileBlobService {
	
	FileBlob get(Repository project, String revision, String path);
	
	ObjectStream openStream(Repository project, String revision, String path);
}
