#docker run -it --rm -v /tmp/.X11-unix:/tmp/.X11-unix:ro -v $XAUTHORITY:/root/.Xauthority -e DISPLAY=unix$DISPLAY --net=host --name hub misalcedo/rapbot:hub-1.0

FROM openjdk:jdk-alpine

RUN echo -e '@edgunity http://nl.alpinelinux.org/alpine/edge/community\n\
@edge http://nl.alpinelinux.org/alpine/edge/main\n\
@testing http://nl.alpinelinux.org/alpine/edge/testing\n\
@community http://dl-cdn.alpinelinux.org/alpine/edge/community'\
  >> /etc/apk/repositories
RUN apk --update upgrade && apk add --update --no-cache \
      build-base openblas-dev unzip wget cmake \
      #Intel® TBB, a widely used C++ template library for task parallelism'
      libtbb@testing libtbb-dev@testing \
      # Wrapper for libjpeg-turbo
      libjpeg  \
      # accelerated baseline JPEG compression and decompression library
      libjpeg-turbo-dev \
      # Portable Network Graphics library
      libpng-dev \
      # A software-based implementation of the codec specified in the emerging JPEG-2000 Part-1 standard (development files)
      jasper-dev \
      # Provides support for the Tag Image File Format or TIFF (development files)
      tiff-dev \
      # Libraries for working with WebP images (development files)
      libwebp-dev \
      # A C language family front-end for LLVM (development files)
      clang-dev \
      linux-headers \
      # Necessary to build OpenCV Java
      apache-ant python

ENV CC /usr/bin/clang
ENV CXX /usr/bin/clang++

ENV OPENCV_VERSION=3.3.0

RUN mkdir /opt && cd /opt && \
  wget -O opencv.zip https://github.com/opencv/opencv/archive/${OPENCV_VERSION}.zip && \
  wget -O opencv_contrib.zip https://github.com/opencv/opencv_contrib/archive/${OPENCV_VERSION}.zip && \
  unzip opencv.zip && unzip opencv_contrib.zip && rm -rf opencv*.zip

RUN mkdir -p /opt/opencv-${OPENCV_VERSION}/build && \
  cd /opt/opencv-${OPENCV_VERSION}/build && \
  cmake \
  -D BUILD_SHARED_LIBS=OFF \
  -D CMAKE_BUILD_TYPE=RELEASE \
  -D CMAKE_INSTALL_PREFIX=/usr/local \
  -D WITH_TBB=YES \
  -D OPENCV_EXTRA_MODULES_PATH=/opt/opencv_contrib-${OPENCV_VERSION}/modules \
  .. && \
  make -j12 && \
  make install

# Install application-level libraries
RUN apk add vlc libx11
COPY . /staging
WORKDIR /staging
RUN rm src/main/resources/opencv-330.jar && \
    ln -s /opt/opencv-3.3.0/build/bin/opencv-330.jar src/main/resources/opencv-330.jar && \
    sh gradlew build

# Hub
WORKDIR /usr/src/app
RUN mv /staging/build/distributions/Hub-*.tar /usr/src/app.tar && tar -xf /usr/src/app.tar --strip-components 1 && rm /usr/src/app.tar
ENV HUB_OPTS '-Djava.library.path=/opt/opencv-3.3.0/build/lib'
CMD ["bin/Hub"]