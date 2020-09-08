### Some notes from Helen:

Changes I made that different from the original repository:

1. Created 'server.js' file under src folder, added necessary codes for connection.

1. Deleted 'http-common' file under src folder.

1. In 'src/services/tutorial.service.js', change 'import http from "../http-common"' to 'import http from "../server"'.

1. To run the frontend locally, in '.env' file, change the PORT into 8081 (to a number other than 8080). If deploying on Openshift, leave the PORT as 8080 (As the default port on Openshift is 8080). 


# React.js CRUD App with Vue Router & Axios

For more detail, please visit:
> [React (Components) CRUD example to consume Web API](https://bezkoder.com/react-crud-web-api/)

> [React (Hooks) CRUD example to consume Web API](https://bezkoder.com/react-hooks-crud-axios-api/)

Fullstack with Node.js Express:
> [React.js + Node.js Express + MySQL](https://bezkoder.com/react-node-express-mysql/)

> [React.js + Node.js Express + PostgreSQL](https://bezkoder.com/react-node-express-postgresql/)

> [React.js + Node.js Express + MongoDB](https://bezkoder.com/react-node-express-mongodb-mern-stack/)

Fullstack with Spring Boot:
> [React.js + Spring Boot + MySQL](https://bezkoder.com/react-spring-boot-crud/)

> [React.js + Spring Boot + MongoDB](https://bezkoder.com/react-spring-boot-mongodb/)

Fullstack with Django:

> [React.js + Django Rest Framework](https://bezkoder.com/django-react-axios-rest-framework/)


This project was bootstrapped with [Create React App](https://github.com/facebook/create-react-app).

### Set port
.env
```
PORT=8081
```

## Project setup

In the project directory, you can run:

```
npm install
# or
yarn install
```

or

### Compiles and hot-reloads for development

```
npm start
# or
yarn start
```

Open [http://localhost:8081](http://localhost:8081) to view it in the browser.

The page will reload if you make edits.
