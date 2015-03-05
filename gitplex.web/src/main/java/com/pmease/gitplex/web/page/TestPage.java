package com.pmease.gitplex.web.page;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.pmease.commons.lang.Tokenizers;
import com.pmease.commons.wicket.CommonPage;
import com.pmease.commons.wicket.assets.CodeMirrorResourceReference;
import com.pmease.gitplex.core.GitPlex;

@SuppressWarnings("serial")
public class TestPage extends CommonPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<String> lines;
		try {
			lines = FileUtils.readLines(new File("w:\\temp\\cgroup.c.bak"));
			System.out.println(GitPlex.getInstance(Tokenizers.class).tokenize(lines, "c").size());
 			
			/*
			add(new ListView<TokenizedLine>("lines", tokenizedLines) {

				@Override
				protected void populateItem(ListItem<TokenizedLine> item) {
					item.add(new Label("line", item.getModelObject().toHtml(4)).setEscapeModelStrings(false));
				}
				
			});
			*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(CodeMirrorResourceReference.INSTANCE));
	}

}
