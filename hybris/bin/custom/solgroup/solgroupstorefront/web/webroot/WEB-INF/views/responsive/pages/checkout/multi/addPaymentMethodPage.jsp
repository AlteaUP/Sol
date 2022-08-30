<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="multiCheckout" tagdir="/WEB-INF/tags/responsive/checkout/multi"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="address" tagdir="/WEB-INF/tags/responsive/address" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>

<spring:htmlEscape defaultHtmlEscape="true" />

<template:page pageTitle="${pageTitle}" hideHeaderLinks="true">

    <div class="row">
        <div class="col-sm-6">
            <div class="checkout-headline">
                <span class="glyphicon glyphicon-lock"></span>
                <spring:theme code="checkout.multi.secure.checkout"/>
            </div>
            <multiCheckout:checkoutSteps checkoutSteps="${checkoutSteps}" progressBarId="${progressBarId}">
                <jsp:body>
                    <ycommerce:testId code="checkoutStepThree">
                 		<form:form id="paymentMethodPostForm" name="paymentMethodPostForm" commandName="sopPaymentDetailsForm" action="${request.contextPath}/checkout/multi/note/show" method="GET">
                        	<div class="checkout-paymentmethod">
								<div class="checkout-shipping-items row">
									<div class="col-sm-12 col-lg-12">
										<c:set var="billingAddress" value="${billingAddress}"/>
						                <span>
						                    <b>${fn:escapeXml(deliveryAddress.title)}&nbsp;${fn:escapeXml(billingAddress.firstName)}&nbsp;${fn:escapeXml(billingAddress.lastName)}</b>
						                    <br/>
						                    <c:if test="${ not empty billingAddress.line1 }">
						                        ${fn:escapeXml(billingAddress.line1)},&nbsp;
						                    </c:if>
						                    <c:if test="${ not empty billingAddress.line2 }">
						                        ${fn:escapeXml(billingAddress.line2)},&nbsp;
						                    </c:if>
						                    <c:if test="${not empty billingAddress.town }">
						                        ${fn:escapeXml(billingAddress.town)},&nbsp;
						                    </c:if>
						                    <c:if test="${ not empty billingAddress.region.name }">
						                        ${fn:escapeXml(billingAddress.region.name)},&nbsp;
						                    </c:if>
						                    <c:if test="${ not empty billingAddress.postalCode }">
						                        ${fn:escapeXml(billingAddress.postalCode)},&nbsp;
						                    </c:if>
						                    <c:if test="${ not empty billingAddress.country.name }">
						                        ${fn:escapeXml(billingAddress.country.name)}
						                    </c:if>
						                    <br/>
						                    <c:if test="${ not empty billingAddress.phone }">
						                        ${fn:escapeXml(billingAddress.phone)}
						                    </c:if>
						                </span>
						            </div>
					            </div>
                            </div>
                            <button type="submit" class="btn btn-primary btn-block submit_paymentMethodPostForm checkout-next"><spring:theme code="checkout.multi.paymentMethod.continue"/></button>
						</form:form>
                    </ycommerce:testId>
               </jsp:body>
            </multiCheckout:checkoutSteps>
		</div>

        <div class="col-sm-6 hidden-xs">
            <multiCheckout:checkoutOrderDetails cartData="${cartData}" showDeliveryAddress="true" showPaymentInfo="false" showTaxEstimate="false" showTax="true" />
        </div>

		<div class="col-sm-12 col-lg-12">
			<cms:pageSlot position="SideContent" var="feature" element="div" class="checkout-help">
				<cms:component component="${feature}"/>
			</cms:pageSlot>
		</div>
	</div>

</template:page>
