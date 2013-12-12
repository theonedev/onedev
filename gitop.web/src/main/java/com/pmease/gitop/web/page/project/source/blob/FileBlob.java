package com.pmease.gitop.web.page.project.source.blob;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MimeType;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectStream;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FS;
import org.gitective.core.BlobUtils;
import org.gitective.core.CommitUtils;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.TreeNode;
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
import com.pmease.gitop.web.util.MimeTypeUtils;

/**
 * A FileBlob is a wrapper around the blob object in git to make it easy to 
 * access the basic information of it. 
 *
 */
public class FileBlob {
	private final Long projectId;
	private final String revision;
	private final String path;
	
	// calculated fields when build object
	private String objectId;
	private FileMode mode;
	private MimeType mimeType;
	private Language language;
	private byte[] data;
	private long size;
	private Charset charset;
	
	private FileBlob(final Long projectId, final String revision, final String path) {
		this.projectId = Preconditions.checkNotNull(projectId);
		this.revision = Preconditions.checkNotNull(revision);
		this.path = Preconditions.checkNotNull(path);
	}
	
	public static FileBlob of(Project project, String revision, String path) {
		return of(project, revision, path, Gitop.getInstance(FileTypeRegistry.class));
	}
	
	public static FileBlob of(Project project, String revision, String path, 
			FileTypeRegistry registry) {
		return initWithJGit(project, revision, path, registry);
	}
	
	// TODO: change this to be configurable
	private static final int MAX_SIZE = Amount.of(5, Data.MB).as(Data.BYTES);
	
	static FileBlob initWithJGit(Project project, String revision,
			String path, FileTypeRegistry registry) {

		FileBlob blob = new FileBlob(project.getId(), revision, path);

		Optional<Language> language = Languages.INSTANCE.guessLanguage(path);
		if (language.isPresent()) {
			blob.language = language.get();
		}
		
		File gitDir = project.code().repoDir();
		Repository repo = null;
		try {
			repo = RepositoryCache.open(FileKey.lenient(gitDir, FS.DETECTED));
			RevCommit commit = CommitUtils.getCommit(repo, revision);
			TreeWalk tw = TreeWalk.forPath(repo, path, commit.getTree());
			if (tw == null) {
				throw new RepositoryException("File " + path + " at " + revision + " doesn't exist");
			}
			
			blob.mode = tw.getFileMode(0);
			ObjectId blobId = tw.getObjectId(0);
			blob.objectId = blobId.name();
			ObjectLoader ol = repo.open(blobId, Constants.OBJ_BLOB);
			blob.size = ol.getSize();
			
			ObjectStream os = null;
			try {
				os = ol.openStream();
				blob.mimeType = registry.getMimeType(path, os);
				
				if (!blob.isLarge() && (
						language.isPresent() 
						|| registry.isSafeInline(blob.mimeType))) {
					
					CharsetDetector detector = new CharsetDetector();
					detector.setText(os);
					CharsetMatch match = detector.detect();
					if (match != null) {
						blob.charset = Charset.forName(match.getName());
					}
				}
			} finally {
				IOUtils.closeQuietly(os);
			}

			// read blob content to memory only when blob is not large (1MB),
			// and safe to embed to html
			if (!blob.isLarge() && (
					language.isPresent() 
					|| registry.isSafeInline(blob.mimeType))) {
				blob.data = ol.getCachedBytes(MAX_SIZE);
			}
			
			return blob;
		} catch (IOException e) {
			throw new RepositoryException(e);
		} finally {
			RepoUtils.close(repo);
		}
	}
	
	static FileBlob buildWithCli(Project project, String revision, 
			String path, FileTypeRegistry registry) {
		
		FileBlob blob = new FileBlob(project.getId(), revision, path);

		Git git = project.code();
		List<TreeNode> nodes = git.listTree(revision, path, false);
		Preconditions.checkState(!nodes.isEmpty());
		
		TreeNode node = nodes.get(0);
		blob.objectId = node.getHash();
		blob.mode = node.getMode();
		blob.size = node.getSize();
		
		Optional<Language> language = Languages.INSTANCE.guessLanguage(path);
		if (language.isPresent()) {
			blob.language = language.get();
		}
		
		byte[] data = git.show(revision, path);
		blob.data = data;
		
		blob.mimeType = registry.getMimeType(path, data);
		
		return blob;
	}
	
	public boolean isLarge() {
		return size > MAX_SIZE;
	}

	public String getStringContent() {
		if (data == null) {
			return null;
		}
		
		if (charset != null) {
			return new String(data, charset);
		} else {
			return new String(data);
		}
	}
	
	public ObjectStream openStream() {
		Project project = Gitop.getInstance(ProjectManager.class).get(projectId);
		Repository db = RepoUtils.open(project.code().repoDir());
		try {
			ObjectId id = BlobUtils.getId(db, revision, getPath());
			return db.open(id, Constants.OBJ_BLOB).openStream();
		} catch (IOException e) {
			throw new RepositoryException(e);
		} finally {
			RepoUtils.close(db);
		}
	}
	
	public boolean isText() {
		return MimeTypeUtils.isTextType(mimeType);
	}
	
	public boolean isImage() {
		return MimeTypeUtils.isImageType(mimeType);
	}
	
	public boolean isExecutable() {
		return mode == FileMode.EXECUTABLE_FILE;
	}
	
//	private static final Splitter LINE_SPLITTER = Splitter.on(Pattern.compile("\r\n|\n|\r"));
	
	public List<String> getLines() {
		if (isLarge() || !isText() || data == null) {
			Collections.emptyList();
		}
		
		try {
			return CharStreams.readLines(
					CharStreams.newReaderSupplier(getStringContent()));
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}
	
	public String getObjectId() {
		return objectId;
	}

	public FileMode getMode() {
		return mode;
	}

	public MimeType getMimeType() {
		return mimeType;
	}

	public @Nullable Language getLanguage() {
		return language;
	}

	public @Nullable byte[] getData() {
		return data;
	}

	public @Nullable Charset getCharset() {
		return charset;
	}

	public long getSize() {
		return size;
	}

	public Long getProjectId() {
		return projectId;
	}

	public String getRevision() {
		return revision;
	}

	public String getPath() {
		return path;
	}
}
