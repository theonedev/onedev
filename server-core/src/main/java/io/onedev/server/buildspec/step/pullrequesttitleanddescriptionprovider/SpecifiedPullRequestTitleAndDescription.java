package io.onedev.server.buildspec.step.pullrequesttitleanddescriptionprovider;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.model.PullRequest;

@Editable(order=200, name="Use specified title and description")
public class SpecifiedPullRequestTitleAndDescription implements PullRequestTitleAndDescriptionProvider {

    private String title;

    private String description;
    
    @Editable(order=100)
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Editable(order=200)
	@Interpolative(variableSuggester="suggestVariables")
    @Multiline
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Pair<String, String> getTitleAndDescription(PullRequest pullRequest) {
        return ImmutablePair.of(getTitle(), getDescription());
    }

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

}
