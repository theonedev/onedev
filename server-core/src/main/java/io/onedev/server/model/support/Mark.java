package io.onedev.server.model.support;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.util.diff.WhitespaceOption;

@Embeddable
public class Mark implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String PROP_COMMIT_HASH = "commitHash";
	
	public static final String PROP_PATH = "path";
	
	@Column(nullable=false)
	private String commitHash;

	@Column(nullable=false)
	private String path;
	
	@Column(nullable=false)
	private PlanarRange range;

	public String getCommitHash() {
		return commitHash;
	}

	public void setCommitHash(String commitHash) {
		this.commitHash = commitHash;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public PlanarRange getRange() {
		return range;
	}

	public void setRange(PlanarRange range) {
		this.range = range;
	}
	
	public Mark() {
	}
	
	public Mark(String commitHash, @Nullable String path, @Nullable PlanarRange range) {
		this.commitHash = commitHash;
		this.path = path;
		this.range = range;
	}
	
	@Override
	public String toString() {
		if (range != null) 
			return commitHash + ":" + path + ":" + range;
		else if (path != null)
			return commitHash + ":" + path;
		else
			return commitHash;
	}
	
	@Nullable
	public static Mark fromString(@Nullable String string) {
		if (string != null) {
			String commit = StringUtils.substringBefore(string, ":");
			String path = null;
			PlanarRange range = null;
			String pathAndRange = StringUtils.substringAfter(string, ":");
			if (pathAndRange.length() != 0) {
				path = StringUtils.substringBefore(pathAndRange, ":");
				String rangeString = StringUtils.substringAfter(pathAndRange, ":");
				if (rangeString.length() != 0)
					range = new PlanarRange(rangeString);
			}
			return new Mark(commit, path, range);
		} else {
			return null;
		}
	}
	
	@Nullable
	public Mark mapTo(Project project, ObjectId commitId) {
		List<String> newLines = new ArrayList<>();
		RevCommit commit = project.getRevCommit(commitId, true);
		try (TreeWalk treeWalk = TreeWalk.forPath(project.getRepository(), path, commit.getTree())) {
			if (treeWalk == null)
				return null;
			
			BlobIdent newBlobIdent = new BlobIdent(commitId.name(), path, treeWalk.getFileMode(0).getBits());
			Blob newBlob = new Blob(newBlobIdent, treeWalk.getObjectId(0), treeWalk.getObjectReader());
			if (newBlob.getText() == null)
				return null;
			for (String line: newBlob.getText().getLines())
				newLines.add(WhitespaceOption.DEFAULT.process(line));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		List<String> oldLines = new ArrayList<>();
		RevCommit markCommit = project.getRevCommit(ObjectId.fromString(commitHash), true);
		try (TreeWalk treeWalk = TreeWalk.forPath(project.getRepository(), path, markCommit.getTree())) {
			BlobIdent oldBlobIdent = new BlobIdent(commitHash, path, treeWalk.getFileMode(0).getBits());
			Blob oldBlob = new Blob(oldBlobIdent, treeWalk.getObjectId(0), treeWalk.getObjectReader());
			for (String line: oldBlob.getText().getLines())
				oldLines.add(WhitespaceOption.DEFAULT.process(line));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Map<Integer, Integer> lineMapping = DiffUtils.mapLines(oldLines, newLines);
		
		Integer newBeginLine = lineMapping.get(range.getFromRow());
		Integer newEndLine = lineMapping.get(range.getToRow());
		if (newBeginLine != null && newEndLine != null && newBeginLine <= newEndLine) {
			PlanarRange newRange = new PlanarRange(newBeginLine, range.getFromColumn(), 
					newEndLine, range.getToColumn());
			return new Mark(commitId.name(), path, newRange);
		} else {
			return null;
		}
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Mark))
			return false;
		if (this == other)
			return true;
		Mark otherPos = (Mark) other;
		return new EqualsBuilder()
				.append(commitHash, otherPos.commitHash)
				.append(path, otherPos.path)
				.append(range, otherPos.range)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(commitHash)
				.append(path)
				.append(range)
				.toHashCode();
	}
	
}
