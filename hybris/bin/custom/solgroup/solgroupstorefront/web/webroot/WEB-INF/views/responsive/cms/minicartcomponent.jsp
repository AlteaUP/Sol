<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:htmlEscape defaultHtmlEscape="true" />

<spring:url value="/cart/miniCart/{/totalDisplay}" var="refreshMiniCartUrl" htmlEscape="false">
	<spring:param name="totalDisplay"  value="${totalDisplay}"/>
</spring:url>
<spring:url value="/cart/rollover/{/componentUid}" var="rolloverPopupUrl" htmlEscape="false">
	<spring:param name="componentUid"  value="${component.uid}"/>
</spring:url>
<c:url value="/cart" var="cartUrl"/>

<div class="nav-cart">
	<a 	href="${cartUrl}"
		class="mini-cart-link"
		>  
		<div class="mini-cart-icon">
			<span class="glyphicon glyphicon-shopping-cart "></span>
		</div>
		<ycommerce:testId code="miniCart_items_label">
		
		  <div class="mini-cart-price js-mini-cart-price hidden-xs hidden-sm">
			<c:if test="${!isRefill}">
				<c:if test="${totalDisplay == 'TOTAL'}">
					<format:price priceData="${totalPrice}" />
				</c:if>
	
				<c:if test="${totalDisplay == 'SUBTOTAL'}">
					<format:price priceData="${subTotal}" />
				</c:if>
				
				<c:if test="${totalDisplay == 'TOTAL_WITHOUT_DELIVERY'}">
					<format:price priceData="${totalNoDelivery}" />
				</c:if>
			</c:if>
		  </div>
		  <div class="mini-cart-count js-mini-cart-count"><span class="nav-items-total">${totalItems lt 100 ? totalItems : "99+"}<span class="items-desktop hidden-xs">&nbsp;<spring:theme code="basket.items"/></span></span></div>
		</ycommerce:testId>

	</a>
</div>
