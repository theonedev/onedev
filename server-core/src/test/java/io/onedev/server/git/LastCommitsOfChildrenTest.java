package io.onedev.server.git;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.jupiter.api.Test;

import io.onedev.server.git.LastCommitsOfChildren.Cache;
import io.onedev.server.git.LastCommitsOfChildren.Value;

public class LastCommitsOfChildrenTest extends AbstractGitTest {

	@Test
	public void testMergeWithoutTouchingSameFile() throws Exception {
		addFileAndCommit("initial", "", "initial commit");
		git.checkout().setName("dev").setCreateBranch(true).call();
		addFileAndCommit("d", "", "add a file to dev branch");
		git.checkout().setName("main").call();
		addFileAndCommit("m", "", "add a file to main branch");
		git.merge().include(git.getRepository().resolve("dev")).setCommit(true).call();

		LastCommitsOfChildren lastCommits = new LastCommitsOfChildren(git.getRepository(), git.getRepository().resolve("main"));
		assertEquals(git.getRepository().resolve("main~1"), lastCommits.get("m").getId());
		assertEquals(git.getRepository().resolve("dev"), lastCommits.get("d").getId());
		assertEquals(git.getRepository().resolve("main~2"), lastCommits.get("initial").getId());
	}

	@Test
	public void testMergeTouchingSameFile() throws Exception {
		addFileAndCommit("file", "1\n2\n3\n4\n5\n", "initial commit");
		git.checkout().setName("dev").setCreateBranch(true).call();
		addFileAndCommit("file", "0\n1\n2\n3\n4\n5\n", "add first line");
		git.checkout().setName("main").call();
		addFileAndCommit("file", "1\n2\n3\n4\n5\n6\n", "add last line");
		git.merge().include(git.getRepository().resolve("dev")).setCommit(true).call();

		LastCommitsOfChildren lastCommits = new LastCommitsOfChildren(git.getRepository(), git.getRepository().resolve("main"));
		assertEquals(git.getRepository().resolve("main"), lastCommits.get("file").getId());
	}

	@Test
	public void testDirAndFile() throws Exception {
		createDir("dir/dir1/subdir");
		createDir("dir/dir2");
		addFileAndCommit("dir/dir1/file", "", "commit");
		addFileAndCommit("dir/dir2/file", "", "commit");
		addFileAndCommit("dir/dir1/subdir/file", "", "commit");
		addFileAndCommit("dir/file", "", "commit");

		LastCommitsOfChildren lastCommits = new LastCommitsOfChildren(git.getRepository(), git.getRepository().resolve("main"), "dir");
		assertEquals(3, lastCommits.size());
		assertEquals(git.getRepository().resolve("main~1"), lastCommits.get("dir1").getId());
		assertEquals(git.getRepository().resolve("main~2"), lastCommits.get("dir2").getId());
		assertEquals(git.getRepository().resolve("main"), lastCommits.get("file").getId());
	}

	@Test
	public void testWithCache() throws Exception {
		addFileAndCommit("initial", "", "initial commit");
		git.checkout().setName("feature1").setCreateBranch(true).call();
		addFileAndCommit("feature1", "", "add feature1");
		git.checkout().setName("main").call();
		addFileAndCommit("main", "", "add main");
		git.merge().include(git.getRepository().resolve("feature1")).setCommit(true).call();

		final ObjectId oldId = git.getRepository().resolve("main");
		final LastCommitsOfChildren oldLastCommits = new LastCommitsOfChildren(git.getRepository(), oldId);
		Cache cache = new Cache() {

			@Override
			public Map<String, Value> getLastCommitsOfChildren(ObjectId commitId) {
				if (commitId.equals(oldId))
					return oldLastCommits;
				else
					return null;
			}

		};
		assertEquals(oldLastCommits, new LastCommitsOfChildren(git.getRepository(), oldId, cache));

		git.checkout().setName("feature2").setCreateBranch(true).call();
		addFileAndCommit("initial", "hello", "modify initial");
		git.checkout().setName("main").call();
		git.merge().include(git.getRepository().resolve("feature2")).setCommit(true).call();

		ObjectId newId = git.getRepository().resolve("main");
		assertEquals(new LastCommitsOfChildren(git.getRepository(), newId), new LastCommitsOfChildren(git.getRepository(), newId, cache));
	}


	@Test
	public void testEmptyTreeAndInvalidPaths() throws Exception {
		ObjectId empty = snapshot(Map.of(), 1);
		assertEquals(Map.of(), new LastCommitsOfChildren(git.getRepository(), empty));
		ObjectId head = snapshot(Map.of("file", "content"), 2, empty);
		assertThrows(IllegalArgumentException.class,
				() -> new LastCommitsOfChildren(git.getRepository(), head, "missing"));
		assertThrows(IllegalArgumentException.class,
				() -> new LastCommitsOfChildren(git.getRepository(), head, "file"));
	}

	@Test
	public void testAllChildrenShareOneWalkAndStopEarly() throws Exception {
		ObjectId parent = snapshot(Map.of("old", "old"), 1);
		Map<String, String> files = new HashMap<>();
		for (int i = 0; i < 200; i++)
			files.put("file" + i, "new");
		ObjectId head = snapshot(files, 2, parent);
		List<ObjectId> visited = new ArrayList<>();
		LastCommitsOfChildren result = new LastCommitsOfChildren(git.getRepository(), head, id -> {
			visited.add(id.copy());
			return null;
		});
		assertEquals(200, result.size());
		for (Value value : result.values())
			assertEquals(head, value.getId());
		assertEquals(List.of(head), visited);
	}

	@Test
	public void testPrunesMergeParentForUnresolvedChildren() throws Exception {
		ObjectId root = snapshot(Map.of("dir/file", "base", "dir/unchanged", "base"), 1);
		ObjectId main = snapshot(Map.of("dir/file", "main", "dir/unchanged", "base"), 2, root);
		ObjectId side = root;
		for (int i = 3; i < 70; i++)
			side = snapshot(Map.of("dir/file", "side" + i, "dir/unchanged", "base"), i, side);
		ObjectId merge = snapshot(Map.of("dir/file", "main", "dir/unchanged", "base"), 70, main, side);
		List<ObjectId> visited = new ArrayList<>();
		LastCommitsOfChildren result = new LastCommitsOfChildren(git.getRepository(), merge, "dir", id -> {
			visited.add(id.copy());
			return null;
		});
		assertEquals(main, result.get("file").getId());
		assertEquals(root, result.get("unchanged").getId());
		assertEquals(List.of(merge, main, root), visited);
	}

	@Test
	public void testCacheWaitsForOtherMergeBranches() throws Exception {
		ObjectId root = snapshot(Map.of("a", "0", "b", "0", "c", "0"), 1);
		ObjectId main = snapshot(Map.of("a", "1", "b", "0", "c", "0"), 3, root);
		ObjectId side = snapshot(Map.of("a", "0", "b", "1", "c", "0"), 2, root);
		ObjectId merge = snapshot(Map.of("a", "1", "b", "1", "c", "0"), 4, main, side);
		LastCommitsOfChildren cached = new LastCommitsOfChildren(git.getRepository(), main);
		LastCommitsOfChildren result = new LastCommitsOfChildren(git.getRepository(), merge,
				id -> id.equals(main) ? cached : null);
		assertEquals(main, result.get("a").getId());
		assertEquals(side, result.get("b").getId());
		assertEquals(root, result.get("c").getId());
		assertEquals(new LastCommitsOfChildren(git.getRepository(), merge), result);
	}

	@Test
	public void testMergeRoutesEachChildThroughItsMatchingParent() throws Exception {
		ObjectId root = snapshot(Map.of("a", "0", "b", "0"), 1);
		ObjectId main = snapshot(Map.of("a", "main", "b", "0"), 2, root);
		ObjectId side = snapshot(Map.of("a", "discarded", "b", "side"), 3, root);
		ObjectId merge = snapshot(Map.of("a", "main", "b", "side"), 4, main, side);

		LastCommitsOfChildren result = new LastCommitsOfChildren(git.getRepository(), merge);
		assertEquals(main, result.get("a").getId());
		assertEquals(side, result.get("b").getId());
		for (ObjectId boundary : List.of(root, main, side, merge)) {
			LastCommitsOfChildren cached = new LastCommitsOfChildren(git.getRepository(), boundary);
			assertEquals(result, new LastCommitsOfChildren(git.getRepository(), merge,
					id -> id.equals(boundary) ? cached : null));
		}
	}

	@Test
	public void testFirstMatchingParentDoesNotDependOnOtherChildren() throws Exception {
		ObjectId root = snapshot(Map.of("a", "0", "b", "0"), 1);
		ObjectId main = snapshot(Map.of("a", "same", "b", "0"), 2, root);
		ObjectId side = snapshot(Map.of("a", "same", "b", "side"), 3, root);
		ObjectId merge = snapshot(Map.of("a", "same", "b", "side"), 4, main, side);
		LastCommitsOfChildren result = new LastCommitsOfChildren(git.getRepository(), merge);
		assertEquals(main, result.get("a").getId());
		assertEquals(side, result.get("b").getId());
		LastCommitsOfChildren cached = new LastCommitsOfChildren(git.getRepository(), side);
		assertEquals(result, new LastCommitsOfChildren(git.getRepository(), merge,
				id -> id.equals(side) ? cached : null));
	}

	@Test
	public void testClockSkewCanReachAnAncestorAgainForDifferentChildren() throws Exception {
		ObjectId root = snapshot(Map.of("a", "0", "b", "0"), 1);
		ObjectId base = snapshot(Map.of("a", "1", "b", "1"), 5, root);
		ObjectId main = snapshot(Map.of("a", "1", "b", "0"), 100, base);
		ObjectId side = snapshot(Map.of("a", "0", "b", "1"), 2, base);
		ObjectId merge = snapshot(Map.of("a", "1", "b", "1"), 101, main, side);
		LastCommitsOfChildren result = new LastCommitsOfChildren(git.getRepository(), merge);
		assertEquals(base, result.get("a").getId());
		assertEquals(base, result.get("b").getId());
	}

	@Test
	public void testFileDirectoryConflictDoesNotFollowMissingEntry() throws Exception {
		ObjectId root = snapshot(Map.of("anchor", "0"), 1);
		ObjectId file = snapshot(Map.of("anchor", "0", "node", "file"), 2, root);
		ObjectId missing = snapshot(Map.of("anchor", "0"), 3, root);
		ObjectId directory = snapshot(Map.of("anchor", "0", "node/child", "content"), 4, root);
		ObjectId merge = snapshot(Map.of("anchor", "0", "node/child", "content"), 5,
				file, missing, directory);
		assertEquals(directory, new LastCommitsOfChildren(git.getRepository(), merge).get("node").getId());
		assertEquals(directory, new LastCommitsOfChildren(git.getRepository(), merge, "node").get("child").getId());
	}

	@Test
	public void testPartialCacheDoesNotLoseChildren() throws Exception {
		ObjectId root = snapshot(Map.of("a", "0", "b", "0"), 1);
		ObjectId head = snapshot(Map.of("a", "0", "b", "0"), 2, root);
		LastCommitsOfChildren expected = new LastCommitsOfChildren(git.getRepository(), head);
		assertEquals(expected, new LastCommitsOfChildren(git.getRepository(), head,
				id -> id.equals(head) ? Map.of("a", expected.get("a")) : null));
	}

	@Test
	public void testCacheDoesNotReadHistoryBehindBoundary() throws Exception {
		ObjectId missing = ObjectId.fromString("1234567890123456789012345678901234567890");
		ObjectId boundary = snapshot(Map.of("a", "0", "b", "0"), 1, missing);
		ObjectId head = snapshot(Map.of("a", "1", "b", "0"), 2, boundary);
		try (RevWalk walk = new RevWalk(git.getRepository())) {
			RevCommit commit = walk.parseCommit(boundary);
			Map<String, Value> cached = Map.of("a", new Value(commit), "b", new Value(commit));
			LastCommitsOfChildren result = new LastCommitsOfChildren(git.getRepository(), head,
					id -> id.equals(boundary) ? cached : null);
			assertEquals(head, result.get("a").getId());
			assertEquals(boundary, result.get("b").getId());
		}
	}

	@Test
	public void testPathBoundariesAndFileDirectoryReplacement() throws Exception {
		ObjectId root = snapshot(Map.of("dir/child", "file", "dir-other/file", "0"), 1);
		ObjectId replaced = snapshot(Map.of("dir/child/nested", "0", "dir/中文", "0", "dir-other/file", "0"), 2, root);
		ObjectId head = snapshot(Map.of("dir/child/nested", "0", "dir/中文", "0", "dir-other/file", "1"), 3, replaced);
		LastCommitsOfChildren result = new LastCommitsOfChildren(git.getRepository(), head, "dir");
		assertEquals(2, result.size());
		assertEquals(replaced, result.get("child").getId());
		assertEquals(replaced, result.get("中文").getId());
		assertEquals(replaced, new LastCommitsOfChildren(git.getRepository(), head, "dir/child").get("nested").getId());
	}

	@Test
	public void testModeChangeAndRecreatedChild() throws Exception {
		ObjectId first = ObjectId.fromString(addFileAndCommit("file", "content", "initial"));
		var index = git.getRepository().lockDirCache();
		try {
			index.getEntry("file").setFileMode(FileMode.EXECUTABLE_FILE);
			index.write();
			index.commit();
		} finally {
			index.unlock();
		}
		ObjectId modeChange = ObjectId.fromString(commit("executable"));
		assertEquals(modeChange, new LastCommitsOfChildren(git.getRepository(), modeChange).get("file").getId());
		removeFileAndCommit("file", "remove");
		ObjectId recreated = ObjectId.fromString(addFileAndCommit("file", "content", "recreate"));
		LastCommitsOfChildren cached = new LastCommitsOfChildren(git.getRepository(), first);
		assertEquals(recreated, new LastCommitsOfChildren(git.getRepository(), recreated,
				id -> id.equals(first) ? cached : null).get("file").getId());
	}

	private ObjectId snapshot(Map<String, String> files, int time, ObjectId... parents) throws Exception {
		try (var inserter = git.getRepository().newObjectInserter()) {
			DirCache index = DirCache.newInCore();
			var builder = index.builder();
			for (var file : new TreeMap<>(files).entrySet()) {
				DirCacheEntry entry = new DirCacheEntry(file.getKey());
				entry.setFileMode(FileMode.REGULAR_FILE);
				entry.setObjectId(inserter.insert(Constants.OBJ_BLOB, Constants.encode(file.getValue())));
				builder.add(entry);
			}
			builder.finish();
			CommitBuilder commit = new CommitBuilder();
			commit.setTreeId(index.writeTree(inserter));
			commit.setParentIds(parents);
			PersonIdent ident = new PersonIdent("author", "author@example.com", Instant.ofEpochSecond(time), ZoneOffset.UTC);
			commit.setAuthor(ident);
			commit.setCommitter(ident);
			commit.setMessage("commit " + time);
			ObjectId id = inserter.insert(commit);
			inserter.flush();
			return id;
		}
	}
}
