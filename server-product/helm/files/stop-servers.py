"""Quiesce an existing OneDev StatefulSet before Helm changes its image."""
import json
import os
import ssl
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path


def stop_servers(api, namespace, name, image, timeout):
    base = f"/apis/apps/v1/namespaces/{namespace}/statefulsets/{name}"
    try:
        statefulset = api("GET", base)
    except urllib.error.HTTPError as error:
        if error.code == 404:
            raise RuntimeError(
                f"Refusing to upgrade: StatefulSet {namespace}/{name} was not found; "
                "cannot verify that existing OneDev servers are stopped. Keep the existing "
                "StatefulSet name when upgrading (check global.nameOverride and "
                "global.fullnameOverride).") from error
        raise
    retention = statefulset["spec"].get("persistentVolumeClaimRetentionPolicy", {})
    if retention.get("whenScaled") == "Delete":
        raise RuntimeError(
            "Refusing to upgrade: persistentVolumeClaimRetentionPolicy.whenScaled is Delete; "
            "scaling down could delete OneDev data. Set whenScaled to Retain on the existing "
            "StatefulSet before retrying.")
    strategy = statefulset["spec"].get("updateStrategy", {})
    if strategy.get("rollingUpdate", {}).get("partition", 0) != 0:
        raise RuntimeError(
            "Refusing to upgrade: the existing StatefulSet has a nonzero rolling-update "
            "partition; servers could restart with the old image. Set "
            "spec.updateStrategy.rollingUpdate.partition to 0 on the existing StatefulSet "
            "before retrying.")
    uid = statefulset["metadata"]["uid"]
    selector = urllib.parse.urlencode({"labelSelector": ",".join(
        f"{key}={value}" for key, value in statefulset["spec"]["selector"]["matchLabels"].items())})
    pods_path = f"/api/v1/namespaces/{namespace}/pods"

    def pods():
        return [pod for pod in api("GET", pods_path + "?" + selector)["items"]
                if any(owner["uid"] == uid for owner in pod["metadata"].get("ownerReferences", []))]

    def server_image(spec):
        return next(container["image"] for container in spec["containers"]
                    if container["name"] == "onedevserver")

    existing = pods()
    if (server_image(statefulset["spec"]["template"]["spec"]) == image
            and all(server_image(pod["spec"]) == image for pod in existing)):
        print("OneDev image is unchanged; no upgrade shutdown required", flush=True)
        return

    print("Stopping all OneDev servers before upgrading the shared database", flush=True)
    api("PATCH", base + "/scale", {"spec": {"replicas": 0}})
    deadline = time.monotonic() + timeout
    deleting = set()
    while True:
        existing = pods()
        current = api("GET", base)
        if current["spec"]["replicas"] != 0:
            raise RuntimeError("StatefulSet was scaled up during upgrade; refusing to proceed")
        if (not existing and current.get("status", {}).get("observedGeneration", 0)
                >= current["metadata"]["generation"]):
            print("All OneDev pods have stopped; Helm can now apply the new image", flush=True)
            return
        # Older StatefulSet controllers can wait for an unready pod during scale-down.
        # Delete only this StatefulSet's pods, respecting their normal grace period.
        for pod in existing:
            metadata = pod["metadata"]
            if metadata["uid"] not in deleting and not metadata.get("deletionTimestamp"):
                try:
                    api("DELETE", pods_path + "/" + metadata["name"],
                        {"apiVersion": "v1", "kind": "DeleteOptions",
                         "preconditions": {"uid": metadata["uid"]}})
                except urllib.error.HTTPError as error:
                    if error.code != 404:
                        raise
                deleting.add(metadata["uid"])
        if time.monotonic() >= deadline:
            raise TimeoutError("OneDev pods did not stop; upgrade aborted with replicas left at zero")
        time.sleep(2)


def main():
    credentials = Path("/var/run/secrets/kubernetes.io/serviceaccount")
    context = ssl.create_default_context(cafile=str(credentials / "ca.crt"))
    host = os.environ["KUBERNETES_SERVICE_HOST"]
    if ":" in host:
        host = f"[{host}]"
    endpoint = f"https://{host}:{os.environ['KUBERNETES_SERVICE_PORT']}"

    def api(method, path, body=None):
        request = urllib.request.Request(endpoint + path, method=method,
            data=json.dumps(body).encode() if body is not None else None,
            headers={"Authorization": "Bearer " + (credentials / "token").read_text().strip(),
                     "Content-Type": "application/merge-patch+json" if method == "PATCH" else "application/json"})
        with urllib.request.urlopen(request, context=context, timeout=15) as response:
            return json.load(response)

    stop_servers(api, os.environ["POD_NAMESPACE"], os.environ["STATEFULSET_NAME"],
                 os.environ["TARGET_IMAGE"], int(os.environ["STOP_TIMEOUT"]))


if __name__ == "__main__":
    main()
