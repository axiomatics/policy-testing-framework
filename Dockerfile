FROM eclipse-temurin:11-alpine

EXPOSE 8081 8082

WORKDIR /opt/ads/domain

COPY lib ../lib
COPY domain ./

ENTRYPOINT ["java", "-cp", "../lib/*", "com.axiomatics.ads.App"]
CMD ["server", "deployment.yaml"]