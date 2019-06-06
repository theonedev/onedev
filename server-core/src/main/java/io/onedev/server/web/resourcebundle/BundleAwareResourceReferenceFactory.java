package io.onedev.server.web.resourcebundle;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IReferenceHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.request.resource.IResourceReferenceFactory;
import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.ResourceReference.Key;
import org.apache.wicket.resource.bundles.ConcatResourceBundleReference;

import io.onedev.commons.utils.PathUtils;

/*
 * When css is bundled, and its access url can be changed (unless its resource is annotated with @ResourceBundle), 
 * and this can cause the issue that relative resources referenced in the css via url property (for instance background
 * image etc.) can not be loaded correctly. To solve this issue, this factory class tries to locate the relative 
 * resource against each bundled resource if it can not be found with its original resource key 
 */
class BundleAwareResourceReferenceFactory implements IResourceReferenceFactory {

	private final List<ConcatResourceBundleReference<? extends IReferenceHeaderItem>> bundles = new ArrayList<>();
	
	public BundleAwareResourceReferenceFactory(List<ConcatResourceBundleReference<JavaScriptReferenceHeaderItem>> javaScriptBundles, 
			List<ConcatResourceBundleReference<CssReferenceHeaderItem>> cssBundles) {
		bundles.addAll(javaScriptBundles);
		bundles.addAll(cssBundles);
	}
	
	@Override
	public ResourceReference create(Key key) {
		if (PackageResource.exists(key)) {
			return new PackageResourceReference(key);
		} else {
			for (ConcatResourceBundleReference<? extends IReferenceHeaderItem> bundle: bundles) {
				if (bundle.getScope().getName().equals(key.getScope())) {
					String bundleParentPath = getParentPath(bundle.getName());
					String relativePath;
					if (bundleParentPath != null)
						relativePath = PathUtils.relativize(bundleParentPath, key.getName());
					else
						relativePath = key.getName();
					for (IReferenceHeaderItem headerItem: bundle.getProvidedResources()) {
						String referenceParentPath = getParentPath(headerItem.getReference().getName());
						String possibleName;
						if (referenceParentPath != null)
							possibleName = PathUtils.resolve(referenceParentPath, relativePath);
						else
							possibleName = relativePath.toString();
						possibleName = PathUtils.normalizeDots(possibleName);
						if (possibleName != null) {
							Key possibleKey = new Key(headerItem.getReference().getScope().getName(), possibleName, 
									key.getLocale(), key.getStyle(), key.getVariation());
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
	private String getParentPath(String pathName) {
		String parentPath = pathName;
		int lastIndex = parentPath.lastIndexOf('/');
		if (lastIndex != -1)
			return parentPath.substring(0, lastIndex);
		else
			return null;
	}
	
}
