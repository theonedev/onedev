package com.pmease.gitop.web.page.project.source.tags;

import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.component.link.GitPersonLink.Mode;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.git.command.ArchiveCommand.Format;
import com.pmease.gitop.web.git.command.Tag;
import com.pmease.gitop.web.git.command.TagForEachRefCommand;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.RepositoryTabPage;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.page.project.source.commit.SourceCommitPage;

@SuppressWarnings("serial")
public class TagsPage extends RepositoryTabPage {

	private final IModel<Map<String, Tag>> tagsModel;
	
	private String before;
	
	public static PageParameters newParams(Repository project, String before) {
		PageParameters parameters = PageSpec.forRepository(project);
		if (!Strings.isNullOrEmpty(before)) {
			parameters.add("before", before);
		}
		
		return parameters;
	}
	
	public TagsPage(PageParameters params) {
		super(params);
		
		tagsModel = new LoadableDetachableModel<Map<String, Tag>>() {

			@Override
			protected Map<String, Tag> load() {
				Map<String, Tag> map = new TagForEachRefCommand(getProject().code().repoDir()).call();
				return map;
			}
			
		};
		
		before = params.get("before").toString();
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		Map<String, Tag> tags = tagsModel.getObject();
		if (tags.isEmpty()) {
			Fragment frag = new Fragment("content", "norecord", this);
			add(frag);
		} else {
			Fragment frag = new Fragment("content", "tagsfrag", this);
			add(frag);
			
			IModel<List<String>> tagNames = new AbstractReadOnlyModel<List<String>>() {

				@Override
				public List<String> getObject() {
					return getCurrentTagNames();
				}
				
			};
			
			frag.add(new ListView<String>("tags", tagNames) {

				@Override
				protected void populateItem(ListItem<String> item) {
					String tagName = item.getModelObject();
					Tag tag = getTagCommit(tagName);
					
					if (tag.getTagger() == null) {
						item.add(new WebMarkupContainer("creator").setVisibilityAllowed(false));
						item.add(new WebMarkupContainer("date").setVisibilityAllowed(false));
					} else {
						item.add(new GitPersonLink("creator", 
								Model.of(GitPerson.of(tag.getTagger())), 
								Mode.AVATAR).enableTooltip("right"));
						item.add(new AgeLabel("date", Model.of(tag.getTagger().getDate())));
					}
					
					item.add(new Label("tagname", tagName));
					item.add(new Label("message", tag.getMessage()));
					AbstractLink commitLink = new BookmarkablePageLink<Void>("commitlink",
							SourceCommitPage.class,
							SourceCommitPage.newParams(getProject(), tag.getSha()));
					
					item.add(commitLink);
					commitLink.add(new Label("hash", GitUtils.abbreviateSHA(tag.getSha())));
					
					item.add(new ResourceLink<Void>("ziplink", 
							new GitArchiveResourceReference(),
							GitArchiveResourceReference.newParams(getProject(), tagName, Format.ZIP)));
					item.add(new ResourceLink<Void>("targzlink",
							new GitArchiveResourceReference(),
							GitArchiveResourceReference.newParams(getProject(), tagName, Format.TAR_GZ)));
				}
				
			});
			
			String last = getLastInAll();
			
			WebMarkupContainer navigator = new WebMarkupContainer("navigator");
			navigator.setVisibilityAllowed(
					!Objects.equal(getCurrentTagNames(), getAllTagNames()));
			frag.add(navigator);
			
			String previous = getPreviousPos();
			String next = getNextPos();
			navigator.add(new BookmarkablePageLink<Void>("previouslink", 
					TagsPage.class,
					newParams(getProject(), previous)).setEnabled(previous != null));
			navigator.add(new BookmarkablePageLink<Void>("nextlink", TagsPage.class, 
					newParams(getProject(), next)).setEnabled(!Objects.equal(last, next)));
		}
	}
	
	private int getPageNo() {
		List<String> allTags = getAllTagNames();
		int beforePos = findPosition(allTags, before);
		if (beforePos <= 0) {
			return 0;
		}
		
		return (beforePos + 1) / MAX_ROWS;
	}
	
	private List<String> getTagNamesInPage(int pageNo) {
		List<String> allTags = getAllTagNames();
		int start = pageNo * MAX_ROWS;
		int end = Math.min((pageNo + 1) * MAX_ROWS, allTags.size());
		
		return ImmutableList.copyOf(allTags.subList(start, end));
	}
	
	private List<String> getCurrentTagNames() {
		int pageNo = getPageNo();
		return getTagNamesInPage(pageNo);
	}
	
	private String getNextPos() {
		List<String> current = getCurrentTagNames();
		return Iterables.getLast(current, null);
	}
	
	private String getPreviousPos() {
		int previousPage = getPageNo() - 1;
		if (previousPage < 0) {
			return null;
		} else if (previousPage == 0) {
			return "";
		}

		List<String> names = getTagNamesInPage(previousPage);
		return Iterables.getFirst(names, null);
	}
	
	private List<String> getAllTagNames() {
		return Lists.newArrayList(tagsModel.getObject().keySet());
	}
	
	private String getLastInAll() {
		Map<String, Tag> tags = tagsModel.getObject();
		return Iterables.getLast(tags.keySet(), null);
	}
	
	static final int MAX_ROWS = 50;
	
	static int findPosition(List<String> list, String str) {
		if (Strings.isNullOrEmpty(str))
			return -1;
		
		for (int i = 0; i < list.size(); i++) {
			if (Objects.equal(str, list.get(i)))
				return i;
		}
		
		return -1;
	}
	
	Tag getTagCommit(String name) {
		return tagsModel.getObject().get(name);
	}
	
	@Override
	protected String getPageTitle() {
		return getProject() + " - tags";
	}

	@Override
	public void onDetach() {
		super.onDetach();
		
		if (tagsModel != null) {
			tagsModel.detach();
		}
	}
}
