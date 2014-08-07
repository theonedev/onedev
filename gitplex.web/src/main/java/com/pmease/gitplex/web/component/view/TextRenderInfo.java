package com.pmease.gitplex.web.component.view;

import java.io.Serializable;
import java.nio.charset.Charset;

import com.pmease.commons.git.GitText;

@SuppressWarnings("serial")
public class TextRenderInfo implements Serializable {

	private final String path;
	
	private final String revision;
	
	private final GitText text;
	
	private final Charset charset;
	
	public TextRenderInfo(String path, String revision, GitText text, Charset charset) {
		this.path = path;
		this.revision = revision;
		this.text = text;
		this.charset = charset;
	}

	public String getPath() {
		return path;
	}

	public String getRevision() {
		return revision;
	}

	public GitText getText() {
		return text;
	}

	public Charset getCharset() {
		return charset;
	}
	
	public static TextRenderInfo from(BlobRenderInfo blob, Charset charset) {
		return new TextRenderInfo(blob.getPath(), blob.getRevision(), 
				GitText.from(blob.getContent(), charset), charset);
	}
}
