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
	
	public BlobIdent() {
	}
	
	public BlobIdent(Project project, List<String> urlSegments) {
		StringBuilder revisionBuilder = new StringBuilder();
		for (int i=0; i<urlSegments.size(); i++) {
			if (i != 0)
				revisionBuilder.append("/");
			revisionBuilder.append(urlSegments.get(i));
			if (project.getObjectId(revisionBuilder.toString(), false) != null) {
				revision = revisionBuilder.toString();
				StringBuilder pathBuilder = new StringBuilder();
				for (int j=i+1; j<urlSegments.size(); j++) {
					if (j != i+1)
						pathBuilder.append("/");
					pathBuilder.append(urlSegments.get(j));
				}
				if (pathBuilder.length() != 0) {
					path = pathBuilder.toString();
					mode = project.getMode(revision, path);
				} else {
					mode = FileMode.TREE.getBits();
				}
				return;
			}
		}
		if (revisionBuilder.length() != 0) {
			throw new ObjectNotFoundException("Revision not found: " + revisionBuilder.toString());
		} else {
			revision = project.getDefaultBranch();
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