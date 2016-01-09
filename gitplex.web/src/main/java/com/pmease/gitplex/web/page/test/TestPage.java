package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.html.link.Link;
import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.AuxiliaryManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				/*
				Repository repo = GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
				try (	FileRepository jgitRepo = repo.openAsJGitRepo();
						RevWalk revWalk = new RevWalk(jgitRepo);) {
					ObjectId masterId = jgitRepo.resolve("master~10000");
					for (Ref ref: jgitRepo.getRefDatabase().getRefs(Constants.R_TAGS).values()) {
						jgitRepo.peel(ref);
						ObjectId commitId = null;
						RevObject revObject = revWalk.parseAny(ref.getObjectId());
						if (revObject instanceof RevTag) {
							RevTag revTag = (RevTag) revObject;
							if (revTag.getObject() instanceof RevCommit) {
								revWalk.markStart((RevCommit)revTag.getObject());
							}
						} else {
							commitId = ref.getObjectId();
							revWalk.markStart(revWalk.lookupCommit(commitId));
						}
					}
					revWalk.setRevFilter(RevFilter.MERGE_BASE);
					System.out.println(revWalk.next().getId());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
				Repository repo = GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
//				GitPlex.getInstance(AuxiliaryManager.class).check(repo, "master");
				long time = System.currentTimeMillis();
				System.out.println(GitPlex.getInstance(AuxiliaryManager.class).getDescendants(repo, ObjectId.fromString("1da177e4c3f41524e886b7f1b8a0c1fc7321cac2")).size());
				System.out.println(System.currentTimeMillis()-time);
			}
			
		});
	}

}
