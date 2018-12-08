# email-to-queue
This project is a simple use of spring-integration-flows and the spring-integration-library to read a message from an email address and forward any attachments to a queue.

Steps to get started:
Copy application.EXAMPLE.yml into a new file called application.yml in same directory (main/resources/application.yml)

After copying and changing the name of the example file you will need to configure it. The names in the application.yml file should be descriptive for configuration with a few caveats which I have described below.

FOR ANY CONFIGURATION WITH A USERNAME WITH A '@' SIGN : Replace the @ sign with the following '%40'.

Below is an example for logging into an outlook account.

CONFIGURE THIS WAY WHEN USERNAME HAS A '@' SIGN:
imaps://username%40outlook.com:password@outlook.office365.com:993/inbox

DO **NOT** CONFIGURE THIS WAY WHEN USERNAME HAS A '@' SIGN:
imaps://username@outlook.com:password@outlook.office365.com:993/inbox

Spring integration includes a property file for the mail. To configure it wrap the values in quotes. If you do not wrap in quotes spring will resolve to a different object type and the values will not work (but the program will still run!!)
Example of correct way to wrap key/value. Note the use of quotation marks:

java-mail-properties:
  java-mail-properties:
    mail.imap.socketFactory.class: "javax.net.ssl.SSLSocketFactory"  

To run run a mvn install to get the war. Put the application.yml in the /bin folder and deploy the application to the server.

Please note, there is a current bug with Spring 2.0.x and so this project uses Spring 1.5.x. The bug can be found here:
https://jira.spring.io/browse/INT-4299. When the bug is fixed the project should be tested to migrate to the newer version of spring.
