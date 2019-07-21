FROM openjdk:8-jdk
COPY target/extracted/onedev /app
RUN cd /usr/local/bin && curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.15.0/bin/linux/amd64/kubectl && chmod +x kubectl
EXPOSE 6610
CMD ["sh", "-c", "/app/bin/upgrade.sh /opt/onedev && /opt/onedev/bin/server.sh console"]
