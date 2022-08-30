<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="multi-checkout" tagdir="/WEB-INF/tags/responsive/checkout/multi"%>
<%@ taglib prefix="b2b-multi-checkout" tagdir="/WEB-INF/tags/addons/b2bacceleratoraddon/responsive/checkout/multi" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<spring:url value="/checkout/multi/solgroup-summary/placeOrder" var="placeOrderUrl"/>
<spring:url value="/checkout/multi/termsAndConditions" var="getTermsAndConditionsUrl"/>
<spring:url value="/privacyPolicy" var="getPersonalDataUrl"/>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>

<template:page pageTitle="${pageTitle}" hideHeaderLinks="true">


<div class="row">
    <div class="col-sm-6">
        <div class="checkout-headline">
            <span class="glyphicon glyphicon-lock"></span>
            <spring:theme code="checkout.multi.secure.checkout"></spring:theme>
        </div>

        <multi-checkout:checkoutSteps checkoutSteps="${checkoutSteps}" progressBarId="${progressBarId}">
            <ycommerce:testId code="checkoutStepFour">
            	<c:if test="${!cartData.refill}">
	                <div class="checkout-review hidden-xs">
	                    <div class="checkout-order-summary">
	                        <multi-checkout:orderTotals cartData="${cartData}" showTaxEstimate="${showTaxEstimate}" showTax="${showTax}" subtotalsCssClasses="dark"/>
	                    </div>
	                </div>
	            </c:if>
                <div class="place-order-form hidden-xs">
                    <form:form action="${placeOrderUrl}" id="placeOrderForm1" commandName="placeOrderForm" >
                        <div class="checkbox">
                            <label> 
                            	<form:checkbox id="Terms1" path="termsCheck" />
                                <spring:theme code="checkout.summary.placeOrder.readTermsAndConditions" arguments="${getTermsAndConditionsUrl}" />
                            </label>
                            <br></br>
                            <span title="Per completare l'ordine bisogna leggere ed accettare l'informativa sui dati personali">
                            <label> 
                            	<form:checkbox id="Terms2" path="termsCheck" disabled="true"/>
                                <spring:theme code="checkout.summary.placeOrder.readPersonalData" arguments="${getPersonalDataUrl}" />
                            </label>
                            </span>
                        </div>
                        <%-- 
                        <a href="" class="help js-cart-help" data-help="<spring:theme code="text.help" />"><spring:theme code="text.help" />
                			<span class="glyphicon glyphicon-info-sign"></span>
            			</a>
						 --%>
						<!-- pop-up -->
						<div id="termsTitle" data-title="<spring:theme code="checkout.summary.popup.title"/>" style="margin-bottom: 10px" hidden="true"></div>
						<div id="terms-and-condition" class="clear" hidden="true" >
							<%-- <h1 class=""documentFirstHeading""><spring:theme code="checkout.summary.popup.title"/></h1> --%>
							
							<div id="content-core" style="margin-top: 30px">
    
								<p><spring:theme code="checkout.summary.popup.paragraph"/></p>
									<ol>
									
										<li>
											<strong><spring:theme code="checkout.summary.popup.conditions"/></strong>
											<ul>
												<li>
													<spring:theme code="checkout.summary.popup.conditions1"/>
												</li>
												<li>
													<spring:theme code="checkout.summary.popup.conditions2"/>
												</li>
												<li>
													<spring:theme code="checkout.summary.popup.conditions3"/>
												</li>
												<li>
													<spring:theme code="checkout.summary.popup.conditions4"/>
												</li>
												<li>
													<spring:theme code="checkout.summary.popup.conditions5"/>
												</li>
											</ul>
										</li>
										
										<br/>
										
										<li>
											<strong><spring:theme code="checkout.summary.popup.validity"/> </strong>
											<ul>
												<li>
													<spring:theme code="checkout.summary.popup.validity1"/> 
												</li>
												<li>
													<spring:theme code="checkout.summary.popup.validity2"/> 
												</li>
											</ul>
										</li>
										
										<br/>
										
										<li>
											<strong><spring:theme code="checkout.summary.popup.priceAndpaymentterms"/> </strong>
											<ul>
												<li>
													<spring:theme code="checkout.summary.popup.priceAndpaymentterms1"/>
												</li>
												<li>
													<spring:theme code="checkout.summary.popup.priceAndpaymentterms2"/>
												</li>
												<li>
													<spring:theme code="checkout.summary.popup.priceAndpaymentterms3"/>
												</li>
												<li>
													<spring:theme code="checkout.summary.popup.priceAndpaymentterms4"/>
												</li>
												<li>
													<spring:theme code="checkout.summary.popup.priceAndpaymentterms5"/>
												</li>
											</ul>
										</li>
										
										<br/>
										
										<li>
											<strong><spring:theme code="checkout.summary.popup.Delivery"/></strong>
											<ul>
												<li>
													<spring:theme code="checkout.summary.popup.Delivery1"/>
												</li>
												<li>
													<spring:theme code="checkout.summary.popup.Delivery2"/>
														<ul>
															<li>
																<spring:theme code="checkout.summary.popup.Delivery21"/>
															</li>
															<li>
																<spring:theme code="checkout.summary.popup.Delivery22"/>
															</li>
														</ul>
												</li>
											</ul>
										</li>
										
										<br/>
										
										<li>
											<strong><spring:theme code="checkout.summary.popup.mobileContainers"/></strong>
											<ul>
												<li>
													<spring:theme code="checkout.summary.popup.mobileContainers1"/>
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.mobileContainers2"/>
												</li>
													
												<li>
													<spring:theme code="checkout.summary.popup.mobileContainers3"/>
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.mobileContainers4"/>
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.mobileContainers5"/>
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.mobileContainers6"/>					
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.mobileContainers7"/>
													<ul>
														<li><spring:theme code="checkout.summary.popup.mobileContainers71"/></li>
														<li><spring:theme code="checkout.summary.popup.mobileContainers72"/></li>
														<li><spring:theme code="checkout.summary.popup.mobileContainers73"/></li>
														<li><spring:theme code="checkout.summary.popup.mobileContainers74"/></li>
													</ul>
													<spring:theme code="checkout.summary.popup.mobileContainers75"/>					
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.mobileContainers8"/>
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.mobileContainers9"/>
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.mobileContainers10"/>
												</li>
												
												
											</ul>
											
										</li>
										
										<br/>
										
										<li>
											<strong><spring:theme code="checkout.summary.popup.insurance"/></strong>
											<ul>
												<li>
													<spring:theme code="checkout.summary.popup.insurance1"/>
												</li>
											</ul>
										</li>
										
										<br/>
										
										<li>
											<strong><spring:theme code="checkout.summary.popup.damage"/></strong>
											<ul>
												<li>
													<spring:theme code="checkout.summary.popup.damage1"/>
													<ul>
														<li>
															<spring:theme code="checkout.summary.popup.damage11"/>
														</li>
														<li>
															<spring:theme code="checkout.summary.popup.damage12"/>
														</li>
														<li>
															<spring:theme code="checkout.summary.popup.damage13"/>
														</li>
													</ul>
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.damage2"/>
													<ul>
														<li>
															<spring:theme code="checkout.summary.popup.damage21"/>
														</li>
														<li>
															<spring:theme code="checkout.summary.popup.damage22"/>
														</li>
													</ul>
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.damage3"/>
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.damage4"/>
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.damage5"/>
													<ul>
														<li>
															<spring:theme code="checkout.summary.popup.damage51"/>
														</li>
														
														<li>
															<spring:theme code="checkout.summary.popup.damage52"/>
														</li>
														
														<li>
															<spring:theme code="checkout.summary.popup.damage53"/>
														</li>
														
														<li>
															<spring:theme code="checkout.summary.popup.damage54"/>
														</li>
													</ul>
												</li>
											</ul>
										</li>
										
										<br>
										
										<li>
											<strong><spring:theme code="checkout.summary.popup.risks"/></strong>
											
											<ul>
												<li>
													<spring:theme code="checkout.summary.popup.risks1"/> 
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.risks2"/>
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.risks3"/>
												</li>
											</ul>
							
										</li>
										
										<br/>
										
										<li>
											<strong><spring:theme code="checkout.summary.popup.various"/></strong>
											<ul>
												<li>
													<spring:theme code="checkout.summary.popup.various1"/>
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.various2"/>
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.various3"/>
												</li>
												
												<li>
													<spring:theme code="checkout.summary.popup.various4"/>
												</li>
											</ul>
										</li>
									
									
									</ol>
							        
								</div>
						</div>
						<!-- fine pop-up -->
                        <button id="placeOrder" type="submit" class="btn btn-primary btn-block btn-place-order btn-block btn-lg checkoutSummaryButton" disabled="disabled">
                            <spring:theme code="checkout.summary.placeOrder"/>
                        </button>
                        <!-- FIX SOL403 -->
                        <%-- <c:if test="${cartData.quoteData eq null}">
	                        <button id="scheduleReplenishment" type="button" class="btn btn-default btn-block scheduleReplenishmentButton checkoutSummaryButton" disabled="disabled">
	                            <spring:theme code="checkout.summary.scheduleReplenishment"/>
	                        </button>
	
	                        <b2b-multi-checkout:replenishmentScheduleForm/>
                        </c:if> --%>
                    </form:form>
                </div>

            </ycommerce:testId>
        </multi-checkout:checkoutSteps>
    </div>

    <div class="col-sm-6">
        <b2b-multi-checkout:checkoutOrderSummary cartData="${cartData}" showDeliveryAddress="true" showPaymentInfo="true" showTaxEstimate="true" showTax="true" />
    </div>

    <div class="col-sm-12 col-lg-12">
        <br class="hidden-lg">
        <cms:pageSlot position="SideContent" var="feature" element="div" class="checkout-help">
            <cms:component component="${feature}"/>
        </cms:pageSlot>
    </div>
</div>

	
</template:page>