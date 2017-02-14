package com.gitplex.commons.wicket.resourcebundle;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.IResourceReferenceFactory;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.ResourceReference.Key;

/*
 * When css is bundled, and its access url can be changed (unless its resource is annotated with @ResourceBundle), 
 * and this can cause the issue that relative resources referenced in the css via url property (for instance background
 * image etc.) can not be loaded correctly. To solve this issue, this factory class tries to locate the relative 
 * resource against each bundled resource if it can not be found with its original resource key 
 */
class BundleAwareResourceReferenceFactory implements IResourceReferenceFactory {

	private final List<BundleInfo<? extends ResourceReference>> bundles = new ArrayList<>();
	
	public BundleAwareResourceReferenceFactory(List<BundleInfo<JavaScriptResourceReference>> javaScriptBundles, 
			List<BundleInfo<CssResourceReference>> cssBundles) {
		bundles.addAll(javaScriptBundles);
		bundles.addAll(cssBundles);
	}
	
	@Override
	public ResourceReference create(Key key) {
		if (PackageResource.exists(key)) {
			return new PackageResourceReference(key);
		} else {
			Path keyPath = Paths.get(key.getName());
			for (BundleInfo<? extends ResourceReference> bundle: bundles) {
				if (bundle.getScope().getName().equals(key.getScope())) {
					Path bundleParentPath = getParentPath(bundle.getName());
					Path relativePath;
					if (bundleParentPath != null)
						relativePath = bundleParentPath.relativize(keyPath);
					else
						relativePath = keyPath;
					for (ResourceReference reference: bundle.getReferences()) {
						Path referenceParentPath = getParentPath(reference.getName());
						String possibleName;
						if (referenceParentPath != null)
							possibleName = referenceParentPath.resolve(relativePath).toString();
						else
							possibleName = relativePath.toString();
						possibleName = FilenameUtils.normalize(possibleName);
						if (possibleName != null) {
							possibleName = possibleName.replace('\\', '/');
							Key possibleKey = new Key(reference.getScope().getName(), possibleName, key.getLocale(), key.getStyle(), key.getVariation());
							if (PackageResource.exists(possibleKey)) {
								return new PackageResourceReference(possibleKey);
							}
						}
					}
				}
			}
			return null;
		}
	}

	@Nullable
	private Path getParentPath(String pathName) {
		String parentPath = pathName;
		int lastIndex = parentPath.lastIndexOf('/');
		if (lastIndex != -1)
			return Paths.get(parentPath.substring(0, lastIndex));
		else
			return null;
	}
}
