package io.onedev.server.workspace;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.support.workspace.spec.UserData;
import io.onedev.server.model.support.workspace.spec.UserDataEntry;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;

class WorkspaceUserDataValidationTest {

    @Test
    void rejectsEmptyResolvedKeysBeforeProvisioning() throws Exception {
        var run = DefaultWorkspaceService.class.getDeclaredMethod("run", Workspace.class);
        run.setAccessible(true);
        for (var key: Arrays.asList(null, "", "@branch@")) {
            var data = new UserData();
            data.setKey(key);
            data.setEntries(List.of(UserDataEntry.of("/home/user/data", null)));
            var spec = new WorkspaceSpec();
            spec.setName("test");
            spec.setRunInContainer(true);
            spec.setImage("image");
            spec.setUserDatas(List.of(data));
            var workspace = new Workspace() {
                @Override
                public WorkspaceSpec getSpec() {
                    return spec;
                }

                @Override
                public String getBranch() {
                    // A workspace without a branch resolves @branch@ to an empty string.
                    return null;
                }

                @Override
                public String getToken() {
                    return fail("Invalid user data must be rejected before provisioning proceeds");
                }
            };

            var exception = assertThrows(InvocationTargetException.class,
                    () -> run.invoke(new DefaultWorkspaceService(), workspace));
            var cause = assertInstanceOf(ExplicitException.class, exception.getCause());
            assertEquals("User data #1: data key must not be empty after interpolation", cause.getMessage());
            assertEquals(key, data.getKey());
        }
    }
}
