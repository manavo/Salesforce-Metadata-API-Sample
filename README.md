Sample code for retrieving objects from the Metadata API and inspecting them via the Web Services Connector.

### Usage

Assuming you have Java installed, you can clone the following code and run the RetrieveObjects.java class. Ensure that you update the config.properties file with your authentication information. At the moment it only is set up to hit production instances.

Currently the code is set up to retrieve the Account object and then spit out some information about fields and validation rules.

### Setup

Reference [Introduction to the Force.com Web Services Connector](http://wiki.developerforce.com/page/Introduction_to_the_Force.com_Web_Services_Connector)
* Note that the WSC now has a new URL: https://github.com/forcedotcom/wsc

I only leveraged the Partner and Metadata WSDLs version 29.0 (didn't see a need for Enterprise).

Generating Jar files from the WSC 30.0 version (follow directions on the repo above)
* java -classpath force-wsc-30.0.0-uber.jar com.sforce.ws.tools.wsdlc partner.wsdl partner.jar
* java -classpath force-wsc-30.0.0-uber.jar com.sforce.ws.tools.wsdlc metadata.wsdl metadata.jar