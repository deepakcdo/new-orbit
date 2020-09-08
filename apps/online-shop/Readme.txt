For chapter 12 I had to give docker access to my documents on my mac
Go to System preferences > Security & Privacy > Files and Folders, and add Docker for Mac and your shared directory.

Also to get the config server running locally as a spring app I had to add this to /Users/deepakcdo/Documents/MySpace/Dev/new-orbit/Apps/online-shop/spring-cloud/config-server/src/main/resources/application.yml
spring:
  security:
    user:
      name: dev
      password: dev
  profiles:
    active: native
encrypt:
  key: 123
--------------------