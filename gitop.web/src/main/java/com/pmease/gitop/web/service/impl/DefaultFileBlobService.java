package com.pmease.gitop.web.service.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MediaType;
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

import com.google.common.base.Objects;
import com.google.common.io.ByteStreams;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.jgit.RepoUtils;
import com.pmease.gitop.web.jgit.RepositoryException;
import com.pmease.gitop.web.service.FileBlob;
import com.pmease.gitop.web.service.FileBlobService;
import com.pmease.gitop.web.service.FileTypes;
import com.pmease.gitop.web.util.UniversalEncodingDetector;

public class DefaultFileBlobService implements FileBlobService {

	final FileTypes fileTypes;
	
	@Inject
	DefaultFileBlobService(FileTypes fileTypes) {
		this.fileTypes = fileTypes;
	}
	
	@Override
	public FileBlob get(Project project, String revision, String path) {
		FileBlob blob = new FileBlob(project.getId(), revision, path);

		File gitDir = project.code().repoDir();
		Repository repo = null;
		try {
			repo = RepoUtils.open(gitDir);
			RevCommit commit = CommitUtils.getCommit(repo, revision);
			TreeWalk tw = TreeWalk.forPath(repo, path, commit.getTree());
			if (tw == null) {
				throw new RepositoryException("File " + path + " at " + revision + " doesn't exist");
			}
			if (tw.isSubtree()) {
				throw new RepositoryException("Not a file blob");
			}
			
			FileMode mode = tw.getFileMode(0);
			blob.setMode(mode);
			ObjectId blobId = tw.getObjectId(0);
			blob.setObjectId(blobId.name());
			ObjectLoader ol = repo.open(blobId, Constants.OBJ_BLOB);
			blob.setSize(ol.getSize());

			if (blob.isEmpty()) {
				blob.setMediaType(MediaType.OCTET_STREAM);
			} else {
				BufferedInputStream stream = null;
				try {
					stream = new BufferedInputStream(ol.openStream());
					blob.setMediaType(fileTypes.getMediaType(path, stream));
					
					if (!UniversalEncodingDetector.isBinary(stream) 
							&& Objects.equal(blob.getMediaType(), MediaType.OCTET_STREAM)) {
						// error detecting by tika
						blob.setMediaType(MediaType.TEXT_PLAIN);
					}
					
					if (!blob.isLarge() && fileTypes.isSafeInline(blob.getMediaType())) {
						blob.setCharset(UniversalEncodingDetector.detect(stream));
						blob.setData(ByteStreams.toByteArray(stream));
					}
				} finally {
					IOUtils.closeQuietly(stream);
				}
			}
			
			return blob;
		} catch (IOException e) {
			throw new RepositoryException(e);
		} finally {
			RepoUtils.close(repo);
		}
	}

	@Override
	public ObjectStream openStream(Project project, String revision, String path) {
		File gitDir = project.code().repoDir();
		Repository repo = null;
		try {
			repo = RepoUtils.open(gitDir);
			return BlobUtils.getStream(repo, revision, path); 
		} finally {
			RepoUtils.close(repo);
		}
	}

}
