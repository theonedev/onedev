package com.pmease.gitplex.web.page.repository.code.commit.diff;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
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
import org.parboiled.common.Preconditions;

import com.pmease.commons.util.MediaTypes;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.common.wicket.bootstrap.Alert;
import com.pmease.gitplex.web.common.wicket.bootstrap.Icon;
import com.pmease.gitplex.web.git.command.DiffTreeCommand;
import com.pmease.gitplex.web.page.repository.code.commit.diff.patch.FileHeader;
import com.pmease.gitplex.web.page.repository.code.commit.diff.patch.FileHeader.PatchType;
import com.pmease.gitplex.web.page.repository.code.commit.diff.patch.HunkHeader;
import com.pmease.gitplex.web.page.repository.code.commit.diff.patch.Patch;
import com.pmease.gitplex.web.page.repository.code.commit.diff.renderer.BlobMessagePanel;
import com.pmease.gitplex.web.page.repository.code.commit.diff.renderer.image.ImageDiffPanel;
import com.pmease.gitplex.web.page.repository.code.commit.diff.renderer.text.TextDiffPanel;

@SuppressWarnings("serial")
public class DiffViewPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String sinceRevision;
	
	private final String untilRevision;

	private final IModel<Patch> patchModel;
	
	public DiffViewPanel(String id, IModel<Repository> repoModel, @Nullable String sinceRevision, String untilRevision) {
		super(id);
	
		this.repoModel = repoModel;
		this.sinceRevision = sinceRevision;
		this.untilRevision = Preconditions.checkNotNull(untilRevision);
		
		this.patchModel = new LoadableDetachableModel<Patch>() {

			@Override
			protected Patch load() {
				return loadPatch();
			}
			
		};
	}

	private Patch loadPatch() {
		Patch patch = new DiffTreeCommand(repoModel.getObject().git().repoDir())
			.since(sinceRevision)
			.until(untilRevision)
			.recurse(true) // -r
			.root(true)    // --root
			.contextLines(Constants.DEFAULT_CONTEXT_LINES) // -U3
			.findRenames(true) // -M
			.call();
		return patch;
	}
	
	protected String getWarningMessage() {
		return "This commit contains too many changed files to render. "
				+ "Showing only the first " + Constants.MAX_RENDERABLE_BLOBS 
				+ " changed files. You can still get all changes "
				+ "manually by running below command: "
				+ "<pre><code>git diff-tree --root -M -r -p " 
				+ (sinceRevision!=null?sinceRevision + " ":"") + untilRevision 
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
			 .withHtmlMessage(true)
			 .setCloseButtonVisible(true)
			 .type(Alert.Type.Warning);
		
		return alert;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(createAlert("largecommitwarning"));
		createDiffToc();
		add(createFileList("filelist"));
	}
	
	protected Component createFileList(String id) {
		IModel<List<? extends FileHeader>> model = new LoadableDetachableModel<List<? extends FileHeader>>() {

			@Override
			protected List<? extends FileHeader> load() {
				return getDiffPatch().getFiles();
			}
		};
		
		return new ListView<FileHeader>(id, model) {

			@Override
			protected void populateItem(ListItem<FileHeader> item) {
				int index = item.getIndex();
				item.setMarkupId("diff-" + item.getIndex());
				item.add(createFileDiffPanel("file", item.getModel(), index));
			}
			
		};
	}
	
	private Component newMessagePanel(String id, int index, IModel<FileHeader> model, IModel<String> messageModel) {
		return new BlobMessagePanel(id, index, repoModel, model, sinceRevision, untilRevision, messageModel);
	}
	
	protected Component createFileDiffPanel(String id, IModel<FileHeader> model, int index) {
		FileHeader file = model.getObject();
		List<? extends HunkHeader> hunks = file.getHunks();
		
		if (hunks.isEmpty()) {
			// hunks is empty when this file is renamed, or the file is binary
			// or this file is just an empty file
			//
			
			// renamed without change
			if (file.getChangeType() == ChangeType.RENAME) {
				return newMessagePanel(id, index, model, Model.of("File renamed without changes")); 
			}
			
			// binary file also including image file, so we need detect the
			// media type
			if (file.getPatchType() == PatchType.BINARY) {
				String path;
				if (file.getChangeType() == ChangeType.DELETE) {
					path = file.getOldPath();
				} else {
					path = file.getNewPath();
				}
				
				// fast detect the media type without loading file blob
				//
				MediaType mediaType = MediaTypes.detectFrom(new byte[0], path);
				if (MediaTypes.isImage(mediaType) 
						&& MediaTypes.isSafeInline(mediaType)) {
					return new ImageDiffPanel(id, index, repoModel, model, sinceRevision, untilRevision);
				} else {
					// other binary diffs
					return newMessagePanel(id, index, model, Model.of("File is a binary file"));
				}
			}
			
			// file is just an empty file
			return newMessagePanel(id, index, model, Model.of("File is empty"));
			
		} else {
			// blob is text and we can show diffs
			
			if (index > Constants.MAX_RENDERABLE_BLOBS) {
				// too many renderable blobs
				// only show diff stats instead of showing the contents
				//
				return newMessagePanel(id, index, model, Model.of(
						file.getDiffStat().getAdditions() + " additions, " 
						+ file.getDiffStat().getDeletions() + " deletions"));
			}
			
			if (hunks.size() > Constants.MAX_RENDERABLE_DIFF_LINES) {
				// don't show huge diff (exceed 10000 lines)
				//
				return newMessagePanel(id, index, model, Model.of(
						"<p>"
						+ "The diff for this file is too large to render. "
						+ "You can run below command to get the diff manually:"
						+ "</p> "
						+ "<pre><code>"
						+ "git diff -C -M " + (sinceRevision!=null?sinceRevision:"") + " " + untilRevision + " -- " 
						+ StringUtils.quoteArgument(file.getNewPath())
						+ "</code></pre>"));
			}
		}
		
		return new TextDiffPanel(id, index, repoModel, model, sinceRevision, untilRevision);
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
			return "fa-diff-added";
			
		case MODIFY:
			return "fa-diff-modified";
			
		case DELETE:
			return "fa-diff-removed";
			
		case RENAME:
			return "fd-diff-renamed";
			
		case COPY:
			return "fa-diff-copy";
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
		repoModel.detach();
		patchModel.detach();
		
		super.onDetach();
	}
}
