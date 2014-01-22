package com.pmease.gitop.web.page.project.source.commit.diff.renderer.text;

import java.util.List;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.CommitComment;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.FileHeader;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.HunkHeader;
import com.pmease.gitop.web.service.FileBlob;
import com.pmease.gitop.web.service.FileBlobService;

@SuppressWarnings("serial")
public class TextDiffTable extends Panel {

	private final IModel<FileBlob> newFileModel;
	private final IModel<FileBlob> oldFileModel;
	
	private final IModel<FileHeader> fileModel;
	private final IModel<Project> projectModel;
	private final IModel<String> sinceModel;
	private final IModel<String> untilModel;
	private final IModel<List<CommitComment>> commentsModel;
	
	public TextDiffTable(String id, 
			IModel<FileHeader> fileModel, 
			IModel<Project> projectModel,
			final IModel<String> sinceModel,
			final IModel<String> untilModel,
			final IModel<List<CommitComment>> commentsModel) {
		
		super(id);
		
		this.fileModel = fileModel;
		this.projectModel = projectModel;
		this.sinceModel = sinceModel;
		this.untilModel = untilModel;
		this.newFileModel = new LoadableDetachableModel<FileBlob>() {

			@Override
			protected FileBlob load() {
				return loadBlob(getUntil(), getFileHeader().getNewPath());
			}
			
		};
		this.oldFileModel = new LoadableDetachableModel<FileBlob>() {

			@Override
			protected FileBlob load() {
				return loadBlob(getSince(), getFileHeader().getOldPath());
			}
		};
		
		this.commentsModel = commentsModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<HunkHeader>("hunks", new LoadableDetachableModel<List<? extends HunkHeader>>() {

			@Override
			protected List<? extends HunkHeader> load() {
				return getFileHeader().getHunks();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<HunkHeader> item) {
				item.add(new HunkPanel("hunk", 
						Model.of(item.getIndex()),
						fileModel,
						new LoadableDetachableModel<List<String>>() {

							@Override
							protected List<String> load() {
								FileHeader file = getFileHeader();
								if (file.getChangeType() == ChangeType.DELETE) {
									return oldFileModel.getObject().getLines();
								} else {
									return newFileModel.getObject().getLines();
								}
							}
						},
						commentsModel));
			}
		});
	}
	
	private FileBlob loadBlob(String revision, String path) {
		if (GitUtils.isNullHash(revision) || GitUtils.isEmptyPath(path)) {
			return null;
		}
		
		return Gitop.getInstance(FileBlobService.class).get(getProject(), revision, path);
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}
	
	private FileHeader getFileHeader() {
		return fileModel.getObject();
	}
	
	private String getSince() {
		return sinceModel.getObject();
	}
	
	private String getUntil() {
		return untilModel.getObject();
	}
	
	@Override
	public void onDetach() {
		if (fileModel != null) {
			fileModel.detach();
		}
		
		if (projectModel != null) {
			projectModel.detach();
		}
		
		if (sinceModel != null) {
			sinceModel.detach();
		}
		
		if (untilModel != null) {
			untilModel.detach();
		}
		
		if (newFileModel != null) {
			newFileModel.detach();
		}
		
		if (oldFileModel != null) {
			oldFileModel.detach();
		}

		if (commentsModel != null) {
			commentsModel.detach();
		}
		
		super.onDetach();
	}
}
