<%--
  Created by IntelliJ IDEA.
  User: balexand
  Date: 3/14/13
  Time: 9:02 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title></title>
</head>
<body>
<h1>Time required to pull back activities for all compounds=${elapsedTime/1000} seconds</h1>
<h1>Total number of compounds=${numberOfCompoundActivities} </h1>
<g:each var="str" in="${allExperiments as List}">
    <li>"Experiment id=${str}"
</g:each>
</body>
</html>