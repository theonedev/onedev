FROM ubuntu:18.04
COPY onedev /app
COPY entrypoint.sh idle.sh /root/bin/
COPY kubectl /usr/local/bin/
RUN apt-get update && apt-get install -y iproute2 curl git openjdk-8-jre-headless 
EXPOSE 6610
CMD ["/root/bin/entrypoint.sh"]
