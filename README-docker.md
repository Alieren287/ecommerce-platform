# Docker Setup for Ecommerce Platform

This README provides instructions for running the ecommerce platform using Docker.

## Prerequisites

- Docker Desktop for Windows installed and running
- Docker Compose installed (comes with Docker Desktop)

## Included Services

The Docker Compose setup includes the following services:

- **Redis**: In-memory data structure store, used for caching
- **Kafka**: Event streaming platform for publishing and consuming events
- **Zookeeper**: Required for Kafka coordination
- **PostgreSQL**: Relational database for product data
- **Elasticsearch**: Search engine for product search functionality
- **Ecommerce Product Service**: The Spring Boot application that uses all the above services

## Running the Development Environment

### Starting the Environment

Simply run the `start-dev-environment.bat` script:

```
.\start-dev-environment.bat
```

This will start all the required containers for development.

### Stopping the Environment

To stop all containers, run the `stop-dev-environment.bat` script:

```
.\stop-dev-environment.bat
```

## Service URLs and Ports

- **Redis**: localhost:6379
- **Kafka**: localhost:9092
- **Zookeeper**: localhost:2181
- **PostgreSQL**: localhost:5432
    - Database: ecommerce_products
    - Username: postgres
    - Password: postgres
- **Elasticsearch**: localhost:9200
- **Product Service API**: localhost:8081

## Helpful Docker Commands

- View logs for all services:
  ```
  docker-compose logs -f
  ```

- View logs for a specific service:
  ```
  docker-compose logs -f [service-name]
  ```
  Example: `docker-compose logs -f kafka`

- Access Redis CLI:
  ```
  docker exec -it my-redis redis-cli
  ```

- Restart a service:
  ```
  docker-compose restart [service-name]
  ```

## Configuration Files

- `docker-compose.yml`: Main configuration for all services
- `docker-compose.override.yml`: Development-specific overrides
- `ecommerce-product-service/src/main/resources/application-docker.properties`: Docker-specific application
  configuration

## Notes

- The first startup may take some time as Docker needs to download all required images.
- Data is persisted using Docker volumes for Redis, PostgreSQL, and Elasticsearch. 