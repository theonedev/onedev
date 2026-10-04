package io.onedev.server.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.service.impl.DefaultUserService;

class WorkspaceDataKeyTest {

    @TempDir
    Path temp;

    @Test
    void rejectsKeysThatResolveToStorageRootOrParent() {
        var service = new DefaultUserService() {
            @Override
            public File getWorkspaceDataBaseDir(Long userId) {
                return temp.toFile();
            }
        };
        for (var key: List.of(".", "..")) {
            assertThrows(ExplicitException.class, () -> service.getWorkspaceDataDir(1L, key, false));
            assertThrows(ExplicitException.class, () -> service.downloadWorkspaceData(1L, key, "/data", is -> fail()));
            assertThrows(ExplicitException.class, () -> service.uploadWorkspaceData(1L, key, "/data", os -> fail()));
        }
        assertArrayEquals(new String[0], temp.toFile().list());
    }

    @Test
    void encodesOtherKeysAsSingleDirectoryNamesWithoutChangingIdentity() {
        for (var key: List.of("data", "data..backup", "../outside", "/absolute", "..\\outside", "%2e%2e", "a/b")) {
            var encoded = User.encodeWorkspaceDataKey(key);
            assertEquals(temp, temp.resolve(encoded).normalize().getParent());
            assertEquals(key, User.decodeWorkspaceDataKey(encoded));
        }
    }
}
