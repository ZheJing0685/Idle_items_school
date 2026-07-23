FROM alpine:3.21

RUN apk add --no-cache tzdata openjdk17-jre nginx redis supervisor && \
    mkdir -p /var/log/supervisor /app/uploads

COPY backend/target/school-1.0.0.jar /app/app.jar
COPY frontend/dist /usr/share/nginx/html
RUN rm -f /etc/nginx/conf.d/default.conf
COPY deploy/nginx.conf /etc/nginx/http.d/default.conf
COPY deploy/supervisord.conf /etc/supervisord.conf

WORKDIR /app
ENV TZ=Asia/Shanghai

EXPOSE 80 7000 6379

CMD ["supervisord", "-c", "/etc/supervisord.conf"]
