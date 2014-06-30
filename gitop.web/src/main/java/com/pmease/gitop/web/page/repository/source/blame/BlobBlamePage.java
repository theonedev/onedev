package com.pmease.gitop.web.page.repository.source.blame;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.common.datatype.DataTypes;
import com.pmease.gitop.web.common.quantity.Data;
import com.pmease.gitop.web.common.wicket.bootstrap.Icon;
import com.pmease.gitop.web.component.link.AvatarLink.Mode;
import com.pmease.gitop.web.component.link.PersonLink;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.git.command.BlameCommand;
import com.pmease.gitop.web.git.command.BlameEntry;
import com.pmease.gitop.web.page.repository.source.AbstractFilePage;
import com.pmease.gitop.web.page.repository.source.blob.SourceBlobPage;
import com.pmease.gitop.web.page.repository.source.blob.renderer.TextBlobPanel;
import com.pmease.gitop.web.page.repository.source.commit.SourceCommitPage;
import com.pmease.gitop.web.page.repository.source.commits.CommitsPage;
import com.pmease.gitop.web.page.repository.source.component.PathsBreadcrumb;
import com.pmease.gitop.web.service.FileBlob;
import com.pmease.gitop.web.util.UrlUtils;

@SuppressWarnings("serial")
public class BlobBlamePage extends AbstractFilePage {

	private final IModel<FileBlob> blobModel;
	private final IModel<List<BlameEntry>> blamesModel;
	
	public BlobBlamePage(PageParameters params) {
		super(params);
		
		this.blobModel = new LoadableDetachableModel<FileBlob>() {

			@Override
			protected FileBlob load() {
				return FileBlob.of(getRepository(), getRevision(), getFilePath());
			}
			
		};
		
		this.blamesModel = new LoadableDetachableModel<List<BlameEntry>>() {

			@Override
			protected List<BlameEntry> load() {
				BlameCommand cmd = new BlameCommand(getRepository().git().repoDir());
				cmd.fileName(getFilePath()).objectId(getRevision());
				
				return cmd.call();
			}
			
		};
	}

	protected String getFilePath() {
		return UrlUtils.concatSegments(getPaths());
	}
	
	@Override
	protected String getPageTitle() {
		return getFilePath() + " at " + getRevision() + " " + getRepository().getPathName();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new PathsBreadcrumb("paths", repositoryModel, revisionModel, pathsModel));
		
		FileBlob blob = getBlob();
		if (blob.isText()) {
			add(createBlameView("content"));
		} else {
			add(createInfoView("content"));
		}
	}
	
	private Component createBlameView(String id) {
		Fragment frag = new Fragment(id, "blamefrag", this);
		
		frag.add(new Label("mode", new AbstractReadOnlyModel<String>() {

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
		
		frag.add(new Icon("typeicon", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "icon-file-text";
			}
		}));

		frag.add(new Label("size", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return Data.formatBytes(getBlob().getSize(), Data.KB);
			}
			
		}));
		
		frag.add(new Label("loc", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return getBlob().getLines().size();
			}
			
		}).setVisibilityAllowed(getBlob().isText()));
		
		frag.add(new Label("sloc", new AbstractReadOnlyModel<Integer>() {

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
		Repository repository = Gitop.getInstance(Dao.class).load(Repository.class, blob.getRepositoryId());
		List<String> paths = Lists.newArrayList(Splitter.on("/").split(blob.getFilePath())); 
		frag.add(new BookmarkablePageLink<Void>("historylink", CommitsPage.class,
				CommitsPage.newParams(
						repository,
						blob.getRevision(), 
						paths,
						0)));
		
		frag.add(new BookmarkablePageLink<Void>("normallink",
						SourceBlobPage.class,
						SourceBlobPage.newParams(repository, blob.getRevision(), paths)));
		
		frag.add(new TextBlobPanel("body", blobModel) {
			
			@Override
			protected Component createPrependColumn(String id) {
				Fragment frag = new Fragment(id, "blameColumnFrag", BlobBlamePage.this);
				
				List<BlameEntry> entries = blamesModel.getObject();
				
				RepeatingView view = new RepeatingView("rows");
				frag.add(view);
				for (BlameEntry each : entries) {
					WebMarkupContainer container = new WebMarkupContainer(view.newChildId());
					view.add(container);
					container.add(AttributeModifier.replace("title", each.getCommit().getSubject()));
					String hash = each.getCommit().getHash();
					BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>(
							"shalink", 
							SourceCommitPage.class,
							SourceCommitPage.newParams(getRepository(), hash));
					
					link.add(new Label("sha", GitUtils.abbreviateSHA(each.getCommit().getHash(), 8)));
					container.add(link);
					container.add(new PersonLink("author", each.getCommit().getAuthor(), Mode.NAME));
					
					container.add(new Label("date", 
							DataTypes.DATE.asString(each.getCommit().getAuthor().getWhen(), "yyyy-MM-dd")));
					BookmarkablePageLink<Void> blameLink = new BookmarkablePageLink<Void>(
							"blamelink",
							BlobBlamePage.class,
							BlobBlamePage.newParams(getRepository(), hash, getPaths()));
					container.add(blameLink);
					Loop loop = new Loop("empties", each.getNumLines() - 1) {

						@Override
						protected void populateItem(LoopItem item) {
							
						}
					};
					container.add(loop);
				}
			
				frag.setRenderBodyOnly(true);
				return frag;
			}
		});
		
		return frag;
	}
	
	private Component createInfoView(String id) {
		Fragment frag = new Fragment(id, "infofrag", this);
		return frag;
	}
	
	private FileBlob getBlob() {
		return blobModel.getObject();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(OnDomReadyHeaderItem.forScript("$('.blame-row').tooltip({placement: 'top'});"));
	}
	
	@Override
	public void onDetach() {
		if (blobModel != null) {
			blobModel.detach();
		}

		if (blamesModel != null) {
			blamesModel.detach();
		}
		
		super.onDetach();
	}
}
