#!/bin/bash

source /home/ubuntu/env_vars.sh

ROOT_PATH="/home/ubuntu/spring-github-action"
JAR="$ROOT_PATH/inconcert-0.0.1-SNAPSHOT.jar"

APP_LOG="$ROOT_PATH/application.log"
ERROR_LOG="$ROOT_PATH/error.log"
START_LOG="$ROOT_PATH/start.log"

NOW=$(date +%c)

echo "현재 사용자: $(whoami)" >> $START_LOG
echo "secretKey: $(secretKey)" >> $START_LOG

echo "[$NOW] $JAR 복사" >> $START_LOG
cp $ROOT_PATH/build/libs/inconcert-0.0.1-SNAPSHOT.jar $JAR

echo "[$NOW] > $JAR 실행" >> $START_LOG
nohup java -jar $JAR > $APP_LOG 2> $ERROR_LOG &

SERVICE_PID=$(pgrep -f $JAR)
echo "[$NOW] > 서비스 PID: $SERVICE_PID" >> $START_LOG
