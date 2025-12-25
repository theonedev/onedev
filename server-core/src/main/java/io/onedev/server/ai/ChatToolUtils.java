package io.onedev.server.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Project;
import io.onedev.server.util.IgnoreLinearRangeMixin;
import io.onedev.server.util.IgnorePlanarRangeMixin;

public class ChatToolUtils {

    public static String convertToJson(Object data) {
        try {
            var mapper = OneDev.getInstance(ObjectMapper.class).copy();
            mapper.addMixIn(PlanarRange.class, IgnorePlanarRangeMixin.class);
            mapper.addMixIn(LinearRange.class, IgnoreLinearRangeMixin.class);
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

	public static String getFilesAndFolders(Project project, ObjectId commitId, @Nullable String path) {
        var gitService = OneDev.getInstance(GitService.class);
		if (path != null) {
			var blobIdent = gitService.getBlobIdent(project, commitId, path);
			if (blobIdent == null) 
				return convertToJson(Map.of("successful", false, "failReason", "Folder not found"));
			if (!blobIdent.isTree())
				return convertToJson(Map.of("successful", false, "failReason", "Not a folder"));
			path = StringUtils.strip(path.replace('\\', '/'), "/");
		}			
		List<BlobIdent> children = gitService.getChildren(project, commitId,
			path, BlobIdentFilter.ALL, false);
		var filesAndFolders = new ArrayList<Map<String, Object>>();
		for (var child : children) {
			var fileAndFolder = new HashMap<String, Object>();
			fileAndFolder.put("name", child.getName());
			fileAndFolder.put("type", child.isTree() ? "folder" : "file");
			filesAndFolders.add(fileAndFolder);
		}
		return convertToJson(Map.of("successful", true, "filesAndFolders", filesAndFolders));
	}

}