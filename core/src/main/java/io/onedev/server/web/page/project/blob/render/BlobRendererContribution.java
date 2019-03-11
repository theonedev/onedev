package io.onedev.server.web.page.project.blob.render;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.web.PrioritizedComponentRenderer;

@ExtensionPoint
public interface BlobRendererContribution extends Serializable {

	@Nullable PrioritizedComponentRenderer getRenderer(BlobRenderContext context);
	
}
