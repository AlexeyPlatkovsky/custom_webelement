FROM selenium/standalone-chrome:latest
ARG GRADLE_VERSION=7.3.3
ENV JAVA_VERSION=17
ENV JAVA_URL=https://download.oracle.com/java/${JAVA_VERSION}/archive/${JAVA_VERSION}/binaries/openjdk-${JAVA_VERSION}_linux-x64_bin.tar.gz

# Download and install Gradle
RUN curl -L https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle-${GRADLE_VERSION}-bin.zip &&\
    unzip gradle-${GRADLE_VERSION}-bin.zip -d /opt && \
    rm -f gradle-${GRADLE_VERSION}-bin.zip && \
    sudo chmod +x /opt/gradle-${GRADLE_VERSION}/bin/gradle

# Add Gradle to the PATH
ENV PATH="/opt/gradle-${GRADLE_VERSION}/bin:${PATH}"

# Download and extract the OpenJDK 17 tarball
RUN mkdir -p /opt/java && \
    curl -L -b "oraclelicense=accept-securebackup-cookie" ${JAVA_URL} -o /tmp/openjdk.tar.gz && \
    tar -C /opt/java --strip-components=1 -xzf /tmp/openjdk.tar.gz && \
    rm -f /tmp/openjdk.tar.gz

# Set the required environment variables
ENV JAVA_HOME=/opt/java
ENV PATH=$JAVA_HOME/bin:$PATH

#copy entrypoint script
COPY entrypoint.sh /home/entrypoint.sh
RUN chmod +x /home/entrypoint.sh

ENTRYPOINT ["/home/entrypoint.sh"]