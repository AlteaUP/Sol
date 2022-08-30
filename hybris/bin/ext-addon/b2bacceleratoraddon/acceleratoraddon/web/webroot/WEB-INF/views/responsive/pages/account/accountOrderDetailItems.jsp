<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="b2b-order" tagdir="/WEB-INF/tags/addons/b2bacceleratoraddon/responsive/order" %>
<%@ taglib prefix="order" tagdir="/WEB-INF/tags/responsive/order" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="account-orderdetail account-consignment">
    <ycommerce:testId code="orderDetail_itemList_section">
       	<%-- <c:if test="${not empty orderData.unconsignedEntries}">
            <b2b-order:orderUnconsignedEntries order="${orderData}"/>
        </c:if> --%>
        <c:if test="${not empty orderData}">
            <b2b-order:orderEntries order="${orderData}"/>
        </c:if>
        <c:forEach items="${orderData.consignments}" var="consignment" varStatus="loop">
            <c:if test="${consignment.status.code eq 'WAITING' or consignment.status.code eq 'PICKPACK' or consignment.status.code eq 'READY' and consignment.status.code eq 'DELETED'}">
                <div class="fulfilment-states-${fn:escapeXml(consignment.status.code)}">
                    <order:accountOrderDetailsItem order="${orderData}" consignment="${consignment}" inProgress="true" consignmentIndex="2${loop.index}"/>
                </div>
            </c:if>
        </c:forEach>
        <c:forEach items="${orderData.consignments}" var="consignment" varStatus="loop">
            <c:if test="${consignment.status.code ne 'WAITING' and consignment.status.code ne 'PICKPACK' and consignment.status.code ne 'READY' and consignment.status.code ne 'DELETED'}">
                <div class="fulfilment-states-${fn:escapeXml(consignment.status.code)}">
                    <order:accountOrderDetailsItem order="${orderData}" consignment="${consignment}" consignmentIndex="2${loop.index}"/>
                </div>
            </c:if>
        </c:forEach>
    </ycommerce:testId>
</div>