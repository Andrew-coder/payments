<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page trimDirectiveWhitespaces="true" %>
<html>
<head>
    <fmt:setLocale value="${sessionScope['locale']}"/>
    <fmt:requestEncoding value="UTF-8" />
    <fmt:setBundle basename="${bundleFile}" var="msg"/>
    <title>Title</title>
    <link rel="stylesheet" href="/css/bootstrap.css"/>
    <link rel="stylesheet" href="/css/bootstrap-theme.css"/>

    <script src="/js/jquery-3.2.1.min.js" ></script>
    <script src="/js/bootstrap.js" ></script>


</head>
<body>
    <jsp:include page="header.jsp"></jsp:include>

    <jsp:include page="footer.jsp"></jsp:include>
</body>
</html>
