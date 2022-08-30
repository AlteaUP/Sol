<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="multi-checkout" tagdir="/WEB-INF/tags/responsive/checkout/multi"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<spring:htmlEscape defaultHtmlEscape="true" />


<style type="text/css"> 
textarea {
    -webkit-box-sizing: border-box;
    -moz-box-sizing: border-box;
    box-sizing: border-box;

    width: 100%;
}
</style>

<template:page pageTitle="${pageTitle}" hideHeaderLinks="true">

<div class="row">
    <div class="col-sm-6">
        <div class="checkout-headline">
            <span class="glyphicon glyphicon-lock"></span>
            <spring:theme code="checkout.multi.secure.checkout" />
        </div>
        
		<multi-checkout:checkoutSteps checkoutSteps="${checkoutSteps}" progressBarId="${progressBarId}">
			<jsp:body>
				<ycommerce:testId code="checkoutStepTwo">
					<div class="checkout-shipping">
						<div class="checkout-indent">
							<div class="headline"><spring:theme code="checkout.summary.newNote" /></div>
							<ul>
				                
				            </ul>
							
							<!-- 
							<form id="addNoteCart" action="${request.contextPath}/checkout/multi/note/add" method="post">
								<div class="form-group">
									<textarea id="note" name="note" rows="10" cols="104" maxlength="1000" >${cartData.note}</textarea>								
								</div>
								
								<p><spring:theme code="checkout.multi.deliveryMethod.message" htmlEscape="false"/></p>
								<button id="addNoteCartSubmit" type="submit" class="btn btn-primary btn-block checkout-next"><spring:theme code="checkout.multi.deliveryMethod.continue"/></button>
					
							</form>
							-->
							
							<form:form method="post" commandName="noteForm" action="${request.contextPath}/checkout/multi/note/add">

								<div class="form-group">
									<textarea id="note" name="note" rows="10" cols="104" maxlength="1000" >${cartData.note}</textarea>								
								</div>
        						<c:if test="${cartData.refill}"><spring:theme code="checkout.multi.deliveryMethod.disclaimer"></spring:theme></c:if>
        						<br></br>
								<p><spring:theme code="checkout.multi.deliveryMethod.message" htmlEscape="false"/></p>
								<c:if test="${cartData.showAgentVoucher}">
									<div class="form-group" style="margin-top: 3%">
										<spring:theme code="checkout.multi.agent.voucher" />
										<input id="agentVoucher" name="agentVoucher" size="36" maxlength="36" style="margin-left: 2%" />
									</div>
								</c:if>
								
								<button id="noteFormSubmit" type="submit" class="btn btn-primary btn-block checkout-next"><spring:theme code="checkout.multi.deliveryMethod.continue"/></button>

	
							</form:form>
							

						</div>
					</div>
					
					
				</ycommerce:testId>
			</jsp:body>
		</multi-checkout:checkoutSteps>
    </div>

    <div class="col-sm-6 hidden-xs">
		<multi-checkout:checkoutOrderDetails cartData="${cartData}" showDeliveryAddress="true" showPaymentInfo="false" showTaxEstimate="false" showTax="true" />
    </div>

    
</div>

</template:page>
