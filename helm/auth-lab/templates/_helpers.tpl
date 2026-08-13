{{- define "auth-lab.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "auth-lab.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- include "auth-lab.name" . }}
{{- end }}
{{- end }}

{{- define "auth-lab.labels" -}}
app.kubernetes.io/name: {{ include "auth-lab.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "auth-lab.credentialsSecretName" -}}
{{- if .Values.database.credentials.existingSecret }}
{{- .Values.database.credentials.existingSecret }}
{{- else }}
{{- printf "%s-db-credentials" (include "auth-lab.fullname" .) }}
{{- end }}
{{- end }}
