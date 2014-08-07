package com.pmease.gitplex.web.extensionpoint;

import javax.annotation.Nullable;

import org.apache.tika.mime.MediaType;

import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface DiffRendererProvider {
	@Nullable DiffRenderer getDiffRenderer(MediaType originalMediaType, MediaType revisedMediaType);
}
