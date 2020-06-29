# CBLiteTester
This app provides a UI to create a local Couchbase Lite DB and Sync Data to the DB from a Couchbase Sync Gateway. 

## Following features have already been implemented:

* Create Local CB Lite DB at a configurable location
* Sync with Couchbase Sync Gateway
* Support for displaying all documents synced to CB Lite in a table
* Support for searching data within a document
* Supoort for searching documents by docment id
* Support for both ws and wss based sync using certificates
* Support for both Continuous and On-demand Sync
* Support for multiple Sync Gateways in different environments e.g. Dev, QA, Prod etc
* Support for deleteing and initializing local CBLite DB

## Future roadmap:

* Support to show a list of available channels and sync with one or more specified channels that a user has access to
* Support to edit data in the UI and do 'push' replication to sync gateway
* Support to search documents based on specified criteria
* Support to run N1QL like queries on CBLite DB
* Support to create/delete users on Sync Gateway and grant access to channels
* Support to run in headless mode as a lightweight CLI based tool with support for wss (cert pinning) which is currently lacking in the Couchbase Labs provided CBLite CLI tool
* ...

