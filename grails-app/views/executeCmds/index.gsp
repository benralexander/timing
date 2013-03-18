<%--
  Created by IntelliJ IDEA.
  User: balexand
  Date: 3/13/13
  Time: 8:27 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
  <title>Timing tests</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
    <script>

        $(document).ready(function () {
            $('#assayListing').click(function () {

                $.ajax({
                    url: '/timtest/executeCmds/assaylist',
                    type: 'GET',
                    cache: false,
                    success: function (molSpreadSheetData) {
                        $('#assayhere').html(molSpreadSheetData);
                    },
                    error: function () {
                        $('#assayhere').html('No data found');
                    },
                    complete: function () {

                    }
                });
            });

            $('#projectListing').click(function () {

                $.ajax({
                    url: '/timtest/executeCmds/projectlist',
                    type: 'GET',
                    cache: false,
                    success: function (molSpreadSheetData) {
                        $('#assayhere').html(molSpreadSheetData);
                    },
                    error: function () {
                        $('#assayhere').html('No data found');
                    },
                    complete: function () {

                    }
                });
            });


        });
    </script>
</head>
<body>
<div style="vertical-align:top;height: 1100px; width: 800px; ">
    <h1>Welcome to the friendly REST API timing service</h1>
<div id="assays" style="vertical-align:middle;text-align: left;   float: right; height: 1000px; width: 200px; margin-right: 100px;  padding-right:20px;padding-top: 40px; ">
    <button id="assayListing">Get assay IDs</button>
    <button id="projectListing">Get project IDs</button>
    <g:textArea rows="50" cols="20"  name="assayhere"></g:textArea>
</div>
<div id="controls" style="vertical-align:middle;float: left; padding-right: 10px;padding-top: 40px; height: 250px; width: 300px; ">
    <div style="text-align: left;vertical-align:middle;">

        <g:form action="test2">
          <span  style="text-align :left">
              <g:submitButton name="assayTiming" value="Retrieve all results for ASSAY" style="vertical-align:middle"></g:submitButton>
              <g:textArea name="assayid" rows="1" cols="8" style="vertical-align:middle"/>
          </span>


        </g:form>


        <g:form action="test2p">
            <span  style="text-align :left">
                <g:submitButton name="projectTiming" value="Retrieve all results for PROJECT" style="vertical-align:middle"></g:submitButton>
                <g:textArea name="projectid" rows="1" cols="8" style="vertical-align:middle"/>
            </span>
        </g:form>

        <g:form action="cmpdsperproj">
            <span  style="text-align :left">
                <g:submitButton name="getcmpds" value="Retrieve cmpds for PROJECT" style="vertical-align:middle"></g:submitButton>
                <g:textArea name="projectid" rows="1" cols="8" maxlength="100"  style="vertical-align:middle"/>
            </span>
        </g:form>


    </div>
</div>
</div>
</body>
</html>