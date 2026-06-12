package io.onedev.server.workspace;

import java.io.Serializable;

public interface ShellOutputCallback extends Serializable {

    void onOutput(String base64Data);
    
}
