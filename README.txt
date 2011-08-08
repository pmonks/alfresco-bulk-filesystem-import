Description
-----------
This module provides a bulk import process that will load content into the
repository from the local filesystem.  It will (optionally) replace existing
content items if they already exist in the repository, but does _not_ perform
deletes (ie. this module is _not_ designed to fully synchronise the repository
with the local filesystem).

The module also provides a facility to load metadata for the files and spaces
being ingested.  This mechanism is pluggable (allowing metadata to be loaded
via various different strategies and from various different sources) -
currently the code includes:

* a basic metadata loader that determines the type of the object ("cm:content"
  for files or "cm:folder" for folders) and populates the "cm:name" and
  "cm:title" properties with the filename of the file as it appears on disk
* an properties metadata loader that reads a "shadow" metadata file in Java
  properties format and loads the type, aspects and properties listed there
  into the associated content.  Note that as of v1.0 this metadata loader is
  disabled by default.
* an XML properties metadata loader that reads a "shadow" metadata file in
  Java XML properties format and loads the type, aspects and properties listed
  there into the associated content. Note that as of v1.0 this metadata loader
  is enabled the default.

Metadata loaders for other formats (eg. JSON, YAML) may follow eventually.

As of v1.0, the module also supports the loading of version histories for
files. Each version may consist of content, metadata or both.

Please don't hesitate to contact the author if you'd like to contribute!


Author
------
Peter Monks (reverse sknompATocserflaDOTmoc)


Pre-requisites
--------------
Alfresco 3.3 or better (this package tested on Alfresco Enterprise 3.4SP3).

Alfresco 3.0 to 3.2 can use the older v0.8 version of the AMP - I am no longer
actively back porting bug fixes / enhancements to pre-3.3 versions of
Alfresco.


Installation
------------
1. ALWAYS BACKUP YOUR ORIGINAL ALFRESCO.WAR FILE BEFORE APPLYING *ANY* AMPS
   TO YOUR INSTALLATION!!
   Although the apply_amps script will backup your alfresco.war, it's better
   to manually back it up prior to installing *any* AMP files, so that you
   can roll back to a pristine binary if needed.
2. Copy the provided AMP file into the ${ALFRESCO_HOME}/amps directory
3. Run the ${ALFRESCO_HOME}/apply_amps[.sh|.bat] script to install the AMP
   into your Alfresco instance


Preparation of the Source Content and Folder Structure
------------------------------------------------------
"Normal" (non-metadata) files and folders on disk are loaded verbatim by the
tool.  In other words, if you have a file called "bar.txt" in a directory
called "fu", a space will be created in Alfresco called "fu" and a content
node (aka "file") will be created in that space called "bar.txt".  In other
words, no mapping or transformation takes place - whatever is on disk will be
loaded directly into Alfresco.


Preparation of Metadata
-----------------------
In addition, the tool has the ability to load metadata (types, aspects & their
properties) into the repository.  This is accomplished using "shadow" Java
property files in XML format (*not* the old key=value format that was used in
earlier versions of the tool - the XML format has significantly better support
for Unicode characters).  These shadow properties files must have *exactly*
the same name and extension as the file for which it describes the metadata,
but with the suffix ".metadata.properties.xml".  So for example, if there is a
file called "IMG_1967.jpg", the "shadow" metadata file for it would be called
"IMG_1967.jpg.metadata.properties.xml".

These shadow files can also be used for directories - e.g. if you have a
directory called "MyDocuments", the shadow metadata file would be called
"MyDocuments.metadata.properties.xml".

The metadata file itself follows the usual syntax for Java XML properties
files:

  <?xml version="1.0" encoding="UTF-8"?>
  <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
  <properties>
    <entry key="key1">value1</entry>
    <entry key="key2">value2</entry>
    ...
  </properties>

There are two special keys:

  1. "type" - contains the qualified name of the content type to use for the
     file or folder
  2. "aspects" - contains a comma-delimited list of the qualified names of the
     aspect(s) to attach to the file or folder
     
The remaining entries in the file are treated as metadata properties, with the
key being the qualified name of the property and the value being the value of
that property.  Multi-valued properties are comma-delimited, but please note
that these values are *not* trimmed so it's advisable to not place a space
character either before or after the comma, unless you actually want that in
the value of the property.

Here's a fully worked example for IMG_1967.jpg.metadata.properties.xml:

  <?xml version="1.0" encoding="UTF-8"?>
  <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
  <properties>
    <entry key="type">cm:content</entry>
    <entry key="aspects">cm:versionable,cm:dublincore</entry>
    <entry key="cm:title">A photo of a flower.</entry>
    <entry key="cm:description">A photo I took of a flower while walking around Bantry Bay.</entry>
    <entry key="cm:created">1901-01-01T12:34:56.789+10:00</entry>
    <!-- cm:dublincore properties -->
    <entry key="cm:author">Peter Monks</entry>
    <entry key="cm:publisher">Peter Monks</entry>
    <entry key="cm:contributor">Peter Monks</entry>
    <entry key="cm:type">Photograph</entry>
    <entry key="cm:identifier">IMG_1967.jpg</entry>
    <entry key="cm:dcsource">Canon Powershot G2</entry>
    <entry key="cm:coverage">Worldwide</entry>
    <entry key="cm:rights">Copyright (c) Peter Monks 2002, All Rights Reserved</entry>
    <entry key="cm:subject">A photo of a flower.</entry>
  </properties>

Additional notes on metadata loading:
  * the metadata must conform to the type and aspect definitions configured in
    Alfresco (including mandatory fields, constraints and data types).  Any
    violations will terminate the bulk import process.
  * associations between content items loaded by the tool are not yet nicely
    supported - see issue #10 [1]
    Associations to objects that are already in the repository can be created
    using the NodeRef of the target object as the value of the property
  * non-string data types (including numeric and date types) have not been
    exhaustively tested.  Date values have been tested and do work when
    specified using ISO8601 format [2].  See [3] for more details on this
    issue.
  * updating the aspects or metadata on existing content will _not_ remove
    any existing aspects not listed in the new metadata file - this tool is
    not intended to provide a full filesystem synchronisation mechanism
  * the metadata loading facility can be used to decorate content that's
    already in the Alfresco repository, without having to upload that content
    again.  To use this mechanism, create a "naked" metadata file in the same
    path as the target content file - the tool will match it up with the file
    in the repository and decorate that existing file with the new aspect(s)
    and/or metadata

[1] http://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=10
[2] http://www.iso.org/iso/date_and_time_format
[3] http://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=19


Preparation of Versions
-----------------------
The import tool also supports loading a version history for each file
(Alfresco doesn't support version histories for folder).  To do this, create
a file with the same name as the main file, but append a "v#" extension.  For
example:

  IMG_1967.jpg.v1   <- version 1 of the file
  IMG_1967.jpg.v2   <- version 2 of the file
  IMG_1967.jpg      <- "head" (latest) revision of the file

This also applies to metadata files, if you wish to capture metadata history
as well.  For example:

  IMG_1967.jpg.metadata.properties.xml.v1   <- version 1 of the metadata
  IMG_1967.jpg.metadata.properties.xml.v2   <- version 2 of the metadata
  IMG_1967.jpg.metadata.properties.xml      <- "head" (latest) revision of the
                                               metadata

Additional notes on version history loading:
  * version numbers don't have to be contiguous - you can number your version
    files however you wish, provided you use whole numbers (integers)
  * the version numbers in your version files will *not* be used in Alfresco -
    the version numbers in Alfresco will be contiguous, starting at 1.0 and
    increasing by 1.0 for every version (so 1.0, 2.0, 3.0, etc. etc.).
    Alfresco doesn't allow version labels to be set to arbitrary values, and
    currently the bulk import doesn't provide any way to specify whether a
    given version is a major or minor version (TODO: create enhancement
    request for this)
  * each version can contain a content update a metadata update or both - you
    are not limited to updating everything for every version.  If not included
    in a version, the prior version's content or metadata will remain in place
    for the next version.


Usage
-----
The tool is exposed as a series of 3 Web Scripts:
  1. A simple "UI" Web Script that can be used to manually initiate an
     import.  This is an HTTP GET Web Script with a path of:
       /bulk/import/filesystem
       
  2. An "initiate" Web Script that actually kicks off an import, using
     parameters that are passed to it (for the source directory, target space,
     etc.).  This is an HTTP GET Web Script with a path of:
       /bulk/import/filesystem/initiate
     If you wish to script or programmatically invoke the tool, this is the
     Web Script you would need to call.
       
  3. A status Web Script that returns status information on the current import
     (if one is in progress), or the status of the last import that was
     initiated.  This is an HTTP GET Web Script with a path of:
       /bulk/import/filesystem/status
     This Web Script has both HTML and XML views, allowing external programs
     to programmatically monitor the status of imports.

Note: the paths shown above are *not* the full URLs.  As with any Alfresco Web
Scripts, you need to prefix them with the access protocol (HTTP or HTTPS),
host, port, alfresco content and service path.  For example:

    http://localhost:8080/alfresco/service/bulk/import/filesystem


The "UI" Web Script provides a simple HTML form that has three fields:

    Import directory
    Target space
    Update existing files flag

Import directory is required and indicates the absolute filesystem directory
to load the content and spaces from, in an OS-specific format.  Note that this
directory must be locally accessible to the server the Alfresco instance is
running on - it must either be a local filesystem or a locally mounted remote
filesystem.

Target space is also required and indicates the target space to load the
content into, as a path starting with "/Company Home".  The separator
character is Unix-style (i.e. "/"), regardless of the platform Alfresco is
running on.  This field includes an AJAX auto-suggest feature, so you may type
any part of the target space name, and an AJAX search will be performed to
find and display matching items.

The Replace existing files checkbox indicates whether to replace files that
already exist in the repository (checked) or skip them (unchecked).

Once an import has been initiated, the status Web Script will be automatically
displayed.


Issues / Enhancement Requests
-----------------------------
See http://code.google.com/p/alfresco-bulk-filesystem-import/issues/list


Compiling the Package from Source
---------------------------------
In order to compile the package from source you will need to have both a
Subversion (svn) client and the Maven build tool installed.

1. Checkout the source code:
       svn checkout http://alfresco-bulk-filesystem-import.googlecode.com/svn/trunk/ alfresco-bulk-filesystem-import-read-only

2. If you're an Alfresco Employee:
   1. Follow the instructions at:
          https://svn.alfresco.com/repos/field/maven/README.txt
   2. Change into the checkout directory and build the AMP file using the
      following command:
          mvn clean package
  
   If you're an Enterprise Customer:
   1. Create your own Maven repository and populate it with the Alfresco
      Enterprise artifacts.  You may also wish to raise a support ticket via
      http://network.alfresco.com/ requesting that Alfresco expedite the
      creation of an official Maven repository hosting the Alfresco Enterprise
      artifacts.  This work has been in progress for some time, but at the
      time of writing (2011-01-07) is not yet complete.
   2. Change into the checkout directory and build the AMP file using the
      following command:
          mvn clean package
          
   If you're a Community user:
   1. Change into the checkout directory and build the AMP file using the
      following command:
          mvn -f pom-comm.xml clean package
   NOTE: currently (2011-01-07) there are issues with the maven.alfresco.com
         Community artifact repository that prevent the build from succeeding.
         The authors of the Bulk Filesystem Tool are not directly involved in
         the support or maintenance of that repository but have raised an
         issue against it at http://issues.alfresco.com/jira/browse/BDE-61


Release History
---------------
See CHANGES.