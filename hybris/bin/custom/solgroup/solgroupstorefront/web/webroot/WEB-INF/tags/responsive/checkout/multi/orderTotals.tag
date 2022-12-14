<%@ attribute name="cartData" required="true" type="de.hybris.platform.commercefacades.order.data.CartData" %>
<%@ attribute name="showTax" required="false" type="java.lang.Boolean" %>
<%@ attribute name="showTaxEstimate" required="false" type="java.lang.Boolean" %>
<%@ attribute name="subtotalsCssClasses" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>

<spring:htmlEscape defaultHtmlEscape="true" />

<div class="subtotals ${subtotalsCssClasses}">

	<c:set var="displayTax" value="false"/>
	<c:set var="displayDeliveryCost" value="false"/>

	<c:choose>
		<c:when test="${not empty cartData.invoicePaymentInfo}">
			<c:set var="displayTax" value="${cartData.invoicePaymentInfo.showTax}"/>
			<c:set var="displayDeliveryCost" value="${cartData.invoicePaymentInfo.showDeliveryCost}"/>
		</c:when>
		<c:when test="${not empty cartData.paymentInfo}">
			<c:set var="displayTax" value="${cartData.paymentInfo.showTax}"/>
			<c:set var="displayDeliveryCost" value="${cartData.paymentInfo.showDeliveryCost}"/>
		</c:when>
	</c:choose>

	
	<%-- 
	<c:if test="${cartData.net && cartData.totalTax.value > 0 && showTax}">
	--%>
	<!-- Section subtotal -->
	<c:if test="${displayTax || displayDeliveryCost}">
		<div class="subtotal">
			<spring:theme code="basket.page.totals.subtotal"/>
			<span>
				<ycommerce:testId code="Order_Totals_Subtotal">
					<format:price priceData="${cartData.subTotal}" />
				</ycommerce:testId>
			</span>
		</div>
	</c:if>
	
	<!-- Section discounts -->
	<c:if test="${cartData.quoteDiscounts.value > 0}">
		<div class="subtotals__item--state-discount">
			<spring:theme code="basket.page.quote.discounts" />
			<span>
				<ycommerce:testId code="Quote_Totals_Savings">
					<format:price priceData="${cartData.quoteDiscounts}" displayNegationForDiscount="true" />
				</ycommerce:testId>
			</span>
		</div>
	</c:if>
	<c:if test="${cartData.totalDiscounts.value > 0}">
		<div class="subtotals__item--state-discount">
			<spring:theme code="text.account.order.discount"/>
			<span>
				<ycommerce:testId code="Order_Totals_Savings">
					<format:price priceData="${cartData.totalDiscounts}" displayNegationForDiscount="true" />
				</ycommerce:testId>
			</span>
		</div>
	</c:if>

	<!-- Section delivery cost -->	
	<%--  
	<c:if test="${not empty cartData.deliveryCost && cartData.deliveryCost.value > 0}">
	--%>
	<c:if test="${displayDeliveryCost}">
		<div class="shipping">
			<spring:theme code="basket.page.totals.delivery"/>
			<span>
				<ycommerce:testId code="Order_Totals_Delivery">
					<format:price priceData="${cartData.deliveryCost}" displayFreeForZero="TRUE"/>
				</ycommerce:testId>
			</span>
		</div>
	</c:if>
	
	<!-- Section taxes -->
	<%--  
	<c:if test="${cartData.net && cartData.totalTax.value > 0 && showTax}">
	--%>
	<c:if test="${displayTax}">
		<div class="tax">
			<spring:theme code="basket.page.totals.netTax"/>
			<span>
				<format:price priceData="${cartData.totalTax}"/>
			</span>
		</div>
	</c:if>
	
	<div class="totals">
		<spring:theme code="basket.page.totals.total"/>
		<span>
			<ycommerce:testId code="cart_totalPrice_label">
					<c:choose>
						<c:when test="${showTax}">
							<format:price priceData="${cartData.totalPriceWithTax}"/>
						</c:when>
						<c:otherwise>
							<format:price priceData="${cartData.totalPrice}"/>
						</c:otherwise>
					</c:choose>
			</ycommerce:testId>
		</span>
	</div>
	<%-- <c:if test="${not cartData.net}">
		<div class="realTotals">
			<ycommerce:testId code="cart_taxes_label">
				<p>
					<spring:theme code="basket.page.totals.grossTax" arguments="${cartData.totalTax.formattedValue}" argumentSeparator="!!!!"/>
				</p>
			</ycommerce:testId>
		</div>
	</c:if> 
	<c:if test="${cartData.net}">
		<div class="realTotals">
			<ycommerce:testId code="cart_taxes_label">
				<p>
					<spring:theme code="basket.page.totals.noNetTax"/>
				</p>
			</ycommerce:testId>
		</div>
	</c:if>
	--%>	
	
	<c:choose>
		<c:when test="${not displayDeliveryCost && not displayTax}">
			<div class="cart-totals-taxes text-right">
            	<ycommerce:testId code="cart_taxes_label">
            		<spring:theme code="basket.page.totals.noNetTaxAndDeliveryCosts"/>
            	</ycommerce:testId>
        	</div>	
		</c:when>
		<c:when test="${not displayTax}">
			<div class="cart-totals-taxes text-right">
            	<ycommerce:testId code="cart_taxes_label">
            		<spring:theme code="basket.page.totals.noNetTax"/>
            	</ycommerce:testId>
        	</div>	
        </c:when>
		<c:when test="${not displayDeliveryCost}">
			<div class="cart-totals-taxes text-right">
            	<ycommerce:testId code="cart_taxes_label">
            		<spring:theme code="basket.page.totals.noDeliveryCost"/>
            	</ycommerce:testId>
        	</div>	
		</c:when>
	</c:choose>
</div>
	
