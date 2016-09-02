package com.pmease.commons.wicket.util;

import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.junit.Assert;
import org.junit.Test;

import com.pmease.commons.wicket.resourcebundle.PackageResourceBundler;
import com.pmease.commons.wicket.util.testdata.a.Js1ResourceReferenceA;
import com.pmease.commons.wicket.util.testdata.a.Js2ResourceReferenceA;
import com.pmease.commons.wicket.util.testdata.a.Js3ResourceReferenceA;
import com.pmease.commons.wicket.util.testdata.b.Js1ResourceReferenceB;
import com.pmease.commons.wicket.util.testdata.b.Js2ResourceReferenceB;
import com.pmease.commons.wicket.util.testdata.b.Js3ResourceReferenceB;
import com.pmease.commons.wicket.util.testdata.c.Js1ResourceReferenceC;
import com.pmease.commons.wicket.util.testdata.c.Js2ResourceReferenceC;
import com.pmease.commons.wicket.util.testdata.c.Js3ResourceReferenceC;
import com.pmease.commons.wicket.util.testdata.c.Js4ResourceReferenceC;

public class PackageResourceBundlerTest {

	@Test
	public void testa() {
		PackageResourceBundler bundler = new PackageResourceBundler(Js1ResourceReferenceA.class);
		Assert.assertEquals(1, bundler.getJavaScriptBundles().size());
		Assert.assertArrayEquals(
				new JavaScriptResourceReference[]{new Js1ResourceReferenceA(), new Js2ResourceReferenceA(), new Js3ResourceReferenceA()}, 
				bundler.getJavaScriptBundles().get(0).getReferences());
	}

	@Test
	public void testb() {
		PackageResourceBundler bundler = new PackageResourceBundler(Js1ResourceReferenceB.class);
		
		Assert.assertEquals(2, bundler.getJavaScriptBundles().size());
		Assert.assertArrayEquals(
				new JavaScriptResourceReference[]{new Js2ResourceReferenceB(), new Js3ResourceReferenceB()}, 
				bundler.getJavaScriptBundles().get(0).getReferences());
		Assert.assertArrayEquals(
				new JavaScriptResourceReference[]{new Js1ResourceReferenceB()}, 
				bundler.getJavaScriptBundles().get(1).getReferences());
	}
	
	@Test
	public void testc() {
		PackageResourceBundler bundler = new PackageResourceBundler(Js1ResourceReferenceC.class);

		Assert.assertEquals(2, bundler.getJavaScriptBundles().size());
		Assert.assertArrayEquals(
				new JavaScriptResourceReference[]{new Js3ResourceReferenceC(), new Js4ResourceReferenceC()}, 
				bundler.getJavaScriptBundles().get(0).getReferences());
		Assert.assertArrayEquals(
				new JavaScriptResourceReference[]{new Js1ResourceReferenceC(), new Js2ResourceReferenceC()}, 
				bundler.getJavaScriptBundles().get(1).getReferences());
		
		Assert.assertEquals(2, bundler.getCssBundles().size());
		Assert.assertArrayEquals(
				new CssResourceReference[]{new CssResourceReference(Js3ResourceReferenceC.class, "3.css"), new CssResourceReference(Js4ResourceReferenceC.class, "4.css")}, 
				bundler.getCssBundles().get(0).getReferences());
		Assert.assertArrayEquals(
				new CssResourceReference[]{new CssResourceReference(Js1ResourceReferenceC.class, "1.css"), new CssResourceReference(Js2ResourceReferenceC.class, "2.css")}, 
				bundler.getCssBundles().get(1).getReferences());
	}
	
}
