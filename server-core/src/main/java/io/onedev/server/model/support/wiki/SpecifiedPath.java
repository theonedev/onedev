package io.onedev.server.model.support.wiki;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Path;

@Editable(name="Specified Path")
public class SpecifiedPath implements WikiFolder {

	private static final long serialVersionUID = 1L;

	private String path = "wiki";

	@Override
	@Editable
	@Path(Path.Type.RELATIVE)
	@NotEmpty
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
