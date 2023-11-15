package io.onedev.server.plugin.pack.container;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.onedev.server.OneDev;

import java.io.IOException;

public class ContainerData {
	
	private final JsonNode manifest;

	private final String contentType;
	
	public ContainerData(byte[] manifestBytes, String contentType) {
		try {
			manifest = OneDev.getInstance(ObjectMapper.class).readTree(manifestBytes);
			var schemaVersionNode = manifest.get("schemaVersion");
			if (schemaVersionNode == null || !schemaVersionNode.asText().equals("2"))
				throw new BadRequestException("Invalid manifest schema version");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.contentType = contentType;
	}
	
	public boolean isImageManifest() {
		return contentType.equals("application/vnd.oci.image.manifest.v1+json") 
				|| contentType.equals("application/vnd.docker.distribution.manifest.v2+json");
	}

	public boolean isImageIndex() {
		return contentType.equals("application/vnd.oci.image.index.v1+json")
				|| contentType.equals("application/vnd.docker.distribution.manifest.list.v2+json");
	}
	
	public JsonNode getManifest() {
		return manifest;
	}
	
}
