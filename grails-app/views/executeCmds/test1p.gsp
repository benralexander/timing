

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Sample essay IDs</title>
</head>
<body>
<h1>Here is a list of available Project IDs ( the first 500 only )</h1>
<h3>Time required for call to complete ${elapsedTime}</h3>
type="${allAssays.getClass().name}"
<g:each var="str" in="${allAssays as List}">
    <li>"${str}"
</g:each>
</body>
</html>