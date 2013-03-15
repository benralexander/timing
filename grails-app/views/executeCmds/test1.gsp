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
  <title>Sample essay IDs</title>
</head>
<body>
<h1>Here is a list of assay IDs ( the first 500 only )</h1>
<h3>Time required for call to complete ${elapsedTime}</h3>
type="${allAssays.getClass().name}"
<g:each var="str" in="${allAssays as List}">
<li>"${str}"
</g:each>
</body>
</html>