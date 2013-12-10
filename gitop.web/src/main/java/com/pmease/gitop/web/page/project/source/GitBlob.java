package com.pmease.gitop.web.page.project.source;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.tika.io.IOUtils;
import org.apache.tika.mime.MimeType;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectStream;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.gitective.core.CommitUtils;

import com.google.common.base.Charsets;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.common.quantity.Amount;
import com.pmease.gitop.web.common.quantity.Data;
import com.pmease.gitop.web.service.FileTypeRegistry;
import com.pmease.gitop.web.util.jgit.RepoUtils;
import com.pmease.gitop.web.util.jgit.RepositoryException;

public class GitBlob {
	private final Long projectId;
	private final String path;
	private final String revision;
	
	private String blobId;
	private long size;
	private FileMode mode;
	private byte[] content;
	private MimeType mime;
	
	GitBlob(Long projectId, final String path, final String revision) {
		this.projectId = projectId;
		this.path = path;
		this.revision = revision;
	}
	
	public static GitBlob of(Project project, String revision, String path) {
		return of(project, revision, path, Gitop.getInstance(FileTypeRegistry.class));
	}
	
	private static final int MAX_BLOB_SIZE = Amount.of(10, Data.MB).as(Data.BYTES);
	
	public static GitBlob of(Project project, String revision, String path, FileTypeRegistry registry) {
		Repository db = RepoUtils.open(project.code().repoDir());
		GitBlob blob = new GitBlob(project.getId(), path, revision);
		TreeWalk treeWalk = null;
		try {
			RevCommit commit = CommitUtils.getCommit(db, revision);
			treeWalk = TreeWalk.forPath(db, path, commit.getTree());
			
			if (treeWalk == null) {
				throw new RepositoryException("File " + path + " at " + revision + " doesn't exist");
			}
			blob.mode = treeWalk.getFileMode(0);
			ObjectId id = treeWalk.getObjectId(0);
			blob.blobId = id.getName();
			
			ObjectLoader ol = db.open(id, Constants.OBJ_BLOB);
			blob.size = ol.getSize();
			
			ObjectStream os = null;
			MimeType mime = null;
			try {
				os = ol.openStream();
				mime = registry.getMimeType(path, os);
				blob.mime = mime;
			} finally {
				IOUtils.closeQuietly(os);
			}

			if (registry.isSafeInline(mime)) {
				// TODO: process LargeObjectException
				blob.content = ol.getBytes(MAX_BLOB_SIZE);
			}
			
			return blob;
		} catch (MissingObjectException e) {
			throw new RepositoryException(e);
		} catch (IncorrectObjectTypeException e) {
			throw new RepositoryException(e);
		} catch (IOException e) {
			throw new RepositoryException(e);
		} finally {
			RepoUtils.close(db);
		}
	}


	public String getBlobId() {
		return blobId;
	}

	public void setBlobId(String blobId) {
		this.blobId = blobId;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public FileMode getMode() {
		return mode;
	}

	public void setMode(FileMode mode) {
		this.mode = mode;
	}

	public @Nullable byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public MimeType getMime() {
		return mime;
	}

	public void setMime(MimeType mime) {
		this.mime = mime;
	}

	public Long getProjectId() {
		return projectId;
	}

	public String getPath() {
		return path;
	}

	public String getRevision() {
		return revision;
	}

	public @Nullable String getStringContent() {
		if (content == null) {
			return null;
		}
		
		return new String(content, Charsets.UTF_8);
	}
}
