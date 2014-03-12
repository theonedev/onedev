package com.pmease.gitop.web.page.project.source.branches;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pmease.commons.git.BriefCommit;
import com.pmease.gitop.web.common.wicket.component.BarLabel;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.component.link.GitPersonLink.Mode;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.git.command.AheadBehind;
import com.pmease.gitop.web.git.command.AheadBehindCommand;
import com.pmease.gitop.web.git.command.BranchForEachRefCommand;
import com.pmease.gitop.web.page.project.ProjectCategoryPage;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.page.project.source.tree.SourceTreePage;

@SuppressWarnings("serial")
public class BranchesPage extends ProjectCategoryPage {

	private final IModel<String> defaultBranchModel;
	private final IModel<Map<String, BriefCommit>> branchesModel;
	private final IModel<Map<String, AheadBehind>> aheadBehindsModel;
	
	public BranchesPage(PageParameters params) {
		super(params);
		
		defaultBranchModel = new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return GitUtils.getDefaultBranch(getProject().code());
			}
			
		};
		
		branchesModel = new LoadableDetachableModel<Map<String, BriefCommit>>() {

			@Override
			protected Map<String, BriefCommit> load() {
				BranchForEachRefCommand cmd = new BranchForEachRefCommand(getProject().code().repoDir());
				return cmd.call();
			}
		};
		
		aheadBehindsModel = new LoadableDetachableModel<Map<String ,AheadBehind>>() {

			@Override
			protected Map<String, AheadBehind> load() {
				List<String> branchNames = getBranchNames();
				
				Map<String, AheadBehind> map = Maps.newHashMap();
				File repoDir = getProject().code().repoDir();
				String defaultBranch = getDefaultBranch();
				for (String each : branchNames) {
					if (Objects.equal(defaultBranch, each)) {
						map.put(each, new AheadBehind());
					} else {
						AheadBehindCommand command = new AheadBehindCommand(repoDir);
						AheadBehind ab = command.leftBranch(each).rightBranch(defaultBranch).call();
						map.put(each, ab);
					}
				}
				
				return map;
			}
		};
	}

	@Override
	protected String getPageTitle() {
		return getProject() + " - branches";
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		if (getDefaultBranch() == null) {
			add(new Fragment("content", "nobranch", this));
		} else {
			Fragment frag = new Fragment("content", "branchesFrag", this);
			frag.add(new MetaFrag("defaultMeta", getDefaultBranch()));
			
			IModel<List<String>> names = new AbstractReadOnlyModel<List<String>>() {

				@Override
				public List<String> getObject() {
					Map<String, BriefCommit> branches = branchesModel.getObject();
					List<String> list = Lists.newArrayList();
					String defaultName = getDefaultBranch();
					for (String each : branches.keySet()) {
						if (!Objects.equal(each, defaultName)) {
							list.add(each);
						}
					}
					
					return list;
				}
			};
			
			frag.add(new ListView<String>("branches", names) {

				@Override
				protected void populateItem(ListItem<String> item) {
					String refName = item.getModelObject();
					
					item.add(new MetaFrag("meta", refName));
					AheadBehind ab = getAheadBehind(refName);
					
					Fragment barFrag = new Fragment("statbar", "statBarFrag", BranchesPage.this);
					item.add(barFrag);
					barFrag.add(new BarLabel("aheadbar", Model.of(getPercent(ab.getAhead(), true))));
					barFrag.add(new Label("aheadnum", ab.getAhead()));
					barFrag.add(new BarLabel("behindbar", Model.of(getPercent(ab.getBehind(), false))));
					barFrag.add(new Label("behindnum", ab.getBehind()));
				}
			});
			
			add(frag);
		}
	}
	
	private int getMax(boolean isAhead) {
		Map<String, AheadBehind> map = aheadBehindsModel.getObject();
		int max = 0;
		for (Entry<String, AheadBehind> entry : map.entrySet()) {
			max = Math.max(max, isAhead ? entry.getValue().getAhead() : entry.getValue().getBehind()); 
		}
		return max;
	}
	
	// in case the bar is empty when percent is zero, we give it a very small 
	// width, so the bar can be displayed always
	//
	static final double MIN_SIZE = 0.0001;
	
	private Double getPercent(int value, boolean isAhead) {
		int max = getMax(isAhead);
		double percent = max == 0 ? 0d : new Double(value) / max * 0.9d;
		if (percent == 0d) {
			percent = MIN_SIZE;
		}
		
		return percent;
	}
	
	String getDefaultBranch() {
		return defaultBranchModel.getObject();
	}
	
	private List<String> getBranchNames() {
		return Lists.newArrayList(branchesModel.getObject().keySet());
	}
	
	class MetaFrag extends Fragment {
		private final String refName;
		
		public MetaFrag(String id, String refName) {
			super(id, "metaFrag", BranchesPage.this);
			this.refName = refName;
		}
		
		@Override
		protected void onInitialize() {
			super.onInitialize();
			
			AbstractLink link = new BookmarkablePageLink<Void>("branchlink", 
					SourceTreePage.class,
					SourceTreePage.newParams(getProject(), refName));
			link.add(new Label("name", refName));
			add(link);
			
			BriefCommit commit = getLastCommit(refName);
			add(new AgeLabel("updatedTime", Model.of(commit.getAuthor().getDate())));
			add(new GitPersonLink("author", 
					Model.<GitPerson>of(GitPerson.of(commit.getAuthor())),
					Mode.NAME));
		}
	}
	
	BriefCommit getLastCommit(String refName) {
		Map<String, BriefCommit> branches = branchesModel.getObject();
		if (branches.containsKey(refName))
			return branches.get(refName);
		else
			throw new IllegalStateException();
	}
	
	AheadBehind getAheadBehind(String refName) {
		Map<String, AheadBehind> map = aheadBehindsModel.getObject();
		return map.get(refName);
	}
	
	@Override
	public void onDetach() {
		if (branchesModel != null) {
			branchesModel.detach();
		}
		
		if (defaultBranchModel != null) {
			defaultBranchModel.detach();
		}
		
		if (aheadBehindsModel != null) {
			aheadBehindsModel.detach();
		}
		
		super.onDetach();
	}
}
