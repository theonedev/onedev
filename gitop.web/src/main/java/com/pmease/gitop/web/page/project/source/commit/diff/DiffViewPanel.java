package com.pmease.gitop.web.page.project.source.commit.diff;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.criterion.Restrictions;
import org.parboiled.common.Preconditions;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.CommitCommentManager;
import com.pmease.gitop.model.CommitComment;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.FileHeader;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.Patch;
import com.pmease.gitop.web.page.project.source.commit.diff.renderer.BlobDiffPanel;

@SuppressWarnings("serial")
public class DiffViewPanel extends Panel {

	private final IModel<Project> projectModel;
	private final IModel<String> sinceModel;
	private final IModel<String> untilModel;
	private final IModel<List<CommitComment>> commentsModel;
	
	public DiffViewPanel(String id,
			IModel<Patch> patchModel,
			IModel<Project> projectModel,
			IModel<String> sinceModel,
			IModel<String> untilModel) {
		
		super(id, patchModel);
		
		this.projectModel = projectModel;
		this.sinceModel = sinceModel;
		this.untilModel = untilModel;
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

	private Project getProject() {
		return Preconditions.checkNotNull(projectModel.getObject());
	}
	
	private @Nullable String getSince() {
		return sinceModel.getObject();
	}
	
	private String getUntil() {
		return Preconditions.checkNotNull(untilModel.getObject());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
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
		return (Patch) getDefaultModelObject();
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
