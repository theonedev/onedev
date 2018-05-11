package io.onedev.server.web.page.test;

import java.util.List;

import org.apache.wicket.markup.html.link.Link;

import com.google.common.collect.Lists;

import io.onedev.server.web.component.diff.plain.PlainDiffPanel;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
			}
			
		});
		
		List<String> oldLines = Lists.newArrayList(
				"1", 
				"2", 
				"3", 
				"4", 
				"5", 
				"6", 
				"7", 
				"8 81 82 83 84 85",
				"9");
		List<String> newLines = Lists.newArrayList(
				"1", 
				"2", 
				"3", 
				"4", 
				"5", 
				"6", 
				"7", 
				"80 81 82 83 84 85",
				"9");
		add(new PlainDiffPanel("diffs", oldLines, newLines));
	}

}
