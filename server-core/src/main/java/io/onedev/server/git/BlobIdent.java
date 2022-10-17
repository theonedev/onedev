package io.onedev.server.git;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.MoreObjects;

import io.onedev.server.git.exception.ObjectNotFoundException;
import io.onedev.server.model.Project;
import io.onedev.server.util.RevisionAndPath;

public class BlobIdent implements Serializable, Comparable<BlobIdent> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * null when there is no commit yet
	 */
	@Nullable
	public String revision;
	
	@Nullable
	public String path;
	
	@Nullable
	public Integer mode;
	
	public BlobIdent(BlobIdent blobIdent) {
		revision = blobIdent.revision;
		path = blobIdent.path;
		mode = blobIdent.mode;
	}
	
	public BlobIdent(@Nullable String revision, @Nullable String path, @Nullable Integer mode) {
		this.revision = revision;
		this.path = path;
		this.mode = mode;
	}
	
	public BlobIdent(@Nullable String revision, @Nullable String path) {
		this(revision, path, FileMode.REGULAR_FILE.getBits());
	}
	
	public BlobIdent() {
	}
	
	public BlobIdent(Project project, List<String> segments) {
		RevisionAndPath revisionAndPath = RevisionAndPath.parse(project, segments); 
		revision = revisionAndPath.getRevision();
		path = revisionAndPath.getPath();
		if (path != null) {
			mode = project.getMode(revision, path);
			if (mode == 0) {
				throw new ObjectNotFoundException("Unable to find blob path '" + path
						+ "' in revision '" + revision + "'");
			}
		} else {
			mode = FileMode.TREE.getBits();
		}
	}
	
	public boolean isTree() {
		return (FileMode.TYPE_MASK & mode) == FileMode.TYPE_TREE;
	}
	
	public boolean isGitLink() {
		return (FileMode.TYPE_MASK & mode) == FileMode.TYPE_GITLINK;
	}
	
	public boolean isSymbolLink() {
		return (FileMode.TYPE_MASK & mode) == FileMode.TYPE_SYMLINK;
	}

	public boolean isFile() {
		return (FileMode.TYPE_MASK & mode) == FileMode.TYPE_FILE;
	}
	
	@Nullable
	public String getName() {
		if (path != null) {
			return path.substring(path.lastIndexOf('/')+1);
		} else {
			return null;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BlobIdent)) 
			return false;
		if (this == obj)
			return true;
		BlobIdent otherIdent = (BlobIdent) obj;
		return new EqualsBuilder()
			.append(revision, otherIdent.revision)
			.append(path, otherIdent.path)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(revision)
			.append(path)
			.toHashCode();
	}		

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(BlobIdent.class)
				.add("revision", revision)
				.add("path", path)
				.omitNullValues()
				.toString();
	}

	@Override
	public int compareTo(BlobIdent ident) {
		if (isTree()) {
			if (ident.isTree()) 
				return GitUtils.comparePath(path, ident.path);
			else
				return -1;
		} else if (ident.isTree()) {
			return 1;
		} else {
			return GitUtils.comparePath(path, ident.path);
		}
	}

}