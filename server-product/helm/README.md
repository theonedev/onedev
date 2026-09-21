# OneDev Helm Chart

Git Server with CI/CD, Kanban, and Packages

### Homepage
https://onedev.io

### Documentation 
https://docs.onedev.io/installation-guide/deploy-to-k8s

### Chart Values
https://code.onedev.io/onedev/server/~files/main/server-product/helm/values.yaml

### Upgrading

Follow the [Kubernetes upgrade guide](https://docs.onedev.io/upgrade-guide/deploy-to-k8s)
and use a chart version containing the upgrade hook.

Image upgrades require downtime. The hook shuts down all OneDev pods in this
release before Helm applies the new image, then Helm restores `onedev.replicas`.
Unchanged images skip shutdown. Shut down any other servers sharing the database
separately, and keep PVC retention on scale-down set to `Retain` (the default).
Keep the StatefulSet name unchanged and any rolling-update partition at zero.

Configure the hook under `onedev.upgrade` in [values.yaml](values.yaml):

- `stopAllServers`: defaults to `true`; disable only when external tooling stops the servers.
- `stopTimeoutSeconds`: wait for servers to stop, default 240 seconds. Also allow enough time with Helm's `--timeout`.
- `image`: override the hook image when using a private registry.
- `podLabels` / `podAnnotations`: configure the hook pod separately; disable sidecar injection here when needed.

If the hook fails, inspect its Job logs, resolve the cause, and retry. The
StatefulSet may remain scaled to zero. Rolling back the image does not undo
database migrations.
