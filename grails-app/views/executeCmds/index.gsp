<%--
  Created by IntelliJ IDEA.
  User: balexand
  Date: 3/13/13
  Time: 8:27 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title></title>
</head>
<body>
    <h1>Welcome to the friendly REST API timing service</h1>
<g:form action="test1">
    <g:submitButton name="assayTiming" value="retrieve some sample assay IDs (the first 500)"></g:submitButton>
</g:form>
<g:form action="test2">
    <g:textArea name="assayid" rows="1" cols="10"/>
    <g:submitButton name="assayTiming" value="Retrieve all activities for assay"></g:submitButton>
</g:form>
<g:form action="test1p">
    <g:submitButton name="projectTiming" value="retrieve all existing projects IDs"></g:submitButton>
</g:form>
<g:form action="test2p">
    <g:textArea name="projectid" rows="1" cols="10"/>
    <g:submitButton name="projectTiming" value="Retrieve all activities for project"></g:submitButton>
</g:form>
</body>
</html>