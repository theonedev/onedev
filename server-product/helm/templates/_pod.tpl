{{/*
Defines the PodSpec for OneDever Server.
*/}}
{{- define "ods.pod" -}}
serviceAccountName: {{ include "ods.serviceAccountName" . }}
{{- if .Values.podHostNetwork }}
hostNetwork: {{ .Values.podHostNetwork }}
{{- end }}
{{- if .Values.podSecurityContext }}
securityContext: {{ toYaml .Values.podSecurityContext | nindent 2 }}
{{- end }}
{{- if .Values.podPriorityClassName }}
priorityClassName: {{ .Values.podPriorityClassName }}
{{- end }}
{{- if .Values.dnsPolicy }}
dnsPolicy: {{ .Values.dnsPolicy }}
{{- end }}
{{- if .Values.dnsConfig }}
dnsConfig: {{ toYaml .Values.dnsConfig | nindent 2 }}
{{- end }}
{{- if .Values.image.pullSecrets }}
imagePullSecrets: {{ toYaml .Values.image.pullSecrets | nindent 2 }}
{{- end }}
{{- if .Values.initContainers }}
initContainers: {{ toYaml .Values.initContainers | nindent 2 }}
{{- end }}
containers:
  - name: onedevserver
{{- if .Values.securityContext }}
    securityContext: {{ toYaml .Values.securityContext | nindent 6 }}
{{- end }}
    image: "{{ .Values.image.repository }}/{{ .Values.image.name }}:{{ .Values.image.tag | default .Chart.AppVersion }}"
    imagePullPolicy: {{ .Values.image.pullPolicy }}
{{- if .Values.command }}
    command: {{ toYaml .Values.command | nindent 6 }}
{{- end }}
{{- if .Values.args }}
    args: {{ toYaml .Values.args | nindent 6 }}
{{- end }}
    env:
    - name: k8s_service
      value: {{ include "ods.fullname" . }}
{{- if .Values.onedev.jvm.maxMemoryPercent }}
    - name: max_memory_percent
      value: "{{ .Values.onedev.jvm.maxMemoryPercent }}"
{{- end }}
{{- if .Values.onedev.initSettings.user }}
    - name: initial_user
      value: {{ .Values.onedev.initSettings.user }}
{{- end }}
{{- if .Values.onedev.initSettings.password }}
    - name: initial_password
      valueFrom:
        secretKeyRef:
          name: {{ include "ods.fullname" . }}-secrets
          key: password
{{- end }}
{{- if .Values.onedev.initSettings.email }}
    - name: initial_email
      value: {{ .Values.onedev.initSettings.email }}
{{- end }}
{{- if .Values.onedev.initSettings.serverUrl }}
    - name: initial_server_url
      value: {{ .Values.onedev.initSettings.serverUrl }}
{{- end }}
{{- if .Values.onedev.initSettings.sshRootUrl }}
    - name: initial_ssh_root_url
      value: {{ .Values.onedev.initSettings.sshRootUrl }}
{{- end }}
{{- if .Values.ingress.enabled }}
    - name: ingress_host
      value: {{ .Values.ingress.host }}
    - name: ingress_tls
      value: "{{ .Values.ingress.tls.enabled }}"
{{- end }}
{{- if .Values.database.external }}
{{- include "setDatabaseEnvVars" . | indent 2 }}
    - name: hibernate_connection_url
      value: {{ include "getConnectionURL" . | quote }}
    - name: hibernate_connection_username
      value: {{ .Values.database.user }}
    - name: hibernate_connection_password
      valueFrom:
        secretKeyRef:
          name: {{ include "ods.fullname" . }}-secrets
          key: dbPassword
    - name: hibernate_hikari_maximumPoolSize
      value: {{ .Values.database.maximumPoolSize | quote }}
{{- end }}

{{- if .Values.env }}
{{ toYaml .Values.env | indent 4 }}
{{- end }}
{{- if .Values.envFrom }}
    envFrom: {{ toYaml .Values.envFrom | nindent 6 }}
{{- end }}
    ports:
    - name: http
      containerPort: 6610
      protocol: TCP
    - name: ssh
      containerPort: 6611
      protocol: TCP
{{- if .Values.livenessProbe }}
    livenessProbe: {{ toYaml .Values.livenessProbe | trim | nindent 6 }}
{{- end }}
{{- if .Values.readinessProbe }}
    readinessProbe: {{ toYaml .Values.readinessProbe | trim | nindent 6 }}
{{- end }}
{{- if .Values.resources }}
    resources: {{ toYaml .Values.resources | nindent 6 }}
{{- end }}
{{- if .Values.lifecycle }}
    lifecycle: {{ toYaml .Values.lifecycle | nindent 6 }}
{{- end }}
    volumeMounts:
    - name: data
      mountPath: "/opt/onedev"
    - name: trust-certs
      mountPath: "/opt/onedev/conf/trust-certs"
{{- if .Values.extraVolumeMounts }}
{{ toYaml .Values.extraVolumeMounts | indent 6 }}
{{- end }}
{{- if .Values.extraContainers }}
{{ toYaml .Values.extraContainers | indent 2 }}
{{- end }}
terminationGracePeriodSeconds: {{ .Values.terminationGracePeriodSeconds }}
{{- if .Values.nodeSelector }}
nodeSelector: {{ toYaml .Values.nodeSelector | nindent 2 }}
{{- end }}
{{- if .Values.affinity }}
affinity: {{ toYaml .Values.affinity | nindent 2 }}
{{- end }}
{{- if .Values.tolerations }}
tolerations: {{ toYaml .Values.tolerations | nindent 2 }}
{{- end }}
{{- if  .Values.topologySpreadConstraints }}
topologySpreadConstraints: {{ toYaml .Values.topologySpreadConstraints | nindent 2 }}
{{- end }}
volumes:
- name: trust-certs
  secret:
    secretName: {{ .Values.onedev.trustCerts.secretName }}
    optional: true
{{- if .Values.extraVolumes }}
{{ toYaml .Values.extraVolumes | indent 2 }}
{{- end }}
{{- end }}
