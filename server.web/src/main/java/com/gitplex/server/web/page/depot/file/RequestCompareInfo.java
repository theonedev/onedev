package com.gitplex.server.web.page.depot.file;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.gitplex.commons.lang.diff.WhitespaceOption;
import com.gitplex.server.core.entity.support.CommentPos;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.changes.RequestChangesPage;

public class RequestCompareInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	public Long requestId;
	
	public RequestChangesPage.State compareState;
	
	@Override
	public String toString() {
		List<String> fields = new ArrayList<>();
		fields.add(requestId.toString());
		fields.add(encode(compareState.anchor));
		fields.add(encode(compareState.blameFile));
		fields.add(encode(compareState.newCommit));
		fields.add(encode(compareState.oldCommit));
		fields.add(encode(compareState.pathFilter));
		if (compareState.commentId != null)
			fields.add(encode(compareState.commentId.toString()));
		else
			fields.add("");
		if (compareState.mark != null)
			fields.add(encode(compareState.mark.toString()));
		else
			fields.add("");
		if (compareState.whitespaceOption != null)
			fields.add(encode(compareState.whitespaceOption.name()));
		else
			fields.add("");
		return Joiner.on("/").join(fields);
	}

	private static String encode(String value) {
		if (value != null) {
			try {
				return URLEncoder.encode(value, Charsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		} else {
			return "";
		}
	}
	
	private static String decode(String value) {
		if (value.length() != 0) {
			try {
				return URLDecoder.decode(value, Charsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}
	
	public static RequestCompareInfo fromString(@Nullable String str) {
		if (str != null) {
			List<String> fields = Splitter.on('/').splitToList(str);
			RequestCompareInfo compareInfo = new RequestCompareInfo();
			compareInfo.requestId = Long.valueOf(fields.get(0));
			compareInfo.compareState = new RequestChangesPage.State();
			compareInfo.compareState.anchor = decode(fields.get(1));
			compareInfo.compareState.blameFile = decode(fields.get(2));
			compareInfo.compareState.newCommit = decode(fields.get(3));
			compareInfo.compareState.oldCommit = decode(fields.get(4));
			compareInfo.compareState.pathFilter = decode(fields.get(5));
			String value = decode(fields.get(6));
			if (value != null)
				compareInfo.compareState.commentId = Long.valueOf(value);
			compareInfo.compareState.mark = CommentPos.fromString(decode(fields.get(7)));
			compareInfo.compareState.whitespaceOption = WhitespaceOption.ofNullableName(decode(fields.get(8)));
			
			return compareInfo;
		} else {
			return null;
		}
	}

}
