mViewer(Micro/Mongo Viewer) is a light (read web based like Futon for couchdb) GUI for mongo db which does not need any install steps.

### Current Features are:

   1. Managed db and collections (usual add/drop of collections and drop of db)
   2. Viewing collection and db stats (count, size, storage et al)
   3. query executor
   4. Mongo stats

### Download and Use

Download the package from here https://github.com/Imaginea/mViewer/downloads

Unzip/Untar it and run the script/batch (with +x permission).

>
> $./start_mviewer.sh \<port\> 
>

or

> 
> \>start_mviewer.bat \<port\>
>

<port> is optional, if not given it'll take the default port from properties file.

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

