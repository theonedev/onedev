package com.pmease.commons.product.web;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;

@SuppressWarnings("serial")
public class HomePage extends WebPage {
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new BookmarkablePageLink<Void>("bookmarkablePageLink", TestPage.class));
		add(new Link<Void>("actionLink") {

			@Override
			public void onClick() {
				dirty();
			}
			
		});
		add(new Link<Void>("actionLinkToPageViaClass") {

			@Override
			public void onClick() {
				dirty();
				setResponsePage(TestPage.class);
			}
			
		});
		add(new Link<Void>("actionLinkToPageViaNew") {

			@Override
			public void onClick() {
				dirty();
				setResponsePage(new TestPage());
			}
			
		});
		add(new Link<Void>("actionLinkToPageViaThis") {

			@Override
			public void onClick() {
				dirty();
				setResponsePage(HomePage.this);
			}
			
		});
		add(new AjaxLink<Void>("ajaxLink") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				dirty();
			}
			
		});
		add(new AjaxLink<Void>("ajaxLinkToPageViaClass") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				dirty();
				setResponsePage(TestPage.class);
			}
			
		});
		add(new AjaxLink<Void>("ajaxLinkToPageViaNew") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				dirty();
				setResponsePage(new TestPage());
			}
			
		});
		add(new AjaxLink<Void>("ajaxLinkToPageViaThis") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				dirty();
				setResponsePage(HomePage.this);
			}
			
		});
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
    	oos.defaultWriteObject();
    	System.out.println("HomePage.writeObject: " + getPageId() + ":" + getRenderCount());
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
		System.out.println("HomePage.readObject: " + getPageId() + ":" + getRenderCount());
	}

}