package io.onedev.server.plugin.pack.container;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.onedev.server.OneDev;

import javax.annotation.Nullable;
import java.io.IOException;

public class ContainerData {
	
	private final JsonNode manifest;

	public ContainerData(byte[] manifestBytes) {
		try {
			manifest = OneDev.getInstance(ObjectMapper.class).readTree(manifestBytes);
			var schemaVersionNode = manifest.get("schemaVersion");
			if (schemaVersionNode == null || !schemaVersionNode.asText().equals("2"))
				throw new BadRequestException("Invalid manifest schema version");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Nullable
	public String getMediaType() {
		var mediaTypeNode = getManifest().get("mediaType");
		return mediaTypeNode != null? mediaTypeNode.asText(): null;
	}
	
	public boolean isImageManifest() {
		var mediaType = getMediaType();
		return "application/vnd.oci.image.manifest.v1+json".equals(mediaType) 
				|| "application/vnd.docker.distribution.manifest.v2+json".equals(mediaType);
	}

	public boolean isImageIndex() {
		var mediaType = getMediaType();
		return "application/vnd.oci.image.index.v1+json".equals(mediaType)
				|| "application/vnd.docker.distribution.manifest.list.v2+json".equals(mediaType);
	}
	
	public JsonNode getManifest() {
		return manifest;
	}
	
}
