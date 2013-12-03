package com.pmease.gitop.web.util;

import static org.eclipse.jgit.lib.Constants.R_HEADS;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

public class JGitUtilsTest {

	private static Repository db;
	
	@BeforeClass
	public static void setUp() throws RepositoryNotFoundException, IOException {
		db = RepositoryCache.open(FileKey.lenient(new File("/Users/zhenyu/data/gitop/projects/1/code"), FS.DETECTED));
	}
	
	@AfterClass
	public static void tearDown() {
		RepositoryCache.close(db);
	}
	
	@Test public void testListRefs() {
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

}
