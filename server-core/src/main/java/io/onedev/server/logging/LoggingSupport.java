package io.onedev.server.logging;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import org.jspecify.annotations.Nullable;

import io.onedev.server.buildspec.job.log.instruction.LogInstruction;
import io.onedev.server.cluster.ClusterTask;

public interface LoggingSupport extends Serializable {

    LoggingIdentity getIdentity();

    Collection<String> getMaskSecrets();

    String getChangeObservable();

    Collection<LogInstruction> getInstructions();

    <T> T runOnActiveServer(ClusterTask<T> task);

    void fileModified();

    @Nullable
    Date getEffectiveDate();
    
}
