<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html>
<head>
    <title>Error</title>
</head>
<body>
    <h1>Error</h1>
    <c:forEach items="${errors}" var="error">
        <h3>${error.paramName} ${error.message}</h3>
    </c:forEach>
</body>
</html>
