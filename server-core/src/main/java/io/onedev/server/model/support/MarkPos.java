package io.onedev.server.model.support;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.OptimisticLock;

import io.onedev.commons.utils.PlanarRange;

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
	private PlanarRange range;

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

	public PlanarRange getRange() {
		return range;
	}

	public void setRange(PlanarRange range) {
		this.range = range;
	}
	
	public MarkPos() {
	}
	
	public MarkPos(String commit, @Nullable String path, @Nullable PlanarRange range) {
		this.commit = commit;
		this.path = path;
		this.range = range;
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
	
	public static MarkPos fromString(@Nullable String string) {
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
			return new MarkPos(commit, path, range);
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
