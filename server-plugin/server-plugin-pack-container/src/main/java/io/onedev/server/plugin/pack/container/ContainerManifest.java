package io.onedev.server.plugin.pack.container;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.onedev.server.OneDev;

import org.jspecify.annotations.Nullable;
import java.io.IOException;

public class ContainerManifest {
	
	private final JsonNode json;
	
	public ContainerManifest(byte[] manifestBytes) {
		try {
			json = OneDev.getInstance(ObjectMapper.class).readTree(manifestBytes);
			var schemaVersionNode = json.get("schemaVersion");
			if (schemaVersionNode == null || !schemaVersionNode.asText().equals("2"))
				throw new BadRequestException("Invalid manifest schema version");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Nullable
	public String getMediaType() {
		var mediaTypeNode = json.get("mediaType");
		if (mediaTypeNode != null)
			return mediaTypeNode.asText();
		else if (json.get("config") != null)
			return "application/vnd.oci.image.manifest.v1+json";
		else if (json.get("manifests") != null)
			return "application/vnd.oci.image.index.v1+json";
		else 
			return null;
	}
	
	public boolean isImageManifest() {
		return isImageManifest(getMediaType());
	}

	public boolean isImageIndex() {
		return isImageIndex(getMediaType());
	}

	public static boolean isImageManifest(@Nullable String mediaType) {
		return "application/vnd.oci.image.manifest.v1+json".equals(mediaType)
				|| "application/vnd.docker.distribution.manifest.v2+json".equals(mediaType);
	}

	public static boolean isImageIndex(@Nullable String mediaType) {
		return "application/vnd.oci.image.index.v1+json".equals(mediaType)
				|| "application/vnd.docker.distribution.manifest.list.v2+json".equals(mediaType);
	}
	
	public JsonNode getJson() {
		return json;
	}
	
}
