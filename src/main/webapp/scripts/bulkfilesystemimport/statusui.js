/*
 * Copyright (C) 2012 Peter Monks.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This file is part of an unsupported extension to Alfresco.
 * 
 */

/*
 * This file contains the browser functionality used by the HTML status page.
 */

// Global variables
var statusURI;
var previousData;
var currentData;
var filesPerSecondChart;
var bytesPerSecondChart;
var getImportStatusTimer;
var refreshTextTimer;


/*
 * Boot the UI
 */
function onLoad(alfrescoWebScriptContext, filesPerSecondCanvasElement, bytesPerSecondCanvasElement)
{
  statusURI = alfrescoWebScriptContext + "/bulk/import/filesystem/status.json";
  
  getStatusInfo();  // Pull down an initial set of status info
  startImportStatusTimer();
  startRefreshTextTimer();
  startFilesPerSecondChart(filesPerSecondCanvasElement);
  startBytesPerSecondChart(bytesPerSecondCanvasElement);
}


/*
 * Toggle visibility of two elements
 */
function toggleDivs(elementToHide, elementToShow)
{
  elementToHide.style.display = "none";
  elementToShow.style.display = "block";
}

/*
 * Get status information via an AJAX call
 */
function getStatusInfo()
{
  YUI().use("io-base", function(Y)
  {
    function complete(id, o, args)
    {
      // Parse the JSON response
      YUI().use('json-parse', function(Y)
      {
        try
        {
          previousData = currentData;
          currentData  = Y.JSON.parse(o.responseText);
        }
        catch (e)
        {
          //####TODO: better error handling...
          document.getElementById("errorMessage").innerHTML = "JSON parsing exception: " + e;
        }
      });

      // If we're idle, stop the world
      if (currentData != null && currentData.currentStatus === "Idle")
      {
        // Update the text one last time
        refreshTextElements(currentData);
        
        // Kill all the timers
        if (filesPerSecondChart  != null) filesPerSecondChart.stop();
        if (bytesPerSecondChart  != null) bytesPerSecondChart.stop();
        if (getImportStatusTimer != null) getImportStatusTimer.stop();
        if (refreshTextTimer     != null) refreshTextTimer.stop();
      }
    };

    Y.on('io:complete', complete, Y, null);
    var request = Y.io(statusURI);
  });
}


/*
 * Start the timer that periodically pulls the import status info down
 */
function startImportStatusTimer()
{
	YUI( { gallery: 'gallery-2012.07.25-21-36' } ).use('gallery-timer', function(Y)
	{
	  var getImportStatus = function()
	    {
	      Y.log('Retrieving import status information...', 'debug');
	
	      getStatusInfo();
	    };
	
	  getImportStatusTimer = new Y.Timer( { length : 1000, repeatCount : 0, callback : getImportStatus } );
	
	  getImportStatusTimer.on('timer:start', function(e) { Y.log('AJAX timer started', 'debug') });
	  getImportStatusTimer.on('timer:stop',  function(e) { Y.log('AJAX timer stopped', 'debug') });
	
	  getImportStatusTimer.start();
	});
}


/*
 * Start the timer that refreshes the details section of the page
 */
function startRefreshTextTimer()
{
  YUI( { gallery: 'gallery-2012.07.25-21-36' } ).use('gallery-timer', function(Y)
  {
	  var refreshText = function()
	    {
	      Y.log('Refreshing text elements on page...', 'debug');
	      
	      refreshTextElements(currentData);
	    };
	
	  refreshTextTimer = new Y.Timer( { length : 2000, repeatCount : 0, callback : refreshText } );
	
	  refreshTextTimer.on('timer:start', function(e) { Y.log('Refresh text timer started', 'debug') });
	  refreshTextTimer.on('timer:stop',  function(e) { Y.log('Refresh text timer stopped', 'debug') });
	
	  refreshTextTimer.start();
	});
}


/*
 * Start the "files per second" chart
 */
function startFilesPerSecondChart(canvasElement)
{
	// Initialise the files per second chart
	filesPerSecondChart = new SmoothieChart({
	  grid: { strokeStyle      :'rgb(127, 127, 127)',
	          fillStyle        :'rgb(0, 0, 0)',
	          lineWidth        : 1,
	          millisPerLine    : 500,
	          verticalSections : 10 },
	  labels: { fillStyle :'rgb(255, 255, 255)' }
	});
	filesPerSecondChart.streamTo(canvasElement, 1000);  // 1 second delay in rendering (for extra smoothiness!)
	
	// Data
	var fileScannedTimeSeries  = new TimeSeries();
	var filesReadTimeSeries    = new TimeSeries();
	var nodesCreatedTimeSeries = new TimeSeries();
	var filesZeroTimeSeries    = new TimeSeries();
	
	// Update the graph every second
	setInterval(function()
	{
	  var now            = new Date().getTime();
	  var pd             = previousData;
	  var cd             = currentData;
	  var filesScanned   = 0;
	  var filesRead      = 0;
	  var nodesCreated   = 0;
	
	  if (cd != null)
	  {
	    filesScanned = cd.sourceStatistics.filesScanned;
	    filesRead    = cd.sourceStatistics.contentFilesRead + cd.sourceStatistics.metadataFilesRead + cd.sourceStatistics.contentVersionFilesRead + cd.sourceStatistics.metadataVersionFilesRead;
	    nodesCreated = cd.targetStatistics.contentNodesCreated;
	
	    if (pd != null)
	    {
	      filesScanned = Math.max(0, filesScanned - pd.sourceStatistics.filesScanned);
	      filesRead    = Math.max(0, filesRead    - (pd.sourceStatistics.contentFilesRead + pd.sourceStatistics.metadataFilesRead + pd.sourceStatistics.contentVersionFilesRead + pd.sourceStatistics.metadataVersionFilesRead));
	      nodesCreated = Math.max(0, nodesCreated - pd.targetStatistics.contentNodesCreated);
	    }
	  }
	
	  fileScannedTimeSeries.append( now, filesScanned);
	  filesReadTimeSeries.append(   now, filesRead);
	  nodesCreatedTimeSeries.append(now, nodesCreated);
	  filesZeroTimeSeries.append(   now, 0); // Used to keep a fixed baseline - I don't like how smoothie has a variable baseline
	}, 1000);  // Update every second
	
	// Add the time series' to the chart
	filesPerSecondChart.addTimeSeries(fileScannedTimeSeries,  { strokeStyle:'rgb(255, 0, 0)', fillStyle:'rgba(255, 0, 0, 0.0)', lineWidth:3 } );
	filesPerSecondChart.addTimeSeries(filesReadTimeSeries,    { strokeStyle:'rgb(0, 255, 0)', fillStyle:'rgba(0, 255, 0, 0.0)', lineWidth:3 } );
	filesPerSecondChart.addTimeSeries(nodesCreatedTimeSeries, { strokeStyle:'rgb(0, 0, 255)', fillStyle:'rgba(0, 0, 255, 0.0)', lineWidth:3 } );
	filesPerSecondChart.addTimeSeries(filesZeroTimeSeries,    { strokeStyle:'rgba(0, 0, 0, 0)', fillStyle:'rgba(0, 0, 0, 0.0)', lineWidth:0 } );
}


/*
 * Start the "bytes per second" chart
 */
function startBytesPerSecondChart(canvasElement)
{
  // Initialise the bytes per second chart
  bytesPerSecondChart = new SmoothieChart({
    grid: { strokeStyle      :'rgb(127, 127, 127)',
            fillStyle        :'rgb(0, 0, 0)',
            lineWidth        : 1,
            millisPerLine    : 500,
            verticalSections : 10 },
    labels: { fillStyle :'rgb(255, 255, 255)' }
  });
  bytesPerSecondChart.streamTo(canvasElement, 1000);  // 1 second delay in rendering (for extra smoothiness!)
  
  // Data
  var bytesReadTimeSeries    = new TimeSeries();
  var bytesWrittenTimeSeries = new TimeSeries();
  var bytesZeroTimeSeries    = new TimeSeries();
  
  // Update the graph every second
  setInterval(function()
  {
    var now            = new Date().getTime();
    var pd             = previousData;
    var cd             = currentData;
    var bytesRead      = 0;
    var bytesWritten   = 0;
  
    if (cd != null)
    {
      bytesRead    = cd.sourceStatistics.contentBytesRead + cd.sourceStatistics.contentVersionBytesRead;
      bytesWritten = cd.targetStatistics.contentBytesWritten + cd.targetStatistics.contentVersionsBytesWritten;
  
      if (pd != null)
      {
        bytesRead    = Math.max(0, bytesRead    - (pd.sourceStatistics.contentBytesRead + pd.sourceStatistics.contentVersionBytesRead));
        bytesWritten = Math.max(0, bytesWritten - (pd.targetStatistics.contentBytesWritten + pd.targetStatistics.contentVersionsBytesWritten));
      }
    }
  
    bytesReadTimeSeries.append(   now, bytesRead);
    bytesWrittenTimeSeries.append(now, bytesWritten);
    bytesZeroTimeSeries.append(   now, 0); // Used to keep a fixed baseline - I don't like how smoothie has a variable baseline
  }, 1000);  // Update every second
  
  // Add the time series' to the chart
  bytesPerSecondChart.addTimeSeries(bytesReadTimeSeries,    { strokeStyle:'rgb(0, 255, 0)', fillStyle:'rgba(0, 255, 0, 0.0)', lineWidth:3 } );
  bytesPerSecondChart.addTimeSeries(bytesWrittenTimeSeries, { strokeStyle:'rgb(0, 0, 255)', fillStyle:'rgba(0, 0, 255, 0.0)', lineWidth:3 } );
  bytesPerSecondChart.addTimeSeries(bytesZeroTimeSeries,    { strokeStyle:'rgba(0, 0, 0, 0)', fillStyle:'rgba(0, 0, 0, 0.0)', lineWidth:0 } );
}


/*
 * Refresh all of the text elements on the page.
 */
function refreshTextElements(cd)
{
  if (cd != null)
  {
    // Status
    document.getElementById("currentStatus").textContent = cd.currentStatus;
    if (cd.currentStatus === "Idle") document.getElementById("currentStatus").style.color = "green";
    
    // Threads
    document.getElementById("detailsActiveThreads").textContent = cd.activeThreads;
    
    // End date
    if (cd.endDate) document.getElementById("detailsEndDate").textContent = cd.endDate;  //####TODO: format this
    
    // Duration
    document.getElementById("detailsDurationCaption").textContent = cd.endDate ? "Duration:" : "Elapsed Time:";
    document.getElementById("detailsDuration").textContent        = formatDuration(cd.durationInNS);
    
    // Completed batches
    document.getElementById("detailsCompletedBatches").textContent = cd.completedBatches;
    
    // Current file or folder
    document.getElementById("detailsCurrentFileOrFolder").textContent = cd.currentFileOrFolder;
    
    // Source (read) statistics
    document.getElementById("detailsFoldersScanned").textContent           = cd.sourceStatistics.foldersScanned;
    document.getElementById("detailsFilesScanned").textContent             = cd.sourceStatistics.filesScanned;
    document.getElementById("detailsUnreadableEntries").textContent        = cd.sourceStatistics.unreadableEntries;
    document.getElementById("detailsContentFilesRead").textContent         = cd.sourceStatistics.contentFilesRead;
    document.getElementById("detailsContentBytesRead").textContent         = formatBytes(cd.sourceStatistics.contentBytesRead);
    document.getElementById("detailsMetadataFilesRead").textContent        = cd.sourceStatistics.metadataFilesRead;
    document.getElementById("detailsMetadataBytesRead").textContent        = formatBytes(cd.sourceStatistics.metadataBytesRead);
    document.getElementById("detailsContentVersionFilesRead").textContent  = cd.sourceStatistics.contentVersionFilesRead;
    document.getElementById("detailsContentVersionBytesRead").textContent  = formatBytes(cd.sourceStatistics.contentVersionBytesRead);
    document.getElementById("detailsMetadataVersionFilesRead").textContent = cd.sourceStatistics.metadataVersionFilesRead;
    document.getElementById("detailsMetadataVersionBytesRead").textContent = formatBytes(cd.sourceStatistics.metadataVersionBytesRead);
    
    // Throughput (read)
    var durationInS = cd.durationInNS / (1000 * 1000 * 1000);
    document.getElementById("detailsEntriesScannedPerSecond").textContent = roundToDigits((cd.sourceStatistics.filesScanned +
                                                                                           cd.sourceStatistics.foldersScanned) / durationInS, 2);
    document.getElementById("detailsFilesReadPerSecond").textContent      = roundToDigits((cd.sourceStatistics.contentFilesRead +
                                                                                           cd.sourceStatistics.metadataFilesRead +
                                                                                           cd.sourceStatistics.contentVersionFilesRead +
                                                                                           cd.sourceStatistics.metadataVersionFilesRead) / durationInS, 2);
    document.getElementById("detailsDataReadPerSecond").textContent       = formatBytes((cd.sourceStatistics.contentBytesRead +
                                                                                         cd.sourceStatistics.metadataBytesRead +
                                                                                         cd.sourceStatistics.contentVersionBytesRead +
                                                                                         cd.sourceStatistics.metadataVersionBytesRead) / durationInS);
    
    // Target (write) statistics
    document.getElementById("detailsSpaceNodesCreated").textContent               = cd.targetStatistics.spaceNodesCreated;
    document.getElementById("detailsSpaceNodesReplaced").textContent              = cd.targetStatistics.spaceNodesReplaced;
    document.getElementById("detailsSpaceNodesSkipped").textContent               = cd.targetStatistics.spaceNodesSkipped;
    document.getElementById("detailsSpacePropertiesWritten").textContent          = cd.targetStatistics.spacePropertiesWritten;
    document.getElementById("detailsContentNodesCreated").textContent             = cd.targetStatistics.contentNodesCreated;
    document.getElementById("detailsContentNodesReplaced").textContent            = cd.targetStatistics.contentNodesReplaced;
    document.getElementById("detailsContentNodesSkipped").textContent             = cd.targetStatistics.contentNodesSkipped;
    document.getElementById("detailsContentBytesWritten").textContent             = formatBytes(cd.targetStatistics.contentBytesWritten);
    document.getElementById("detailsContentPropertiesWritten").textContent        = cd.targetStatistics.contentPropertiesWritten;
    document.getElementById("detailsContentVersionsCreated").textContent          = cd.targetStatistics.contentVersionsCreated;
    document.getElementById("detailsContentVersionBytesWritten").textContent      = formatBytes(cd.targetStatistics.contentVersionsBytesWritten);
    document.getElementById("detailsContentVersionPropertiesWritten").textContent = cd.targetStatistics.contentVersionsPropertiesWritten;
    
    // Throughput (write)
    document.getElementById("detailsNodesWrittenPerSecond").textContent = roundToDigits((cd.targetStatistics.spaceNodesCreated +
                                                                                         cd.targetStatistics.spaceNodesReplaced +
                                                                                         cd.targetStatistics.contentNodesCreated +
                                                                                         cd.targetStatistics.contentNodesReplaced +
                                                                                         cd.targetStatistics.contentVersionsCreated) / durationInS, 2);
    document.getElementById("detailsDataWrittenPerSecond").textContent  = formatBytes((cd.targetStatistics.contentBytesWritten +
                                                                                       cd.targetStatistics.contentVersionsBytesWritten) / durationInS);

    // Exceptions //####TODO: finish this off
/*
    document.getElementById("detailsFileThatFailed").textContent = ;
    document.getElementById("detailsLastException").textContent = ;
*/
  }
}


function formatDuration(durationInNs)
{
  var days         = Math.floor(durationInNs / (1000 * 1000 * 1000 * 60 * 60 * 24));
  var hours        = Math.floor(durationInNs / (1000 * 1000 * 1000 * 60 * 60)) % 24;
  var minutes      = Math.floor(durationInNs / (1000 * 1000 * 1000 * 60)) % 60;
  var seconds      = Math.floor(durationInNs / (1000 * 1000 * 1000)) % 60;
  var milliseconds = Math.floor(durationInNs / (1000 * 1000)) % 1000;
  var microseconds = Math.floor(durationInNs / (1000)) % 1000;
  
  return("" + days + "d " + hours + "h " + minutes + "m " + seconds + "s " + milliseconds + "." + microseconds + "ms");
}


function formatBytes(bytes)
{
  if      (bytes > (1024 * 1024 * 1024 * 1024 * 1024)) return("" + roundToDigits(bytes / (1024 * 1024 * 1024 * 1024 * 1024), 2) + "PB");
  else if (bytes > (1024 * 1024 * 1024 * 1024))        return("" + roundToDigits(bytes / (1024 * 1024 * 1024 * 1024), 2) + "TB");
  else if (bytes > (1024 * 1024 * 1024))               return("" + roundToDigits(bytes / (1024 * 1024 * 1024), 2) + "GB");
  else if (bytes > (1024 * 1024))                      return("" + roundToDigits(bytes / (1024 * 1024), 2) + "MB");
  else if (bytes > 1024)                               return("" + roundToDigits(bytes / 1024, 2) + "kB");
  else                                                 return("" + bytes + "B");
}


function roundToDigits(number, numberOfDigits)
{
  var multiplicationFactor = Math.pow(10, numberOfDigits);
  
  return(Math.round(number * multiplicationFactor) / multiplicationFactor);
}