<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="order" required="true" type="de.hybris.platform.commercefacades.order.data.OrderData" %>
<%@ attribute name="consignments" required="false" type="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="order" tagdir="/WEB-INF/tags/responsive/order" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<spring:htmlEscape defaultHtmlEscape="true" />

<div class="well well-quinary well-xs">
    <div class="well-headline" style="text-transform: uppercase;" >
        <spring:theme code="text.account.order.entry.summary" />
    </div>
    <div class="well-content">
        <div class="row">
            <div class="col-md-5 order-ship-to">
                <div class="label-order"><spring:theme code="text.account.order.shipto" /></div>
                <div class="value-order"><order:addressItem address="${order.deliveryAddress}"/></div>
            </div>
        </div>
    </div>
</div>
<ul class="item__list">
    <li class="hidden-xs hidden-sm">
        <ul class="item__list--header">
            <li class="item__toggle"></li>
            <li class="item__image"></li>
            <li class="item__info"><spring:theme code="basket.page.item"/></li>
            <li class="item__quantity"><spring:theme code="basket.page.qty"/></li>
            <li class="item__price"><spring:theme code="basket.page.price"/></li>
            <li class="item__price"><spring:theme code="basket.page.po"/></li>
            <li class="item__price"><spring:theme code="basket.page.dataOrdine"/></li>
            <li class="item__price"><spring:theme code="basket.page.cgi"/></li>
            <li class="item__price"><spring:theme code="basket.page.cup"/></li>
            <li class="item__total"><spring:theme code="basket.page.total"/></li>
        </ul>
    </li>
	<c:forEach items="${order.entries}" var="entry" varStatus="var">
		<order:orderEntryDetails orderEntry="${entry}" order="${order}" itemIndex="${var.index}"/>
	</c:forEach>
</ul>