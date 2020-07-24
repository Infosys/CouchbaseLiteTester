# CBLiteTester 
###### version 1.2
This app provides a UI to create a local Couchbase Lite DB and Sync Data to the DB from a Couchbase Sync Gateway. Details on how to use the tool and its features is available in this [blog](https://amrishraje.github.io/CBLiteTester.html).

## Binary Releases
For your convenience, I have uploaded a pre-built binary to the [Releases](https://github.com/amrishraje/CBLiteTester/releases) tab. Currently, the binary is tested for Windows only.
Run the Binary by double clicking on the CBLiteTester.jar file or using ``java -jar CBLiteTester.jar``. Java JRE must be correctly installed on the system.
 
> Note: Binary releases are provided for major versions. Please build from source for latest features. 

## Building the tool
Download from git and import into your IDE of choice.
```
git clone https://github.com/amrishraje/CBLiteTester.git  
```
Build and run from your IDE using method of your choice. 

#####Build using maven
```
mvn javafx:run
``` 
#####Build a fat JAR for distribution
```
mvn compile package
```
This will create a distributable JAR file in build folder. Package an appropriate defaults.xml file along with your jar file with appropriate environments setup.  

## Tips: 
* You can define multiple environments in the defaults.xml file. This will automatically be picked up by the
settings pane to display a dropdown of various environments and their corresponding Sync Gateways.
* All channels that a user has access to will be automatically listed in the Sync Channels drop down. This requires Sync Gateway Admin URL to be explicitly specified and admin APIs to be exposed outside the Sync Gateway VM. This is dangerous - do not do this unless you know what you are doing.
* You may put a reverse proxy like NGINX in front of SG to protect it or use SSH Tunneling. If your sync gateway Admin URL requires authentication, it can be supplied by adding below property in config.xml file
```
<entry key="sgAdminAuth">Basic encodedCredentials</entry>
``` 

## Features
###### version 1.2
* Support for loading data from another CBLite DB (Change CBLite location in settings and click Reload Table)
###### version 1.1
* Fat JAR for distribution (tested on windows 10 with java 11.0.4)
###### version 1.0
* Create Local CB Lite DB at a configurable location
* Sync with Couchbase Sync Gateway
* Support for both ws and wss based sync using certificates
* Support for both Continuous and On-demand Sync
* Support for Pull, Push and Pull and Push replication
* Support for displaying all documents synced to CB Lite in a table
* Support for displaying documents in pretty JSON format (same as Couchbase Server)
* Support for searching documents by document id
* Support for searching data within a document
* Support for multiple Sync Gateways in different environments e.g. Dev, QA, Prod etc
* Support for deleting and initializing local CBLite DB
* Support to show a list of available channels and sync with one or more specified channels
* Support to edit data in the UI and do 'push' replication to sync gateway

## Future roadmap 
* Support to search documents based on specified criteria
* Support to run N1QL like queries on CBLite DB
* Support to create/delete users on Sync Gateway and grant access to channels
* Support to run in headless mode as a lightweight CLI based tool with support for wss (cert pinning) which is currently lacking in the Couchbase Labs provided CBLite CLI tool
* Support to create documents in CBLite from UI and sync up to the DB via Sync Gateway
* Support for adding Attachments (Blobs) to Documents in CBLite Tester tool and syncing it up to the server
* Improve performance for loading full documents in Table
* Modular Java app with distributable package
