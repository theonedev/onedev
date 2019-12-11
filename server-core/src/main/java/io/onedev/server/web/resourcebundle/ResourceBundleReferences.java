package io.onedev.server.web.resourcebundle;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Application;
import org.apache.wicket.css.ICssCompressor;
import org.apache.wicket.javascript.IJavaScriptCompressor;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IReferenceHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.IResourceReferenceFactory;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.bundles.ConcatResourceBundleReference;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.commons.utils.DependencyAware;
import io.onedev.commons.utils.DependencyUtils;

public class ResourceBundleReferences {

	private final Map<ResourceReference, DependencyAware<ResourceReference>> dependencyMap;
	
	private final Map<ResourceReference, Set<ResourceReference>> dependentMap;
	
	private final List<ResourceReference> sorted;
	
	private final List<ConcatResourceBundleReference<JavaScriptReferenceHeaderItem>> javaScriptBundleReferences = new ArrayList<>();
	
	private final List<ConcatResourceBundleReference<CssReferenceHeaderItem>> cssBundleReferences = new ArrayList<>();
	
	public ResourceBundleReferences(Class<?>... packageScopes) {
		dependencyMap = buildDependencyMap(packageScopes);
		for (Map.Entry<ResourceReference, DependencyAware<ResourceReference>> entry: dependencyMap.entrySet()) {
			if (entry.getKey().getName().endsWith(".css")) {
				for (ResourceReference dependency: entry.getValue().getDependencies()) {
					if (dependency.getName().endsWith(".js"))
						throw new RuntimeException("Css resource '" + entry.getKey() + "' should not depend on javascript resource '" + dependency.getKey() + "' as it can cause circular dependency between js bundle and css bundle");
				}
			}
		}
		dependentMap = DependencyUtils.getDependentMap(dependencyMap);
		sorted = DependencyUtils.sortDependencies(dependencyMap);

		for (int i=sorted.size()-1; i>=0; i--) {
			ResourceReference resourceReference = sorted.get(i);
			DependencyAware<ResourceReference> dependencyAware = dependencyMap.get(resourceReference);
			if (dependencyAware != null) {
				if (dependencyAware.getDependencies().size() != resourceReference.getDependencies().size() 
						|| resourceReference.getClass().isAnnotationPresent(ResourceBundle.class)) {
					/* 
					 * Create separate resource bundle in two cases:
					 * 
					 * 1. A resource is marked with ResourceBundle annotation explicitly
					 * 2. Some of the resource's dependencies are not being included in dependencyMap. For instance 
					 * dependency of type OnDomReadyHeaderItem is not included, and we should create separate bundle to 
					 * avoid possible circular bundle dependencies   
					 *  
					 * Besides current resource, A bundle also includes all resources depending on it, and any 
					 * dependencies used only by these resources
					 */
					Set<ResourceReference> resourceReferences = DependencyUtils.getTransitiveDependents(dependentMap, resourceReference);
					resourceReferences.add(resourceReference);
					resourceReferences = includeSoleDependencies(resourceReferences);

					createBundles(resourceReference.getScope(), resourceReferences);
					
					dependencyMap.keySet().removeAll(resourceReferences);
					for (DependencyAware<ResourceReference> each: dependencyMap.values())
						each.getDependencies().removeAll(resourceReferences);
					dependentMap.keySet().removeAll(resourceReferences);
					for (Set<ResourceReference> each: dependentMap.values())
						each.removeAll(resourceReferences);
				}
			}
		}
		
		createBundles(Application.class, dependencyMap.keySet());
	}
	
	private void createBundles(Class<?> scope, Set<ResourceReference> resourceReferences) {
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
		
		List<ResourceReference> resourceReferenceList = new ArrayList<>(resourceReferences);
		resourceReferenceList.sort((o1, o2)->sorted.indexOf(o1)-sorted.indexOf(o2));
		
		List<JavaScriptReferenceHeaderItem> javaScriptResourceReferences = new ArrayList<>();
		List<CssReferenceHeaderItem> cssResourceReferences = new ArrayList<>();
		for (ResourceReference resourceReference: resourceReferenceList) {
			if (resourceReference instanceof JavaScriptResourceReference) {
				javaScriptResourceReferences.add(JavaScriptReferenceHeaderItem.forReference(resourceReference));
			} else if (resourceReference instanceof CssResourceReference) {
				cssResourceReferences.add(CssReferenceHeaderItem.forReference(resourceReference));
			}
		}
		
		if (!javaScriptResourceReferences.isEmpty()) {
			javaScriptBundleReferences.add(new JavaScriptConcatResourceBundleReference(
					scope, name + ".js", javaScriptResourceReferences));			
		}
		
		if (!cssResourceReferences.isEmpty()) {
			cssBundleReferences.add(new CssConcatResourceBundleReference(
					scope, name + ".css", cssResourceReferences));			
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
	
	private Map<ResourceReference, DependencyAware<ResourceReference>> buildDependencyMap(Class<?> packageScopes[]) {
		Map<ResourceReference, DependencyAware<ResourceReference>> dependencyMap = new LinkedHashMap<>();
		Collection<Class<? extends ResourceReference>> resourceClasses = new ArrayList<>();
		for (Class<?> packageScope: packageScopes) {
			for (Class<? extends ResourceReference> resourceClass: ClassUtils.findImplementations(ResourceReference.class, packageScope)) {
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
	
	public List<ConcatResourceBundleReference<JavaScriptReferenceHeaderItem>> getJavaScriptBundles() {
		return javaScriptBundleReferences;
	}

	public List<ConcatResourceBundleReference<CssReferenceHeaderItem>> getCssBundles() {
		return cssBundleReferences;
	}

	public void installInto(Application application) {
		for (ConcatResourceBundleReference<JavaScriptReferenceHeaderItem> bundleReference: javaScriptBundleReferences) {
			IJavaScriptCompressor javaScriptCompressor = application.getResourceSettings().getJavaScriptCompressor();
			bundleReference.setCompressor(javaScriptCompressor);
			application.getResourceBundles().addBundle(JavaScriptHeaderItem.forReference(bundleReference));
		}
		for (ConcatResourceBundleReference<CssReferenceHeaderItem> bundleReference: cssBundleReferences) {
			ICssCompressor cssCompressor = application.getResourceSettings().getCssCompressor();
			bundleReference.setCompressor(cssCompressor);
			application.getResourceBundles().addBundle(CssHeaderItem.forReference(bundleReference));
		}
		
		IResourceReferenceFactory factory = new BundleAwareResourceReferenceFactory(javaScriptBundleReferences, cssBundleReferences);
		application.getResourceReferenceRegistry().setResourceReferenceFactory(factory);
	}
	
}
