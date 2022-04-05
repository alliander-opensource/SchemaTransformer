from maven:3.8.4-jdk-11

# copy source code and maven build instructions (pom.xml)
COPY src /opt/schema-transformer/src
COPY pom.xml /opt/schema-transformer

# set entry directory
WORKDIR /opt/schema-transformer

# compile source code and build dependencies
RUN mvn clean install
RUN mvn dependency:copy-dependencies

# remove source code, clutter and place exectutable on top
RUN mv target/* /opt/schema-transformer
RUN rm -rf src pom.xml target
