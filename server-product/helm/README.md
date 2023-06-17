# OneDev Helm Chart

All-In-One DevOps Platform

**Homepage:** <https://onedev.io>

**Documentation** [https://docs.onedev.io/](https://docs.onedev.io/installation-guide/deploy-into-k8s)

## Chart Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| affinity | object | `{}` | Configure [affinity and anti-affinity](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#affinity-and-anti-affinity). |
| args | list | `[]` | Override default image arguments. |
| command | list | `[]` | Override default image command. |
| database.dbHost | string | `""` | IP address or hostname of database |
| database.dbMaximumPoolSize | string | `"25"` | Database maximum pool size |
| database.dbName | string | `"onedev"` | Name of the database |
| database.dbPassword | string | `"changeme"` | Database password  |
| database.dbPort | string | `"3306"` | Port Number |
| database.dbType | string | `"mysql"` | Required: Set type of external database. Possible values `mysql`, `mariadb`, `postgresql`, `mssql`, `oracle` [external databases](https://docs.onedev.io/installation-guide/run-as-docker-container#use-external-database) |
| database.dbUser | string | `"onedev"` | User with access to database |
| database.external | bool | `false` | Set to **true** when using external database |
| dnsConfig | object | `{}` | Specify the [dnsConfig](https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/#pod-dns-config). |
| dnsPolicy | string | `"ClusterFirst"` | Specify the [dnsPolicy](https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/#pod-s-dns-policy). |
| env | list | `[]` | Define additional environment variables. |
| envFrom | list | `[]` | Define environment variables from ConfigMap or Secret data. |
| extraContainers | list | `[]` | Specify extra Containers to be added. |
| extraVolumeMounts | list | `[]` | Specify Additional VolumeMounts to use. |
| extraVolumes | list | `[]` | Specify additional Volumes to use. |
| global.commonLabels | object | `{}` | To apply labels to all resources. |
| global.fullnameOverride | string | `""` | Override the fully qualified app name. |
| global.nameOverride | string | `""` | Override the name of the app. |
| image.name | string | `"1dev/server"` | Specify the image name to use (relative to `image.repository`). |
| image.pullPolicy | string | `"IfNotPresent"` | Specify the [pullPolicy](https://kubernetes.io/docs/concepts/containers/images/#image-pull-policy). |
| image.pullSecrets | list | `[]` | Specify the [imagePullSecrets](https://kubernetes.io/docs/concepts/containers/images/#specifying-imagepullsecrets-on-a-pod). |
| image.repository | string | `"docker.io"` | Specify the image repository to use. |
| image.tag | string | `"latest"` | Specify the image tag to use. |
| ingress.annotations | object | `{}` | Specify annotations for the Ingress. |
| ingress.className | string | `""` | Specify the [ingressClassName](https://kubernetes.io/blog/2020/04/02/improvements-to-the-ingress-api-in-kubernetes-1.18/#specifying-the-class-of-an-ingress), requires Kubernetes >= 1.18. |
| ingress.enabled | bool | `false` | If **true**, create an Ingress resource. |
| ingress.hosts.host | string | `"onedev.example.com"` | Set the host name |
| ingress.hosts.paths.path | string | `"/"` |  |
| ingress.hosts.paths.pathType | string | `"ImplementationSpecific"` |  |
| ingress.hosts.paths.port.name | string | `"http"` | Specify the port name |
| ingress.hosts.paths.port.number | string | `""` | Specify the port number |
| ingress.tls | list | `[]` | Configure TLS for the Ingress. |
| initContainers | list | `[]` | Specify initContainers to be added. |
| lifecycle | object | `{}` | Specify lifecycle hooks for Containers. |
| livenessProbe | object | `{}` | Specify the livenessProbe [configuration](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/#configure-probes). |
| nodeSelector | object | `{}` | Configure [nodeSelector](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#nodeselector). |
| onedev.enableSSL | bool | `false` | This configuration determines whether SSL should be enabled on the server. |
| onedev.initSettings.email | string | `"abc@example.com"` | admin Email address |
| onedev.initSettings.enabled | bool | `true` | Enables initial settings for deployment |
| onedev.initSettings.password | string | `"password"` | admin Password, if not specified a random password will be generated automatically |
| onedev.initSettings.serverUrl | string | `""` | Server url will be ignored if ingress.host is specified |
| onedev.initSettings.sshRootUrl | string | `""` |  |
| onedev.initSettings.user | string | `"admin"` | Admin username |
| onedev.maintenance | bool | `false` | Set this as **true** to stop OneDev server and perform various maintenance tasks such as backup, restore. [Check Docs](https://code.onedev.io/onedev/manual/~files/main/pages/backup-restore.md) for more details |
| onedev.trustCerts.enabled | bool | `false` | Set to **true** to provide trust certs |
| onedev.trustCerts.existingSecret | string | `""` | Existing secret name containing the cert file |
| onedev.trustCerts.path | string | `""` | Path of cert file on disk |
| onedev.updateStrategy.type | string | `"RollingUpdate"` | valid options for statefulset `RollingUpdate`, `OnDelete` / Deployment: `RollingUpdate`, `Recreate` |
| onedev.useStatefulSet | bool | `false` | Set to true to use a StatefulSet instead of a Deployment |
| persistence.accessModes | string | `"ReadWriteOnce"` | Specify the accessModes for PersistentVolumeClaims. |
| persistence.enabled | bool | `true` | If **true**, create and use PersistentVolumeClaims. |
| persistence.existingClaim | string | `""` | Name of an existing PersistentVolumeClaim to use. |
| persistence.selector | object | `{}` | Specify the selectors for PersistentVolumeClaims. |
| persistence.size | string | `"10Gi"` | Specify the size of PersistentVolumeClaims. |
| persistence.storageClassName | string | `""` | Specify the storageClassName for PersistentVolumeClaims. |
| podAnnotations | object | `{}` | Set annotations on Pods. |
| podHostNetwork | bool | `false` | Enable the hostNetwork option on Pods. |
| podLabels | object | `{}` | Set labels on Pods. |
| podPriorityClassName | string | `""` | Set the [priorityClassName](https://kubernetes.io/docs/concepts/scheduling-eviction/pod-priority-preemption/#priorityclass). |
| podSecurityContext | object | `{}` | Allows you to overwrite the default [PodSecurityContext](https://kubernetes.io/docs/tasks/configure-pod-container/security-context/). |
| readinessProbe | object | `{}` | Specify the readinessProbe [configuration](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/#configure-probes). |
| resources | object | `{}` | Specify resource requests and limits. |
| securityContext | object | `{}` | Specify securityContext for Containers. |
| service.annotations | object | `{}` | Specify annotations for the Service. |
| service.enabled | bool | `true` | If **true**, create a Service resource. |
| service.externalTrafficPolicy | string | `""` | Specify the [externalTrafficPolicy](https://kubernetes.io/docs/tasks/access-application-cluster/create-external-load-balancer/#preserving-the-client-source-ip). |
| service.ipFamilies | list | `[]` | Configure [IPv4/IPv6 dual-stack](https://kubernetes.io/docs/concepts/services-networking/dual-stack/). |
| service.ipFamilyPolicy | string | `""` | Configure [IPv4/IPv6 dual-stack](https://kubernetes.io/docs/concepts/services-networking/dual-stack/). |
| service.loadBalancerIP | string | `""` | Required: If service type is loadbalancer  [loadBalancerIP](https://kubernetes.io/docs/concepts/services-networking/service/#loadbalancer). |
| service.nodePort | string | `""` | Specify a nodePort for servcie |
| service.ports | object | `{"httpPort":"","sshPort":""}` | Manually change the ServicePorts |
| service.separateSSHService.annotations | object | `{}` |  |
| service.separateSSHService.clusterIP | string | `""` |  |
| service.separateSSHService.enabled | bool | `false` | If separate SSH service is enabled, a separate service is created for SSH |
| service.separateSSHService.externalIPs | list | `[]` |  |
| service.separateSSHService.externalTrafficPolicy | string | `""` |  |
| service.separateSSHService.ipFamilies | list | `[]` |  |
| service.separateSSHService.ipFamilyPolicy | string | `""` |  |
| service.separateSSHService.loadBalancerIP | string | `""` |  |
| service.separateSSHService.loadBalancerSourceRanges | list | `[]` |  |
| service.separateSSHService.nodePort | string | `""` |  |
| service.separateSSHService.port | int | `22` |  |
| service.separateSSHService.topologyKeys | list | `[]` |  |
| service.separateSSHService.type | string | `"ClusterIP"` |  |
| service.topologyKeys | array | `[]` | Specify the [topologyKeys](https://kubernetes.io/docs/concepts/services-networking/service-topology/#using-service-topology). |
| service.type | string | `"ClusterIP"` | Specify the type for the Service. ClusterIP, LoadBalancer |
| serviceAccount.annotations | object | `{}` | Annotations to add to the ServiceAccount, if `serviceAccount.create` is **true**. |
| serviceAccount.create | bool | `true` | If **true**, create a ServiceAccount. |
| serviceAccount.name | string | `"onedev"` | Specify a pre-existing ServiceAccount to use if `serviceAccount.create` is **false**. |
| terminationGracePeriodSeconds | int | `60` | Override terminationGracePeriodSeconds. |
| tolerations | list | `[]` | Configure [taints and tolerations](https://kubernetes.io/docs/concepts/scheduling-eviction/taint-and-toleration/). |
| topologySpreadConstraints | list | `[]` | Configure [topology spread constraints](https://kubernetes.io/docs/concepts/scheduling-eviction/topology-spread-constraints/). |
