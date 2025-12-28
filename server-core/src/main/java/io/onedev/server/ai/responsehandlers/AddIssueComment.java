package io.onedev.server.ai.responsehandlers;

import java.util.Date;

import io.onedev.server.OneDev;
import io.onedev.server.ai.ResponseHandler;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.User;
import io.onedev.server.service.IssueCommentService;
import io.onedev.server.service.IssueService;

public class AddIssueComment implements ResponseHandler {

    private final Long issueId;

    public AddIssueComment(Long issueId) {
        this.issueId = issueId;
    }

    @Override
    public void onResponse(User ai, String response) {
        var issue = OneDev.getInstance(IssueService.class).load(issueId);
        var comment = new IssueComment();
        comment.setIssue(issue);
        comment.setContent(response);
        comment.setUser(ai);
        comment.setDate(new Date());
        OneDev.getInstance(IssueCommentService.class).create(comment);
    }

}