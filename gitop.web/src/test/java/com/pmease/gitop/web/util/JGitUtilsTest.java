package com.pmease.gitop.web.util;

import static org.eclipse.jgit.lib.Constants.R_HEADS;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.util.FS;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmease.commons.git.Commit;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class JGitUtilsTest {

	private static Repository db;
	private static final File REPO = new File("/Users/zhenyu/SCM/gitrep/linux.git");
	@BeforeClass
	public static void setUp() throws RepositoryNotFoundException, IOException {
//		db = RepositoryCache.open(FileKey.lenient(new File("/Users/zhenyu/data/gitop/projects/1/code"), FS.DETECTED));
		db = RepositoryCache.open(FileKey.lenient(REPO, FS.DETECTED));
	}
	
	@AfterClass
	public static void tearDown() {
		RepositoryCache.close(db);
	}
	
	public void testListRefs() {
		Stopwatch watch = new Stopwatch().start();
		List<Ref> refs = Lists.newArrayList();
		refs.addAll(getRefs(db, R_HEADS));
		refs.addAll(db.getTags().values());
		
		System.out.println(refs.size() + "refs found, " + watch.elapsed(TimeUnit.MILLISECONDS) + " mills");
		
		System.out.println(refs);
	}
	
	/**
     * Get refs with prefix
     * @param db
     * @param prefix
     * @return all refs with specified prefix
     */
    public static List<Ref> getRefs(Repository db, String prefix) {
        try {
            return Lists.newArrayList(db.getRefDatabase().getRefs(prefix).values());
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public void testBlame() throws GitAPIException, IOException {
    	Git git = new Git(db);
    	BlameResult result = git.blame().setFilePath("MAINTAINERS").setFollowFileRenames(true).call();
    	for (int i = 0; i < result.getResultContents().size(); i++) {
    		System.out.println(result.getSourceCommitter(i));
    	}
    }
    
    @Test public void testGetContributors() throws NoHeadException, GitAPIException {
//    	Git git = new Git(db);
//    	Iterable<RevCommit> commits = git.log().addPath("MAINTAINERS").call();
//    	Set<String> names = Sets.newHashSet();
//    	for (RevCommit each : commits) {
//    		names.add(each.getCommitterIdent().getEmailAddress());
//    	}
//    	
//    	System.out.println(names);
    	
    	List<String> committers = getCommitters(REPO, "MAINTAINERS");
    	System.out.println("Total " + committers.size() + "\n" + committers);
    }
    
    @Test public void testGetCC() {
    	com.pmease.commons.git.Git git = new com.pmease.commons.git.Git(REPO);
    	List<Commit> commits = git.log(null, null, "MAINTAINERS", 0);
    	Set<String> set = Sets.newHashSet();
    	for (Commit each : commits) {
    		set.add(each.getCommitter().getEmail());
    	}
    	System.out.println("Total " + set.size() + "\n" + set);
    }
    
    List<String> getCommitters(File repoDir, String file) {
    	Commandline cmd = new Commandline("git").workingDir(repoDir);
    	cmd.addArgs("log",  "--format=%ce", "--", file);
    	final Set<String> lines = Sets.newHashSet();
    	cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				lines.add(line);
			}
    		
    	}, new LineConsumer() {

			@Override
			public void consume(String line) {
				System.err.println(line);
			}
    	}).checkReturnCode();
    	
    	return Lists.newArrayList(lines);
    }
}
