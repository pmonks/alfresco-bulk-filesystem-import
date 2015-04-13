# Prerequisites #
Please make sure you are running a version of Alfresco that the tool has been developed for.  As of the time of writing, this means either Alfresco v4.0, v4.1 or v4.2.  Earlier versions of the tool are available for Alfresco v3.x.

You also need to be running at least JDK 1.6 - the tool uses Java features that were added in this version and will not work on earlier versions.  Note that Alfresco itself hasn't supported JDK 1.5 since v2.2 - since v3.0 Alfresco v3.0 has required JDK 1.6, and since Alfresco v4.2 has required JDK 1.7, independent of this tool.

# Installation Steps #
The following steps describe how to download and install the Alfresco Bulk Filesystem Import Tool:

  1. Download the latest AMP file containing the tool from [here](https://drive.google.com/folderview?id=0B9zzxEjFzFf9SzRmT0s2UE5TMXM&usp=sharing#list)
  1. Shutdown your Alfresco instance
  1. Make a backup of the original alfresco.war file.  On Tomcat, this is located in ${ALFRESCO\_HOME}/tomcat/webapps
  1. Use the [Alfresco Module Management Tool](http://wiki.alfresco.com/wiki/Module_Management_Tool) to install the AMP file obtained in step 1
  1. Restart Alfresco, watching the log carefully for errors


---

Back to the [main page](Main.md).