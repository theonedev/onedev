package io.onedev.server.model.support.workspace.spec.template;

import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.workspace.spec.ShortcutConfig;
import io.onedev.server.model.support.workspace.spec.UserData;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.model.support.workspace.spec.shell.CustomLinuxShell;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=400, name="nt:Cursor CLI in Container", description="""
        Create a workspace spec running Cursor CLI inside container for isolation and security purpose.
        You may customize the <a href='https://code.onedev.io/onedev/docker/cursor' target='_blank'>container image</a> 
        later to suit your needs if desired""")
public class CursorInContainer extends WorkspaceSpecTemplate {

    @Override
    public WorkspaceSpec createWorkspaceSpec() {
        var workspaceSpec = new WorkspaceSpec();

        workspaceSpec.setName(getName());
        workspaceSpec.setRunInContainer(true);
        workspaceSpec.setImage("1dev/cursor");
        workspaceSpec.setShell(new CustomLinuxShell());
        workspaceSpec.setRunAs("1001:1001");

        var shortcutConfig = new ShortcutConfig();
        shortcutConfig.setName("Cursor CLI");
        shortcutConfig.setCommand("agent");
        workspaceSpec.getShortcutConfigs().add(shortcutConfig);

        var userData = new UserData();
        userData.setKey("cursor");
        userData.getPaths().add("/home/cursor/.profile");
        userData.getPaths().add("/home/cursor/.bashrc");
        userData.getPaths().add("/home/cursor/.cursor");
        userData.getPaths().add("/home/cursor/.config/cursor");
        workspaceSpec.getUserDatas().add(userData);

        configureTaskAutomation(workspaceSpec, "agent -p --force --approve-mcps --trust \"$TASK_PROMPT\"");

        return workspaceSpec;
    }

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}

}
