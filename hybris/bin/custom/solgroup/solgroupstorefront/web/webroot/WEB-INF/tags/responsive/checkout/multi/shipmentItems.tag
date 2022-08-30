<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="cartData" required="true" type="de.hybris.platform.commercefacades.order.data.CartData" %>
<%@ attribute name="showDeliveryAddress" required="true" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<spring:htmlEscape defaultHtmlEscape="true" />

<c:set var="hasShippedItems" value="${cartData.deliveryItemsQuantity > 0}" />
<c:set var="deliveryAddress" value="${cartData.deliveryAddress}"/>
<c:set var="firstShippedItem" value="true"></c:set>

<c:if test="${hasShippedItems}">
	<div class="checkout-shipping-items row">
        <div class="col-sm-12 col-lg-6">
            <div class="checkout-shipping-items-header">
            	<spring:theme code="checkout.multi.shipment.items" arguments="${cartData.deliveryItemsQuantity}" />
            </div>
            <ul>
                <c:forEach items="${cartData.entries}" var="entry">
                    <c:if test="${entry.deliveryPointOfService == null}">
                        <li class="row">
                            <span class="name col-xs-8">${fn:escapeXml(entry.product.name)}</span>
                            <span class="qty col-xs-4"><spring:theme code="basket.page.qty"/>:&nbsp;${entry.quantity}</span>
                        </li>
                    </c:if>
                </c:forEach>
            </ul>
        </div>

        <c:if test="${showDeliveryAddress and not empty deliveryAddress}">
            <div class="col-sm-12 col-lg-6">
                <div class="checkout-shipping-items-header"><spring:theme code="checkout.summary.shippingAddress"></spring:theme></div>
                <span>
                	<c:if test="${not empty deliveryAddress.lastName}">
                    <div><b>${fn:escapeXml(deliveryAddress.title)}&nbsp;${fn:escapeXml(deliveryAddress.firstName)}&nbsp;${fn:escapeXml(deliveryAddress.lastName)}</b></div>
                    </c:if>
                    <c:if test="${ not empty deliveryAddress.line1 }">                       
                   		<strong>
                   			<spring:theme code="checkout.multi.deliveryAddress.street"></spring:theme>
                   		</strong>
                       	${fn:escapeXml(deliveryAddress.line1)}
                    </c:if>
                    <c:if test="${ not empty deliveryAddress.line2 }">
                        ,&nbsp;${fn:escapeXml(deliveryAddress.line2)}
                    </c:if>
                    <c:if test="${not empty deliveryAddress.town }">
                    	<div>
                    		<strong>
                    			<spring:theme code="checkout.multi.deliveryAddress.town"></spring:theme>
                    		</strong>
                        	${fn:escapeXml(deliveryAddress.town)}
                        </div>
                    </c:if>
                    <c:if test="${ not empty deliveryAddress.region.name }">
                    	<div>
                    		<strong>
                    			<spring:theme code="checkout.multi.deliveryAddress.region"></spring:theme>
                    		</strong>
                        	${fn:escapeXml(deliveryAddress.region.name)}
                        </div>
                    </c:if>
                    <c:if test="${ not empty deliveryAddress.postalCode }">
                    	<div>
                    		<strong>
                    			<spring:theme code="checkout.multi.deliveryAddress.postcode"></spring:theme>
                    		</strong>
                        	${fn:escapeXml(deliveryAddress.postalCode)}
                        </div>
                    </c:if>
                    <c:if test="${ not empty deliveryAddress.country.name }">
                    	<div>
                    		<strong>
                    			<spring:theme code="checkout.multi.deliveryAddress.country"></spring:theme>
                    		</strong>
                        	${fn:escapeXml(deliveryAddress.country.name)}
                        </div>
                    </c:if>
                    <c:if test="${ not empty deliveryAddress.email }">
                    	<div>
                    		<strong>
                    			<spring:theme code="checkout.multi.deliveryAddress.email"></spring:theme>
                    		</strong>
                        	${fn:escapeXml(deliveryAddress.email)}
                        </div>
                    </c:if>
                    <c:if test="${ not empty deliveryAddress.phone }">
                    	<div>
                    		<strong>
                    			<spring:theme code="checkout.multi.deliveryAddress.phone"></spring:theme>
                    		</strong>
                        	${fn:escapeXml(deliveryAddress.phone)}
                        </div>
                    </c:if>
                </span>
            </div>
        </c:if>
	</div>

    <hr/>
</c:if>