package io.onedev.server.model.support.code;

import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.*;

public class BranchProtectionTest {

    @Test
    public void checkCommitMessageWithConventionalCommitChecker() {
		var branchProtection = new BranchProtection();
		var conventionalCommitChecker = new ConventionalCommitChecker();
		conventionalCommitChecker.setCommitTypes(Lists.newArrayList("feat", "bug"));
		conventionalCommitChecker.setCommitScopes(Lists.newArrayList("docs", "api", "docs/api", "docs api"));
		branchProtection.setCommitMessageChecker(conventionalCommitChecker);
		branchProtection.setMaxCommitMessageLineLength(80);
		assertNull(branchProtection.checkCommitMessage("Revert \"hello world\"", false));
		assertNotNull(branchProtection.checkCommitMessage("revert  \"hello world\"", false));
		assertNull(branchProtection.checkCommitMessage("feat: hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat2: hello world", false));
		assertNull(branchProtection.checkCommitMessage("feat(docs): hello world", false));
		assertNull(branchProtection.checkCommitMessage("feat(docs)!: hello world", false));
		assertNull(branchProtection.checkCommitMessage("feat(docs/api)!: hello world", false));
		assertNull(branchProtection.checkCommitMessage("feat(docs api)!: hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat(docs  api)!: hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat(docs2)!: hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat(docs)!:  hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat:hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat ! :hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat(docs):hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat(docs)!:hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat ( docs/api ) ! :hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat ( docs , api ) ! :hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat:hello world\n\nbody", false));
		assertNull(branchProtection.checkCommitMessage("hello world", true));
		assertNull(branchProtection.checkCommitMessage("fix(ä): ö", true));
		assertNotNull(branchProtection.checkCommitMessage("feat ( docs/api ) ! hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("unknown ( docs/api ) ! : hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat ( docs unknown ) ! : hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat ( docs api  ! : hello world", false));
		assertNotNull(branchProtection.checkCommitMessage("feat:hello world\nbody", false));
		assertNotNull(branchProtection.checkCommitMessage("feat:hello world\n\n\nbody", false));
		assertNotNull(branchProtection.checkCommitMessage("hello world hello world hello world hello world hello world hello world hello world hello world hello world ", true));
	
		conventionalCommitChecker.setCheckCommitMessageFooter(true);
		conventionalCommitChecker.setCommitMessageFooterPattern("Signed By: .+");
		assertNull(branchProtection.checkCommitMessage("hello world", true));
		assertNotNull(branchProtection.checkCommitMessage("feat: a feature", false));
		assertNotNull(branchProtection.checkCommitMessage("feat: a feature\n", false));
		assertNotNull(branchProtection.checkCommitMessage("feat: a feature\nSigned By: robin", false));
		assertNotNull(branchProtection.checkCommitMessage("feat: a feature\n\na footer", false));
		assertNull(branchProtection.checkCommitMessage("feat: a feature\n\nSigned By: robin", false));
		
		conventionalCommitChecker.setCommitTypesForFooterCheck(Lists.newArrayList("fix"));
		assertNull(branchProtection.checkCommitMessage("feat: a feature", false));
		assertNotNull(branchProtection.checkCommitMessage("fix: a bug", false));
		assertNotNull(branchProtection.checkCommitMessage("fix: a bug\n\nSigned By: Robin Shen", false));
    }

    @Test
    public void checkCommitMessageWithRegexChecker() {
		var branchProtection = new BranchProtection();
		var regexChecker = new RegexpCommitChecker();
		regexChecker.setPattern("^\\[JIRA-\\d+\\].*");
		branchProtection.setCommitMessageChecker(regexChecker);
		
		assertNull(branchProtection.checkCommitMessage("[JIRA-123] Fix bug", false));
		assertNull(branchProtection.checkCommitMessage("[JIRA-456] Add feature", false));
		assertNotNull(branchProtection.checkCommitMessage("Fix bug without JIRA", false));
		assertNotNull(branchProtection.checkCommitMessage("JIRA-123 Fix bug", false)); // Missing brackets

		// Subject and body should be separated by exactly one blank line
		regexChecker.setPattern("(?s)^\\[JIRA-\\d+\\][^\\n]*\\n\\n(?!\\s*\\n).+");
		assertNull(branchProtection.checkCommitMessage("[JIRA-789] Subject line\n\nBody line", false));
		assertNotNull(branchProtection.checkCommitMessage("[JIRA-789] Subject line\nBody line", false)); // No blank line
		assertNotNull(branchProtection.checkCommitMessage("[JIRA-789] Subject line\n\n\nBody line", false)); // More than one blank line
		
		// Merge commits should be skipped
		assertNull(branchProtection.checkCommitMessage("Merge branch 'main'", true));
    }

    @Test
    public void checkCommitMessageWithoutChecker() {
		var branchProtection = new BranchProtection();
		branchProtection.setMaxCommitMessageLineLength(80);
		
		// Without checker, any message format is valid
		assertNull(branchProtection.checkCommitMessage("Any message format", false));
		assertNull(branchProtection.checkCommitMessage("feat: conventional format", false));
		
		// But line length is still checked
		assertNotNull(branchProtection.checkCommitMessage("hello world hello world hello world hello world hello world hello world hello world hello world hello world ", false));
    }
	
}
