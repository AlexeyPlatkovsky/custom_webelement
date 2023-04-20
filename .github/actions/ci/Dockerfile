FROM selenium/standalone-chrome:latest
ARG GRADLE_VERSION=7.3.3
ENV JAVA_VERSION=17
ENV JAVA_URL=https://download.oracle.com/java/${JAVA_VERSION}/archive/${JAVA_VERSION}/binaries/openjdk-${JAVA_VERSION}_linux-x64_bin.tar.gz

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
    sudo rm -rf /var/lib/apt/lists/* \

# Set JAVA_HOME environment variable
ENV JAVA_HOME /usr/lib/jvm/java-17-openjdk-amd64

# Verify the installation
RUN java --version

#copy entrypoint script
COPY entrypoint.sh /home/entrypoint.sh
RUN sudo chmod +x /home/entrypoint.sh

ENTRYPOINT ["/home/entrypoint.sh"]