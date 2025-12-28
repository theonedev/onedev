package io.onedev.server.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;

public class CodeCommentHelper {
    
    private static ObjectMapper getObjectMapper() {
        return OneDev.getInstance(ObjectMapper.class);
    }

    public static Map<String, Object> getDetail(CodeComment comment) {        
        var typeReference = new TypeReference<LinkedHashMap<String, Object>>() {};
        var summary = getObjectMapper().convertValue(comment, typeReference);
        summary.remove("id");
        summary.remove("uuid");
        summary.remove("userId");
        summary.put("user", comment.getUser().getName());
        summary.put("project", comment.getProject().getPath());
        summary.put("filePath", comment.getMark().getPath());
        summary.put("fromLineNumber", comment.getMark().getRange().getFromRow() + 1);
        summary.put("toLineNumber", comment.getMark().getRange().getToRow() + 1);
        summary.remove("lastActivity");
        summary.remove("replyCount");
        summary.remove("compareContext");
        summary.remove("mark");
        return summary;
    }

    public static List<Map<String, Object>> getReplies(CodeComment comment) {
        var replies = new ArrayList<Map<String, Object>>();
        comment.getReplies().stream().sorted(Comparator.comparing(CodeCommentReply::getId)).forEach(reply -> {
            var replyMap = new HashMap<String, Object>();
            replyMap.put("user", reply.getUser().getName());
            replyMap.put("date", reply.getDate());
            replyMap.put("content", reply.getContent());
            replies.add(replyMap);
        });
        return replies;
    }

}
