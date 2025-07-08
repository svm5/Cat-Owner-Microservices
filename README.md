# Service for owners and their cats

Pet project - Async REST API to interact with owners and their cats.

### Technologies

* Spring MVC, Spring Boot
* Spring Data JPA: repositories
* Spring Security + JWT: all endpoints except login and user creation are secured. After successful login, the access token is stored in the cookies
* Kafka - message broker. Communication between 3 microservices. One microservice is external, receives requests, sends messages through the broker to two other microservices (for logic related to cats and owners), receives responses, and sends them in the required format. Each microservice has its own database
* Testing
  * integration tests using Testcontainers for repositories
  * Juint5 and Mockito for services unit tests
  * mockMvc for controllers tests
* Docker, Docker Compose, Flyway: start database containers, broker, microservices, and apply migrations.
* Swagger: documentation
* CI CD: run tests

### Entities:

* User
  * ID
  * username
  * password
  * roles (user/admin)

* Owner
  * ID
  * name
  * birthday
  * list of cat IDs

* Cat
  * ID
  * name
  * birthday
  * breed
  * color
  * eyesColor
  * owner ID
  * list of friends IDs

User and Owner are connected by a one-to-one relationship

### Functional:

* User:
    * create (with Owner)
    * login 
    * logout

* Owner
  * get info
  * delete
  * get owners with several criteria (by name/birthday; only for admin)
  * delete all owners (only for admin)

* Cat
  * create
  * get info
  * get owners with several criteria (name/birthday/breed/color/eyes color). Admin will get all cats, owners only their cats
  * change owner
  * adding and removing cat to friends list
  * delete
  * delete all cats (only for admin)

TODO

* Store and use refresh token

* Testing microservices communication with Testcontainers (Kafka)
