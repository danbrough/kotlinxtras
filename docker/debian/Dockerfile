FROM debian:bullseye-slim
ARG TARGETPLATFORM
ARG BUILDPLATFORM

RUN echo "GENERATED at `date` running on $BUILDPLATFORM, building for $TARGETPLATFORM" > /.info
RUN apt-get  -y update
RUN apt -y upgrade

#wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo tee /usr/share/keyrings/adoptium.asc

#RUN apt -y install gccgo
RUN apt install -y build-essential
#RUN apt install -y default-jdk-headless
#RUN apt install -y gcc-arm-linux-gnueabihf gcc-aarch64-linux-gnu
RUN apt install -y procps git nano  unzip sudo wget file apt-transport-https
RUN apt install -y autoconf make pkg-config cmake libtool
#RUN APT install -y mingw-w64/stable libssl-dev



#RUN wget -q https://dl.google.com/android/repository/android-ndk-r23b-linux.zip -O ndk.zip &&\
#    unzip ndk.zip -d /opt/ && mv /opt/android* /opt/ndk && rm ndk.zip


RUN useradd -s /bin/bash bob -d /src -G sudo,users
RUN echo 'PS1="\[\033[01;31m\]\u@\h\[\033[01;32m\] \w \$\[\033[00m\] "' >> /root/.bashrc
#
#COPY root /
#RUN apt-get  -y update
#RUN /setup.sh 2
RUN apt install -y yarn   net-tools iproute2 iputils-ping netcat upx
#RUN apt install -y curl libcurl4-openssl-dev

RUN wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo tee /usr/share/keyrings/adoptium.asc

RUN echo "deb [signed-by=/usr/share/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
RUN apt update

RUN apt install -y temurin-11-jdk



#RUN apt install -y mingw-w64 rsync
#RUN wget -q https://go.dev/dl/go1.17.7.linux-amd64.tar.gz -O - | tar -xz -C /opt/

#COPY root /
#RUN /setup.sh

#ENTRYPOINT /setup.sh


