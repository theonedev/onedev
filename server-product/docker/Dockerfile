# Build the docker image for onedev server
FROM ubuntu:18.04
COPY app /app
COPY entrypoint.sh idle.sh /root/bin/
COPY kubectl /usr/local/bin/
RUN apt-get update && apt-get install -y locales docker.io iproute2 curl git openjdk-8-jre-headless 
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
    locale-gen
ENV LANG en_US.UTF-8  
ENV LANGUAGE en_US:en  
ENV LC_ALL en_US.UTF-8 
EXPOSE 6610
CMD ["/root/bin/entrypoint.sh"]
