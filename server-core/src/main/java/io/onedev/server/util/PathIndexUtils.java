package io.onedev.server.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;

import io.onedev.server.service.support.PathInfo;

/**
 * Utility for managing a directory whose user-visible paths are stored as
 * files named by an integer index. The mapping from path to index is persisted
 * in a single index file inside the directory.
 *
 * <p>Used by both the run cache storage and the user workspace data storage to
 * avoid touching the filesystem with arbitrary user-supplied path names.
 */
public class PathIndexUtils {

    public static final String INDEX_FILE_NAME = ".path-index";

    private PathIndexUtils() {
    }

    public static File getIndexFile(File dir) {
        return new File(dir, INDEX_FILE_NAME);
    }

    public static boolean exists(File dir) {
        return getIndexFile(dir).exists();
    }

    public static Map<String, Integer> read(File dir) {
        var indexFile = getIndexFile(dir);
        if (!indexFile.exists())
            return new HashMap<>();
        try (var is = new FileInputStream(indexFile)) {
            return SerializationUtils.deserialize(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(File dir, Map<String, Integer> indexes) {
        var indexFile = getIndexFile(dir);
        try (var os = new FileOutputStream(indexFile)) {
            SerializationUtils.serialize((Serializable) indexes, os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int allocate(Map<String, Integer> indexes, String path) {
        Integer existing = indexes.get(path);
        if (existing != null)
            return existing;
        int next = indexes.values().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
        indexes.put(path, next);
        return next;
    }

    /**
     * List the path and size of every indexed path file that exists under the
     * given directory, sorted by path.
     */
    public static List<PathInfo> listIndexedPaths(File dir) {
        var paths = new ArrayList<PathInfo>();
        for (var entry : read(dir).entrySet()) {
            var pathFile = new File(dir, String.valueOf(entry.getValue()));
            if (pathFile.isFile())
                paths.add(new PathInfo(entry.getKey(), pathFile.length()));
        }
        paths.sort(Comparator.comparing(PathInfo::getPath));
        return paths;
    }

}
