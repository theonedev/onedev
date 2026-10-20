package io.onedev.server.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.CodeCommentReplyService;
import io.onedev.server.service.CodeCommentStatusChangeService;
import io.onedev.server.service.UrlService;

public class CodeCommentHelper {
    
    public static Map<String, Object> addReply(Subject subject, CodeComment comment, String content) {
        var user = SecurityUtils.getUser(subject);
        if (user == null)
            throw new UnauthenticatedException();

        if (!SecurityUtils.canReadCode(subject, comment.getProject()))
            throw new UnauthorizedException();

        var reply = new CodeCommentReply();
        reply.setComment(comment);
        reply.setContent(content);
        reply.setUser(user);
        reply.setDate(new Date());
        reply.setCompareContext(comment.getCompareContext());
        getCodeCommentReplyService().create(reply);

        return Map.of("id", reply.getId(), "link", getUrlService().urlFor(reply, true));
    }

    public static Map<String, Object> changeStatus(Subject subject, CodeComment comment, 
                boolean resolved, @Nullable String note) {
        if (!SecurityUtils.canChangeStatus(subject, comment))
            throw new UnauthorizedException();
            
        if (comment.isResolved() != resolved) {
            var statusChange = new CodeCommentStatusChange();
            statusChange.setComment(comment);
            statusChange.setUser(SecurityUtils.getUser(subject));
            statusChange.setResolved(resolved);
            statusChange.setCompareContext(comment.getCompareContext());
            getCodeCommentStatusChangeService().create(statusChange, note);    
        }

        return getIdentity(comment);
    }

    public static Map<String, Object> getIdentity(CodeComment comment) {
        return Map.of("id", comment.getId(), "link", getUrlService().urlFor(comment, true));
    }

    public static Map<String, Object> getDetail(CodeComment comment) {        
        var typeReference = new TypeReference<LinkedHashMap<String, Object>>() {};
        var data = getObjectMapper().convertValue(comment, typeReference);
        data.remove("uuid");
        data.remove("userId");
        data.put("user", comment.getUser().getName());
        data.put("project", comment.getProject().getPath());
        data.put("filePath", comment.getMark().getPath());
        data.put("fromLineNumber", comment.getMark().getRange().getFromRow() + 1);
        data.put("toLineNumber", comment.getMark().getRange().getToRow() + 1);
        data.remove("lastActivity");
        data.remove("replyCount");
        data.remove("compareContext");
        data.remove("mark");
        return data;
    }

    public static List<Map<String, Object>> getReplies(CodeComment comment) {
        var replies = new ArrayList<Map<String, Object>>();
        comment.getReplies().stream().sorted(Comparator.comparing(CodeCommentReply::getId)).forEach(reply -> {
            replies.add(getDetail(reply));
        });
        return replies;
    }

    public static Map<String, Object> getDetail(CodeCommentReply reply) {
        var replyMap = new HashMap<String, Object>();
        replyMap.put("user", reply.getUser().getName());
        replyMap.put("date", reply.getDate());
        replyMap.put("content", reply.getContent());
        return replyMap;
    }

    private static ObjectMapper getObjectMapper() {
        return OneDev.getInstance(ObjectMapper.class);
    }

    private static CodeCommentReplyService getCodeCommentReplyService() {
        return OneDev.getInstance(CodeCommentReplyService.class);
    }

    private static CodeCommentStatusChangeService getCodeCommentStatusChangeService() {
        return OneDev.getInstance(CodeCommentStatusChangeService.class);
    }

    private static UrlService getUrlService() {
        return OneDev.getInstance(UrlService.class);
    }
    
}
