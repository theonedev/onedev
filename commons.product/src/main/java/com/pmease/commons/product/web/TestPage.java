package com.pmease.commons.product.web;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.wicket.markup.html.WebPage;

@SuppressWarnings("serial")
public class TestPage extends WebPage {
	
	public TestPage() {
		System.out.println("");
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setStatelessHint(false);
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
    	oos.defaultWriteObject();
    	System.out.println("TestPage.writeObject: " + getPageId() + ":" + getRenderCount());
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
		System.out.println("TestPage.readObject: " + getPageId() + ":" + getRenderCount());
	}

}