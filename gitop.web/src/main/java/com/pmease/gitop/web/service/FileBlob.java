package com.pmease.gitop.web.service;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.mime.MediaType;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.util.RawParseUtils;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.common.quantity.Data;
import com.pmease.gitop.web.page.project.source.blob.language.Language;
import com.pmease.gitop.web.page.project.source.blob.language.Languages;
import com.pmease.gitop.web.util.MediaTypeUtils;

public class FileBlob implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Long projectId;
	private final String revision;
	private final String file;
	
	// calculated fields
	private String objectId;
	private MediaType mediaType;
	private byte[] data;
	private long size;
	private FileMode mode;
	private Charset charset;

	private static final long LARGE_FILE_SIZE = 2 * Data.ONE_MB;
	
	public FileBlob(final Long projectId, final String revision, final String file) {
		this.projectId = Preconditions.checkNotNull(projectId);
		this.revision = Preconditions.checkNotNull(revision);
		this.file = Preconditions.checkNotNull(file);
	}

	public static FileBlob of(Project project, String revision, String file) {
		return Gitop.getInstance(FileBlobService.class).get(project, revision, file);
	}
	
	public boolean isText() {
		return MediaTypeUtils.isTextType(mediaType);
	}
	
	public boolean isImage() {
		return MediaTypeUtils.isImageType(mediaType);
	}
	
	public boolean isEmpty() {
		return size <= 0L;
	}
	
	public boolean isLarge() {
		return size > LARGE_FILE_SIZE;
	}
	
	public boolean isExecutable() {
		return mode == FileMode.EXECUTABLE_FILE;
	}
	
	public @Nullable Language getLanguage() {
		Language lang = Languages.INSTANCE.findByMediaType(mediaType);
		if ((lang == null) && (MediaTypeUtils.isXMLType(mediaType))) {
			return Languages.INSTANCE.findByMediaType(MediaType.APPLICATION_XML);
		}
		
		return lang;
	}
	
	public boolean isHighlightable() {
		return getSize() < LARGE_FILE_SIZE 
				&& isText() 
				&& getLanguage() != null; 
	}
	
	public @Nullable String getStringContent() {
		if (data == null) {
			return null;
		}
		
		return RawParseUtils.decode(charset, data);
	}
	
	public List<String> getLines() {
		if (isEmpty() || isLarge() || !isText() || data == null) {
			return Collections.emptyList();
		}
		
		try {
			return CharStreams.readLines(
					CharStreams.newReaderSupplier(getStringContent()));
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}
	
	public String getName() {
		return FilenameUtils.getName(getFilePath());
	}
	
	/**
	 * Example:
	 * /a/b/c.txt -> /a/b/
	 * 
	 * @return
	 */
	public String getFullPath() {
		return FilenameUtils.getFullPath(getFilePath());
	}
	
	public Long getProjectId() {
		return projectId;
	}

	public String getRevision() {
		return revision;
	}

	/**
	 * Returns the full file name
	 * 
	 * @return
	 */
	public String getFilePath() {
		return file;
	}

	public String getObjectId() {
		return objectId;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public @Nullable byte[] getData() {
		return data;
	}

	public long getSize() {
		return size;
	}

	public FileMode getMode() {
		return mode;
	}

	public Charset getCharset() {
		return charset;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void setMode(FileMode mode) {
		this.mode = mode;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof FileBlob))
			return false;
		
		FileBlob rhs = (FileBlob) other;
		return Objects.equal(projectId, rhs.projectId)
				&& Objects.equal(revision, rhs.revision)
				&& Objects.equal(file, rhs.file);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(projectId, revision, file);
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("Project Id", projectId)
				.add("Revision", revision)
				.add("Path", file)
				.toString();
	}
}
