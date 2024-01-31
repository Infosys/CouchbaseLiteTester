#Requesting to undo option
(when the sync is established we have to disconnected using single button)
# CouchbaseLiteTester 
###### version 1.10.2
This app provides a UI to create a local Couchbase Lite DB and Sync Data to the DB from a Couchbase Sync Gateway. It provides features to search for documents in the CBLite DB, selectively sync certain channels and supports both Pull and Push replication.

## Getting Started
For your convenience, I have uploaded a pre-built binary to the [Releases](https://github.com/Infosys/CouchbaseLiteTester/releases) tab.
> Note: Binary releases are provided for major versions. Please build from source for latest features. 

Run the Binary by double clicking on the CBLiteTester.jar file or using ``java -jar CBLiteTester.jar``. Java JRE must be correctly installed on the system.

![CBLite Tester Tool](https://github.com/amrishraje/amrishraje.github.io/blob/master/CBLiteTester_files/image001.png)  

Click on Settings and type in your Sync Gateway information and click Save. Alternatively, you can select an Environment from the drop down and all Sync Gateway settings for that environment will be pre-populated. 

![CBLite Tester Settings](https://github.com/amrishraje/amrishraje.github.io/blob/master/CBLiteTester_files/image002.png)

By default, the Environments is populated with xxx,yyy. This can be replaced with your Sync Gateway settings by editing the defaults.xml file. The defaults.xml file is present in the same folder where your CBLiteTester.jar file is. The defaults file has an entry called environments which can accept a list of environments where you have sync gateways. A sample entry representing Dev and QA region could be
```
<entry key="environments">Dev,QA</entry> 
```
For each environment listed in the environments section, please follow instructions in the defaults.xml file and provide entries for Sync Gateway URL, port, database, etc. This is a one-time step to setup your environment. Once done, the tool will remember your settings. 

### Connecting to Sync Gateway and Syncing Data
One the Sync Gateway has been configured in settings, type in your Sync Gateway credentials for the user you want to sync data with. By default, the tool syncs ALL channels that the user has access to. The default replication method is PULL replication. This can be changed by choosing the appropriate replication method from the Replication Mode drop down. The tool also allows you to specify channels that you want to sync with. Click on the Sync Channels drop down and click on “Click to add…”. This will open up a channel editor window. Type in a list of channels you wish to sync with as a comma separated list and click ‘Set Channels’.

![Channel Editor](https://github.com/amrishraje/amrishraje.github.io/blob/master/CBLiteTester_files/image003.png)

Now, if you click on Sync Channels drop down, you should see a list of your channels. Check the channels you wish to sync with.

![Select Channels](https://github.com/amrishraje/amrishraje.github.io/blob/master/CBLiteTester_files/image004.png)

If you provided your Sync Gateway Admin URL in settings (SG Admin URL field) and if your Sync Gateway Admin API is exposed outside of the Sync Gateway machine, then the Sync Channels drop down should automatically pull all channels that a user has access to. Note that exposing Sync Gateway Admin API outside the machine on which SG is running is NOT recommended and is quite dangerous as anyone can access your data via the Admin APIs. You may put a reverse proxy like NGINX in front of SG to protect it or use SSH Tunneling. If your sync gateway Admin URL requires authentication, it can be supplied by adding below property in config.xml file
```
<entry key="sgAdminAuth">Basic encodedCredentials</entry>
```

To sync data, click on Sync. 

![Data Sync](https://github.com/amrishraje/amrishraje.github.io/blob/master/CBLiteTester_files/image005.png)

Data for the user and specified channel will be synced to the tool. Note that the user must be setup in sync gateway to have access to the channels they are requesting data for. Click on any document to open it. You may also click on the ‘Load Full Document’ toggle at the bottom right corner to load all documents at once. Note that this will take some time if you have thousands of documents! 
You can search for any documents by typing in the Document ID in the search box. 
You may also open a document and edit it in the doc editor and sync it back to the server. Click on any document to open it. In the editor window, you will see that the document is displayed in a JSON format just like you would see it in the Couchbase Console. Make desired changes and click Save. You may change or add any new attributes to the document if it is a valid JSON. 

![Data Editor](https://github.com/amrishraje/amrishraje.github.io/blob/master/CBLiteTester_files/image006.png)

Any changes to the document, will be locally saved to the CBLite Database. To sync the changes back to the server, make sure you select the replication mode as Push or ‘Pull and Push’. Click sync.
> Note: Sync Gateway function should allow user to Push documents to server.
 
Continuous Sync Mode can also be enabled by clicking on the Continuous Sync toggle.
 
The Delete button can be used to delete the local CBLite Database. Note: Deleting the CBLite Database will NOT delete the documents on the server even if you click Sync or Continuous Sync is on.
 
The Initialize button can be used to re-initialize the local CBLite database by downloading all documents from the server again. If you change the user and click Initialize, the data for previous user is deleted from the local CBLite DB and data for the new user is Synced and displayed in the table. 

## Creating and Syncing new documents
New documents can be created by clicking the 'Add Document' button. 
![Create New Documents](https://github.com/amrishraje/amrishraje.github.io/blob/master/CBLiteTester_files/create_new_docs.png)
On clicking save, the document is saved to the local CBLite database. Click 'Reload Table' to refresh the table view from the CBLite DB. To sync the new doc up to the server, make sure that Replication Mode is selected as either Push or 'Pull and Push'. Enter sync gateway credentials and click sync. 

## Working with an existing CBLite DB
You can load an existing CBLite database file (dbname.cblite2) downloaded from a mobile device, or created using Couchbase CBLite CLI tool in the CBLite Tester tool. Launch the tool and click settings. Click the ‘Choose CBLite DB’ button and point to the folder containing your *.cblite2 file. Note: Do not point to the cblite2 folder itself, but point to the folder containing it. Also, ensure that the name of the cblite2 file is the same as the name of your database. In this example, the database name is syncdb and the tool will expect the CBLite database file to be called syncdb.cblite2

![Chooing existing CBLite DB](https://github.com/amrishraje/amrishraje.github.io/blob/master/CBLiteTester_files/image007.png)

Click on Save to save the settings. Then click Reload Table to load the new CBLite Database to the Table.

![Reload Table](https://github.com/amrishraje/amrishraje.github.io/blob/master/CBLiteTester_files/image008.png)

## Creating a Pre-built CBLite Database image for Mobile
The CBLite Tester can also be used to create a pre-built DB image that can be deployed on a mobile app. Simply sync data with any Sync Gateway so that the tool creates a dbname.cblite2 file. Edit documents in the tool as desired and save them. All changes will be saved to the dbname.cblite2 file. Simply copy the database file to your mobile device and all the data in the file should be available in the Couchbase Lite mobile application. Future enhancements to the tool will provide ability to add new documents and attachments (Blobs) to CBLite DB via the tool rather than having to sync from a Sync Gateway.

## Exporting documents from CBLite and importing to Couchbase DB
The CBLite Tester can be used to export documents in CBLite file to a txt file. This file can be imported into Couchbase Server. This can be very handy if there is a Sync issue preventing some docs from syncing. The file can be imported into CB Server using cbimport or Import Documents functionality in the CB web console.
```
cbimport json --format lines -c http://cb-hostname:8091 -u <login> -p <password> -d 'file://cblite_export.txt' -b 'bucket' -g #UUID# 
```   


## Building the tool from Code
Download from git and import into your IDE of choice.
```
git clone https://github.com/Infosys/CouchbaseLiteTester.git  
```
Build and run from your IDE using method of your choice. 

##### Build using maven
```
mvn javafx:run
``` 
##### Build a fat JAR for distribution
```
mvn compile package
```
This will create a distributable JAR file in build folder. Package an appropriate defaults.xml file along with your jar file with appropriate environments setup.  

## Features
###### version 1.10.2
* Bug fixes
###### version 1.10.1
* Support for Generating Sync Gateway Session Token
~~* Upgraded CBL library to version 2.8.4 CE~~
###### version 1.9
* Support for Sync Gateway Session Token authentication
* Bug fixes
* Minor UI enhancement
###### version 1.8
* Bug fixes
###### version 1.7
* Export docs from CBLite to a txt/json file
* Minor enhancements and bug fixes
###### version 1.6
* Minor enhancements and bug fixes
* Search displays matched doc counts
###### version 1.5
* Support to search documents (Advance Search) based on multiple keywords
###### version 1.4
* Support for adding Attachments (Blobs) to Documents in CBLite Tester and syncing it up to the server
###### version 1.3
* Support for creating new documents. Documents are saved locally in CBLite DB
* Support to sync new documents created in CBLite Tester to CB Server
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
* ~~Support to search documents based on specified criteria~~ (implemented in v1.5)
* Support to delete documents from CBLite via UI and sync deletes to CB Server
* Support to run N1QL like queries on CBLite DB
* Support to create/delete users on Sync Gateway and grant access to channels
* Support to run in headless mode as a lightweight CLI based tool with support for wss (cert pinning) which is currently lacking in the Couchbase Labs provided CBLite CLI tool
* ~~Support for adding Attachments (Blobs) to Documents in CBLite Tester tool and syncing it up to the server~~ (implemented in v1.4)
* Improve performance for loading full documents in Table
* Modular Java app with distributable package
* ~~Support for exporting synced documents~~ (implemented in v1.7)
* ~~Package app for distribution as JAR~~ (implemented in v1.1)
* ~~Support for loading data from another CBLite DB~~ (implemented in v1.2)
* ~~Support to create documents in CBLite from UI and sync up to the DB via Sync Gateway~~ (implemented in v1.3)
* ~~Support for Sync Gateway Session Token Authentication~~ (implemented in v1.9)
* ~~Support for Generating Sync Gateway Session Token~~ (implemented in v1.10)
