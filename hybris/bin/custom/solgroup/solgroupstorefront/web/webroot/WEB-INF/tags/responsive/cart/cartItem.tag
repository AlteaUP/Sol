<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="cartData" required="true" type="de.hybris.platform.commercefacades.order.data.CartData" %>
<%@ attribute name="entry" required="true" type="de.hybris.platform.commercefacades.order.data.OrderEntryData" %>
<%@ attribute name="index" required="false" type="java.lang.Integer"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/responsive/product" %>
<%@ taglib prefix="grid" tagdir="/WEB-INF/tags/responsive/grid" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="order" tagdir="/WEB-INF/tags/responsive/order" %>


<%--
    Represents single cart item on cart page
 --%>

<spring:htmlEscape defaultHtmlEscape="true" />





<c:set var="errorStatus" value="<%= de.hybris.platform.catalog.enums.ProductInfoStatus.valueOf(\"ERROR\") %>" />
<c:set var="entryNumber" value="${entry.entryNumber}"/>
<c:if test="${empty index}">
    <c:set property="index" value="${entryNumber}"/>
</c:if>

<c:if test="${not empty entry}">

        <c:if test="${not empty entry.statusSummaryMap}" >
            <c:set var="errorCount" value="${entry.statusSummaryMap.get(errorStatus)}"/>
            <c:if test="${not empty errorCount && errorCount > 0}" >
                <div class="notification has-error">
                    <spring:theme code="basket.error.invalid.configuration" arguments="${errorCount}"/>
                    <a href="<c:url value="/cart/${entry.entryNumber}/configuration/${ycommerce:encodeUrl(entry.configurationInfos[0].configuratorType)}" />" >
                        <spring:theme code="basket.error.invalid.configuration.edit"/>
                    </a>
                </div>
            </c:if>
        </c:if>
        <c:set var="showEditableGridClass" value=""/>
        <c:url value="${entry.product.url}" var="productUrl"/>

        <li class="item__list--item cart-item-min-height">
            <%-- chevron for multi-d products --%>
            <div class="hidden-xs hidden-sm item__toggle">
                <c:if test="${entry.product.multidimensional}" >
                    <div class="js-show-editable-grid" data-index="${index}" data-read-only-multid-grid="${not entry.updateable}">
                        <ycommerce:testId code="cart_product_updateQuantity">
                            <span class="glyphicon glyphicon-chevron-down"></span>
                        </ycommerce:testId>
                    </div>
                </c:if>
            </div>

            <%-- product image --%>
            <div class="item__image">
                <a href="${productUrl}"><product:productPrimaryImage product="${entry.product}" format="thumbnail"/></a>
            </div>

            <%-- product name, code, promotions --%>
            <div class="item__info">
                <ycommerce:testId code="cart_product_name">
                    <a href="${productUrl}"><span class="item__name">${fn:escapeXml(entry.product.name)}</span></a>
                </ycommerce:testId>

                <div class="item__code">${fn:escapeXml(entry.product.erpCode)}</div>

                <%-- availability --%>
                <div class="item__stock">
                    <c:set var="entryStock" value="${entry.product.stock.stockLevelStatus.code}"/>
                    <c:forEach items="${entry.product.baseOptions}" var="option">
                        <c:if test="${not empty option.selected and option.selected.url eq entry.product.url}">
                            <c:forEach items="${option.selected.variantOptionQualifiers}" var="selectedOption">
                                <div>
                                    <strong>${fn:escapeXml(selectedOption.name)}:</strong>
                                    <span>${fn:escapeXml(selectedOption.value)}</span>
                                </div>
                                <c:set var="entryStock" value="${option.selected.stock.stockLevelStatus.code}"/>
                            </c:forEach>
                        </c:if>
                    </c:forEach>

                    <%-- <div>
                        <c:choose>
                            <c:when test="${not empty entryStock and entryStock ne 'outOfStock' or entry.product.multidimensional}">
                                <span class="stock"><spring:theme code="product.variants.in.stock"/></span>
                            </c:when>
                            <c:otherwise>
                                <span class="out-of-stock"><spring:theme code="product.variants.out.of.stock"/></span>
                            </c:otherwise>
                        </c:choose>
                    </div> --%>
                </div>

                <c:if test="${ycommerce:doesPotentialPromotionExistForOrderEntryOrOrderEntryGroup(cartData, entry)}">
                    <c:forEach items="${cartData.potentialProductPromotions}" var="promotion">
                        <c:set var="displayed" value="false"/>
                        <c:forEach items="${promotion.consumedEntries}" var="consumedEntry">
                            <c:if test="${not displayed && ycommerce:isConsumedByEntry(consumedEntry,entry) && not empty promotion.description}">
                                <c:set var="displayed" value="true"/>

                                    <div class="promo">
                                         <ycommerce:testId code="cart_potentialPromotion_label">
                                             ${ycommerce:sanitizeHTML(promotion.description)}
                                         </ycommerce:testId>
                                    </div>
                            </c:if>
                        </c:forEach>
                    </c:forEach>
                </c:if>
                <c:if test="${ycommerce:doesAppliedPromotionExistForOrderEntryOrOrderEntryGroup(cartData, entry)}">
                    <c:forEach items="${cartData.appliedProductPromotions}" var="promotion">
                        <c:set var="displayed" value="false"/>
                        <c:forEach items="${promotion.consumedEntries}" var="consumedEntry">
                            <c:if test="${not displayed && ycommerce:isConsumedByEntry(consumedEntry,entry) }">
                                <c:set var="displayed" value="true"/>
                                <div class="promo">
                                    <ycommerce:testId code="cart_appliedPromotion_label">
                                        ${ycommerce:sanitizeHTML(promotion.description)}
                                    </ycommerce:testId>
                                </div>
                            </c:if>
                        </c:forEach>
                    </c:forEach>
                </c:if>

                <c:if test="${entry.product.configurable}">
                    <div class="hidden-xs hidden-sm">
                        <spring:url value="/cart/{/entryNumber}/configuration/{/configuratorType}" var="entryConfigUrl" htmlEscape="false">
                            <spring:param name="entryNumber"  value="${entry.entryNumber}"/>
                            <spring:param name="configuratorType"  value="${entry.configurationInfos[0].configuratorType}" />
                        </spring:url>
                        <div class="item__configurations">
                            <c:forEach var="config" items="${entry.configurationInfos}">
                                <c:set var="style" value=""/>
                                <c:if test="${config.status eq errorStatus}">
                                    <c:set var="style" value="color:red"/>
                                </c:if>
                                <div class="item__configuration--entry" style="${style}">
                                    <div class="row">
                                        <div class="item__configuration--name col-sm-4">
                                                ${fn:escapeXml(config.configurationLabel)}
                                                <c:if test="${not empty config.configurationLabel}">:</c:if>

                                        </div>
                                        <div class="item__configuration--value col-sm-8">
                                                ${fn:escapeXml(config.configurationValue)}
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                        <c:if test="${not empty entry.configurationInfos}">
                            <div class="item__configurations--edit">
                                <a class="btn" href="${entryConfigUrl}"><spring:theme code="basket.page.change.configuration"/></a>
                            </div>
                        </c:if>
                    </div>
                </c:if>
            </div>

            <%-- price --%>
            <div class="item__price">
                <c:if test="${not entry.product.multidimensional}">
					<span class="visible-xs visible-sm"><spring:theme code="basket.page.itemPrice"/>: </span>
                	<input type="hidden" name="unit" value="${entry.unit}"/>
                	<format:price priceData="${entry.basePrice}" displayFreeForZero="true"/>
                </c:if>
            </div>

            <%-- quantity --%>
            <div class="item__quantity hidden-xs hidden-sm" style="padding:0px;">
                <c:choose>
                    <c:when test="${not entry.product.multidimensional}" >
                        <c:url value="/cart/update" var="cartUpdateFormAction" />
                        <form:form id="updateCartForm${entryNumber}" action="${cartUpdateFormAction}" method="post" commandName="updateQuantityForm${entry.entryNumber}"
                                   class="js-qty-form${entryNumber}"
                                    data-cart='{"cartCode" : "${fn:escapeXml(cartData.code)}","productPostPrice":"${entry.basePrice.value}","productName":"${fn:escapeXml(entry.product.name)}"}'>
                            <input type="hidden" name="entryNumber" value="${entry.entryNumber}"/>
                            <input type="hidden" name="productCode" value="${fn:escapeXml(entry.product.code)}"/>
                            <input type="hidden" name="initialQuantity" value="${entry.quantity}"/>
                            <ycommerce:testId code="cart_product_quantity">
                                <form:label cssClass="visible-xs visible-sm" path="quantity" for="quantity${entry.entryNumber}"></form:label>
                                <form:input cssClass="form-control js-update-entry-quantity-input" disabled="${not entry.updateable}" type="text" size="1" id="quantity_${entryNumber}" path="quantity" maxlength="3"/>
                            </ycommerce:testId>
                        </form:form>
                    </c:when>
					
					                     
                    <c:otherwise>
                        <c:url value="/cart/updateMultiD" var="cartUpdateMultiDFormAction" />
                        <form:form id="updateCartForm${entryNumber}" action="${cartUpdateMultiDFormAction}" method="post" class="js-qty-form${entryNumber}" commandName="updateQuantityForm${entryNumber}">
                            <input type="hidden" name="entryNumber" value="${entry.entryNumber}"/>
                            <input type="hidden" name="productCode" value="${fn:escapeXml(entry.product.code)}"/>
                            <input type="hidden" name="initialQuantity" value="${entry.quantity}"/>
                            <label class="visible-xs visible-sm"><spring:theme code="basket.page.qty"/>:</label>
                            <span class="qtyValue"><c:out value="${entry.quantity}" /></span>
                            <input type="hidden" name="quantity" value="0"/>
                            <ycommerce:testId code="cart_product_updateQuantity">
                                <div id="QuantityProduct${entryNumber}" class="updateQuantityProduct"></div>
                            </ycommerce:testId>
                        </form:form>
                    </c:otherwise>
                    
                </c:choose>
            </div>
            
            <%-- PO --%>
            <div class="item__purchaseOrderNumber hidden-xs hidden-sm" style="padding:0px;">
                <c:url value="/cart/updatePo" var="cartUpdatePoUrl" />
                <c:choose>
                	<c:when test="${not entry.product.multidimensional}" >
    					<div class="item__purchaseOrderNumber hidden-xs hidden-sm" style="padding:0px;">
							<label for="purchaseOrderNumber_${entryNumber}" class="visible-xs visible-sm"></label>
							<input id="purchaseOrderNumber_${entryNumber}" name="purchaseOrderNumber_${entryNumber}" class="form-control js-update-entry-po-input" type="text" value="${entry.purchaseOrderNumber}" size="2" maxlength="15" data-product="${fn:escapeXml(entry.product.code)}" data-url="${cartUpdatePoUrl}">
						</div>
                    </c:when>
                </c:choose>
            </div>
            
            
            <%-- data ordine --%>
            <div class="item__dataOrdine hidden-xs hidden-sm" style="padding:0px;">
                <c:url value="/cart/updateOrderDate" var="cartUpdateOrderDateUrl" />
                <c:choose>
                    <c:when test="${not entry.product.multidimensional}" >
	                    <div class="item__quantity hidden-xs hidden-sm form-element-icon datepicker dataOrdine_${entryNumber}" id="js-dataOrdine_${entryNumber}" data-date-format-for-date-picker="dd/mm/yy" style="padding:0px;">
	                    	<label cssClass="visible-xs visible-sm" for="dataOrdine_${entry.entryNumber}"></label>
	                    	<input class="form-control js-update-entry-dataOrdine-input" style="text-align:left; padding-left:10px;" type="text" size="2" id="dataOrdine_${entryNumber}" name="dataOrdine_${entryNumber}" maxlength="10" value="${entry.dataOrdine}" data-product="${fn:escapeXml(entry.product.code)}" data-url="${cartUpdateOrderDateUrl}"/>
	                    	
	 		        	<!--  
	 		        	<input id="allDataOrdine" name="allDataOrdine" class="form-control" type="text" value="" size="2" style="background-color:white; color:black; text-align:left; padding-left:10px;" maxlength="10"/>
	 		        	-->
			        	<i class="glyphicon glyphicon-calendar js-open-datepicker-dataOrdine" style="top:13px;"></i>
	                    	
	                    </div>
					</c:when>
				</c:choose>
            </div>
            
            
            <%-- cig --%>
            <div class="item__cig hidden-xs hidden-sm" style="padding:0px;">
                <c:url value="/cart/updateCig" var="cartUpdateCigUrl" />
                <c:choose>
                    <c:when test="${not entry.product.multidimensional}" >
	                    <div class="item__cig hidden-xs hidden-sm" style="padding:0px;">
							<label for="cig_${entryNumber}" class="visible-xs visible-sm"></label>
							<input id="cig_${entryNumber}" name="cig_${entryNumber}" class="form-control js-update-entry-cig-input" type="text" value="${entry.cgi}" size="2" maxlength="10" data-product="${fn:escapeXml(entry.product.code)}" data-url="${cartUpdateCigUrl}">
	                    </div>
	                </c:when>
                </c:choose>
            </div>
            
            
            <%-- cup --%>
            <div class="item__cup hidden-xs hidden-sm" style="padding:0px;">
                <c:url value="/cart/updateCup" var="cartUpdateCupUrl" />
                <c:choose>
                    <c:when test="${not entry.product.multidimensional}" >
	                    <div class="item__cup hidden-xs hidden-sm" style="padding:0px;">
							<label for="cup_${entryNumber}" class="visible-xs visible-sm"></label>
							<input id="cup_${entryNumber}" name="cup_${entryNumber}" class="form-control js-update-entry-cup-input" type="text" value="${entry.cup}" size="2" maxlength="15" data-product="${fn:escapeXml(entry.product.code)}" data-url="${cartUpdateCupUrl}">
	                    </div>
	                </c:when>
                </c:choose>
            </div>

            
            <%-- total --%>
            <ycommerce:testId code="cart_totalProductPrice_label">
                <div class="item__total js-item-total hidden-xs hidden-sm">
                	<c:if test="${entry.showPrice}">
                    	<format:price priceData="${entry.totalPrice}" displayFreeForZero="true"/>
                    </c:if>
                </div>
            </ycommerce:testId>

            <%-- menu icon --%>
            <div class="item__menu">
                <%-- 
                <c:if test="${!entry.updateable}" >
                --%>
                    <div class="btn-group">
                        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" id="editEntry_${entryNumber}">
                            <span class="glyphicon glyphicon-option-vertical"></span>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-right">
                            <c:if test="${not empty cartData.quoteData}">
                                <c:choose>
                                    <c:when test="${not entry.product.multidimensional}">
                                        <li>
                                            <a href="#entryCommentDiv_${entry.entryNumber}" data-toggle="collapse" >
                                                <spring:theme code="basket.page.comment.menu"/>
                                            </a>
                                        </li>
                                    </c:when>
                                    <c:otherwise>
                                        <li>
                                            <a href="#entryCommentDiv_${entry.entries.get(0).entryNumber}" data-toggle="collapse" >
                                                <spring:theme code="basket.page.comment.menu"/>
                                            </a>
                                        </li>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                            <form:form id="cartEntryActionForm" action="" method="post" />
                             <%-- Build entry numbers string for execute action -- Start --%>
                            <c:choose>
					            <c:when test="${entry.entryNumber eq -1}"> <%-- for multid entry --%>
					                <c:forEach items="${entry.entries}" var="subEntry" varStatus="stat">
						    			<c:set var="actionFormEntryNumbers" value="${stat.first ? '' : actionFormEntryNumbers.concat(';')}${subEntry.entryNumber}" />
						    		</c:forEach>
					            </c:when>
					            <c:otherwise>
					                <c:set var="actionFormEntryNumbers" value="${entry.entryNumber}" />
					            </c:otherwise>
					        </c:choose>
					        <%-- Build entry numbers string for execute action -- End --%>
                            <c:forEach var="entryAction" items="${entry.supportedActions}">
                                <c:url value="/cart/entry/execute/${entryAction}" var="entryActionUrl"/>
                                <li class="js-execute-entry-action-button" id="actionEntry_${fn:escapeXml(entryNumber)}"
                                    data-entry-action-url="${entryActionUrl}"
                                    data-entry-action="${fn:escapeXml(entryAction)}"
                                    data-entry-product-code="${fn:escapeXml(entry.product.code)}"
                                    data-entry-initial-quantity="${entry.quantity}"
                                    data-action-entry-numbers="${actionFormEntryNumbers}">
                                    <a href="#"><spring:theme code="basket.page.entry.action.${entryAction}"/></a>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                <%-- 
                </c:if>
                --%>
            </div>

            <div class="item__quantity__total visible-xs visible-sm">
                <c:if test="${entry.product.multidimensional}" >
                    <ycommerce:testId code="cart_product_updateQuantity">
                        <c:set var="showEditableGridClass" value="js-show-editable-grid"/>
                    </ycommerce:testId>
                </c:if>
                
                
                <div class="details ${showEditableGridClass}" data-index="${index}" data-read-only-multid-grid="${not entry.updateable}">
                    <div class="qty">
                        <c:choose>
                            <c:when test="${not entry.product.multidimensional}" >
                                <c:url value="/cart/update" var="cartUpdateFormAction" />
                                <form:form id="updateCartForm${entryNumber}" action="${cartUpdateFormAction}" method="post" commandName="updateQuantityForm${entry.entryNumber}"
                                           class="js-qty-form${entryNumber}"
                                           data-cart='{"cartCode" : "${fn:escapeXml(cartData.code)}","productPostPrice":"${entry.basePrice.value}","productName":"${fn:escapeXml(entry.product.name)}"}'>
                                    <input type="hidden" name="entryNumber" value="${entry.entryNumber}"/>
                                    <input type="hidden" name="productCode" value="${fn:escapeXml(entry.product.code)}"/>
                                    <input type="hidden" name="initialQuantity" value="${entry.quantity}"/>
                                    <ycommerce:testId code="cart_product_quantity">
                                        <form:label cssClass="" path="quantity" for="quantity${entry.entryNumber}"><spring:theme code="basket.page.qty"/>:</form:label>
                                        <form:input cssClass="form-control js-update-entry-quantity-input" disabled="${not entry.updateable}" type="text" size="1" id="quantity_${entryNumber}" path="quantity" maxlength="3"/>
                                    </ycommerce:testId>
                                </form:form>
                            </c:when>
                            <c:otherwise>
                                <c:url value="/cart/updateMultiD" var="cartUpdateMultiDFormAction" />
                                <form:form id="updateCartForm${entryNumber}" action="${cartUpdateMultiDFormAction}" method="post" class="js-qty-form${entryNumber}" commandName="updateQuantityForm${entryNumber}">
                                    <input type="hidden" name="entryNumber" value="${entry.entryNumber}"/>
                                    <input type="hidden" name="productCode" value="${fn:escapeXml(entry.product.code)}"/>
                                    <input type="hidden" name="initialQuantity" value="${entry.quantity}"/>
                                    <%-- <label class="labelQty"><spring:theme code="basket.page.qty"/>:</label> --%>
                                    <%-- <span class="qtyValue"><c:out value="${entry.quantity}" /></span> --%>
                                    <input type="hidden" name="quantity" value="0"/>
                                    <ycommerce:testId code="cart_product_updateQuantity">
                                        <div id="QuantityProduct${entryNumber}" class="updateQuantityProduct"></div>
                                    </ycommerce:testId>
                                </form:form>
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${entry.product.multidimensional}" >
                            <ycommerce:testId code="cart_product_updateQuantity">
                                <span class="glyphicon glyphicon-chevron-right"></span>
                            </ycommerce:testId>
                        </c:if>
                    </div>
                    


			        <!-- Purchase order number for product with no variants  -->
                    
                        <c:choose>
                            <c:when test="${not entry.product.multidimensional}" >
                                <div class="qty">
	                                <c:url value="/cart/updatePo" var="cartUpdatePoFormAction" />
	                                <form:form id="updateCartForm${entryNumber}" action="${cartUpdatePoFormAction}" method="post" commandName="updatePoForm${entry.entryNumber}"
	                                           class="js-po-form${entryNumber}"
	                                           data-cart='{"cartCode" : "${fn:escapeXml(cartData.code)}","productPostPrice":"${entry.basePrice.value}","productName":"${fn:escapeXml(entry.product.name)}"}'>
	                                    <input type="hidden" name="entryNumber" value="${entry.entryNumber}"/>
	                                    <input type="hidden" name="productCode" value="${fn:escapeXml(entry.product.code)}"/>
	                                    <input type="hidden" name="initialPo" value="${entry.purchaseOrderNumber}"/>
	                                    <ycommerce:testId code="cart_product_purchaseOrderNumber">
	                                        <form:label cssClass="" path="purchaseOrderNumber" for="purchaseOrderNumber_${entry.entryNumber}">PO# :</form:label>
	                                        <form:input cssClass="form-control js-update-entry-po-input" disabled="${not entry.updateable}" type="text" size="2" id="purchaseOrderNumber_${entryNumber}" path="purchaseOrderNumber" maxlength="3"/>
	                                    </ycommerce:testId>
	                                </form:form>
                            	</div>
                            </c:when>
                            <c:otherwise>
                            </c:otherwise>
                        </c:choose>
                    
                    
                    <!-- Order data for product with no variants -->
                        <c:choose>
                            <c:when test="${not entry.product.multidimensional}" >
                                <div class="qty">
	                                <c:url value="/cart/updateDataOrdine" var="cartUpdateDataOrdineFormAction" />
	                                <form:form id="updateCartForm_mobile${entryNumber}" action="${cartUpdateDataOrdineFormAction}" method="post" commandName="updateDataOrdineForm${entry.entryNumber}"
	                                           class="js-dataOrdine-form${entryNumber}"
	                                           data-cart='{"cartCode" : "${fn:escapeXml(cartData.code)}","productPostPrice":"${entry.basePrice.value}","productName":"${fn:escapeXml(entry.product.name)}"}'>
	                                    <input type="hidden" name="entryNumber" value="${entry.entryNumber}"/>
	                                    <input type="hidden" name="productCode" value="${fn:escapeXml(entry.product.code)}"/>
	                                    <input type="hidden" name="initialDataOrdine" value="${entry.dataOrdine}"/>
  										<div class="item__quantity visible-xs visible-sm form-element-icon datepicker dataOrdine_mobile_${entryNumber}" id="js-dataOrdine_mobile_${entryNumber}" data-date-format-for-date-picker="dd/mm/yy" style="padding:0px;">
	                                    <ycommerce:testId code="cart_product_dataOrdine">
	                                        <form:label cssClass="" path="dataOrdine" for="dataOrdine_mobile_${entry.entryNumber}">Data :</form:label>
	                                        <form:input cssClass="form-control js-update-entry-dataOrdine-input" disabled="${not entry.updateable}" type="text" size="2" id="dataOrdine_mobile_${entryNumber}" path="dataOrdine" style="padding-left:0px; padding-right:0px;" maxlength="10"/>
	                                    </ycommerce:testId>
	                                    </div>	                                
	                                </form:form>
                            	</div>
                            </c:when>
                            <c:otherwise>
                            </c:otherwise>
                        </c:choose>

            		<%-- cig for product with no variants --%>
                		<c:choose>
                    		<c:when test="${not entry.product.multidimensional}" >
                    			<div class="qty">
	                    			<c:url value="/cart/updateCgi" var="cartUpdateCgiFormAction" />
			                    	<form:form id="updateCartForm${entryNumber}" action="${cartUpdateCgiFormAction}" method="post" commandName="updateCgiForm${entry.entryNumber}" class="js-cig-form${entryNumber}" data-cart='{"cartCode" : "${fn:escapeXml(cartData.code)}","productPostPrice":"${entry.basePrice.value}","productName":"${fn:escapeXml(entry.product.name)}"}'>
			                            <input type="hidden" name="entryNumber" value="${entry.entryNumber}"/>
			                            <input type="hidden" name="productCode" value="${fn:escapeXml(entry.product.code)}"/>
			                            <input type="hidden" name="initialCgi" value="${entry.cgi}"/>
			                            <ycommerce:testId code="cart_product_cig">
			                                <form:label cssClass="" path="cgi" for="cig${entry.entryNumber}">CGI :</form:label>
			                                <form:input cssClass="form-control js-update-entry-cig-input" type="text" size="2" id="cig${entryNumber}" path="cgi" disabled="${not entry.updateable}" maxlength="3"/>
			                            </ycommerce:testId>
			                        </form:form>
	                        	</div>
	                        </c:when>
	                        <c:otherwise>
	                        </c:otherwise>
                        </c:choose>
            		
            
            
            		<%-- cup for product with no variants--%>
                		<c:choose>
                    		<c:when test="${not entry.product.multidimensional}" >
			            		<div class="qty">
	                    	        <c:url value="/cart/updateCup" var="cartUpdateCupFormAction" />
			                        <form:form id="updateCartForm${entryNumber}" action="${cartUpdateCupFormAction}" method="post" commandName="updateCupForm${entry.entryNumber}"
			                                   class="js-cup-form${entryNumber}"
			                                    data-cart='{"cartCode" : "${fn:escapeXml(cartData.code)}","productPostPrice":"${entry.basePrice.value}","productName":"${fn:escapeXml(entry.product.name)}"}'>
			                            <input type="hidden" name="entryNumber" value="${entry.entryNumber}"/>
			                            <input type="hidden" name="productCode" value="${fn:escapeXml(entry.product.code)}"/>
			                            <input type="hidden" name="initialCup" value="${entry.cup}"/>
			                            <ycommerce:testId code="cart_product_cup">
			                                <form:label cssClass="" path="cup" for="cup${entry.entryNumber}">CUP :</form:label>
			                                <form:input cssClass="form-control js-update-entry-cup-input" type="text" size="2" id="cup${entryNumber}" path="cup" disabled="${not entry.updateable}" maxlength="3"/>
			                            </ycommerce:testId>
			                        </form:form>
			            		</div>
                    		</c:when>
                    		<c:otherwise>
                    		</c:otherwise>
                    	</c:choose>                
            		
					<!-- Order entry total -->
            		<div class="qty">
                        <ycommerce:testId code="cart_totalProductPrice_label">
                            <div class="item__total js-item-total">
                                <format:price priceData="${entry.totalPrice}" displayFreeForZero="true"/>
                            </div>
                        </ycommerce:testId>
                    </div>
                </div>

                <c:if test="${entry.product.configurable}">
                    <div class="hidden-md hidden-lg">
                        <spring:url value="/cart/{/entryNumber}/configuration/{/configuratorType}" var="entryConfigUrl" htmlEscape="false">
                            <spring:param name="entryNumber"  value="${entry.entryNumber}"/>
                            <spring:param name="configuratorType"  value="${entry.configurationInfos[0].configuratorType}" />
                        </spring:url>
                        <div class="item__configurations">
                            <c:forEach var="config" items="${entry.configurationInfos}">
                                <c:set var="style" value=""/>
                                <c:if test="${config.status eq errorStatus}">
                                    <c:set var="style" value="color:red"/>
                                </c:if>
                                <div class="item__configuration--entry" style="${style}">
                                    <div class="row">
                                        <div class="item__configuration--name col-sm-4">
                                                ${fn:escapeXml(config.configurationLabel)}
                                                <c:if test="${not empty config.configurationLabel}">:</c:if>
                                        </div>
                                        <div class="item__configuration--value col-sm-8">
                                                ${fn:escapeXml(config.configurationValue)}
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                        <c:if test="${not empty entry.configurationInfos}">
                            <div class="item__configurations--edit">
                                <a class="btn" href="${entryConfigUrl}"><spring:theme code="basket.page.change.configuration"/></a>
                            </div>
                        </c:if>
                    </div>
                </c:if>
            </div>
        </li>

        <li class="item__list--comment">
            <div class="item__comment quote__comments">
                <c:if test="${not empty cartData.quoteData}">
                    <c:choose>
                        <c:when test="${not entry.product.multidimensional}">
                            <c:set var="entryNumber" value="${entry.entryNumber}"/>
                            <c:set var="entryComments" value="${entry.comments}"/>
                        </c:when>
                        <c:otherwise>
                            <c:set var="entryNumber" value="${entry.entries.get(0).entryNumber}"/>
                            <c:set var="entryComments" value="${entry.entries.get(0).comments}"/>
                        </c:otherwise>
                    </c:choose>
                    <c:choose>
                        <c:when test="${not empty entryComments}">
                            <order:orderEntryComments entryComments="${entryComments}" entryNumber="${entryNumber}"/>
                        </c:when>
                        <c:otherwise>
                            <div id="entryCommentListDiv_${fn:escapeXml(entryNumber)}" data-show-all-entry-comments="false"></div>
                        </c:otherwise>
                    </c:choose>
                    <%-- 
                    <c:if test="${!entry.updateable">
                    --%>
                        <div class="row">
                            <div class="col-sm-7 col-sm-offset-5">
                                <div id="entryCommentDiv_${fn:escapeXml(entryNumber)}" class="${not empty entryComments ?'collapse in':'collapse'}">
                                    <textarea class="form-control js-quote-entry-comments" id="entryComment_${fn:escapeXml(entryNumber)}"
                                              placeholder="<spring:theme code="quote.enter.comment"/>"
                                              data-entry-number="${fn:escapeXml(entryNumber)}" rows="3" maxlength="255"></textarea>
                                </div>
                            </div>
                        </div>
                    <%--
                    </c:if>
                    --%>
                </c:if>
            </div>
        </li>

        <li>
            <spring:url value="/cart/getProductVariantMatrix" var="targetUrl"/>
            <grid:gridWrapper entry="${entry}" index="${index}" styleClass="add-to-cart-order-form-wrap display-none"
                targetUrl="${targetUrl}"/>
        </li>
 </c:if>
