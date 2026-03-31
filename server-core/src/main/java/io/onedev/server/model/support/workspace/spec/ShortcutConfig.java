package io.onedev.server.model.support.workspace.spec;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.Editable;

@Editable
public class ShortcutConfig implements Serializable {

	private static final long serialVersionUID = 1L;

    private String name;

	private String command;

    @Editable(order = 200, description = "Specify name of the shortcut")
    @NotEmpty
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    @Editable(order = 300, description = "Specify command to run if this shortcut is opened, for instance <i>opencode</i>, <i>npm run dev</i>")
    @NotEmpty
    public String getCommand() {
        return command;
    }
    
    public void setCommand(String command) {
        this.command = command;
    }

}
