# Initiating an Import Programmatically #

To initiate a bulk import programmatically, you'll need 3 things:
  1. An HTTP client capable of GET requests ([curl](http://en.wikipedia.org/wiki/CURL) and [wget](http://en.wikipedia.org/wiki/Wget) being two example command line HTTP clients).
  1. The URL of the initiate Web Script on your Alfresco instance.  For a default Alfresco installation on a local machine, this URL would be: http://localhost:8080/alfresco/service/bulk/import/filesystem/initiate
  1. The login details of an admin user configured in that Alfresco instance (the tool cannot be initiated anonymously or by non-admin users for security reasons).

The initiate Web Script also accepts 4 query string parameters:
  1. **targetNodeRef** - the NodeRef of the target folder to import into.  The folder must be within the default store (i.e. the NodeRef must begin with "workspace://SpacesStore/").
  1. **targetPath** - the path (delimited with the '/' character) of the target folder to import into.  This path must be within the default store, and should normally start with "Company Home".
  1. **sourceDirectory** - the source directory on disk to read.  The format of this value is OS specific.
  1. **replaceExisting** - set to the value "replaceExisting" to replace existing files that are found in the repository

The first two parameters are mutually exclusive, but one of them must be provided.  The third parameter is also mandatory, while the fourth is optional (and defaults to false).

For more details on topics like file locking, repeated imports etc. there's some good detail in this [mailing list discussion](http://groups.google.com/group/alfresco-bulk-filesystem-import/browse_thread/thread/cc9a22908c938ebd).

## Example ##

Raw (unescaped) URL:
```
http://admin:admin@localhost:8080/alfresco/service/bulk/import/filesystem/initiate?targetNodeRef=workspace://SpacesStore/f6184f65-4ea4-b9a7-7631-88d607cd73a9&sourceDirectory=/var/data/importData
```
Example command line operation using [curl](http://en.wikipedia.org/wiki/CURL):
```
curl -G "http://admin:admin@localhost:8080/alfresco/service/bulk/import/filesystem/initiate?targetNodeRef=workspace%3A%2F%2FSpacesStore%2Ff6184f65-4ea4-b9a7-7631-88d607cd73a9&sourceDirectory=%2Fvar%2Fdata%2FimportData"
```


---

Back to [usage](Usage.md).