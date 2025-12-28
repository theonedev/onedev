package io.onedev.server.ai.responsehandlers;

import java.util.Date;

import io.onedev.server.OneDev;
import io.onedev.server.ai.ResponseHandler;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.User;
import io.onedev.server.service.PullRequestCommentService;
import io.onedev.server.service.PullRequestService;

public class AddPullRequestComment implements ResponseHandler {

    private final Long pullRequestId;

    public AddPullRequestComment(Long pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    @Override
    public void onResponse(User ai, String response) {
        var pullRequest = OneDev.getInstance(PullRequestService.class).load(pullRequestId);
        var comment = new PullRequestComment();
        comment.setRequest(pullRequest);
        comment.setContent(response);
        comment.setUser(ai);
        comment.setDate(new Date());
        OneDev.getInstance(PullRequestCommentService.class).create(comment);
    }

}