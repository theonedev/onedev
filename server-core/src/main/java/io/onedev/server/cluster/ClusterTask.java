package io.onedev.server.cluster;

import java.io.Serializable;
import java.util.concurrent.Callable;

public interface ClusterTask<T> extends Callable<T>, Serializable {

}
