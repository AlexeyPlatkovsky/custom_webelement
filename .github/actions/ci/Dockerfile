FROM selenium/standalone-chrome:latest
ARG GRADLE_VERSION=7.3.3

# Download and install Gradle
RUN sudo curl -L https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle-${GRADLE_VERSION}-bin.zip &&\
    sudo unzip gradle-${GRADLE_VERSION}-bin.zip -d /opt && \
    sudo rm -f gradle-${GRADLE_VERSION}-bin.zip && \
    sudo chmod +x /opt/gradle-${GRADLE_VERSION}/bin/gradle

# Add Gradle to the PATH
ENV PATH="/opt/gradle-${GRADLE_VERSION}/bin:${PATH}"

# Download and extract the OpenJDK 17 tarball
RUN sudo apt-get update && \
    sudo apt-get install -y openjdk-17-jdk && \
    sudo apt-get clean && \
    sudo rm -rf /var/lib/apt/lists/* && \
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 && \
    export PATH=$JAVA_HOME/bin:$PATH && \

#copy entrypoint script
COPY entrypoint.sh /home/entrypoint.sh
RUN sudo chmod +x /home/entrypoint.sh

ENTRYPOINT ["/home/entrypoint.sh"]