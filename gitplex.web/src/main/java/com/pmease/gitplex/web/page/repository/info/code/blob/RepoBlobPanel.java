package com.pmease.gitplex.web.page.repository.info.code.blob;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.common.quantity.Data;
import com.pmease.gitplex.web.common.wicket.bootstrap.Icon;
import com.pmease.gitplex.web.page.repository.info.code.blame.BlobBlamePage;
import com.pmease.gitplex.web.page.repository.info.code.blob.renderer.BlobRenderer;
import com.pmease.gitplex.web.page.repository.info.code.blob.renderer.BlobRendererFactory;
import com.pmease.gitplex.web.page.repository.info.code.blob.renderer.RawBlobResourceReference;
import com.pmease.gitplex.web.page.repository.info.code.commits.RepoCommitsPage;
import com.pmease.gitplex.web.service.FileBlob;

@SuppressWarnings("serial")
public class RepoBlobPanel extends Panel {

	public RepoBlobPanel(String id, IModel<FileBlob> blobModel) {
		super(id, blobModel);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("mode", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				FileMode mode = getBlob().getMode();
				if (mode == FileMode.SYMLINK) {
					return "symbolic link";
				} else if (mode == FileMode.EXECUTABLE_FILE) {
					return "executable file";
				} else {
					return "file";
				}
			}
			
		}));
		
		add(new Icon("typeicon", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "icon-file-text";
			}
		}));

		add(new Label("size", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return Data.formatBytes(getBlob().getSize(), Data.KB);
			}
			
		}));
		
		add(new Label("loc", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return getBlob().getLines().size();
			}
			
		}).setVisibilityAllowed(getBlob().isText()));
		
		add(new Label("sloc", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				List<String> lines = getBlob().getLines();
				int sloc = 0;
				for (String each : lines) {
					if (!StringUtils.isBlank(each)) {
						sloc++;
					}
				}
				
				return sloc;
			}
			
		}));
		
		FileBlob blob = getBlob();
		
		Repository repository = GitPlex.getInstance(Dao.class).load(Repository.class, blob.getRepositoryId());
		add(new BookmarkablePageLink<Void>("historylink", RepoCommitsPage.class,
				RepoCommitsPage.paramsOf(
						repository,
						blob.getRevision(), 
						blob.getFilePath(),
						0)));
		
		add(new BookmarkablePageLink<Void>("blamelink",
						BlobBlamePage.class,
						BlobBlamePage.paramsOf(repository, blob.getRevision(), blob.getFilePath())).setVisibilityAllowed(blob.isText()));
		
		add(new ResourceLink<Void>("rawlink", new RawBlobResourceReference(),
				RawBlobResourceReference.paramsOf(blob)));
		
		BlobRenderer renderer = getRenderer();
		add(renderer.render("body", (IModel<FileBlob>) getDefaultModel()));
	}
	
	private BlobRenderer getRenderer() {
		return GitPlex.getInstance(BlobRendererFactory.class).newRenderer(getBlob());
	}
	
	private FileBlob getBlob() {
		return (FileBlob) getDefaultModelObject();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
}
