### Release History ###

**v1.3.4** released 2014-06-16
  * Workaround for [issue #149](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#149) - Content nodes missing for version history
  * Fixed [issue #150](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#150) (again)  - In-place imports write incorrect content URLs when configured contentstore has a trailing '/' character
  * Fixed [issue #151](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#151) - Bulk import switches to streaming when dir.root starts with a lower case letter on Windows

**v1.3.3** released 2014-06-11
  * Fixed [issue #150](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#150)  - In-place imports write incorrect content URLs when configured contentstore has a trailing '/' character

**v1.3.2** released 2014-05-08
  * Fixed [issue #147](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#147)  - Problem with major version comparison

**v1.3.1** released 2014-01-10
  * Fixed [issue #146](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#146)  - Current 1.3 release appears to be built with Java 1.7, making it incompatible with 4.0.x and 4.1.x, which require Java 1.6

**v1.3** released 2013-12-02
  * Fixed [issue #54](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#54)  - Allow setting properties to current timestamp
  * Fixed [issue #84](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#84)  - Allow user to specify whether a version is major (x.0) or minor (0.x)
  * Reverted and marked "Won't fix" [issue #109](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#109)  - OutOfMemory with large folder hierarchy
  * Fixed [issue #119](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#119)  - Test against Alfresco 4.2
  * Fixed [issue #125](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#125)  - Windows: incorrect paths being written into content store URLs
  * Fixed [issue #128](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#128)  - Add support for non-cm:contains primary parent/child association types
  * Fixed [issue #129](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#129)  - Contentless nodes
  * Fixed [issue #131](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#131)  - Ensure warning-level log output is enabled for the import tool
  * Fixed [issue #132](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#132)  - Migrate off of FileFolderService? (dependency for  [issue #128](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#128) )
  * Fixed [issue #136](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#136)  - Autosized thread pool should be the default but isn't
  * Fixed [issue #137](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#137)  - Improve support for Multi-Valued properties
  * Fixed [issue #138](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#138)  - Content URLs longer than 256 characters need to halt bulk import
  * Fixed [issue #139](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#139)  - Update build to use alfresco-maven-plugin

**v1.2.1** released 2013-02-11
  * Fixed [issue #123](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#123)  - XML view of status Web Script has syntax error

**v1.2** released 2013-02-06
  * Fixed [issue #77](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#77)  - Add graphical display of throughput
  * Fixed [issue #109](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#109)  - OutOfMemory? with large folder hierarchy
  * Fixed [issue #113](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#113)  - Test against Alfresco 4.1
  * Fixed [issue #114](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#114)  - Import Type is Streaming even when source files are in contentstore
  * Fixed [issue #101](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#101)  - Show 'release version' of the Bulk Ingestion Tool in the header of the pages
  * Fixed [issue #104](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#104) - In-place import when contentstore is a symlink results in error "The node's content is missing"
  * Fixed [issue #108](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#108)  - Status page: "This page will automatically refresh in -3072 seconds"
  * Fixed [issue #111](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#111)  - skipped space nodes always 0 and added together with skipped content nodes
  * Fixed [issue #116](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#116)  - Output the data dictionary in some user-readable way (e.g. debug log, web script)
  * Fixed [issue #110](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#110)  - Create different poms for 3.4 and 4.X Community edition
  * Fixed [issue #121](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#121)  - Add the ability to stop an in-progress import

<no release necessary> 2012-06-11
  * Fixed [issue #61](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#61) - Add dedicated build script for Alfresco Community Edition
  * Fixed [issue #107](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#107) - Switch license to Apache 2.0

**v1.1 (for Alfresco 4)** released 2012-03-06
  * Fixed [issue #92](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#92) - Upgrade for Alfresco 4.0

**v1.1** released 2011-11-03
  * Fixed [issue #86](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#86)  - Add support for importing content that is already located in the contentstore
  * Fixed [issue #8](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#8)  - Implement a multi-threaded bulk filesystem importer
  * Fixed [issue #17](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#17)  - Test various different dimensions to see how they affect performance
  * Fixed [issue #91](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#91)  - DirectoryAnalyserImpl? should use a Map, not a List, for intermediate processing
  * Fixed [issue #97](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#97)  - Importing 2 files with same title in a same folder, stops the importation and throws an NPE
  * Closed [issue #56](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#56) (won't fix)  - Add ability to move files directly into content store, instead of copying _(superceded by [issue #86](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#86))_

**v1.0** released 2011-09-23
  * Renumbered version of RC2

**v1.0-RC2** released 2011-09-02
  * Fixed [issue #82](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#82): Switch SCM to Mercurial
  * Tested [issue #69](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#69): bulk import tool v.11 fails on long property name _(could not reproduce)_
  * Tested [issue #73](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#73): unsearchable metadata _(could not reproduce)_
  * Fixed [issue #81](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#81): Remove content of README and CHANGES files and replace with pointer to wiki
  * Fixed [issue #83](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#83): Import with "Replace existing files:" option unchecked only checks if a file exists in the first level of the tree

**v1.0-RC1** released 2011-08-05
  * Fixed [issue #6](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#6): Implement XML metadata loader
  * Fixed [issue #9](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#9): Add support for specialising types, when a file is updated
  * Fixed [issue #18](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#18): Add support for loading multiple versions of the same file
  * Fixed [issue #21](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#21): Consider not halting bulk ingestion when I/O errors occur
  * Fixed [issue #22](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#22): Test multi-byte characters in metadata
  * Fixed [issue #24](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#24): Generalise the notion of input file filters
  * Fixed [issue #44](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#44): Add metadata-specific information to Bulk Import Status
  * Fixed [issue #52](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#52): Updating just properties only does 'root' folder
  * Fixed [issue #64](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#64): Exception when aspects separated by ", "
  * Fixed [issue #72](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#72): alfresco 3.4 support (official fix - had been patched in 0.11 earlier)
  * Fixed [issue #74](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#74): Skipping files in UTF characters (note: was not reproducible, possibly fixed indirectly by other refactoring work in this release)
  * Fixed [issue #76](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#76): Update YUI to 3.x
  * Significantly enhanced the test data used in testing.
  * Refactored the configuration to allow some aspects to be configured via alfresco-global.properties.

**v0.11** released 2011-01-07
  * Fixed [issue #4](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#4): Content creation and modification dates are not settable
  * Fixed [issue #49](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#49): source for class AbstractMetadataLoader is missing
  * Fixed [issue #61](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#61): Add dedicated build script for Alfresco Community Edition NOTE: Not yet fully fixed due to external dependency (http://issues.alfresco.com/jira/browse/BDE-61)
  * Fixed [issue #20](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#20): Add support for multi-valued properties to properties file metadata loader [to andreadepirro and Stefan.Topfstedt for providing a patch for this](thanks.md)
  * Fixed [issue #60](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#60): Verbiage: substitute the word "update" with the word "replace", to clarify the behaviour of the tool
  * Fixed [issue #51](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#51): Files renamed via metadata cannot be re-imported. Error is: Duplicate child name not allowed

**v0.10** released 2010-10-11
  * Fixed [issue #46](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#46): Error reported by importer
  * Worked around [issue #45](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#45): browser caching makes import tool difficult to test

**v0.9** released 2010-10-01
  * Fixed [issue #39](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#39): Ability to decorate existing content with metadata, without having to upload the content again
  * Fixed [issue #40](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#40): Incorrect filename reported in error message
  * Fixed [issue #43](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#43): Add XML template to status Web Script
  * Fixed [issue #33](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#33): Need to strip whitespace from folder and filenames
  * Fixed [issue #35](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#35): Provide more detailed logging of status during import
  * Fixed [issue #12](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#12): Consider adding more information to the Bulk Import Status bean
  * Fixed [issue #13](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#13): Investigate out-of-order counting in the bulk status bean

**v0.8** released 2010-08-01
  * Fixed [issue #5](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#5): Improve error reporting via status bean
  * Fixed [issue #30](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#30): Can't update existing files
  * Fixed [issue #31](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#31): 3.3 compatability?
  * Fixed [issue #32](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#32): Maven dependencies
  * Fixed [issue #38](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#38): Bad version number in .class file
  * Reduced refresh time for status page to 5 seconds
  * Added duration display to status page

**v0.7** released 2010-04-26
  * Fixed [issue #25](https://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=#25): Cannot import directly to "/Company Home"
  * Reduced default batch size to 100.

<= v0.6 - release history not available


---

Back to the [main page](Main.md).