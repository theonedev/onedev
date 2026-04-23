package io.onedev.server.buildspec.step.pullrequesttitleanddescriptionprovider;

import java.io.Serializable;

import org.apache.commons.lang3.tuple.Pair;

import io.onedev.server.annotation.Editable;
import io.onedev.server.model.PullRequest;

@Editable
public interface PullRequestTitleAndDescriptionProvider extends Serializable {

	Pair<String, String> getTitleAndDescription(PullRequest pullRequest);

}