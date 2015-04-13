# Environment Validation #
Before reporting issues with the tool, it is highly recommended to run the [Environment Validation Tool (EVT)](http://code.google.com/p/alfresco-environment-validation/) and resolve all of the issues it identifies.  Alfresco will not function well if environment validation doesn't pass, and bulk imports are a particularly "heavy" operation for the repository.

One environment-related issue that has been observed is bulk import performance trailing off as the import proceeds (often ending up at a fraction of the initial import rate), due to insufficient memory allocated to the Alfresco JVM.  Alfresco includes a number of auto-sized caches, and when Alfresco's heap is small these caches also end up small.  One explanation for the observed drop in performance is that as the amount of imported content grows, the effectiveness of these caches tails off, and if they're small to begin with there's a double impact (small cache + large dataset = cache thrashing).  The recommended solution is to ensure that the Alfresco JVM has as much heap allocated to it as possible - at least 2GB (as suggested by the EVT), but ideally 4GB or more.

# Enabling Debug/Trace Logging #
Although by default the tool is fairly terse in its logging output, it does in fact produce a lot of detailed logging output at debug and trace levels, and this output can be extremely helpful in troubleshooting issues with the tool.

To enable detailed logging:
  1. add one of the following lines to log4j.properties (or, if you're an Alfresco Enterprise customer, use JMX to add a new Log4J category if you'd prefer)
```
log4j.logger.org.alfresco.extension.bulkfilesystemimport=debug
# or for the highest level of logging:
#log4j.logger.org.alfresco.extension.bulkfilesystemimport=trace
```
  1. restart Alfresco
  1. watch alfresco.log for additional logging output from the tool

Note that enabling this level of logging output will slow down the import tool, and will result in the alfresco.log file consuming a lot more disk space as well.  It should not be left at this setting for most long term uses.

# Validating that Custom Content Models have been Registered with the Repository #

If you're seeing errors populating properties defined in custom content models, it's worth running the [Data Dictionary Web Script](http://localhost:8080/alfresco/service/bulk/import/filesystem/datadictionary) to confirm that the custom model has been registered with the repository.  This Web Script displays a simple HTML representation of the entire Data Dictionary that's registered with the running Alfresco server - if a custom model isn't visible on that page it hasn't been registered correctly.

Note that at debug log levels (see above) the import tool also dumps out the same information in text format to the log file.

# Commonly Seen Symptoms #

There are also some issues and mailing list discussions that describe symptoms that have been seen with the tool, and (usually) their resolution:
  * [Issue #87](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#87) - on Linux, files with accented characters in their filenames won't load if the LC\_ALL and LANG environment variables aren't set appropriately
  * [Issue #57](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#57) / [Issue #88](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#88) - misunderstandings about how tags are specified in shadow metadata files
  * [Issue #97](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#97) - describe behaviour if two files end up with the same name (via metadata)
  * [Issue #104](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#104) - on Linux, content urls are not set correctly if an in-place import is initiated and the contentstore is a symlink
  * [Question about metadata files](https://groups.google.com/forum/?fromgroups#!topic/alfresco-bulk-filesystem-import/nWsM4O18zUo) - Java is very picky about XML properties files - they must have **exactly** the right DOCTYPE declaration
  * [Issue #124](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#124) - a space at the start of a properties XML file prevents the entire metadata file from being processed
  * [Issue #126](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#126) - Alfresco Community 3.4.d and earlier have an older version of Freemarker that is missing features the tool relies on.  Recommended solution is to upgrade either the JAR or Alfresco.

If you're having trouble with the tool, please post a message on the [mailing list](https://groups.google.com/forum/#!forum/alfresco-bulk-filesystem-import).


---

Back to the [main page](Main.md).