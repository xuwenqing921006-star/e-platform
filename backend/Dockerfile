FROM eclipse-temurin:17-jre

WORKDIR /app

RUN addgroup --system app && adduser --system --ingroup app app

COPY ruoyi-admin/target/ruoyi-admin.jar /app/app.jar

ENV SERVER_PORT=8003
ENV APP_STORAGE_ROOT=/app/uploads

RUN mkdir -p /app/uploads && chown -R app:app /app

USER app

EXPOSE 8003

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=druid,prod"]
