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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.component.link.GitPersonLink.Mode;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.git.command.ArchiveCommand.Format;
import com.pmease.gitop.web.git.command.Tag;
import com.pmease.gitop.web.git.command.TagForEachRefCommand;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.ProjectCategoryPage;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.page.project.source.commit.SourceCommitPage;

@SuppressWarnings("serial")
public class TagsPage extends ProjectCategoryPage {

	private final IModel<Map<String, Tag>> tagsModel;
	
	private String before;
	private String after;
	
	public static PageParameters newParams(Project project, String before, String after) {
		PageParameters parameters = PageSpec.forProject(project);
		if (!Strings.isNullOrEmpty(before)) {
			parameters.add("before", before);
		}
		
		if (!Strings.isNullOrEmpty(after)) {
			parameters.add("after", after);
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
		after = params.get("after").toString();
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
					return getTagsInRange();
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
								Mode.AVATAR_ONLY).enableTooltip("right"));
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
			
			String rangeFirst = getFirstInRange();
			String rangeLast = getLastInRange();
			String first = getFirstInAll();
			String last = getLastInAll();
			
			WebMarkupContainer navigator = new WebMarkupContainer("navigator");
			navigator.setVisibilityAllowed(
					!Objects.equal(getTagsInRange(), getAllTagNames()));
			frag.add(navigator);
			
			navigator.add(new BookmarkablePageLink<Void>("previouslink", 
					TagsPage.class,
					newParams(getProject(), null, rangeFirst)).setEnabled(!Objects.equal(first, rangeFirst)));
			navigator.add(new BookmarkablePageLink<Void>("nextlink", TagsPage.class, 
					newParams(getProject(),rangeLast, null)).setEnabled(!Objects.equal(last, rangeLast)));
		}
	}
	
	private List<String> getAllTagNames() {
		return Lists.newArrayList(tagsModel.getObject().keySet());
	}
	
	private String getFirstInRange() {
		List<String> tags = getTagsInRange();
		return Iterables.getFirst(tags, null);
	}
	
	private String getLastInRange() {
		List<String> tags = getTagsInRange();
		return Iterables.getLast(tags, null);
	}
	
	private String getFirstInAll() {
		Map<String, Tag> tags = tagsModel.getObject();
		return Iterables.getFirst(tags.keySet(), null);
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
	
	private List<String> getTagsInRange() {
		List<String> allTags = getAllTagNames();

		int beforePos = findPosition(allTags, before);
		int afterPos = findPosition(allTags, after);
		
		List<String> result = Lists.newArrayList(allTags);
		if (beforePos >= 0) {
			int start = beforePos + 1;
			int end = Math.max(start + MAX_ROWS, allTags.size());
			result = allTags.subList(start, end);
		}
		
		if (afterPos >= 0) {
			int start = Math.max(0, afterPos - MAX_ROWS);
			result = allTags.subList(start, afterPos);
		}

		if (result.size() > MAX_ROWS) {
			result = result.subList(0, MAX_ROWS);
		}
		
		return Lists.newArrayList(result);
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
