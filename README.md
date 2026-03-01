# ktor-api-gateway

## About
This repo is backend bone for our MakeBleja application. This is API which holds all services required for app to work. In the next few sections you will learn how to setup API and test endpoints. Later you can connect it to UI and play with our application.
This API uses Ktor framework and PostgreSQL database. 
Our app will be available to all people interested, so they can learn and modify app for their specific purpouses, have fun :) 

## Setup
First of all its important to tell that you need to find the way to host database and API somewhere if you ofc want more people to use app. We used free hosting called Aiven for database.
So when you clone project you will need to create file called 'local.properties' in root folder. That file will contain your credentials for connecting to Aiven, smtp and so on...
Here is template I used:

```
db.url=...
db.user=...
db.password=...

email.user=...
email.password=...
```
You will also need pgAdmin4, where you will connect to hosted server(Aiven in our case).

## Testing app
When you open project in IntelliJ, gradle will do its work and download all plugins. After that go to Application.kt and try to run.

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://127.0.0.1:8080
```
Now. For testing services, i recommend using Postman or Bruno, but you can setup Swagger if you want, its not best practice to use it ktor although...
When you install your API testing software, add new GET request called for example 'GetAllUsers', and give it a route:
```
{{base_url}}/users/getAllUsers
```
this will return list of all Users in your database if everything went well.


