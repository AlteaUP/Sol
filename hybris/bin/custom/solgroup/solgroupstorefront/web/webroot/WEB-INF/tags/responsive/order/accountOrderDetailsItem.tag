<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="order" required="true" type="de.hybris.platform.commercefacades.order.data.OrderData" %>
<%@ attribute name="consignment" required="true" type="de.hybris.platform.commercefacades.order.data.ConsignmentData" %>
<%@ attribute name="inProgress" required="false" type="java.lang.Boolean" %>
<%@ attribute name="consignmentIndex" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="order" tagdir="/WEB-INF/tags/responsive/order" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<spring:htmlEscape defaultHtmlEscape="true" />

<div class="well well-quinary well-xs">
	<ycommerce:testId code="orderDetail_itemHeader_section">		
		
		<div class="${consignment.status}_well-headline">
		<c:if test="${consignment.status ne 'ACCEPTED' && consignment.status ne 'PLANNED' && consignment.status ne 'DELIVERED' }">
			<div class="row">
				<span style="padding-right: 8px; text-transform: uppercase;">
			 		<spring:theme code="text.account.orderHistory.consignment.code" />
				</span>
				${fn:escapeXml(consignment.code)}
			</div>
			</c:if>
			<div class="row">
	            <ycommerce:testId code="orderDetail_consignmentStatus_label">
	                <spring:theme code="text.account.order.consignment.status.${fn:escapeXml(consignment.statusDisplay)}" />
	            </ycommerce:testId>
	
				<ycommerce:testId code="orderDetail_consignmentStatusDate_label">
					<span class="well-headline-sub">
	                    <fmt:formatDate value="${consignment.statusDate}" dateStyle="medium" timeStyle="short" type="both" pattern="dd/MM/yy"/>
	                </span>
				</ycommerce:testId>
			</div>
		</div>

        <div class="well-content col-sm-12 col-md-12">
        	<div class="row">
                <div class="col-sm-12 col-md-12">
	                <%-- <c:if test="${not inProgress}"> --%>
	                	<%-- <c:if test="${consignment.status.code eq 'SHIPPED' and not empty consignment.trackingID}"> --%>
	                	<c:if test="${not empty consignment.trackingID}">
                            <div class="col-sm-4">
                                <div class="order-tracking-no">
                                    <ycommerce:testId code="orderDetail_trackingId_label">
                                        <span class="label-order"><spring:theme code="text.account.order.tracking" text="Tracking No." /></span>
                                        <br>
                                        <span class="order-track-number">${fn:escapeXml(consignment.trackingID)}</span>
                                    </ycommerce:testId>
                                </div>
                            </div>
						</c:if>
						<%-- <c:if test="${consignment.status.code eq 'SHIPPED' and not empty consignment.carrierCode}"> --%>
						<c:if test="${not empty consignment.carrierCode}">
		                    <div class="col-sm-4">
		                    	<div class="order-tracking-no">
		                        	<ycommerce:testId code="orderDetail_trackingId_label">
		                            	<span class="label-order"><spring:theme code="text.account.orderHistory.carrierCode" text="Carrier Co." /></span>
		                                <br>
		                                <span class="order-track-number">${fn:escapeXml(consignment.carrierCode)}</span>
		                            </ycommerce:testId>
		                        </div>
		                    </div>
		                </c:if>
		                <%-- <c:if test="${consignment.status.code eq 'SHIPPED' and not empty consignment.trackingLink}"> --%>
		                <c:if test="${not empty consignment.trackingLink}">
		                    <div class="col-sm-4">
		                    	<div class="order-tracking-no">
		                        	<ycommerce:testId code="orderDetail_trackingId_label">
		                            	<span class="label-order"><spring:theme code="text.account.orderHistory.trackingLink" text="Tracking Link" /></span>
		                                <br>
		                                <span class="order-track-number">
		                                	<a href="${fn:escapeXml(consignment.trackingLink)}" target="_blank">${fn:escapeXml(consignment.trackingLink)}</a>
		                                </span>
		                            </ycommerce:testId>
		                        </div>
		                    </div>
		            	</c:if>
	                <%-- </c:if> --%>
                </div>
            </div>
            <div class="row" style="padding-top: 2%">
                <div class="col-sm-12 col-md-12">
	                <%-- <c:if test="${not inProgress}"> --%>
	                	<%-- <c:if test="${consignment.status.code eq 'SHIPPED' and not empty consignment.documentNumber}"> --%>
	                	<c:if test="${not empty consignment.documentNumber}">
		                    <div class="col-sm-4">
		                    	<div class="order-tracking-no">
		                        	<ycommerce:testId code="orderDetail_trackingId_label">
		                            	<span class="label-order"><spring:theme code="text.account.orderHistory.documentNumber" text="Document No." /></span>
		                                <br>
		                                <span class="order-track-number">${fn:escapeXml(consignment.documentNumber)}</span>
		                            </ycommerce:testId>
		                        </div>
		                    </div>
		                </c:if>
		                <%-- <c:if test="${consignment.status.code eq 'SHIPPED' and not empty consignment.documentType}"> --%>
		                <c:if test="${not empty consignment.documentType}">
		                    <div class="col-sm-8">
		                    	<div class="order-tracking-no">
		                        	<ycommerce:testId code="orderDetail_trackingId_label">
		                            	<span class="label-order"><spring:theme code="text.account.orderHistory.documentType" text="Document Type" /></span>
		                                <br>
		                                <span class="order-track-number">${fn:escapeXml(consignment.documentType)}</span>
		                            </ycommerce:testId>
		                        </div>
		                    </div>
	                    </c:if>
	                <%-- </c:if> --%>
                </div>
            </div>
        </div>
	</ycommerce:testId>
</div>
	
<ul class="item__list">
    <li class="hidden-xs hidden-sm">
        <ul class="${consignment.status}_item__list--header">
            <%-- <li class="item__total--column"><spring:theme code="basket.page.total"/></li> --%>
			<li class="item__toggle"></li>
            <li class="item__image"></li>
            <li class="item__info"><spring:theme code="basket.page.item"/></li>
            <li class="item__quantity"><spring:theme code="basket.page.qty"/></li>
            <li class="item__price"><spring:theme code="basket.page.price"/></li>
            <li class="item__price"><spring:theme code="basket.page.po"/></li>
            <li class="item__price"><spring:theme code="basket.page.dataOrdine"/></li>
            <li class="item__price"><spring:theme code="basket.page.cgi"/></li>
            <li class="item__price"><spring:theme code="basket.page.cup"/></li>
            <%-- <li class="item__total"><spring:theme code="basket.page.total"/></li> --%>
        </ul>
    </li>
	<ycommerce:testId code="orderDetail_itemBody_section">
		<c:forEach items="${consignment.entries}" var="entry" varStatus="loop">
			<order:consignmentEntryDetails orderEntry="${entry.orderEntry}" consignmentEntry="${entry}" consignmentCode="${consignment.code}" consignmentStatus="${consignment.status}" order="${order}" itemIndex="${consignmentIndex}${loop.index}"/>
		</c:forEach>
	</ycommerce:testId>
</ul>