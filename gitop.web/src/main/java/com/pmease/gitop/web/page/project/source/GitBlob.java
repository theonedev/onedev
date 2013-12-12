package com.pmease.gitop.web.page.project.source;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
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
import org.gitective.core.BlobUtils;
import org.gitective.core.CommitUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.common.quantity.Amount;
import com.pmease.gitop.web.common.quantity.Data;
import com.pmease.gitop.web.jgit.RepoUtils;
import com.pmease.gitop.web.jgit.RepositoryException;
import com.pmease.gitop.web.service.FileTypeRegistry;
import com.pmease.gitop.web.service.impl.Language;
import com.pmease.gitop.web.service.impl.Languages;

public class GitBlob {
	private final Long projectId;
	private final String path;
	private final String revision;
	
	private String blobId;
	private long size;
	private FileMode mode;
	private byte[] content;
	
	private MimeType mime;
	private Language language;
	
	GitBlob(Long projectId, final String path, final String revision) {
		this.projectId = projectId;
		this.path = path;
		this.revision = revision;
	}
	
	public static GitBlob of(Project project, String revision, String path) {
		return of(project, revision, path, false);
	}
	
	public static GitBlob of(Project project, String revision, String path, boolean forceFetchContent) {
		return of(project, revision, path, forceFetchContent, Gitop.getInstance(FileTypeRegistry.class));
	}
	
	private static final int MAX_BLOB_SIZE = Amount.of(10, Data.MB).as(Data.BYTES);
	
	public static GitBlob of(
			Project project,
			String revision,
			String path,
			boolean forceFetchContent,
			FileTypeRegistry registry) {
		GitBlob blob = new GitBlob(project.getId(), path, revision);
//		Git git = project.code();
//		List<TreeNode> nodes = git.listTree(revision, path, false);
//		TreeNode node = nodes.get(0);
//		blob.blobId = node.getHash();
//		blob.mode = node.getMode();
//		blob.size = node.getSize();
//		
//		Optional<Language> language = Languages.INSTANCE.guessLanguage(path);
//		if (language.isPresent()) {
//			blob.language = language.get();
//		}
//
//		byte[] bytes = git.show(revision, path);
//		blob.mime = registry.getMimeType(path, bytes);
//		if (forceFetchContent || registry.isSafeInline(blob.mime)) {
//			blob.content = bytes;
//		}
//		
//		return blob;
		
		Repository db = RepoUtils.open(project.code().repoDir());
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
			
			Optional<Language> language = Languages.INSTANCE.guessLanguage(path);
			if (language.isPresent()) {
				blob.language = language.get();
			}
			
			ObjectStream os = null;
			try {
				os = ol.openStream();
				blob.mime = registry.getMimeType(path, os);
			} finally {
				IOUtils.closeQuietly(os);
			}

			if (forceFetchContent) {
				blob.content = ol.getCachedBytes();
			} else if (registry.isSafeInline(blob.mime)) {
				// TODO: process LargeObjectException
				blob.content = ol.getCachedBytes(MAX_BLOB_SIZE);
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

	public ObjectStream openStream() {
		Project project = Gitop.getInstance(ProjectManager.class).get(projectId);
		Repository db = RepoUtils.open(project.code().repoDir());
		try {
			ObjectId id = BlobUtils.getId(db, revision, path);
			return db.open(id, Constants.OBJ_BLOB).openStream();
		} catch (IOException e) {
			throw new RepositoryException(e);
		} finally {
			RepoUtils.close(db);
		}
	}

	public String getBlobId() {
		return blobId;
	}

	public long getSize() {
		return size;
	}

	public FileMode getMode() {
		return mode;
	}

	public @Nullable byte[] getContent() {
		return content;
	}

	public MimeType getMime() {
		return mime;
	}

	public @Nullable Language getLanguage() {
		return language;
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
