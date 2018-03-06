package io.onedev.server.model.support;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.OptimisticLock;

@Embeddable
public class MarkPos implements Serializable {

	private static final long serialVersionUID = 1L;

	@OptimisticLock(excluded=true)
	@Column(nullable=false)
	private String commit;

	@OptimisticLock(excluded=true)
	@Column(nullable=false)
	private String path;
	
	@OptimisticLock(excluded=true)
	@Column(nullable=false)
	private TextRange range;

	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public TextRange getRange() {
		return range;
	}

	public void setRange(TextRange range) {
		this.range = range;
	}

	public MarkPos() {
	}
	
	public MarkPos(String commit, @Nullable String path, @Nullable TextRange mark) {
		this.commit = commit;
		this.path = path;
		this.range = mark;
	}
	
	@Override
	public String toString() {
		if (range != null) 
			return commit + ":" + path + ":" + range;
		else if (path != null)
			return commit + ":" + path;
		else
			return commit;
	}
	
	public static MarkPos fromString(@Nullable String str) {
		if (str != null) {
			String commit = StringUtils.substringBefore(str, ":");
			String path = null;
			TextRange mark = null;
			String pathAndMark = StringUtils.substringAfter(str, ":");
			if (pathAndMark.length() != 0) {
				path = StringUtils.substringBefore(pathAndMark, ":");
				String markStr = StringUtils.substringAfter(pathAndMark, ":");
				if (markStr.length() != 0)
					mark = new TextRange(markStr);
			}
			return new MarkPos(commit, path, mark);
		} else {
			return null;
		}
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MarkPos))
			return false;
		if (this == other)
			return true;
		MarkPos otherPos = (MarkPos) other;
		return new EqualsBuilder()
				.append(commit, otherPos.commit)
				.append(path, otherPos.path)
				.append(range, otherPos.range)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(commit)
				.append(path)
				.append(range)
				.toHashCode();
	}
	
}
