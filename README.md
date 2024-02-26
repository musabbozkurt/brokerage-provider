### Prerequisites

* `Java 21` should be installed --> `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`
* `Docker` should be installed
* `Maven` should be installed
* `pgAdmin`/`DBeaver` can be installed (Optional)

-----

### How to start the application

* First way
    * Run [./scripts/run.sh](scripts%2Frun.sh) script
* Second way
    * Run `docker-compose up -d` command to start the services
    * Run `mvn clean install` or `mvn clean package`
    * Run `mvn spring-boot:run` or `./mvnw spring-boot:run`

-----

### How to test the application

* Swagger Url: http://localhost:8001/swagger-ui/index.html
* Actuator Url: http://localhost:8001/actuator
* Metric Url: http://localhost:8001/actuator/metrics
* Run `mvn test` command to run all the tests

-----

### `docker-compose` contains the followings

* Kafka UI: http://localhost:9091/
* Zipkin: http://localhost:9411/
* Prometheus: http://localhost:9090/graph
* Grafana: http://localhost:3000/
    * `Email or username: admin`
    * `Password: admin`
* PostgreSQL DB connection details
    * `POSTGRES_USER: postgres`
    * `POSTGRES_PASSWORD: postgres`
    * `Port: 5432`

-----