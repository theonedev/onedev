package io.onedev.server.model.support.workspace.spec.template;

import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.workspace.spec.ShortcutConfig;
import io.onedev.server.model.support.workspace.spec.UserData;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.model.support.workspace.spec.shell.CustomLinuxShell;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=1100, name="nt:Claude Code in Container", description="""
        Create a workspace spec running Claude Code inside container for isolation and security purpose. 
        You may customize the <a href='https://code.onedev.io/onedev/docker/claudecode' target='_blank'>container image</a> 
        later to suit your needs if desired. 
        <b class='text-info'>NOTE: </b> Creating workspace inside container requires an enterprise subscription. 
        <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days""")
public class ClaudeCodeInContainer extends WorkspaceSpecTemplate {

    @Override
    public WorkspaceSpec createWorkspaceSpec() {
        var workspaceSpec = new WorkspaceSpec();

        workspaceSpec.setName(getName());
        workspaceSpec.setRunInContainer(true);
        workspaceSpec.setImage("1dev/claudecode:1.0.0");
        workspaceSpec.setShell(new CustomLinuxShell());
        workspaceSpec.setRunAs("1001:1001");

        var shortcutConfig = new ShortcutConfig();
        shortcutConfig.setName("Claude Code");
        shortcutConfig.setCommand("claude");
        workspaceSpec.getShortcutConfigs().add(shortcutConfig);

        var userData = new UserData();
        userData.setKey("claudecode");
        userData.getPaths().add("/home/claude/.profile");
        userData.getPaths().add("/home/claude/.bashrc");
        userData.getPaths().add("/home/claude/.claude");
        userData.getPaths().add("/home/claude/.claude.json");
        userData.getPaths().add("/home/claude/.local/state");
        workspaceSpec.getUserDatas().add(userData);

        return workspaceSpec;
    }

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}

}
