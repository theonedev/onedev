package io.onedev.server.model.support.workspace.spec;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.SubPath;

@Editable
public class UserConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private String path;

    private String content;

    @Editable(order=100, description="Specify path of the config file relative to home directory")
    @SubPath
    @NotEmpty
    public String getPath() {
        return path;
    }    

    public void setPath(String path) {
        this.path = path;
    }

    @Editable(order=200, description="Specify content of the config file")
    @Code(language=Code.PLAIN_TEXT)
    @NotEmpty
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }

}
