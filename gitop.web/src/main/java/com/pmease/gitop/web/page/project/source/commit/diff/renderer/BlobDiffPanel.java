package com.pmease.gitop.web.page.project.source.commit.diff.renderer;

import java.util.List;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffEntry.Side;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.CommitComment;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.Constants;
import com.pmease.gitop.web.common.wicket.bootstrap.Alert;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.page.project.source.blob.SourceBlobPage;
import com.pmease.gitop.web.page.project.source.commit.diff.DiffStatBar;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.FileHeader;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.FileHeader.PatchType;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.HunkHeader;
import com.pmease.gitop.web.page.project.source.commit.diff.renderer.image.ImageBlobDiffPanel;
import com.pmease.gitop.web.page.project.source.commit.diff.renderer.text.TextDiffTable;
import com.pmease.gitop.web.service.FileTypes;
import com.pmease.gitop.web.util.MediaTypeUtils;

@SuppressWarnings("serial")
public class BlobDiffPanel extends Panel {

//	private final IModel<Commit> commitModel;
	private final IModel<Project> projectModel;
	private final IModel<String> sinceModel;
	private final IModel<String> untilModel;
	private final IModel<List<CommitComment>> commentsModel;
	
	private final int index;
	
	public BlobDiffPanel(String id,
			int index,
			IModel<FileHeader> fileModel,
			IModel<Project> projectModel,
			IModel<String> sinceModel,
			IModel<String> untilModel,
			IModel<List<CommitComment>> commentsModel) {
		super(id, fileModel);
	
		this.index = index;
		this.projectModel = projectModel;
		this.sinceModel = sinceModel;
		this.untilModel = untilModel;
		this.commentsModel = commentsModel;
//		this.commitModel = commitModel;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer statspan = new WebMarkupContainer("statspan");
		add(statspan);
		statspan.add(AttributeModifier.replace("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getFile().getDiffStat().toString();
			}
			
		}));
		
		statspan.add(new DiffStatBar("stat", (IModel<FileHeader>) getDefaultModel()));
//		statspan.add(new Label("additions", new AbstractReadOnlyModel<String>() {
//				@Override
//				public String getObject() {
//					int additions = getFile().getDiffStat().getAdditions(); 
//					return additions > 0 ? "+" + additions : "-";
//				} 
//			}));
//		statspan.add(new Label("deletions", new AbstractReadOnlyModel<String>() {
//
//			@Override
//			public String getObject() {
//				int deletions = getFile().getDiffStat().getDeletions(); 
//				return deletions > 0 ? "-" + deletions : "-";
//			}
//		}));
		
//		add(new Label("oldpath", new AbstractReadOnlyModel<String>() {
//
//			@Override
//			public String getObject() {
//				return getFile().getPath(Side.OLD);
//			}
//		}) {
//			@Override
//			protected void onConfigure() {
//				super.onConfigure();
//				
//				this.setVisibilityAllowed(getFile().getChangeType() == ChangeType.RENAME);
//			}
//		});
		
		add(new Label("path", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				String path;
				FileHeader file = getFile();
				if (file.getChangeType() == ChangeType.RENAME) {
					path = file.getPath(Side.OLD) + " &#10142; " + file.getPath(Side.NEW);
				} else if (file.getChangeType() == ChangeType.DELETE) {
					path = file.getPath(Side.OLD);
				} else {
					path = file.getPath(Side.NEW);
				}
				
				return path;
			}
		}).setEscapeModelStrings(false));
		
		String path = getFile().getNewPath();
		List<String> paths = Lists.newArrayList(Splitter.on("/").split(path));
		String until = untilModel.getObject();
		AbstractLink blobLink = new BookmarkablePageLink<Void>("bloblink",
				SourceBlobPage.class,
				SourceBlobPage.newParams(projectModel.getObject(), 
										 until, 
										 paths));
		blobLink.setVisibilityAllowed(getFile().getChangeType() != ChangeType.DELETE);
		add(blobLink);
		blobLink.add(new Label("sha", Model.of(GitUtils.abbreviateSHA(until, 6))));
		
		add(createContent("diffcontent"));
	}

	private Component createContent(String id) {
		FileHeader file = getFile();
		List<? extends HunkHeader> hunks = file.getHunks();
		
		if (hunks.isEmpty()) {
			// hunks is empty when this file is renamed, or the file is binary
			// or this file is just an empty file
			//
			
			// renamed without change
			if (file.getChangeType() == ChangeType.RENAME) {
				return createMessageLabel(id, Model.of("File renamed without changes")); 
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
				
				FileTypes types = Gitop.getInstance(FileTypes.class);
				
				// fast detect the media type without loading file blob
				//
				MediaType mediaType = types.getMediaType(path, new byte[0]);
				if (MediaTypeUtils.isImageType(mediaType) 
						&& types.isSafeInline(mediaType)) {
					// image diffs
					return new ImageBlobDiffPanel(id, getFileModel(), projectModel, sinceModel, untilModel);
					
				} else {
					// other binary diffs
					return createMessageLabel(id, Model.of("File is a binary file"));
					
				}
			}
			
			// file is just an empty file
			return createMessageLabel(id, Model.of("File is empty"));
			
		} else {
			// blob is text and we can show diffs
			
			if (index > Constants.MAX_RENDERABLE_BLOBS) {
				// too many renderable blobs
				// only show diff stats instead of showing the contents
				//
				return createMessageLabel(id, Model.of(
						file.getDiffStat().getAdditions() + " additions, " 
						+ file.getDiffStat().getDeletions() + " deletions"));
			}
			
			if (hunks.size() > Constants.MAX_RENDERABLE_DIFF_LINES) {
				// don't show huge diff (exceed 10000 lines)
				//
				return createMessageLabel(id, new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						String since = sinceModel.getObject();
						String until = untilModel.getObject();
						
						if (Strings.isNullOrEmpty(since)) {
							since = "";
						}
						
						return "<p>"
								+ "The diff for this file is too large to render. "
								+ "You can run below command to get the diff manually:"
								+ "</p> "
								+ "<pre><code>"
								+ "git diff -C -M " + since + " " + until + " -- " 
								+ StringUtils.quoteArgument(getFile().getNewPath())
								+ "</code></pre>";
					}
					
				}).withHtmlMessage(true);
			}
			
			return new TextDiffTable(id, getFileModel(), projectModel, sinceModel, untilModel, commentsModel);
		}
	}
	
	private Alert createMessageLabel(String id, IModel<String> model) {
		Alert alert = new Alert(id, model);
		alert.type(Alert.Type.Warning).setCloseButtonVisible(false);
		return alert;
	}
	
	private FileHeader getFile() {
		return (FileHeader) getDefaultModelObject();	
	}
	
	@SuppressWarnings("unchecked")
	private IModel<FileHeader> getFileModel() {
		return (IModel<FileHeader>) getDefaultModel();
	}
	
	@Override
	public void onDetach() {
		if (projectModel != null) {
			projectModel.detach();
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
