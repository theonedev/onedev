package com.turbodev.server.web.page.project.blob.render;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.turbodev.launcher.loader.ExtensionPoint;
import com.turbodev.server.web.PrioritizedComponentRenderer;

@ExtensionPoint
public interface BlobRendererContribution extends Serializable {

	@Nullable PrioritizedComponentRenderer getRenderer(BlobRenderContext context);
	
}
