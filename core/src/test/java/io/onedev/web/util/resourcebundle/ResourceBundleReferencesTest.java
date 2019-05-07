package io.onedev.web.util.resourcebundle;

import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.junit.Assert;
import org.junit.Test;

import io.onedev.server.web.resourcebundle.ResourceBundleReferences;
import io.onedev.web.util.resourcebundle.testdata.a.Js1ResourceReferenceA;
import io.onedev.web.util.resourcebundle.testdata.a.Js2ResourceReferenceA;
import io.onedev.web.util.resourcebundle.testdata.a.Js3ResourceReferenceA;
import io.onedev.web.util.resourcebundle.testdata.b.Js1ResourceReferenceB;
import io.onedev.web.util.resourcebundle.testdata.b.Js2ResourceReferenceB;
import io.onedev.web.util.resourcebundle.testdata.b.Js3ResourceReferenceB;
import io.onedev.web.util.resourcebundle.testdata.c.Js1ResourceReferenceC;
import io.onedev.web.util.resourcebundle.testdata.c.Js2ResourceReferenceC;
import io.onedev.web.util.resourcebundle.testdata.c.Js3ResourceReferenceC;
import io.onedev.web.util.resourcebundle.testdata.c.Js4ResourceReferenceC;

import com.google.common.collect.Lists;

public class ResourceBundleReferencesTest {

	@Test
	public void testa() {
		ResourceBundleReferences references = new ResourceBundleReferences(Js1ResourceReferenceA.class);
		Assert.assertEquals(1, references.getJavaScriptBundles().size());
		Assert.assertEquals(
				Lists.newArrayList(
						JavaScriptReferenceHeaderItem.forReference(new Js1ResourceReferenceA()), 
						JavaScriptReferenceHeaderItem.forReference(new Js2ResourceReferenceA()), 
						JavaScriptReferenceHeaderItem.forReference(new Js3ResourceReferenceA())), 
				references.getJavaScriptBundles().get(0).getProvidedResources());
	}

	@Test
	public void testb() {
		ResourceBundleReferences references = new ResourceBundleReferences(Js1ResourceReferenceB.class);
		
		Assert.assertEquals(2, references.getJavaScriptBundles().size());
		Assert.assertEquals(
				Lists.newArrayList(
						JavaScriptReferenceHeaderItem.forReference(new Js2ResourceReferenceB()), 
						JavaScriptReferenceHeaderItem.forReference(new Js3ResourceReferenceB())), 
				references.getJavaScriptBundles().get(0).getProvidedResources());
		Assert.assertEquals(
				Lists.newArrayList(JavaScriptReferenceHeaderItem.forReference(new Js1ResourceReferenceB())), 
				references.getJavaScriptBundles().get(1).getProvidedResources());
	}
	
	@Test
	public void testc() {
		ResourceBundleReferences references = new ResourceBundleReferences(Js1ResourceReferenceC.class);

		Assert.assertEquals(2, references.getJavaScriptBundles().size());
		Assert.assertEquals(
				Lists.newArrayList(
						JavaScriptReferenceHeaderItem.forReference(new Js3ResourceReferenceC()), 
						JavaScriptReferenceHeaderItem.forReference(new Js4ResourceReferenceC())), 
				references.getJavaScriptBundles().get(0).getProvidedResources());
		Assert.assertEquals(
				Lists.newArrayList(
						JavaScriptReferenceHeaderItem.forReference(new Js1ResourceReferenceC()), 
						JavaScriptReferenceHeaderItem.forReference(new Js2ResourceReferenceC())), 
				references.getJavaScriptBundles().get(1).getProvidedResources());
		
		Assert.assertEquals(2, references.getCssBundles().size());
		Assert.assertEquals(
				Lists.newArrayList(
						CssReferenceHeaderItem.forReference(new CssResourceReference(Js3ResourceReferenceC.class, "3.css")), 
						CssReferenceHeaderItem.forReference(new CssResourceReference(Js4ResourceReferenceC.class, "4.css"))), 
				references.getCssBundles().get(0).getProvidedResources());
		Assert.assertEquals(
				Lists.newArrayList(
						CssReferenceHeaderItem.forReference(new CssResourceReference(Js1ResourceReferenceC.class, "1.css")), 
						CssReferenceHeaderItem.forReference(new CssResourceReference(Js2ResourceReferenceC.class, "2.css"))), 
				references.getCssBundles().get(1).getProvidedResources());
	}
	
}
