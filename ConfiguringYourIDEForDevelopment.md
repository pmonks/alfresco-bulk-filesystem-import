Because the import tool is built using Maven, project files can be generated for any of the IDEs Maven supports.  At the time of writing (June 2012) this includes:

  * [Eclipse](http://maven.apache.org/eclipse-plugin.html)
  * [NetBeans](http://maven.apache.org/netbeans-module.html)
  * [IntelliJ](http://code.google.com/p/maven-idea-plugin/)

If you're using Eclipse, you would run the following command in the directory in which you checked out the source code:
```
mvn eclipse:clean eclipse:eclipse
```
Or the following, if you're using Alfresco Community:
```
mvn -P community eclipse:clean eclipse:eclipse
```
Once complete, use File > Import > General > Existing Projects into Workspace and point Eclipse at the directory in which you checked out the source code.  Choose **not** to copy the project into your workspace.

For other IDEs, please refer to the pages listed above for specifics on how to prepare an IDE-specific project from the source code.

Note that the Maven AMP plugin interferes with the Maven Eclipse plugin - by default the generated project will not be configured as a Java project, so you won't see source directories etc.  To fix this, temporarily change the packaging type (in pom.xml) to "jar" instead of "amp", run the Maven commands above, then revert the packaging type back to "amp".


---

Back to the [Developers page](Developers.md).