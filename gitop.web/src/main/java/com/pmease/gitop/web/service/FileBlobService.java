package com.pmease.gitop.web.service;

import org.eclipse.jgit.lib.ObjectStream;

import com.pmease.gitop.model.Project;

public interface FileBlobService {
	
	FileBlob get(Project project, String revision, String path);
	
	ObjectStream openStream(Project project, String revision, String path);
}
