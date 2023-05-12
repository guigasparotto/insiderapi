# insiderapi

This is a Java practice project. The goal is to create an API that receives requests for stocks and date ranges, and then returns all transactions made by insiders of those specific companies during the period.

To achieve that, data is retrieved from the unnoficial [Yahoo Finance API](https://rapidapi.com/apidojo/api/yahoo-finance1)

## Implementation

On a high level, what the applications currently does is the following:
* Receives the API request with symbol, region and date range specified as parameters
* Checks the database for the date of the latest update for that symbol and region
* If the date is older than the end date required by the initial request, it sends a request to the external API
* If not, it retrieves and returns the date currently available
* If fetching external data is required, it does that, then filters the data based on the date range requested before returning it

It may seem redundant to retrieve the data from external API and returning it with minor changes - the biggest benefit at the moment is to avoid external calls when the data is already available in the database. But other features will be added as the project expands.
The goal, at the moment, is to avoid larger frameworks, such as Spring, to focus on core Java implementation.

## Known Issues

* At the moment, when fetching the data for a new date, all records are inserted in the database, rather than just the new ones, this causes duplication
* Missing parameters in the request cause an exception

## Libraries and Tools

* JPA and Hibernate for persistence
* Java HttpClient for the requests
* PostgreSQL as the database
* Flyway for database migration
* Jackson for deserialisation
* Log4j2 for logging
* JUnit5 for testing
* Executor with a fixed thread pool to handle multiple requests

## Usage

In order to run the application, a database must be set, and the following configuration should be available in `src/main/resources/application.properties`
* Get your API key on: [Yahoo Finance API](https://rapidapi.com/apidojo/api/yahoo-finance1)

```
api-key=YOUR_KEY
database.url=jdbc:postgresql://localhost:5432/insider_trade
database.username=POSTGRES_USERNAME
database.password=POSTGRES_PASSWORD
```

The application currently uses PostgreSQL as its database and PgAdmin to interact with it. Both are initialised in Docker containers. To be able to use it:
* Install [Docker Desktop](https://www.docker.com/products/docker-desktop/)
* Proceed to the root folder of the application and create a `.env` file, which should contain the configuration below
```
POSTGRES_USERNAME=POSTGRES_USERNAME
POSTGRES_PASSWORD=POSTGRES_PASSWORD
PGADMIN_DEFAULT_EMAIL=
PGADMIN_DEFAULT_PASSWORD=
```
    - Where:
      - Postgres credentials are for the actual database
      - They are the same you set in your `application.properties` file
        - they will be eventually merged in one place to avoid repetition
      - PgAdmin credentials are for logging in to the PgAdmin web interface

* Finally, run the command `docker compose up -d` to start the containers
  * PgAdmin will be accessible on `localhost:5050`
  * you will need to add a new server, for example `insiderapi`
  * then add a new new database, that should be called `insider_trade`
  * the database port is by default `5432` as per configuration in `docker-compose.yml` and `application.properties`
* Once the application is started, the database migration script or scripts located in `db/migration` will be executed, creating the tables required for the application to function
  * be aware the if the credentials configured in your `.env` file mismatch the ones configured during the creation of the database in PgAdmin, the connection will fail and the application won't start

## API

With the configuration above, the API request should look like the example below

```
http://localhost:8080/insiders?symbol=STEM.L&region=GB&startDate=2022-01-01&endDate=2023-04-30
```
