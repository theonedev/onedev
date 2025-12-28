package io.onedev.server.ai.responsehandlers;

import java.util.Date;

import io.onedev.server.OneDev;
import io.onedev.server.ai.ResponseHandler;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.User;
import io.onedev.server.service.CodeCommentReplyService;
import io.onedev.server.service.CodeCommentService;

public class AddCodeCommentReply implements ResponseHandler {

    private final Long commentId;

    public AddCodeCommentReply(Long commentId) {
        this.commentId = commentId;
    }

    @Override
    public void onResponse(User ai, String response) {
        var comment = OneDev.getInstance(CodeCommentService.class).load(commentId);
        var reply = new CodeCommentReply();
        reply.setComment(comment);
        reply.setContent(response);
        reply.setUser(ai);
        reply.setDate(new Date());
        reply.setCompareContext(comment.getCompareContext());
        OneDev.getInstance(CodeCommentReplyService.class).create(reply);
    }

}