# Build the docker image with necessary tools to build onedev server docker image
FROM ubuntu:18.04
# Install maven in a separate command as otherwise its dependency OpenJDK 11 will be installed
RUN apt-get update && apt-get install -y git openjdk-8-jdk-headless && apt-get install -y maven
