### mViewer-v0.9 Released !

mViewer(Micro/Mongo Viewer) is a light web-based GUI for managing MongoDB without needing any installation.

### Enhanced Feature set of mViewer-v0.9 now supports :-

   1. Managing Databases - Create/Drop databases
   2. Managing Collections - Create/Update/Drop collections
   3. Managing GridFS files - Add/View/Download/Drop files
   4. Querying using Query executor
   5. Quick Navigation to all actions items, just needing to type few visible letters of the name using CTRL + Space.
   6. Pagination and Navigation to any subset of documents.
   7. Viewing stats of databases, collections and gridFS
   8. Opening multiple connections from same browser to different MongoDB servers

### Download mViewer-v0.9

Windows :- https://github.com/downloads/Imaginea/mViewer/mViewer-v0.9.zip

Mac/Linux :- https://github.com/downloads/Imaginea/mViewer/mViewer-v0.9.tar.gz

Download previous versions from https://github.com/Imaginea/mViewer/downloads
    
### Usage

Unzip/Untar the downloaded package and simply run start_mViewer.bat/start_mViewer.sh (with +x permission).

>
> $./start_mViewer.sh \<port\> 
>

or

> 
> \>start_mViewer.bat \<port\>
>

'port' is optional, if not provided default port from properties file will be used.


### How to Build (If you prefer maven)

>
> $mvn clean package -DskipTests
>

   Q. Why do we skip tests ?. 
   
   A. Because we need to configure src/test/resources/mongo.config to point to running mongod service. To let the test run.
   Once set, tests should run out of the box.
   
#### Start standalone
Run using maven. It will create a war and run it using the jetty server on a default port (Check the logs that print on your screen to spot it), you can access the service at http://localhost:8080/mViewer/

>
> $mvn -Pserver -DskipTests
>


#### Start and deploy to Other Servlet-Containers.

For building a distributable unit run the target distributable war. This war can be deployed on to tomcat 7x, other server integration can be provided on demand.

Once the war is deployed go to the url http://<server-ip>:<http-port>/mViewer

>
> $mvn clean package -DskipTests
>

### How to build for release

Building a release, bundles the war with winstone servlet container and scripts from scripts folder. The release zip and tgz mviewer-<version>.<type>

>
> $mvn clean package -Prelease -DskipTests
>


### How to Build (Legacy Method using ant)


#### Start standalone
Run build.xml using ant, target is start. It will create a war and run it using the winstone server, you can access the application at http://localhost:<port-no>. You can change the port no. in mViewer.properties file. Default port is 8080

>
> $ant start
>


#### Start and deploy to Other Servlet-Containers.

For building a distributable unit run the target dist, since the default target is also set as dist, just running ant should suffice. dist would create a deployable war in the staging directory, which by default is at the same level as the src folder.
This war can be deployed on to tomcat 7x, other server integration can be provided on demand.

Once the war is deployed go to the url http://<server-ip>:<http-port>/mViewer

>
> $ant dist
>

### How to build for release

Building a release, bundles the war with winstone servlet container and scripts from scripts folder. The release zip and tgz mviewer-<version>.<type>

>
> $ant release
>


##### Eagerly waiting for feature requests and bug reports
##### Team Imaginea

