package io.onedev.server.model.support.role;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.web.util.SuggestionUtils;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Editable
public class PackPrivilege implements Serializable {

	private static final long serialVersionUID = 1L;

	private String packNames;
	
	private boolean writePack;
	
	@Editable(order=100, description="Specify space-separated package names. Use '*' or '?' for wildcard match. "
			+ "Prefix with '-' to exclude")
	@Patterns(suggester = "suggestPackNames")
	@NotEmpty
	public String getPackNames() {
		return packNames;
	}

	public void setPackNames(String packNames) {
		this.packNames = packNames;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestPackNames(String matchWith) {
		List<String> packNames = new ArrayList<>(OneDev.getInstance(PackManager.class).getPackNames(null));
		Collections.sort(packNames);
		return SuggestionUtils.suggest(packNames, matchWith);
	}

	@Editable(order=200, description="Package write permission to add/overwrite package versions")
	public boolean isWritePack() {
		return writePack;
	}

	public void setWritePack(boolean writePack) {
		this.writePack = writePack;
	}
	
}
