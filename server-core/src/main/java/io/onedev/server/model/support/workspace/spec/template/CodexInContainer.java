package io.onedev.server.model.support.workspace.spec.template;

import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.workspace.spec.ShortcutConfig;
import io.onedev.server.model.support.workspace.spec.UserData;
import io.onedev.server.model.support.workspace.spec.UserDataEntry;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.model.support.workspace.spec.shell.PosixShell;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=300, name="nt:Codex in Container", description="""
        Create a workspace spec running Codex inside container for isolation and security purpose.
        You may customize the <a href='https://code.onedev.io/onedev/docker/codex' target='_blank'>container image</a> 
        later to suit your needs if desired""")
public class CodexInContainer extends WorkspaceSpecTemplate {

    @Override
    public WorkspaceSpec createWorkspaceSpec() {
        var workspaceSpec = new WorkspaceSpec();

        workspaceSpec.setName(getName());
        workspaceSpec.setRunInContainer(true);
        workspaceSpec.setImage("1dev/codex");
        var shell = new PosixShell();
        shell.setShell("bash");
        workspaceSpec.setShell(shell);
        workspaceSpec.setRunAs("1001:1001");

        var shortcutConfig = new ShortcutConfig();
        shortcutConfig.setName("Codex");
        shortcutConfig.setCommand("codex");
        workspaceSpec.getShortcutConfigs().add(shortcutConfig);

        var userData = new UserData();
        userData.setKey("codex");
        userData.getEntries().add(newUserDataEntry("/home/codex/.codex"));
        userData.getEntries().add(newUserDataEntry("/home/codex/.agents"));
        workspaceSpec.getUserDatas().add(userData);

        configureTaskAutomation(workspaceSpec, "codex exec --dangerously-bypass-approvals-and-sandbox \"$TASK_PROMPT\"");

        return workspaceSpec;
    }

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}

    private static UserDataEntry newUserDataEntry(String path) {
        var entry = new UserDataEntry();
        entry.setPath(path);
        return entry;
    }

}
