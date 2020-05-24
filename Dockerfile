FROM openjdk:15
ARG JAR_FILE
EXPOSE 8080

WORKDIR /opt/rukou

#dependencies
COPY target/dependency/* ./

#actual code
COPY ${JAR_FILE} rukou-local.jar

CMD java -cp "*" io.rukou.local.Main