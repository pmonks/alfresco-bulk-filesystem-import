# Overview #
The tool is exposed as a series of 5 Web Scripts accessible to Alfresco administrators only:
  1. A simple "UI" Web Script that can be used to manually initiate an import.  This is an HTTP GET Web Script with a path of:
> > `/bulk/import/filesystem`
  1. An "initiate" Web Script that actually kicks off an import, using parameters that are passed to it (for the source directory, target space, etc.).  This is an HTTP GET Web Script with a path of:
> > `/bulk/import/filesystem/initiate`
> > If you wish to script or programmatically invoke the tool, this is the Web Script you should call.
  1. A status Web Script that returns status information on the current import (if one is in progress), or the status of the last import that was initiated.  This is an HTTP GET Web Script with a path of:
> > `/bulk/import/filesystem/status`
> > This Web Script has HTML (default), JSON and XML views, allowing external programs to programmatically monitor the status of imports.
  1. A stop Web Script that will stop an in-progress import (if any).  This is an HTTP GET Web Script with a path of:
> > `/bulk/import/filesystem/stop`
> > This Web Script has an HTML (default) and a JSON view, allowing external programs to programmatically stop an in-progress import.
  1. A data dictionary Web Script that will display the entire data dictionary that's registered with the Alfresco server.  This is an HTTP GET Web Script with a path of:
> > `/bulk/import/filesystem/datadictionary`
> > This Web Script has an HTML (default) view only, and is intended as a troubleshooting tool when custom metadata properties aren't being populated as expected.

Note: the paths shown above are **not** the full URLs.  As with any Alfresco Web Scripts, you need to prefix them with the access protocol (HTTP or HTTPS), host, port, Alfresco webapp context and service path.  For example if you have the tool installed into a default Alfresco instance running on your local machine, the following URL will take you to the "UI" Web Script:


> http://localhost:8080/alfresco/service/bulk/import/filesystem

# The 'UI' Web Script #

The "UI" Web Script presents the following simplified HTML form:

<img src='http://wiki.alfresco-bulk-filesystem-import.googlecode.com/hg/images/UIWebScriptScreenshot.png' align='center' />

The 'Import directory' field is required and indicates the absolute filesystem directory to load the content and spaces from, in an OS-specific format.  Note that this directory must be locally accessible to the server the Alfresco instance is running on - it must either be a local filesystem or a locally mounted remote filesystem (i.e. mounted using NFS, GFS, CIFS or similar).

The 'Target space' field is also required and indicates the target space to load the content into, as a path starting with "/Company Home".  The separator character is Unix-style (i.e. "/"), regardless of the platform Alfresco is running on.  This field includes an AJAX auto-suggest feature, so you may type any part of the target space name, and an AJAX search will be performed to find and display matching spaces.

The 'Replace existing files' checkbox indicates whether to replace nodes that already exist in the repository (checked) or skip them (unchecked).  Note that if versioning is enabled for a node, the node's existing content & metadata will be preserved as the prior version and the new content and/or metadata will be written into the head revision.

Once an import has been initiated, the status Web Script will be automatically displayed.

# The Status Web Script #

The Status Web Script displays status information in two panels and provides an easy way for any Alfresco administrator to monitor and/or terminate an in-progress import.

The first information panel (which is expanded by default) displays throughput graphs while an import is in progress, on browsers that support the canvas tag:

<img src='http://wiki.alfresco-bulk-filesystem-import.googlecode.com/hg/images/StatusWebScriptGraphsScreenshot.png' align='center' />

The second information panel (which is collapsed by default) displays detailed status information in textual format:

<img src='http://wiki.alfresco-bulk-filesystem-import.googlecode.com/hg/images/StatusWebScriptDetailsScreenshot.png' align='center' />

This information is also available in JSON format by appending the ".json" suffix to the URL, and in XML format by appending the ".xml" suffix to the URL.

Please note that the precise status information that's displayed by the tool is continually being tweaked.  For now it's safest to assume that it will change with most releases of the tool.


---

Back to [usage](Usage.md).