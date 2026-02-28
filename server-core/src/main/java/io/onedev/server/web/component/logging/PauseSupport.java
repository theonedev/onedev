package io.onedev.server.web.component.logging;

public interface PauseSupport {

    boolean isPaused();

    boolean canResume();

    void resume();

    String getStatusChangeObservable();

}