FROM danbrough/debian:latest


#RUN apt install -y binutils-mingw-w64-x86-64

RUN wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo tee /usr/share/keyrings/adoptium.asc

RUN echo "deb [signed-by=/usr/share/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
RUN apt update

RUN apt install -y temurin-11-jdk
RUN sed -i  /etc/sudoers -e  's:ALL$:NOPASSWD\:ALL:g'


COPY root /
RUN /setup.sh