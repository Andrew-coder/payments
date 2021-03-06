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
    <script src="js/main-menu.js"></script>
</head>
<body>
    <jsp:include page="header.jsp"></jsp:include>

    <table class="table borderless">
        <c:forEach items="${cards}" var="card">
            <tr>
                <td>${card.getCardNumber()}</td>
                <c:if test="${requestScope.get(card.id.toString())}">
                    <td><button class="btn-primary" id="${card.id}_button" name="${card.id}_button"><fmt:message key="payments.cards.unblock" bundle="${msg}"/> </button></td>
                </c:if>
            </tr>
        </c:forEach>
    </table>

    <jsp:include page="footer.jsp"></jsp:include>

    <script type="text/javascript">
        $(document).on("click", ".btn-primary", function(){
            var but=$(this).attr('id').split('_')[0];

            $.ajax({
                type: "POST",
                url: "/admin/cards/unblock",
                data: {
                    cardID:but
                },
                success: function(){
                    var selector = "#"+but + "_button";
                    $(selector).hide();
                    alert('<fmt:message key="payments.successful.card.unblock" bundle="${msg}"/>');
                },
                error:function () {
                    alert("<fmt:message key="payments.successful.card.unblock" bundle="${msg}"/>");
                }
            });
        });
    </script>
</body>
</html>