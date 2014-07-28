package com.pmease.gitplex.web.service.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MediaType;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectStream;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.gitective.core.BlobUtils;
import org.gitective.core.CommitUtils;

import com.google.common.base.Objects;
import com.google.common.io.ByteStreams;
import com.pmease.commons.util.Charsets;
import com.pmease.commons.util.MediaTypes;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.git.RepoUtils;
import com.pmease.gitplex.web.git.RepositoryException;
import com.pmease.gitplex.web.service.FileBlob;
import com.pmease.gitplex.web.service.FileBlobService;

@Singleton
public class DefaultFileBlobService implements FileBlobService {

	@Override
	public FileBlob get(Repository repository, String revision, String path) {
		FileBlob blob = new FileBlob(repository.getId(), revision, path);

		File gitDir = repository.git().repoDir();
		org.eclipse.jgit.lib.Repository repo = null;
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
				blob.setMediaType(MediaType.TEXT_PLAIN);
			} else {
				BufferedInputStream stream = null;
				try {
					stream = new BufferedInputStream(ol.openStream());
					blob.setMediaType(MediaTypes.detectFrom(stream, path));
					
					if (!Charsets.isBinary(stream) 
							&& Objects.equal(blob.getMediaType(), MediaType.OCTET_STREAM)) {
						// error detecting by tika
						blob.setMediaType(MediaType.TEXT_PLAIN);
					}
					
					if (!blob.isLarge() && MediaTypes.isSafeInline(blob.getMediaType())) {
						blob.setCharset(Charsets.detectFrom(stream));
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
	public ObjectStream openStream(Repository repository, String revision, String path) {
		File gitDir = repository.git().repoDir();
		org.eclipse.jgit.lib.Repository repo = null;
		try {
			repo = RepoUtils.open(gitDir);
			return BlobUtils.getStream(repo, revision, path); 
		} finally {
			RepoUtils.close(repo);
		}
	}

}
