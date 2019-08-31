# conspec-app

A prototype for loading and scheduling of Microsoft project files.

The Conspec-App project is used to extract Microsoft project file data and through the use of Todoist API place that data into todoist.

Notifications are used to notify the project owner when things are due or overdue and it is up to the user to manage these.

# Technical stuff


Once the file is uploaded, the project file can be found in the Projects subheading of the website.

If clicked in this page, this automatically takes the data from the uploaded Microsoft Project file and inserts the task data into Todoist my local todoist account.

The project is generally written in Java using a starter Spring Boot setup, it is hosted on Heroku using a free dyno.

# Technologies
* Heroku (web hosting)
* Amazon S3 (for storage)
* Travis CI
* Java
* Bootstrap

# Java External Libraries
* web-jars
* junit
* MPXJ
* guava
* Spring boot with Thymeleaf
* maven

# Useful resources
* Todoist developer page (i was using version 8) https://developer.todoist.com/sync/v8/
* MPXJ http://www.mpxj.org/
* Amazon S3 https://docs.aws.amazon.com/AmazonS3/latest/dev/Welcome.html
* Thymeleaf https://www.thymeleaf.org/
* Spring https://spring.io/
* Bootstrap https://getbootstrap.com/docs/4.3/getting-started/introduction/

> note there is no code for the Microsoft Flows used for this project as they are used in production
