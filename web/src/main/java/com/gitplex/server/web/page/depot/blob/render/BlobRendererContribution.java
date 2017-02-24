package com.gitplex.server.web.page.depot.blob.render;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.gitplex.launcher.loader.ExtensionPoint;
import com.gitplex.server.web.PrioritizedComponentRenderer;

@ExtensionPoint
public interface BlobRendererContribution extends Serializable {

	@Nullable PrioritizedComponentRenderer getRenderer(BlobRenderContext context);
	
}
