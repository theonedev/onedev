package io.onedev.server.git.location;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;

@Editable(name="Use Specified curl", order=200)
public class SpecifiedCurl extends CurlLocation {

	private static final long serialVersionUID = 1L;
	
	private String curlPath;
	
	@Editable(name="curl Path", description="Specify path to curl executable, for instance: <tt>/usr/bin/curl</tt>")
	@OmitName
	@NotEmpty
	public String getCurlPath() {
		return curlPath;
	}

	public void setCurlPath(String curlPath) {
		this.curlPath = curlPath;
	}

	@Override
	public String getExecutable() {
		return curlPath;
	}

}
