package com.pmease.gitop.web.service;

import org.eclipse.jgit.lib.ObjectStream;

import com.google.inject.ImplementedBy;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.service.impl.DefaultFileBlobService;

@ImplementedBy(DefaultFileBlobService.class)
public interface FileBlobService {
	
	FileBlob get(Repository repository, String revision, String path);
	
	ObjectStream openStream(Repository repository, String revision, String path);
}
