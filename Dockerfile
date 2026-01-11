# React Native Android CI/CD Build Image
# Build: docker build -t 43.88.89.25:5000/react-native-android:latest .
# Push: docker push 43.88.89.25:5000/react-native-android:latest

FROM 43.88.89.25:5000/openjdk:17-jdk-slim

# Build Arguments
ARG UID=1000

LABEL maintainer="DevOps Team"
LABEL description="React Native Android Build Environment"

# Create jenkins user
RUN groupadd --gid ${UID} jenkins && \
    useradd --uid ${UID} --gid ${UID} --create-home --shell /bin/bash jenkins

# Install system dependencies
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl wget unzip gnupg2 ruby-full build-essential \
    libssl-dev python3 python3-pip git ca-certificates && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    npm install -g yarn && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Install Fastlane
RUN gem install bundler:2.4.22 fastlane:2.225.0

# Android SDK setup
ENV ANDROID_HOME=/home/jenkins/android-sdk \
    ANDROID_SDK_ROOT=/home/jenkins/android-sdk
ENV PATH="${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools"

# Create SDK directory
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    chown -R jenkins:jenkins ${ANDROID_HOME}

USER jenkins
WORKDIR /home/jenkins

# Download Android SDK
RUN curl -L "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" \
    -o /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d /tmp && \
    mkdir -p ${ANDROID_HOME}/cmdline-tools/latest && \
    mv /tmp/cmdline-tools/* ${ANDROID_HOME}/cmdline-tools/latest/ && \
    rm -rf /tmp/cmdline-tools*

# Accept licenses and install SDK
RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0" && \
    sdkmanager --update

# Install BundleTool
RUN curl -L "https://github.com/google/bundletool/releases/download/1.15.6/bundletool-all-1.15.6.jar" \
    -o /home/jenkins/bundletool.jar

# Workspace
WORKDIR /workspace
ENV GRADLE_USER_HOME=/workspace/.gradle \
    NPM_CONFIG_CACHE=/workspace/.npm

# Verify installation
RUN java -version && node --version && fastlane --version

CMD ["bash"]
