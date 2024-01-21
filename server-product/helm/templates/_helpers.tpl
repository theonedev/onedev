{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "ods.name" -}}
{{- default .Chart.Name .Values.global.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate strings at 63 characters because some Kubernetes name fields are limited to this (by the DNS naming spec).
If the release name contains a chart name it will be used as a full name.
*/}}
{{- define "ods.fullname" -}}
{{- if .Values.global.fullnameOverride }}
{{- .Values.global.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.global.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "ods.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}


{{/*
Common template labels.
*/}}
{{- define "ods.template-labels" -}}
app.kubernetes.io/name: {{ include "ods.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Common labels.
*/}}
{{- define "ods.labels" -}}
helm.sh/chart: {{ include "ods.chart" . }}
{{ include "ods.template-labels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Values.image.tag | quote }}
{{- end }}
{{- if .Values.global.commonLabels }}
{{ toYaml .Values.global.commonLabels }}
{{- end }}
{{- end -}}

{{/*
Return the ServiceAccount name
*/}}
{{- define "ods.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- template "ods.fullname" . }}
{{- else }}
{{- .Values.serviceAccount.name }}
{{- end -}}
{{- end -}}

{{/*
Return the target Kubernetes version
*/}}
{{- define "ods.kubeVersion" -}}
    {{- .Capabilities.KubeVersion.Version -}}
{{- end -}}

{{/*
Return the appropriate apiVersion for statefulset.
*/}}
{{- define "ods.statefulset.apiVersion" -}}
{{- if semverCompare "<1.14-0" (include "ods.kubeVersion" .) -}}
{{- print "apps/v1beta1" -}}
{{- else -}}
{{- print "apps/v1" -}}
{{- end -}}
{{- end -}}

{{/*
Generate URL string based on the database type
 */}}
{{- define "getConnectionURL" -}}
{{- $connectionURL := "" -}}
{{- if eq $.Values.database.type "mysql" }}
{{- $connectionURL = printf "jdbc:mysql://%s:%s/%s?serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false" $.Values.database.host $.Values.database.port $.Values.database.name -}}
{{- else if eq $.Values.database.type "postgresql" }}
{{- $connectionURL = printf "jdbc:postgresql://%s:%s/%s" $.Values.database.host $.Values.database.port $.Values.database.name -}}
{{- else if eq $.Values.database.type "mariadb" }}
{{- $connectionURL = printf "jdbc:mariadb://%s:%s/%s" $.Values.database.host $.Values.database.port $.Values.database.name -}}
{{- else if eq $.Values.database.type "mssql" }}
{{- $connectionURL = printf "sqlserver://%s:%s;databaseName=%s" $.Values.database.host $.Values.database.port $.Values.database.name -}}
{{- else -}}
Invalid database type
{{- end -}}
{{- trim $connectionURL -}}
{{- end -}}

{{/* 
Set dilect and driver env variables based database type
 */}}
{{- define "setDatabaseEnvVars" -}}
{{- $dbType := .Values.database.type -}}
{{- $dbTypeMap := dict "mysql" (dict "dialect" "org.hibernate.dialect.MySQL5InnoDBDialect" "driver" "com.mysql.cj.jdbc.Driver")
                     "postgresql" (dict "dialect" "io.onedev.server.persistence.PostgreSQLDialect" "driver" "org.postgresql.Driver")
                     "mariadb" (dict "dialect" "org.hibernate.dialect.MySQL5InnoDBDialect" "driver" "org.mariadb.jdbc.Driver")
                     "mssql" (dict "dialect" "org.hibernate.dialect.SQLServer2012Dialect" "driver" "com.microsoft.sqlserver.jdbc.SQLServerDriver")
   -}}
{{- with index $dbTypeMap $dbType }}
  - name: hibernate_dialect
    value: {{ .dialect | quote }}
  - name: hibernate_connection_driver_class
    value: {{ .driver | quote }}
{{- end }}
{{- end -}}
