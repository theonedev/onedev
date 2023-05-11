package io.onedev.server.model.support.code;

import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.*;

public class BranchProtectionTest {

    @Test
    public void checkCommitMessage() {
		var branchProtection = new BranchProtection();
		branchProtection.setEnforceConventionalCommits(true);
		branchProtection.setCommitTypes(Lists.newArrayList("feat", "bug"));
		branchProtection.setCommitScopes(Lists.newArrayList("docs", "api"));
		branchProtection.setMaxCommitMessageLineLength(80);
		assertNull(branchProtection.checkCommitMessage("feat:hello world", false));
		assertNull(branchProtection.checkCommitMessage("feat ! :hello world", false));
		assertNull(branchProtection.checkCommitMessage("feat(docs):hello world", false));
		assertNull(branchProtection.checkCommitMessage("feat(docs)!:hello world", false));
		assertNull(branchProtection.checkCommitMessage("feat ( docs/api ) ! :hello world", false));
		assertNull(branchProtection.checkCommitMessage("feat ( docs , api ) ! :hello world", false));
		assertNull(branchProtection.checkCommitMessage("feat:hello world\n\nbody", false));
		assertNull(branchProtection.checkCommitMessage("hello world", true));
		assertNull(branchProtection.checkCommitMessage("fix(ä): ö", true));
		assertNotNull(branchProtection.checkCommitMessage("feat ( docs/api ) ! hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("unknown ( docs/api ) ! : hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat ( docs unknown ) ! : hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat ( docs api  ! : hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat:hello world\nbody", false));
		assertNotNull(branchProtection.checkCommitMessage("feat:hello world\n\n\nbody", false));
		assertNotNull(branchProtection.checkCommitMessage("hello world hello world hello world hello world hello world hello world hello world hello world hello world ", true));
    }
	
}