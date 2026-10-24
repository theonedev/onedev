package io.onedev.server.plugin.report.unittest;

import java.io.Serial;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class TestArtifactResourceReference extends ResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public TestArtifactResourceReference() {
		super("test-artifact");
	}

	@Override
	public IResource getResource() {
		return new TestArtifactResource();
	}

}
