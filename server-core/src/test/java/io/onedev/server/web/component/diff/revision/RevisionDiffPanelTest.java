package io.onedev.server.web.component.diff.revision;

import static com.google.common.collect.Lists.newArrayList;
import static io.onedev.server.web.component.diff.revision.RevisionDiffPanel.getChildren;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RevisionDiffPanelTest {

    @Test
    public void testGetChildren() {
		var paths = newArrayList("a/b/c", "a/b/d", "a/c", "b/c/d/e", "b/c/d/f");
		assertEquals(newArrayList("a/", "b/c/d/"), getChildren(paths, ""));
		assertEquals(newArrayList("a/b/", "a/c"), getChildren(paths, "a/"));
		
		paths = newArrayList("a", "a/b");
		assertEquals(newArrayList("a", "a/b"), getChildren(paths, ""));

		paths = newArrayList("a/b", "a");
		assertEquals(newArrayList("a/b", "a"), getChildren(paths, ""));

		paths = newArrayList("a/b", "a/c", "a/d");
		assertEquals(newArrayList("a/"), getChildren(paths, ""));

		paths = newArrayList("a/b/c/d", "a/b/d/e", "a/b/d/f");
		assertEquals(newArrayList("a/b/c/d", "a/b/d/"), getChildren(paths, "a/b/"));
    }
	
}