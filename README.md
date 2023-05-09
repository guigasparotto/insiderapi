# insiderapi

This is a Java practice project. The goal is to create an API that receives requests for stocks and date ranges, and then returns all transactions made by insiders of those specific companies during the period.

To achieve that, data is retrieved from the unnoficial [Yahoo Finance API](https://rapidapi.com/apidojo/api/yahoo-finance1)

## Implementation

On a high level, what the applications currently does is the following:
* Receives the API request with symbol, region and date rage specified as parameters
* Checks the database for the date of the lates update for that symbol and region
* If the date is older than the end date required by the initial request, it sends a request to the external API
* If not, it retrieves and returns the date currently available
* If fetching external data is required, it does that, then filters the data based on the date range requested before returning it

It may seem redundant to retrieve the data from external API and returning it with minor changes - the biggest benefit at the moment is to avoid external calls when the data is already available in the database. But other features will be added as the project expands.
The goal, at the moment, is to avoid larger frameworks, such as Spring, to focus on core Java implementation.

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

In order to clone and run the application, a database must be set, and the following configuration shoulb be available in `src/main/resources/application.properties`
* Get your API key on: [Yahoo Finance API](https://rapidapi.com/apidojo/api/yahoo-finance1)

```
api-key=YOUR_KEY
database.url=jdbc:postgresql://localhost:5432/insider_trade
database.username=POSTGRES_USERNAME
database.password=POSTGRES_PASSWORD
```

## API

With the configuration above, the API request should look like the example below

```
http://localhost:8080/insiders?symbol=STEM.L&region=GB&startDate=2022-01-01&endDate=2023-04-30
```
