package com.pmease.gitplex.web.page.test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.markup.html.link.Link;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;
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
				Repository repo = GitPlex.getInstance(Dao.class).load(Repository.class, 1L);
				
				long time = System.currentTimeMillis();
				Commandline cmd = new Commandline("git");
				cmd.addArgs("merge-base", "--all", "master", "branch1", "branch2", 
						"branch3", "branch4", "branch5", "branch6", "branch7",
						"branch8", "branch9");
				cmd.workingDir(new File("w:\\temp\\gitplex_storage\\repositories\\1"));
				cmd.execute(new LineConsumer() {

					@Override
					public void consume(String line) {
						System.out.println(line);
					}
					
				}, new LineConsumer() {

					@Override
					public void consume(String line) {
						System.err.println(line);
					}
					
				});
				
				System.out.println(System.currentTimeMillis()-time);
				time = System.currentTimeMillis();
				
				try(	FileRepository jgitRepo = repo.openAsJGitRepo();
						RevWalk revWalk = new RevWalk(jgitRepo);) {
					revWalk.setRevFilter(RevFilter.MERGE_BASE);
					revWalk.markStart(revWalk.lookupCommit(jgitRepo.resolve("master")));
					revWalk.markStart(revWalk.lookupCommit(jgitRepo.resolve("branch1")));
					revWalk.markStart(revWalk.lookupCommit(jgitRepo.resolve("branch2")));
					revWalk.markStart(revWalk.lookupCommit(jgitRepo.resolve("branch3")));
					revWalk.markStart(revWalk.lookupCommit(jgitRepo.resolve("branch4")));
					revWalk.markStart(revWalk.lookupCommit(jgitRepo.resolve("branch5")));
					revWalk.markStart(revWalk.lookupCommit(jgitRepo.resolve("branch6")));
					revWalk.markStart(revWalk.lookupCommit(jgitRepo.resolve("branch7")));
					revWalk.markStart(revWalk.lookupCommit(jgitRepo.resolve("branch8")));
					revWalk.markStart(revWalk.lookupCommit(jgitRepo.resolve("branch9")));
					
					for (RevCommit commit: revWalk) {
						System.out.println(commit.name());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				System.out.println(System.currentTimeMillis()-time);
				time = System.currentTimeMillis();
				cmd = new Commandline("git");
				cmd.addArgs("rev-list", "^0fc2f137226eff4c9dd90864dda5c237474c3ec5");
				cmd.workingDir(new File("w:\\temp\\gitplex_storage\\repositories\\1"));
				final AtomicInteger commits = new AtomicInteger(0);
				cmd.execute(new LineConsumer() {

					@Override
					public void consume(String line) {
						commits.incrementAndGet();
					}
					
				}, new LineConsumer() {

					@Override
					public void consume(String line) {
						System.err.println(line);
					}
					
				});
				System.out.println(commits.get());
				System.out.println(System.currentTimeMillis()-time);
				
				time = System.currentTimeMillis();
				System.out.println(GitPlex.getInstance(AuxiliaryManager.class).getDescendants(repo, ObjectId.fromString("0fc2f137226eff4c9dd90864dda5c237474c3ec5")).size());
				System.out.println(System.currentTimeMillis()-time);
			}
			
		});
	}

}
