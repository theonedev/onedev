package io.onedev.server.buildspec.step.pullrequesttitleanddescriptionprovider;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.PullRequest;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.SettingService;

@Editable(order=100, name="Use generated title and description", description="""
        Generate title and description based on branch name and commit messages.
        It is highly recommended to configure AI model in <i>Administration / AI Settings</i> 
        to generate good title and description""")
public class GeneratedPullRequestTitleAndDescription implements PullRequestTitleAndDescriptionProvider {

    @Override
    public Pair<String, String> getTitleAndDescription(PullRequest pullRequest) {        
        var title = pullRequest.generateTitleFromCommits();
        var description = pullRequest.generateDescriptionFromCommits();
        if (title == null || description == null) {
            var liteModel = OneDev.getInstance(SettingService.class).getAiSetting().getLiteModel();
            if (liteModel != null) {
                var titleAndDescription = OneDev.getInstance(PullRequestService.class)
                        .suggestTitleAndDescription(pullRequest, liteModel, title == null, description == null);
                if (title == null)
                    title = titleAndDescription.getLeft();
                if (description == null)
                    description = titleAndDescription.getRight();
            }
        }
        if (title == null) 
            title = pullRequest.generateTitleFromBranch();
        
        return ImmutablePair.of(title, description);
    }

}
