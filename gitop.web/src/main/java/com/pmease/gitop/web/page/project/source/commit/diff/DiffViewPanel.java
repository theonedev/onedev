package com.pmease.gitop.web.page.project.source.commit.diff;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.hibernate.criterion.Restrictions;
import org.parboiled.common.Preconditions;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.CommitCommentManager;
import com.pmease.gitop.model.CommitComment;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.Constants;
import com.pmease.gitop.web.common.wicket.bootstrap.Alert;
import com.pmease.gitop.web.common.wicket.bootstrap.Icon;
import com.pmease.gitop.web.git.command.DiffTreeCommand;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.FileHeader;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.Patch;
import com.pmease.gitop.web.page.project.source.commit.diff.renderer.BlobDiffPanel;

@SuppressWarnings("serial")
public class DiffViewPanel extends Panel {

	private final IModel<Project> projectModel;
	private final IModel<Patch> patchModel;
	private final IModel<String> sinceModel;
	private final IModel<String> untilModel;
	private final IModel<List<CommitComment>> commentsModel;
	
	public DiffViewPanel(String id,
			IModel<Project> projectModel,
			IModel<String> sinceModel,
			IModel<String> untilModel) {
		
		super(id);
		
		this.projectModel = projectModel;
		this.sinceModel = sinceModel;
		this.untilModel = untilModel;
		
		this.patchModel = new LoadableDetachableModel<Patch>() {

			@Override
			protected Patch load() {
				return loadPatch();
			}
			
		};
		
		this.commentsModel = new LoadableDetachableModel<List<CommitComment>>() {

			@Override
			protected List<CommitComment> load() {
				CommitCommentManager ccm = Gitop.getInstance(CommitCommentManager.class);
				return ccm.query(
							Restrictions.eq("project", getProject()),
							Restrictions.eq("commit", getUntil()));
			}
			
		};
	}

	private Patch loadPatch() {
		String since = getSince();
		String until = getUntil();
		
		Patch patch = new DiffTreeCommand(getProject().code().repoDir())
			.since(since)
			.until(until)
			.recurse(true) // -r
			.root(true)    // --root
			.contextLines(Constants.DEFAULT_CONTEXT_LINES) // -U3
			.findRenames(true) // -M
			.call();
		return patch;
	}
	
	private Project getProject() {
		return Preconditions.checkNotNull(projectModel.getObject());
	}
	
	private @Nullable String getSince() {
		return sinceModel.getObject();
	}
	
	private String getUntil() {
		return Preconditions.checkNotNull(untilModel.getObject());
	}
	
	protected String getWarningMessage() {
		return "This commit contains too many changed files to render. "
				+ "Showing only the first " + Constants.MAX_RENDERABLE_BLOBS 
				+ " changed files. You can still get all changes "
				+ "manually by running below command: "
				+ "<pre><code>git diff-tree -M -r -p " 
				+ getSince() + " " + getUntil() 
				+ "</code></pre>";
	}
	
	protected Alert createAlert(String id) {
		Alert alert = new Alert(id, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getWarningMessage();
			}
			
		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisibilityAllowed(getDiffPatch().getFiles().size() > Constants.MAX_RENDERABLE_BLOBS );
			}
		};
		
		alert
			 .setCloseButtonVisible(true)
			 .type(Alert.Type.Warning)
			 .setMessageEscapeModelStrings(false);
		
		return alert;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(createAlert("largecommitwarning"));
		
		createDiffToc();
		
		IModel<List<? extends FileHeader>> model = new LoadableDetachableModel<List<? extends FileHeader>>() {

			@Override
			protected List<? extends FileHeader> load() {
				return getDiffPatch().getFiles();
			}
		};
		
		add(new ListView<FileHeader>("filelist", model) {

			@Override
			protected void populateItem(ListItem<FileHeader> item) {
				int index = item.getIndex();
				item.setMarkupId("diff-" + item.getIndex());
				item.add(new BlobDiffPanel("file", index, item.getModel(), projectModel, sinceModel, untilModel, commentsModel));
			}
			
		});
	}
	
	private final Patch getDiffPatch() {
		return patchModel.getObject();
	}
	
	private void createDiffToc() {

		add(new Label("changes", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return getDiffPatch().getFiles().size();
			}
			
		}));
		
		add(new Label("additions", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return getDiffPatch().getDiffStat().getAdditions();
			}
			
		}));
		
		add(new Label("deletions", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return getDiffPatch().getDiffStat().getDeletions();
			}
			
		}));
		
		add(new ListView<FileHeader>("fileitem", new AbstractReadOnlyModel<List<? extends FileHeader>>() {

			@Override
			public List<? extends FileHeader> getObject() {
				return getDiffPatch().getFiles();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<FileHeader> item) {
				FileHeader file = item.getModelObject();
				ChangeType changeType = file.getChangeType();
				item.add(new Icon("icon", getChangeIcon(changeType)).add(AttributeModifier.replace("title", changeType.name())));
				WebMarkupContainer link = new WebMarkupContainer("link");
				link.add(AttributeModifier.replace("href", "#diff-" + item.getIndex()));
				item.add(link);
				link.add(new Label("oldpath", Model.of(file.getOldPath())).setVisibilityAllowed(file.getChangeType() == ChangeType.RENAME));
				link.add(new Label("path", Model.of(file.getNewPath())));
				
				WebMarkupContainer statlink = new WebMarkupContainer("statlink");
				statlink.add(AttributeModifier.replace("href", "#diff-" + item.getIndex()));
				statlink.add(AttributeModifier.replace("title", file.getDiffStat().toString()));
				
				item.add(statlink);
				statlink.add(new DiffStatBar("statbar", item.getModel()));
			}
		});
	}
	
	private static String getChangeIcon(ChangeType changeType) {
		switch (changeType) {
		case ADD:
			return "icon-diff-added";
			
		case MODIFY:
			return "icon-diff-modified";
			
		case DELETE:
			return "icon-diff-deleted";
			
		case RENAME:
			return "icon-diff-renamed";
			
		case COPY:
			return "icon-diff-copy";
		}
		
		throw new IllegalArgumentException("change type " + changeType);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(OnDomReadyHeaderItem.forScript("$('#diff-toc .btn').click(function() { $('#diff-toc').toggleClass('open');})"));
	}
	
	@Override
	public void onDetach() {
		if (projectModel != null) {
			projectModel.detach();
		}
		
		if (patchModel != null) {
			patchModel.detach();
		}
		
		if (sinceModel != null) {
			sinceModel.detach();
		}
		
		if (untilModel != null) {
			untilModel.detach();
		}
		
		if (commentsModel != null) {
			commentsModel.detach();
		}
		
		super.onDetach();
	}
}
