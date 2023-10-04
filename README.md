# Simple Stock Management
A stock management application using latest SpringCloud features

## Uses
* Spring Boot 3.1.4
* Spring Cloud 2022.0.0
    - Netflix Eureka
    - OpenFeign
    - Api Gateway
* Java 17
* Swagger OpenAPI 3.0
* Docker (for publishing)

## Running Instructions

* This whole application is structured to run with a single shell script command
  that will prepare all necessary configurations to build the code and create
  the docker images
* Before running you must have Docker installed on the target machine
* run: `./run.sh` 
* The servers will run with ports 8080 to 8085 and port 8761 for the Eureka Discovery Server
* wait about 1-2 minute(s) before trying any test
* there's also a PostMan collection of the API calls for testing purposes
* The application uses Spring Api Gateway which also servers as Load Balancer

## Documentation

* The code is divided into: 5 services that work together to provide the information
  requested. And each service has it's own Swagger OpenAPI documentation for
  developers use.
* These Swagger documentation can be found at: `http://localhost:8080/<service-name>/api-ui`

## Usage instructions

* Create the Items and Users first, since they don't depend on anything else
* Then you can create Orders or StockMovements in any order
  * creating an order will check if there's on or more StockMovement that can fulfill that order
  * creating a StockMovement will check if there's one or more Orders to fulfill
* Everytime an order is fulfilled, the StockMovement used to do so is recorded with the
  Order-StockMovement Service

## Missing features

* All services support all CRUD operations, except the order-stock-movement-service, 
  which only have `create, delete, list by order and list by stock-movement`
* For the Order and Stock-Movement services, the UPDATE/DELETE operations might cause unexpected
  results if used individually. The data is updated accordingly but the references
  to Order/StockMovement they have themselves might not update as expected.
* This mean that CREATE and Order will check StockMovement but UPDATE/DELETE will not
* Same for StockMovement, CREATE will check Order but UPDATE/DELETE will not
* It's only possible to DELETE Pending Orders
* It's only possible to DELETE StockMovement with available quantities
* Due to time constraints the Logging file is not present



##### copyright(C) Romeu Franzoia - 2023
