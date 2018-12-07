# email-to-queue
This project is a simple use of spring-integration-flows and the spring-integration-library to read a message from an email address and forward any attachments to a queue.

Steps to get started:
Copy application.EXAMPLE.yml into a new file called application.yml in same directory (main/resources/application.yml)

After copying and changing the name of the example file you will need to configure it. The names in the applicaiton.yml file should be descriptive for configuration with a few caveats which I have described below.

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


The following property will have no effect and is a hold-over from a polling solution. If you are interested in a polling solution look through the history of EmailFlow. Changing this property has no effect on the program.
email.pollTimeInSeconds: 5

To run type mvn spring-boot:run in command line. You can also install the program, pick the jar out, put the jar in the same folder as the application.yml file (or in a classpath as described in the spring documentation) and then run by typing: 

`java -jar .\email-to-queue-0.0.1-SNAPSHOT.jar`

Please note, there is a current bug with Spring 5.0 and so this project uses an earlier version of spring. The bug can be found here:
https://jira.spring.io/browse/INT-4299. When the bug is fixed the project should be tested to migrate to the newer version of spring.
