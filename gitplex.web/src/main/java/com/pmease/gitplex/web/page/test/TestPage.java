package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.Link;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private int count = 0;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				long time = System.currentTimeMillis();
				MVStore s = MVStore.open("w:\\temp\\db5");

				// create/get the map named "data"
				MVMap<Long, String> map = s.openMap("data");

				for (int i=1000000; i<1001000; i++)
					map.get(i);
				
				// close the store (this will persist changes)
				s.close();
				System.out.println(System.currentTimeMillis()-time);
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

	}		

}
