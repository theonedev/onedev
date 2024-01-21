package io.onedev.server.rest.resource.support;

import io.onedev.server.rest.annotation.Api;

public class FileCreateOrUpdateRequest extends FileEditRequest {

	private static final long serialVersionUID = 1L;

	@Api(description="Base64 encoding of created/updated file content")
	private String base64Content;

	public String getBase64Content() {
		return base64Content;
	}

	public void setBase64Content(String base64Content) {
		this.base64Content = base64Content;
	}

}