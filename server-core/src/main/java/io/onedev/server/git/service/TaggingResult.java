package io.onedev.server.git.service;

import java.io.Serializable;

import org.eclipse.jgit.lib.ObjectId;

public class TaggingResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private final ObjectId tagId;
	
	private final ObjectId commitId;
	
	public TaggingResult(ObjectId tagId, ObjectId commitId) {
		this.tagId = tagId;
		this.commitId = commitId;
	}

	public ObjectId getTagId() {
		return tagId;
	}

	public ObjectId getCommitId() {
		return commitId;
	}
	
}
