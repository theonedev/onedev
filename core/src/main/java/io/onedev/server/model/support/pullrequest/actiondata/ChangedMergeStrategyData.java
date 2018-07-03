package io.onedev.server.model.support.pullrequest.actiondata;

import java.util.List;

import com.google.common.collect.Lists;

import io.onedev.server.model.support.DiffSupport;
import io.onedev.server.model.support.pullrequest.MergeStrategy;

public class ChangedMergeStrategyData extends ActionData {

	private static final long serialVersionUID = 1L;

	private final MergeStrategy oldStrategy;
	
	private final MergeStrategy newStrategy;
	
	public ChangedMergeStrategyData(MergeStrategy oldStrategy, MergeStrategy newStrategy) {
		this.oldStrategy = oldStrategy;
		this.newStrategy = newStrategy;
	}
	
	@Override
	public String getDescription() {
		return "changed merge strategy";
	}

	@Override
	public DiffSupport getDiffSupport() {
		return new DiffSupport() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<String> getOldLines() {
				return Lists.newArrayList(oldStrategy.getDisplayName());
			}

			@Override
			public List<String> getNewLines() {
				return Lists.newArrayList(newStrategy.getDisplayName());
			}

			@Override
			public String getOldFileName() {
				return null;
			}

			@Override
			public String getNewFileName() {
				return null;
			}
			
		};
	}

}
