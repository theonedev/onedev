package com.pmease.gitplex.search.hit;

public class FileHit extends QueryHit {

	public FileHit(String blobPath) {
		super(blobPath);
	}

	@Override
	public String toString() {
		return getBlobPath();
	}

}
