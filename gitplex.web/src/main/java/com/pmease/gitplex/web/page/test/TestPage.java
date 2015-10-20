package com.pmease.gitplex.web.page.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.link.Link;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.CommitLane;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.command.LogCommand;
import com.pmease.commons.git.command.LogCommand.Order;
import com.pmease.commons.wicket.assets.snapsvg.SnapSvgResourceReference;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				LogCommand log = new LogCommand(new File("w:\\temp\\linux\\.git")).order(Order.DATE).maxCount(10000);
				List<Commit> commits = log.call();
				
				System.out.println(commits.size());
				Map<String, Integer> mapOfHashToRow = new HashMap<>();
				for (int i=0; i<commits.size(); i++) {
					Commit commit = commits.get(i);
					mapOfHashToRow.put(commit.getHash(), i);
				}
				Map<Integer, List<Integer>> mapOfChildToParents = new HashMap<>();
				Map<Integer, List<Integer>> mapOfParentToChildren = new HashMap<>(); 
				for (int i=0; i<commits.size(); i++) {
					Commit commit = commits.get(i);
					List<Integer> parents = new ArrayList<>();
					mapOfChildToParents.put(i, parents);
					for (String parentHash: commit.getParentHashes()) {
						Integer parent = mapOfHashToRow.get(parentHash);
						if (parent != null) {
							parents.add(parent);
							List<Integer> children = mapOfParentToChildren.get(parent);
							if (children == null) {
								children = new ArrayList<>();
								mapOfParentToChildren.put(parent, children);
							}
							children.add(i);
						}
					}
				}
				
				try {
					System.out.println(new ObjectMapper().writeValueAsString(new CommitLane(commits, Integer.MAX_VALUE)).length());
					System.out.println(new ObjectMapper().writeValueAsString(mapOfChildToParents).length());
					System.out.println(new ObjectMapper().writeValueAsString(mapOfParentToChildren).length());
				} catch (JsonProcessingException e) {
				}
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(SnapSvgResourceReference.INSTANCE));
	}		

}
