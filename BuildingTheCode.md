The source code includes a single Maven build script that contains two profiles:
  1. enterprise (the default) - configured to build with Alfresco Enterprise artifacts
  1. community - configured to build with Alfresco Community artifacts

Both of these profiles require you to configure the [Alfresco Artifact Repository](https://artifacts.alfresco.com) in your Maven settings.xml file.  This is introduced in [this blog post](http://mindthegab.com/2012/06/05/introducing-the-alfresco-artifacts-repository-yes-with-alfresco-enterprise/).

If you're an Enterprise customer with a login for the [Alfresco Artifact Repository](https://artifacts.alfresco.com), either of the following commands will compile the code against the Alfresco Enterprise JARs and emit an AMP file:
```
mvn clean package
# OR
mvn -P enterprise clean package
```
If you don't have a login to the Alfresco Artifact Repository, use the following command instead (which will compile the code against the freely available Alfresco Community JARs):
```
mvn -P community clean package
```

These profiles are available in both the 3x and 4x branches of the code.


---

Back to the [Developers page](Developers.md).