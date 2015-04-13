# Basic File/Folder Structure #
The basic on-disk file/folder structure is preserved verbatim in the repository.  So for example if you imported the following file/folder tree, the repository would have exactly the same set of spaces and content nodes:

```
sourceFolder
├── folderA
│   ├── subFolderA
│   │   ├── subSubFolderA
│   │   └── subSubFolderB
│   └── subFolderB
├── test_files
│   ├── Logo.png
│   ├── Favicon.ico
│   ├── Main.css
│   └── Functions.js
├── Jpeg_example_JPG_RIP_100.jpg
├── Newtons_cradle_animation_book_2.gif
├── Pdf32000_2008.pdf
├── Png_transparency_demonstration_1.png
├── Sunflower_as_gif_small.gif
├── Test.html
├── Testdoc.doc
├── Testdocx.docx
├── Testpptx.ppt
├── Testpptx.pptx
├── Testtxt.txt
├── Testxls.xls
└── Testxlsx.xlsx
```

# Metadata Files #
In addition, the tool has the ability to load metadata (types, aspects & their properties) into the repository.  This is accomplished using optional "shadow" [Java property files in XML format](http://docs.oracle.com/javase/7/docs/api/java/util/Properties.html).  These shadow properties files must have **exactly** the same name and extension as the file for which it describes the metadata, but with the suffix ".metadata.properties.xml".  So for example, if there is a file called "IMG\_1967.jpg", the "shadow" metadata file for it would be called "IMG\_1967.jpg.metadata.properties.xml" (note the ".jpg"!).

These shadow files can also be used for directories - e.g. if you have a directory called "My Documents", the shadow metadata file would be called "My Documents.metadata.properties.xml".  Note that the shadow metadata file needs to be a sibling of the directory it describes - it cannot be located _within_ that directory.

The metadata file itself follows the usual syntax for Java XML properties files:

```
  <?xml version="1.0" encoding="UTF-8"?>
  <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
  <properties>
    <entry key="key1">value1</entry>
    <entry key="key2">value2</entry>
    ...
  </properties>
```

There are a number of special keys, all of which are optional (they all have sensible default values):

  1. **separator** - (since v1.3) the separator value (a string) to use for delimiting multi-valued properties and the **aspects** special key.  Defaults to a single comma character (",").
  1. **type** - contains the qualified name of the content type to use for the file or folder.  Defaults to either "cm:folder" (for a folder) or "cm:content" (for a file).
  1. **aspects** - contains a delimited list (see **separator**) of the qualified names of the aspect(s) to attach to the file or folder.  Defaults to the empty list (no aspects added to the node, beyond those that are mandatory for cm:folder or cm:content).
  1. **namespace** - (since v1.3) the namespace URI (**not** prefix!) to use for the node.  Defaults to "http://www.alfresco.org/model/content/1.0".
  1. **parentAssociation** - (since v1.3) the parent association type to use for the node.  Defaults to "cm:contains".

The remaining entries in the file are treated as metadata properties, with the key being the qualified name of the property and the value being the value of that property.  Multi-valued properties are delimited (see the **separator** special key above), but **not** trimmed - any whitespace before or after the delimiter value is retained verbatim.

Here's a fully worked example for IMG\_1967.jpg.metadata.properties.xml:

```
  <?xml version="1.0" encoding="UTF-8"?>
  <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
  <properties>
    <entry key="separator"> # </entry>  <!-- 3 character delimiter: space, hash, space -->
    <entry key="namespace">http://www.alfresco.org/model/application/1.0</entry>
    <entry key="parentAssociation">cm:contains</entry>
    <entry key="type">cm:content</entry>
    <entry key="aspects">cm:versionable # cm:dublincore # cm:taggable</entry>
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
    <entry key="cm:rights">Copyright © Peter Monks 2002, All Rights Reserved</entry>
    <entry key="cm:subject">A photo of a flower.</entry>
    <!-- cm:taggable properties -->
    <!-- Note: The following tag NodeRefs will be invalid in your Alfresco installation -->
    <entry key="cm:taggable">workspace://SpacesStore/a7063e59-ef78-46f2-bc00-560b1b9222ab # workspace://SpacesStore/0672094d-3566-4412-a79f-c3f787bfc629</entry>
  </properties>
```

Additional notes on metadata loading:

  * you **must** specify the <?xml?> and <!DOCTYPE> tags as shown above - failing to do so causes the tool to skip the metadata file (with a warning in the Alfresco log).
  * the metadata must conform to the type and aspect definitions configured in Alfresco (including mandatory fields, constraints and data types).  Any violations will terminate the bulk import process.
  * peer associations between content items loaded by the tool are not yet supported - see [issue #10](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#10).
  * associations to objects that are already in the repository can be created, however, by supplying the NodeRef of the target object as the value of the property (see the cm:taggable property in the example above).
  * date values must be specified using [ISO8601 format](http://www.iso.org/iso/date_and_time_format).  See [issue #19](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#19) for more details.
  * (since v1.3) d:date and d:datetime properties can be set to the current date or date/time by specifying the special value "NOW" (note: case-sensitive) as the value.  Note that this value will be calculated as the **server's** current date/time.
  * updating the aspects or metadata on existing content will _not_ remove any existing aspects not listed in the new metadata file - this tool is not intended to provide a full filesystem synchronisation mechanism
  * the metadata loading facility can be used to decorate content that's already in the Alfresco repository, without having to upload that content again.  To use this mechanism, create a shadow metadata file as described above and import it with replace set to true.  The tool will match the shadow file up with the existing file in the repository and decorate it with the new metadata.

# Version History Files #
The import tool also optionally supports loading a version history for each file (Alfresco doesn't support version histories for folders).  To do this, create a file with the same name as the main file, but append a "v#" or "v#.#" extension.  For example:

```
  IMG_1967.jpg.v1       <- version 1 content
  IMG_1967.jpg.v2       <- version 2 content
  IMG_1967.jpg.v2.1     <- version 2.1 content
  IMG_1967.jpg          <- "head" (latest) revision of the content
```

This also applies to metadata files, if you wish to capture metadata history as well.  For example:

```
  IMG_1967.jpg.metadata.properties.xml.v1     <- version 1 metadata
  IMG_1967.jpg.metadata.properties.xml.v2     <- version 2 metadata
  IMG_1967.jpg.metadata.properties.xml.v2.1   <- version 2.1 metadata
  IMG_1967.jpg.metadata.properties.xml        <- "head" (latest) revision of the metadata
```

Additional notes on version history loading:
  * the tool always imports versions in numeric order.  If you have two files with the same numeric version number (e.g. v1 and v1.0), only one of them will be imported; which one gets imported is non-deterministic however.
  * version numbers don't have to be contiguous - you can number your version files however you wish, provided you use valid numbers (integers or decimals).
  * the version numbers in your version files will **not** be used in Alfresco - the version numbers in Alfresco will be contiguous, starting at 1.0 and increasing by 1.0 for every major version (e.g. 1.0, 2.0, 3.0, etc.) and 0.1 for every minor version (e.g. 1.1, 1.2, 1.3 etc.). Alfresco doesn't allow version labels to be set to arbitrary values (see [issue #85](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#85)).
  * each version may contain a content update, a metadata update, or both - you are not limited to updating everything in every version.  If not included in a version, the prior version's content or metadata will remain in place for the next version.

Here's a fully fleshed out example, showing all possible combinations of content, metadata and version files:

```
  IMG_1967.jpg.v1                             <- version 1 content
  IMG_1967.jpg.metadata.properties.xml.v1     <- version 1 metadata
  IMG_1967.jpg.v1.1                           <- version 1.1 content
  IMG_1967.jpg.metadata.properties.xml.v1.1   <- version 1.1 metadata
  IMG_1967.jpg.v2                             <- version 2 content
  IMG_1967.jpg.metadata.properties.xml.v2     <- version 2 metadata
  IMG_1967.jpg.v2.1                           <- version 2.1 content (content only version)
  IMG_1967.jpg.metadata.properties.xml.v3     <- version 3 metadata (metadata only version)
  IMG_1967.jpg.metadata.properties.xml        <- "head" (latest) revision of the metadata
  IMG_1967.jpg                                <- "head" (latest) revision of the content
```

# In Place Imports #
Since v1.1 the tool also supports "in-place" imports if you're using the default [FileContentStore](http://dev.alfresco.com/resource/docs/java/repository/org/alfresco/repo/content/filestore/FileContentStore.html) for content storage.  To use this feature, simply place your source directory somewhere underneath the Alfresco content store (default location is ${ALFRESCO\_HOME}/alf\_data/contentstore/) and then use that location as the source directory as per normal.  The tool will automatically detect that the source directory is located in the contentstore and perform an in-place import instead of a streaming import (the status page also displays the type of import, if you wish to confirm that this is occurring correctly).

The primary benefit of in-place imports is that no content copying needs to be performed - instead the source files are simply "linked" into the repository as new nodes.  For large-size content imports the performance benefits of taking this approach are sizable.  Some examples:
  * The "FileSizeTests" unit test (9 files totaling 4.1GB) is approximately 400X faster when performed as an in-place import, and that's probably an underestimate as currently the tool can't accurately time imports that take less than 250ms (which is how long the FileSizeTest is reported as taking).
  * An Alfresco Enterprise customer in Europe imported approximately 10,000 files totaling 6.6TB (i.e. a relatively small number of relatively large files) in under 3 minutes, using in-place import.
  * Another Alfresco Enterprise customer in the US imported approximately 40,000,000 files totaling 2.1TB in around 17 hours

Notes:
  * You do not need to modify the structure of the source directory to match the FileContentStore's "timestamp hashbucket" structure.  In fact doing so is counterproductive as that directory structure would then be imported into Alfresco (which is unlikely to be the desired directory structure visible to end users).
  * For ease of management later on, it's recommended that you place the source directory in a clearly named subdirectory of the contentstore.  For example ${ALFRESCO\_HOME}/alf\_data/contentstore/bulk\_import.
  * Once imported, you must not touch in any way (modify, rename, move or delete - even opening a file for reading is not recommended) any of the content files in the source directory.  Of course this rule holds true more generally for any content in the contentstore, but is worth reiterating for the in-place import case (particularly since bulk imported files won't be obfuscated by the FileContentStore's timestamp/guid.bin hashbucket naming convention).
  * Because the shadow metadata files used by the import tool are never registered with the repository, Alfresco will not automatically clean them up after an import is complete.  If you wish to clean these files up you will need to manually do so after you're sure the import has succeeded.  While doing this you must be extremely careful not to remove any files that were, in fact, imported (see the previous point).
  * In-place imports currently don't work if you're using the anything but the default [FileContentStore](http://dev.alfresco.com/resource/docs/java/repository/org/alfresco/repo/content/filestore/FileContentStore.html) (default in Alfresco 3.x) or [AbstractTenantRoutingContentStore](http://dev.alfresco.com/resource/docs/java/repository/org/alfresco/repo/tenant/AbstractTenantRoutingContentStore.html) (default in Alfresco 4.x) for content storage, however there is no technical reason Alfresco's other content storage mechanisms (specifically the [Content Store Selector](http://wiki.alfresco.com/wiki/Content_Store_Selector)) couldn't also be supported.  This is being tracked as [issue #95](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#95) - please comment on that issue if this is a priority for you.
  * The contentstore-relative paths ("content URL") of in-place imported content must not exceed 255 characters.  This is an Alfresco limitation, and as of v1.3 the tool will halt if it detects a content URL longer than this.

# Test Data #
If you're using the tool to populate Alfresco with test data, you may find [this tool](https://github.com/lcabaceira/supersizemyrepo) useful.  It can auto-generate files, folders and metadata in the format the tool expects, and offers some nice control over file format(s), folder sizes, etc.


---

Back to [usage](Usage.md).