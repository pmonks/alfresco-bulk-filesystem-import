# Manually #

If an import is in progress it can be stopped manually from the status page (described [here](InitiatingAnImportManually.md)).

# Programmatically #

The tool includes a "stop" Web Script that can be triggered programmatically via curl, wget or any other HTTP client, by issuing an HTTP GET against:
```
http://<alfrescoHostName>:<alfrescoPort>/alfresco/service/bulk/import/filesystem/stop
```
(this Web Script can only be invoked by an Alfresco administrator - credentials must be provided via [HTTP basic auth](http://tools.ietf.org/html/rfc2617))

The response (by default in HTML format, though JSON is also available by adding a ".json" extension to the URL) will indicate what happened.


---

Back to [usage](Usage.md).