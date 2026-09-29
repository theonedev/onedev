package io.onedev.server.rest.resource.support;

import java.io.Serializable;

import io.onedev.server.model.support.Mark;
import io.onedev.server.rest.annotation.Api;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MarkData implements Serializable {

	private static final long serialVersionUID = 1L;

	private String commitHash;

	private String path;

	private PlanarRangeData range;

	@NotBlank
	@Api(description="Full commit hash")
	public String getCommitHash() {
		return commitHash;
	}

	public void setCommitHash(String commitHash) {
		this.commitHash = commitHash;
	}

	@NotBlank
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@NotNull
	@Valid
	public PlanarRangeData getRange() {
		return range;
	}

	public void setRange(PlanarRangeData range) {
		this.range = range;
	}

	public Mark toMark() {
		return new Mark(commitHash, path, range != null ? range.toPlanarRange() : null);
	}
}
