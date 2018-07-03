package io.onedev.server.model.support.pullrequest.actiondata;

import java.util.List;

import com.google.common.collect.Lists;

import io.onedev.server.model.support.DiffSupport;

public class ChangedTitleData extends ActionData {

	private static final long serialVersionUID = 1L;

	private final String oldTitle;
	
	private final String newTitle;
	
	public ChangedTitleData(String oldTitle, String newTitle) {
		this.oldTitle = oldTitle;
		this.newTitle = newTitle;
	}
	
	@Override
	public String getDescription() {
		return "changed title";
	}

	@Override
	public DiffSupport getDiffSupport() {
		return new DiffSupport() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<String> getOldLines() {
				return Lists.newArrayList(oldTitle);
			}

			@Override
			public List<String> getNewLines() {
				return Lists.newArrayList(newTitle);
			}

			@Override
			public String getOldFileName() {
				return "a.txt";
			}

			@Override
			public String getNewFileName() {
				return "b.txt";
			}
			
		};
	}

}
