package com.pmease.gitop.web.page.repository.source.branches;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.markup.html.WebMarkupContainer;
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
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pmease.commons.git.BriefCommit;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.web.common.wicket.component.BarLabel;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.AvatarLink.Mode;
import com.pmease.gitop.web.component.link.PersonLink;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.git.command.AheadBehind;
import com.pmease.gitop.web.git.command.AheadBehindCommand;
import com.pmease.gitop.web.git.command.BranchForEachRefCommand;
import com.pmease.gitop.web.page.repository.RepositoryPage;
import com.pmease.gitop.web.page.repository.pullrequest.NewRequestPage;
import com.pmease.gitop.web.page.repository.source.tree.SourceTreePage;

@SuppressWarnings("serial")
public class BranchesPage extends RepositoryPage {

	private final IModel<String> defaultBranchModel;
	private final IModel<String> baseBranchModel;
	private final IModel<Map<String, BriefCommit>> branchesModel;
	private final IModel<Map<String, AheadBehind>> aheadBehindsModel;
	
	private final IModel<Map<String, PullRequest>> requestsModel;
	
	public BranchesPage(PageParameters params) {
		super(params);
		
		defaultBranchModel = new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return GitUtils.getDefaultBranch(getRepository().git());
			}
			
		};
		
		baseBranchModel = new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String str = BranchesPage.this.getPageParameters().get("base").toString();
				if (Strings.isNullOrEmpty(str)) {
					return defaultBranchModel.getObject();
				} else {
					return str;
				}
			}
		};
		
		branchesModel = new LoadableDetachableModel<Map<String, BriefCommit>>() {

			@Override
			protected Map<String, BriefCommit> load() {
				BranchForEachRefCommand cmd = new BranchForEachRefCommand(getRepository().git().repoDir());
				return cmd.call();
			}
		};
		
		aheadBehindsModel = new LoadableDetachableModel<Map<String ,AheadBehind>>() {

			@Override
			protected Map<String, AheadBehind> load() {
				List<String> branchNames = getBranchNames();
				
				Map<String, AheadBehind> map = Maps.newHashMap();
				File repoDir = getRepository().git().repoDir();
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
		
		requestsModel = new LoadableDetachableModel<Map<String, PullRequest>>() {

			@Override
			protected Map<String, PullRequest> load() {
				Branch base = Gitop.getInstance(BranchManager.class).findBy(getRepository(), getBaseBranch());
				
				List<PullRequest> requests = Gitop.getInstance(PullRequestManager.class)
						.query(Restrictions.isNotNull("closeInfo"),
							  Restrictions.eq("target", base));
				
				Map<String, PullRequest> result = Maps.newHashMap();
				for (PullRequest each : requests) {
					if (Objects.equal(each.getSource().getRepository(), getRepository())) {
						result.put(each.getSource().getName(), each);
					}
				}
				
				return result;
			}
			
		};
	}

	@Override
	protected String getPageTitle() {
		return getRepository() + " - branches";
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getDefaultBranch() == null) {
			add(new Fragment("content", "nobranch", this));
		} else {
			Fragment frag = new Fragment("content", "branchesFrag", this);
			frag.add(new MetaFrag("baseMeta", getBaseBranch()));
			
			IModel<List<String>> names = new AbstractReadOnlyModel<List<String>>() {

				@Override
				public List<String> getObject() {
					Map<String, BriefCommit> branches = branchesModel.getObject();
					List<String> list = Lists.newArrayList();
					String base = getBaseBranch();
					for (String each : branches.keySet()) {
						if (!Objects.equal(each, base)) {
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
					
					PullRequest request = getPullRequest(refName);
					
					if (request == null) {
						BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>(
								"compareLink",
								NewRequestPage.class,
								NewRequestPage.newParams(getRepository(), refName, getBaseBranch()));
						
						item.add(link);
						item.add(new Label("pullName").setVisibilityAllowed(false));
					} else {
						item.add(new WebMarkupContainer("compareLink").setVisibilityAllowed(false));
						
						// TODO: Change this to LINK to pull request
						item.add(new Label("pullName", request.getId()));
					}
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

	private String getBaseBranch() {
		return baseBranchModel.getObject();
	}
	
	private String getDefaultBranch() {
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
					SourceTreePage.newParams(getRepository(), refName));
			link.add(new Label("name", refName));
			add(link);
			
			BriefCommit commit = getLastCommit(refName);
			add(new AgeLabel("updatedTime", Model.of(commit.getAuthor().getDate())));
			add(new PersonLink("author", commit.getAuthor().getPerson(), Mode.NAME));
			
			add(new Label("default", "default").setVisibilityAllowed(Objects.equal(refName, getDefaultBranch())));
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
	
	private PullRequest getPullRequest(String source) {
		return requestsModel.getObject().get(source);
	}
	
	@Override
	public void onDetach() {
		if (branchesModel != null) {
			branchesModel.detach();
		}
		
		if (defaultBranchModel != null) {
			defaultBranchModel.detach();
		}
		
		if (baseBranchModel != null) {
			baseBranchModel.detach();
		}
		
		if (aheadBehindsModel != null) {
			aheadBehindsModel.detach();
		}
		
		if (requestsModel != null) {
			requestsModel.detach();
		}
		
		super.onDetach();
	}
}
