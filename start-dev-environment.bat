@echo off
echo Starting development environment with Docker Compose...

REM Make sure Docker is running
docker info >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Docker is not running. Please start Docker Desktop and try again.
    exit /b 1
)

REM Start the containers
echo Starting services...
docker-compose up -d

REM Check if containers started successfully
echo.
echo Checking status of containers...
docker-compose ps

REM Check if any containers failed to start
docker-compose ps -q | find /v /c "" > temp.txt
set /p CONTAINER_COUNT=<temp.txt
del temp.txt

docker-compose ps --filter status=running -q | find /v /c "" > temp.txt
set /p RUNNING_COUNT=<temp.txt
del temp.txt

if %CONTAINER_COUNT% NEQ %RUNNING_COUNT% (
    echo.
    echo WARNING: Not all containers are running. Check the logs for errors:
    echo docker-compose logs
    echo.
)

echo.
echo Services available at:
echo  - Redis: localhost:6379
echo  - Kafka: localhost:9092
echo  - Zookeeper: localhost:2181
echo  - Postgres: localhost:5432
echo  - Elasticsearch: localhost:9200
echo.
echo To view logs: docker-compose logs -f
echo To stop: docker-compose down or run stop-dev-environment.bat
echo. 