package com.gitplex.server.web.page.depot.blob;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.gitplex.server.model.support.CommentPos;
import com.gitplex.server.util.diff.WhitespaceOption;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.changes.RequestChangesPage;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.integrationpreview.IntegrationPreviewPage;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class RequestCompareInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	public Long requestId;
	
	public RequestChangesPage.State compareState;
	
	public IntegrationPreviewPage.State previewState;
	
	@Override
	public String toString() {
		List<String> fields = new ArrayList<>();
		fields.add(requestId.toString());
		if (compareState != null) {
			fields.add("compare");
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
		} else {
			fields.add("preview");
			fields.add(encode(previewState.pathFilter));
			if (previewState.whitespaceOption != null)
				fields.add(encode(previewState.whitespaceOption.name()));
			else
				fields.add("");
			fields.add(encode(previewState.blameFile));
		} 
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
			if (fields.get(1).equals("compare")) {
				compareInfo.compareState = new RequestChangesPage.State();
				compareInfo.compareState.anchor = decode(fields.get(2));
				compareInfo.compareState.blameFile = decode(fields.get(3));
				compareInfo.compareState.newCommit = decode(fields.get(4));
				compareInfo.compareState.oldCommit = decode(fields.get(5));
				compareInfo.compareState.pathFilter = decode(fields.get(6));
				String value = decode(fields.get(7));
				if (value != null)
					compareInfo.compareState.commentId = Long.valueOf(value);
				compareInfo.compareState.mark = CommentPos.fromString(decode(fields.get(8)));
				compareInfo.compareState.whitespaceOption = WhitespaceOption.ofNullableName(decode(fields.get(9)));
			} else {
				compareInfo.previewState = new IntegrationPreviewPage.State();
				compareInfo.previewState.pathFilter = decode(fields.get(2));
				compareInfo.previewState.whitespaceOption = WhitespaceOption.ofNullableName(decode(fields.get(3)));
				compareInfo.previewState.blameFile = decode(fields.get(4));
				return compareInfo;
			}
			return compareInfo;
		} else {
			return null;
		}
	}

}
