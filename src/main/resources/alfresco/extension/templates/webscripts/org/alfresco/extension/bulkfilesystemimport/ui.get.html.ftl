[#ftl]
<!DOCTYPE HTML>
<html>
  <head>
    <title>Bulk Filesystem Import Tool</title>
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">

    <!-- YUI 3.x -->
    <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/combo?3.3.0/build/widget/assets/skins/sam/widget.css&3.3.0/build/widget/assets/skins/sam/widget-stack.css&3.3.0/build/autocomplete/assets/skins/sam/autocomplete.css">
    <script type="text/javascript" src="http://yui.yahooapis.com/combo?3.3.0/build/yui/yui-min.js&3.3.0/build/intl/intl-min.js&3.3.0/build/autocomplete/lang/autocomplete.js&3.3.0/build/collection/array-extras-min.js&3.3.0/build/oop/oop-min.js&3.3.0/build/event-custom/event-custom-min.js&3.3.0/build/attribute/attribute-min.js&3.3.0/build/base/base-base-min.js&3.3.0/build/base/base-build-min.js&3.3.0/build/escape/escape-min.js&3.3.0/build/dom/dom-base-min.js&3.3.0/build/dom/selector-native-min.js&3.3.0/build/dom/selector-css2-min.js&3.3.0/build/event/event-base-min.js&3.3.0/build/node/node-base-min.js&3.3.0/build/event/event-base-ie-min.js&3.3.0/build/event/event-synthetic-min.js&3.3.0/build/event/event-focus-min.js&3.3.0/build/event-valuechange/event-valuechange-min.js&3.3.0/build/querystring/querystring-stringify-simple-min.js&3.3.0/build/io/io-base-min.js&3.3.0/build/json/json-parse-min.js&3.3.0/build/jsonp/jsonp-min.js&3.3.0/build/jsonp/jsonp-url-min.js&3.3.0/build/yql/yql-min.js&3.3.0/build/dom/selector-css3-min.js&3.3.0/build/pluginhost/pluginhost-min.js&3.3.0/build/base/base-pluginhost-min.js&3.3.0/build/dom/dom-style-min.js&3.3.0/build/dom/dom-style-ie-min.js&3.3.0/build/node/node-style-min.js&3.3.0/build/classnamemanager/classnamemanager-min.js&3.3.0/build/event/event-delegate-min.js&3.3.0/build/node/node-event-delegate-min.js&3.3.0/build/widget/widget-min.js&3.3.0/build/widget/widget-base-ie-min.js&3.3.0/build/dom/dom-screen-min.js&3.3.0/build/node/node-screen-min.js&3.3.0/build/widget/widget-position-min.js&3.3.0/build/widget/widget-position-align-min.js&3.3.0/build/widget/widget-stack-min.js&3.3.0/build/autocomplete/autocomplete-min.js&3.3.0/build/autocomplete/autocomplete-list-keys-min.js&3.3.0/build/autocomplete/autocomplete-list-keys-min.js"></script>
    
    <style type="text/css">
      .yui3-aclist-content {
        background-color   : white;
        border             : 1px solid darkgrey;
        box-shadow         : 3px 3px 4px lightgrey;
        -webkit-box-shadow : 3px 3px 4px lightgrey; /* Safari and Chrome */
        
       }
    </style>
    
    <!-- Validation functions -->
    <script type="text/javascript">
      function validateRequired(field, errorMessageElement, errorMessage)
      {
        var result = true;

        if (field.value == null || field.value == "")
        {
          errorMessageElement.textContent = errorMessage;
          result = false;
        }
        else
        {
          errorMessageElement.textContent = "";
        }

        return result;
      }


      function validateForm(form)
      {
        var result = true;

        result = validateRequired(form.sourceDirectory, document.getElementById("sourceDirectoryMessage"), "Source directory is mandatory.");

        if (result)
        {
          result = validateRequired(form.targetPath, document.getElementById("targetPathMessage"), "Target space is mandatory.");
        }

        return result;
      }
    </script>
  </head>
  <body class="yui-skin-sam">
    <table>
      <tr>
        <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
        <td><nobr>Bulk Filesystem Import Tool</nobr></td>
      </tr>
      <tr><td><td>Alfresco ${server.edition} v${server.version}
    </table>
    <form action="${url.service}/initiate" method="get" enctype="multipart/form-data" charset="utf-8" onsubmit="return validateForm(this);">
      <table>
        <tr>
          <td>Import directory:</td><td><input type="text" name="sourceDirectory" size="128" /></td><td id="sourceDirectoryMessage" style="color:red"></td>
        </tr>
        <tr>
          <td><br/><label for="targetPath">Target space:</label></td>
          <td>
            <div id="targetNodeRefAutoComplete">
              <input id="targetPath" type="text" name="targetPath" size="128" />
              <div id="targetPathAutoSuggestContainer"></div>
            </div>
          </td>
          <td id="targetPathMessage" style="color:red"></td>
        </tr>
        <tr>
          <td colspan="3">&nbsp;</td>
        </tr>
        <tr>
          <td><label for="replaceExisting">Replace existing files:</label></td><td><input type="checkbox" id="replaceExisting" name="replaceExisting" value="replaceExisting" unchecked/> (unchecked means skip files that already exist in the repository)</td><td></td>
        </tr>
        <tr>
          <td colspan="3">&nbsp;</td>
        </tr>
        <tr>
          <td colspan="3"><input type="submit" name="submit" value="Initiate Bulk Import"></td>
        </tr>
      </table>
      <br/>
    </form>
    <script type="text/javascript">
    YUI().use("autocomplete", "autocomplete-highlighters", "datasource-get", function(Y)
    {
      Y.one('#targetPath').plug(Y.Plugin.AutoComplete,
      {
        source            : '${url.service}/ajax/suggest/spaces.json?query={query}',
        maxResults        : 25,
        resultHighlighter : 'phraseMatch',
        resultListLocator : 'data',
        resultTextLocator : 'path'
      });
    });
    </script>    
  </body>
</html>
