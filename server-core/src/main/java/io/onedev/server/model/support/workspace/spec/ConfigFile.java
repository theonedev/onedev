package io.onedev.server.model.support.workspace.spec;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Path;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class ConfigFile implements Serializable {

    private static final long serialVersionUID = 1L;

    private String path;

    private String content;

    @Editable(order=100, description="Specify path to the config file. Only absolute path is accepted")
    @Path(Path.Type.ABSOLUTE)
    @Interpolative(variableSuggester="suggestVariables", exampleVar = "/")
    @NotEmpty
    public String getPath() {
        return path;
    }    

    public void setPath(String path) {
        this.path = path;
    }

    @Editable(order=200, description="Specify content of the config file")
    @Code(language=Code.PLAIN_TEXT, variableProvider="suggestVariables")
    @Interpolative
    @NotEmpty
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}

}
