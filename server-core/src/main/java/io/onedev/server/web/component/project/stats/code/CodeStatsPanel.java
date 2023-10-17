package io.onedev.server.web.component.project.stats.code;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;

import io.onedev.server.OneDev;
import io.onedev.server.git.service.GitService;
import io.onedev.server.xodus.CommitInfoManager;
import io.onedev.server.model.Project;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.branches.ProjectBranchesPage;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;
import io.onedev.server.web.page.project.tags.ProjectTagsPage;

@SuppressWarnings("serial")
public class CodeStatsPanel extends GenericPanel<Project> {

	private final IModel<Integer> commitCountModel = new LoadableDetachableModel<Integer>() {

		@Override
		protected Integer load() {
			return getCommitInfoManager().getCommitCount(getProject().getId());
		}
		
	};
	
	public CodeStatsPanel(String id, IModel<Project> projectModel) {
		super(id, projectModel);
	}

	private CommitInfoManager getCommitInfoManager() {
		return OneDev.getInstance(CommitInfoManager.class);
	}
	
	private Project getProject() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PageParameters params = ProjectBlobPage.paramsOf(getProject());
		Link<Void> filesLink = new BookmarkablePageLink<Void>("files", ProjectBlobPage.class, params);
		filesLink.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return getCommitInfoManager().getFileCount(getProject().getId()) + " files";
			}
			
		}));
		add(filesLink);
		
		params = ProjectCommitsPage.paramsOf(getProject(), null);
		Link<Void> commitsLink = new BookmarkablePageLink<Void>("commits", ProjectCommitsPage.class, params);
		commitsLink.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return commitCountModel.getObject() + " commits";
			}
			
		}));
		add(commitsLink);
		
		params = ProjectBranchesPage.paramsOf(getProject());
		Link<Void> branchesLink = new BookmarkablePageLink<Void>("branches", ProjectBranchesPage.class, params);
		branchesLink.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				int branchCount = OneDev.getInstance(GitService.class)
						.countRefs(getProject().getId(), Constants.R_HEADS);
				return branchCount + " branches";
			}
			
		}));
		add(branchesLink);
		
		params = ProjectTagsPage.paramsOf(getProject());
		Link<Void> tagsLink = new BookmarkablePageLink<Void>("tags", ProjectTagsPage.class, params);
		tagsLink.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				int tagCount = OneDev.getInstance(GitService.class)
						.countRefs(getProject().getId(), Constants.R_TAGS);
				return tagCount + " tags";
			}
			
		}));
		add(tagsLink);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(commitCountModel.getObject() != 0);
	}

	@Override
	protected void onDetach() {
		commitCountModel.detach();
		super.onDetach();
	}

}
