package io.onedev.server.util.artifact;

import io.onedev.server.rest.ArtifactResource;
import io.onedev.server.rest.annotation.Api;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Api(exampleProvider = "getExample")
public abstract class ArtifactInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String path;

	private final long lastModified;
	
	public ArtifactInfo(@Nullable String path, long lastModified) {
		this.path = path;
		this.lastModified = lastModified;
	}
	
	private static ArtifactInfo getExample() {
		List<ArtifactInfo> children = new ArrayList<>();
		children.add(new DirectoryInfo("directory/sub-directory", 1, null));
		children.add(new FileInfo("directory/file", 1, 1000, null));
		return new DirectoryInfo("directory", 1, children);
	}
	
	@Nullable
	public String getPath() {
		return path;
	}
	
	public long getLastModified() {
		return lastModified;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof ArtifactInfo) {
			ArtifactInfo otherInfo = (ArtifactInfo) obj;
			return Objects.equals(path, otherInfo.path);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(path).toHashCode();
	}
	
}
