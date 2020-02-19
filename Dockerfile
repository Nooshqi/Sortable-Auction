  
FROM maven:3.6.1-jdk-14

RUN mkdir /app
WORKDIR /app

# Download dependencies once, not every build
COPY pom.xml pom.xml

COPY src src
COPY config.json config.json
COPY input.json input.json
RUN mvn clean compile assembly:single

CMD ["java", "-jar", "target/auctioneer-1.0-SNAPSHOT-jar-with-dependencies.jar"]