FROM openjdk:8-jdk
COPY build /app
COPY entrypoint.sh idle.sh /root/bin/
RUN cd /usr/local/bin && curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.15.0/bin/linux/amd64/kubectl && chmod +x kubectl
EXPOSE 6610
CMD ["/root/bin/entrypoint.sh"]
