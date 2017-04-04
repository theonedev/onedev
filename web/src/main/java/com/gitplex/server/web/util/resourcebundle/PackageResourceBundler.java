package com.gitplex.server.web.util.resourcebundle;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Application;
import org.apache.wicket.javascript.IJavaScriptCompressor;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IReferenceHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.IResourceReferenceFactory;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.ITextResourceCompressor;
import org.apache.wicket.resource.bundles.ConcatBundleResource;
import org.apache.wicket.resource.bundles.ConcatResourceBundleReference;
import org.apache.wicket.util.io.ByteArrayOutputStream;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

import com.gitplex.launcher.loader.LoaderUtils;
import com.gitplex.server.util.DependencyAware;
import com.gitplex.server.util.DependencyUtils;

public class PackageResourceBundler {

	private final Map<ResourceReference, DependencyAware<ResourceReference>> dependencyMap;
	
	private final Map<ResourceReference, Set<ResourceReference>> dependentMap;
	
	private final List<ResourceReference> sorted;
	
	private final List<BundleInfo<JavaScriptResourceReference>> javaScriptBundles = new ArrayList<>();
	
	private final List<BundleInfo<CssResourceReference>> cssBundles = new ArrayList<>();
	
	public PackageResourceBundler(Class<?> packageScope, Class<?>... additionalPackageScopes) {
		List<Class<?>> packageScopes = new ArrayList<>();
		packageScopes.add(packageScope);
		packageScopes.addAll(Arrays.asList(additionalPackageScopes));
		dependencyMap = buildDependencyMap(packageScopes);
		dependentMap = DependencyUtils.getDependentMap(dependencyMap);
		sorted = DependencyUtils.sortDependencies(dependencyMap);

		for (int i=sorted.size()-1; i>=0; i--) {
			ResourceReference resource = sorted.get(i);
			if (resource.getClass().isAnnotationPresent(ResourceBundle.class)) {
				/* 
				 * A resource marked with ResourceBundle annotation will be a separate bundle, together 
				 * with all other resources depending on it, and any dependencies used only by these 
				 * resources
				 */
				Set<ResourceReference> resources = DependencyUtils.getTransitiveDependents(dependentMap, resource);
				resources.add(resource);
				resources = includeSoleDependencies(resources);

				String name = StringUtils.stripStart(resource.getName(), "/");
				String cssExt = ".css";
				if (name.endsWith(cssExt))
					name = name.substring(0, name.length()-cssExt.length());
				String jsExt = ".js";
				if (name.endsWith(jsExt))
					name = name.substring(0, name.length()-jsExt.length());

				createBundles(resource.getScope(), resources);
				
				dependencyMap.keySet().removeAll(resources);
				for (DependencyAware<ResourceReference> each: dependencyMap.values())
					each.getDependencies().removeAll(resources);
				dependentMap.keySet().removeAll(resources);
				for (Set<ResourceReference> each: dependentMap.values())
					each.removeAll(resources);
			}
		}
		
		createBundles(Application.class, dependencyMap.keySet());
	}
	
	private void createBundles(Class<?> scope, Set<ResourceReference> resources) {
		/*
		 * Some bundled css file may contain resources relative to parent paths of the css url, 
		 * for instance, a css may define below style:
		 * 
		 * background: url(../images/clock.png)),
		 *  
		 * if we use a resource name for instance "bundle" here, the generated resource 
		 * path will be something like "http://<server>:<port>/wicket/resource/org.apache.wicket.Application/bundle-ver-1472816165384.css", 
		 * and browser will resolve image url above as "http://<server>:<port>/wicket/resource/images/clock.png", which will cause Wicket
		 * resource loading not working at all. However if we use a long path here for resource name, for instance 
		 * "a/l/o/n/g/p/a/t/h/bundle", the resolved image url will be "http://<server>:<port>/wicket/resource/org.apache.wicket.Application/a/l/o/n/g/p/a/t/clock.png",  
		 * which will be resolved to the correct image with help of our BundleAwareResourceReferenceFactory
		 */
		String name = "a/l/o/n/g/p/a/t/h/bundle";
		
		List<ResourceReference> resourceList = new ArrayList<>(resources);
		resourceList.sort((o1, o2)->sorted.indexOf(o1)-sorted.indexOf(o2));
		
		List<JavaScriptResourceReference> jsResources = new ArrayList<>();
		List<CssResourceReference> cssResources = new ArrayList<>();
		for (ResourceReference resource: resourceList) {
			if (resource instanceof JavaScriptResourceReference) {
				jsResources.add((JavaScriptResourceReference) resource);
			} else if (resource instanceof CssResourceReference) {
				cssResources.add((CssResourceReference) resource);
			}
		}
		
		if (!jsResources.isEmpty()) {
			javaScriptBundles.add(new BundleInfo<JavaScriptResourceReference>(scope, name + ".js",
					jsResources.toArray(new JavaScriptResourceReference[jsResources.size()])));
		} 
		
		if (!cssResources.isEmpty()) {
			cssBundles.add(new BundleInfo<CssResourceReference>(scope, name + ".css",
					cssResources.toArray(new CssResourceReference[cssResources.size()])));
		} 
		
	}

	private Set<ResourceReference> includeSoleDependencies(Set<ResourceReference> dependents) {
		while (true) {
			Set<ResourceReference> newDependents = new HashSet<>(dependents);
			for (ResourceReference dependent: dependents) {
				for (ResourceReference dependency: dependencyMap.get(dependent).getDependencies()) {
					if (!dependency.getClass().isAnnotationPresent(ResourceBundle.class) 
							&& dependents.containsAll(dependentMap.get(dependency))) {
						newDependents.add(dependency);
					}
				}
			}
			if (!newDependents.equals(dependents)) {
				dependents = newDependents;
			} else {
				break;
			}
		}
		return dependents;
	}
	
	private Map<ResourceReference, DependencyAware<ResourceReference>> buildDependencyMap(List<Class<?>> packageScopes) {
		Map<ResourceReference, DependencyAware<ResourceReference>> dependencyMap = new LinkedHashMap<>();
		Collection<Class<? extends ResourceReference>> resourceClasses = new ArrayList<>();
		for (Class<?> packageScope: packageScopes) {
			for (Class<? extends ResourceReference> resourceClass: LoaderUtils.findImplementations(ResourceReference.class, packageScope)) {
				if (JavaScriptResourceReference.class.isAssignableFrom(resourceClass) 
						|| CssResourceReference.class.isAssignableFrom(resourceClass)) {
					resourceClasses.add(resourceClass);
				}
			}
		}
		
		for (Class<? extends ResourceReference> resourceClass: resourceClasses) {
			boolean hasDefaultCtor = false;
			for (Constructor<?> ctor: resourceClass.getConstructors()) {
				if (ctor.getParameterCount() == 0) {
					hasDefaultCtor = true;
					break;
				}
			}
			if (hasDefaultCtor) {
				ResourceReference resource;
				try {
					resource = resourceClass.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				if (resource.canBeRegistered()) {
					dependencyMap.put(resource, getDependencyAware(resource));
				}
			}
		}
		
		while (true) {
			Map<ResourceReference, DependencyAware<ResourceReference>> newDependencyMap = new LinkedHashMap<>(dependencyMap);
			for (DependencyAware<ResourceReference> dependent: dependencyMap.values()) {
				for (ResourceReference dependency: dependent.getDependencies()) {
					if (dependency.canBeRegistered()) {
						newDependencyMap.put(dependency, getDependencyAware(dependency));
					}
				}
			}
			if (!dependencyMap.equals(newDependencyMap)) {
				dependencyMap = newDependencyMap;
			} else {
				break;
			}
		}
		
		return dependencyMap;
	}
	
	private DependencyAware<ResourceReference> getDependencyAware(ResourceReference resource) {
		return new DependencyAware<ResourceReference>() {

			@Override
			public ResourceReference getId() {
				return resource;
			}

			@Override
			public int hashCode() {
				return getId().hashCode();
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean equals(Object obj) {
				return getId().equals(((DependencyAware<ResourceReference>)obj).getId());
			}

			@Override
			public Set<ResourceReference> getDependencies() {
				Set<ResourceReference> dependencies = new LinkedHashSet<>();
				for (HeaderItem item: resource.getDependencies()) {
					if (item instanceof IReferenceHeaderItem) {
						ResourceReference reference = ((IReferenceHeaderItem) item).getReference();
						if (reference.canBeRegistered()) {
							dependencies.add(reference);
						}
					}
				}
				return dependencies;
			}
			
		};
	}
	
	public List<BundleInfo<JavaScriptResourceReference>> getJavaScriptBundles() {
		return javaScriptBundles;
	}

	public List<BundleInfo<CssResourceReference>> getCssBundles() {
		return cssBundles;
	}

	public void install(Application application) {
		for (BundleInfo<JavaScriptResourceReference> bundle: javaScriptBundles) {
			List<JavaScriptReferenceHeaderItem> items = new ArrayList<JavaScriptReferenceHeaderItem>();
			for (JavaScriptResourceReference curReference : bundle.getReferences()) {
				items.add(JavaScriptHeaderItem.forReference(curReference));
			}
			
			@SuppressWarnings("serial")
			ConcatResourceBundleReference<JavaScriptReferenceHeaderItem> bundleReference = 
					new ConcatResourceBundleReference<JavaScriptReferenceHeaderItem>(bundle.getScope(), bundle.getName(), items) {

				@Override
				public IResource getResource() {
					ConcatBundleResource bundleResource = new ConcatBundleResource(items) {

						@Override
						protected byte[] readAllResources(List<IResourceStream> resources)
								throws IOException, ResourceStreamNotFoundException {
							ByteArrayOutputStream output = new ByteArrayOutputStream();
							for (IResourceStream curStream : resources) {
								IOUtils.copy(curStream.getInputStream(), output);
								output.write(";".getBytes());
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
				
			};
			IJavaScriptCompressor javaScriptCompressor = application.getResourceSettings().getJavaScriptCompressor();
			bundleReference.setCompressor(javaScriptCompressor);
			application.getResourceBundles().addBundle(JavaScriptHeaderItem.forReference(bundleReference));
		}
		for (BundleInfo<CssResourceReference> bundle: cssBundles) {
			application.getResourceBundles().addCssBundle(bundle.getScope(), bundle.getName(), bundle.getReferences());
		}
		
		IResourceReferenceFactory factory = new BundleAwareResourceReferenceFactory(javaScriptBundles, cssBundles);
		application.getResourceReferenceRegistry().setResourceReferenceFactory(factory);
	}
	
}
