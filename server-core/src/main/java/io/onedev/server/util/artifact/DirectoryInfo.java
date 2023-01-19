package io.onedev.server.util.artifact;

import javax.annotation.Nullable;
import java.util.List;

public class DirectoryInfo extends ArtifactInfo {

	private static final long serialVersionUID = 1L;

	private final List<ArtifactInfo> children;
	
	public DirectoryInfo(@Nullable String path, long lastModified, @Nullable List<ArtifactInfo> children) {
		super(path, lastModified);
		this.children = children;
	}

	@Nullable
	public List<ArtifactInfo> getChildren() {
		return children;
	}
}
