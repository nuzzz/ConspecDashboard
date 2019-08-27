# conspec-app

Conspec app for load and scheduling of Microsoft project files

The Conspec-App project is used to extract Microsoft project file data and through the use of Todoist API place that data into todoist.

It uses the following website to upload the microsoft project file.

![Image of program flow](https://github.com/nuzzz/ConspecDashboard/blob/master/MPP%20to%20Todoist.JPG)

Once the file is uploaded, the project file can be found in the Projects subheading of the website.

If clicked in this page, this automatically takes the data from the uploaded Microsoft Project file and inserts the task data into Todoist application under my local todoist account.

The project is generally written in Java using a starter Spring Boot setup, it is hosted on Heroku using a free dyno.

Technologies used.
* Heroku (web hosting)
* Amazon S3 (for storage)
* Travis CI

Java External Libraries used
* web-jars
* junit
* MPXJ
* guava
* Spring boot with thymeleaf
* maven
