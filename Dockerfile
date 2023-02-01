# The tag is in the format jvmversion_sbtversion_scalaversion
# The jvmversion has an underscore in it, which makes this confusing
FROM sbtscala/scala-sbt:eclipse-temurin-focal-17.0.5_8_1.8.2_2.13.10

RUN apt-get update && \
  apt-get install -y default-mysql-client
