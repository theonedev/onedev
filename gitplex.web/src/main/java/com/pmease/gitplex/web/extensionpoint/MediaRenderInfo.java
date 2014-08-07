package com.pmease.gitplex.web.extensionpoint;

import org.apache.tika.mime.MediaType;

import com.pmease.gitplex.web.component.view.BlobRenderInfo;

public class MediaRenderInfo {
		
	private final String path;
	
	private final String revision;
	
	private final byte[] content;
	
	private final MediaType mediaType;
	
	public MediaRenderInfo(String path, String revision, MediaType mediaType, byte[] content) {
		this.path = path;
		this.revision = revision;
		this.mediaType = mediaType;
		this.content = content;
	}

	public String getPath() {
		return path;
	}

	public String getRevision() {
		return revision;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public byte[] getContent() {
		return content;
	}
	
	public static MediaRenderInfo from(BlobRenderInfo blob, MediaType mediaType) {
		return new MediaRenderInfo(blob.getPath(), blob.getRevision(), mediaType, blob.getContent());
	}
}
