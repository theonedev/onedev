package io.onedev.server.model.support;

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

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.OneDev;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.service.GitService;
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
			return commitHash + "~" + path + "~" + range;
		else if (path != null)
			return commitHash + "~" + path;
		else
			return commitHash;
	}
	
	@Nullable
	public static Mark fromString(@Nullable String string) {
		if (string != null) {
			String commit = StringUtils.substringBefore(string, "~");
			String path = null;
			PlanarRange range = null;
			String pathAndRange = StringUtils.substringAfter(string, "~");
			if (pathAndRange.length() != 0) {
				path = StringUtils.substringBeforeLast(pathAndRange, "~");
				String rangeString = StringUtils.substringAfterLast(pathAndRange, "~");
				if (rangeString.length() != 0)
					range = new PlanarRange(rangeString);
			}
			return new Mark(commit, path, range);
		} else {
			return null;
		}
	}
	
	public BlobIdent getBlobIdent() {
		return new BlobIdent(commitHash, path);
	}
	
	@Nullable
	public Mark mapTo(Project project, ObjectId commitId) {
		GitService gitService = OneDev.getInstance(GitService.class);
		
		List<String> newLines = new ArrayList<>();
		Blob newBlob = gitService.getBlob(project, commitId, path);
		if (newBlob == null || newBlob.getText() == null)
			return null;
		for (String line: newBlob.getText().getLines())
			newLines.add(WhitespaceOption.DEFAULT.apply(line));
		
		List<String> oldLines = new ArrayList<>();
		Blob oldBlob = gitService.getBlob(project, ObjectId.fromString(commitHash), path);
		for (String line: oldBlob.getText().getLines())
			oldLines.add(WhitespaceOption.DEFAULT.apply(line));

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
