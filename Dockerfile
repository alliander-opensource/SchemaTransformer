from gradle:7.4.2-jdk11

# copy requiered stuff
COPY app /opt/schema-transformer/app
COPY settings.gradle.kts /opt/schema-transformer

# set base directory
WORKDIR /opt/schema-transformer

# compile source code and build dependencies
RUN gradle build

# remove source code, clutter and place exectutable on top
RUN mv /opt/schema-transformer/app/build/libs/app-all.jar /opt/schema-transformer/app.jar
RUN rm -rf app build .gradle settings.gradle.kts
