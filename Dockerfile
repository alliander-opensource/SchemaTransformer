from maven:3.8.4-jdk-11

COPY src /opt/schema-transformer
WORKDIR /opt/schema-transformer
RUN mvn clean install
RUN mvn dependency:copy-dependencies
