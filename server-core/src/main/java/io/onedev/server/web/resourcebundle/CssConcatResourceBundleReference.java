package io.onedev.server.web.resourcebundle;

import java.io.IOException;
import java.util.List;

import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.resource.ITextResourceCompressor;
import org.apache.wicket.resource.bundles.ConcatBundleResource;
import org.apache.wicket.util.io.ByteArrayOutputStream;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

@SuppressWarnings("serial")
public class CssConcatResourceBundleReference 
		extends CachedDependenciesConcatResourceBundleReference<CssReferenceHeaderItem> {
	
	public CssConcatResourceBundleReference(Class<?> scope, String name, List<CssReferenceHeaderItem> resources) {
		super(scope, name, resources);
	}
	
	@Override
	public IResource getResource() {
		ConcatBundleResource bundleResource = new ConcatBundleResource(getProvidedResources()) {

			@Override
			protected byte[] readAllResources(List<IResourceStream> resources)
					throws IOException, ResourceStreamNotFoundException {
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				for (IResourceStream curStream : resources) {
					IOUtils.copy(curStream.getInputStream(), output);
					output.write("\n".getBytes());
				}

				byte[] bytes = output.toByteArray();

				if (getCompressor() != null) {
					String nonCompressed = new String(bytes, "UTF-8");
					bytes = getCompressor().compress(nonCompressed).getBytes("UTF-8");
				}

				return bytes;
			}
			
		};
		ITextResourceCompressor compressor = getCompressor();
		if (compressor != null) {
			bundleResource.setCompressor(compressor);
		}
		return bundleResource;
	}
	
}
