<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="m" uri="/WEB-INF/taglib/Paginator.tld" %>
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
    <script src="js/main-menu.js"></script>
</head>
<body>
    <jsp:include page="header.jsp"></jsp:include>

    <table class="table borderless">
        <tr>
            <td><fmt:message key="payments.payment.id" bundle="${msg}"/></td>
            <td><fmt:message key="payments.payment.sum" bundle="${msg}"/></td>
            <td><fmt:message key="payments.payment.time" bundle="${msg}"/></td>
            <td><fmt:message key="payments.payment.type" bundle="${msg}"/></td>
            <td><fmt:message key="payments.payment.details" bundle="${msg}"/></td>
        </tr>
        <c:forEach items="${payments}" var="payment">
            <tr>
                <td>${payment.id}</td>
                <td>${payment.sum.toString()}</td>
                <td>${payment.date.toString()}</td>
                <td>${payment.tariff.type.typeName}</td>
                <td><a href="payments/${payment.id}"><button class="btn-primary"><fmt:message key="payments.view.payment" bundle="${msg}"/></button></a></td>
            </tr>
        </c:forEach>
    </table>
    <m:display paginParamName="page" totalPages="${pages}"/>
    <jsp:include page="footer.jsp"></jsp:include>
</body>
</html>