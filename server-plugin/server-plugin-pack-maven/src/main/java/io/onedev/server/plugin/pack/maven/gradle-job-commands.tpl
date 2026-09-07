cat << EOF > gradle.properties
# gradle:job-token-notice
onedevUsername=@job_token@
# gradle:access-token-notice
onedevPassword=@secret:access-token@
EOF

gradle ${permission.equals("write")? "publish": "build"}
