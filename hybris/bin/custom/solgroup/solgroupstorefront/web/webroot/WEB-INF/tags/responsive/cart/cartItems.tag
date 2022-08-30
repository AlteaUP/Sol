<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="cartData" required="true" type="de.hybris.platform.commercefacades.order.data.CartData" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/responsive/product" %>
<%@ taglib prefix="storepickup" tagdir="/WEB-INF/tags/responsive/storepickup" %>
<%@ taglib prefix="cart" tagdir="/WEB-INF/tags/responsive/cart" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<spring:htmlEscape defaultHtmlEscape="true" />

<c:set var="errorStatus" value="<%= de.hybris.platform.catalog.enums.ProductInfoStatus.valueOf(\"ERROR\") %>" />
<ul class="item__list item__list__cart">
	<table class="hidden-xs hidden-sm cartHeader">
		<tr>
			<td>
				<li class="item__list--item" style="border-top:0px; border-bottom:0px;">

				<div class="hidden-xs hidden-sm item__toggle"></div>
				<div class="item__image"></div>
 				<div class="item__info"><spring:theme code="basket.page.product"/></div>
	            <div class="item__price"></div>
				<div class="item__quantity hidden-xs hidden-sm"><spring:theme code="basket.page.qty"/></div> 
 	            <div class="item__purchaseOrderNumber hidden-xs hidden-sm"><spring:theme code="basket.page.po"/></div>
 	            <div class="item__dataOrdine hidden-xs hidden-sm"><spring:theme code="basket.page.dataOrdine"/></div>
 	            <div class="item__quantity hidden-xs hidden-sm"><spring:theme code="basket.page.cgi"/></div>
 				<div class="item__quantity hidden-xs hidden-sm"><spring:theme code="basket.page.cup"/></div>
	            <div class="item__total js-item-total hidden-xs hidden-sm" style="text-align:left; padding-left:10px; padding-right:0px;"><spring:theme code="basket.page.total"/></div>
 	            <div class="item__menu"></div>
	
	            <!--
	            <div class="hidden-xs hidden-sm item__toggle"></div>
				<div class="item__image"></div>
				<div class="item__info"><spring:theme code="basket.page.item"/></div>
	            <div class="item__price"><spring:theme code="basket.page.price"/></div>
				<div class="item__quantity hidden-xs hidden-sm"><spring:theme code="basket.page.qty"/></div> 
	            <div class="item__purchaseOrderNumber hidden-xs hidden-sm" style="padding:0px;"><spring:theme code="basket.page.po"/></div>
	            <div class="item__dataOrdine hidden-xs hidden-sm" style="padding:0px;"><spring:theme code="basket.page.dataOrdine"/></div>
	            <div class="item__quantity hidden-xs hidden-sm" style="padding:0px;"><spring:theme code="basket.page.cgi"/></div>
				<div class="item__quantity hidden-xs hidden-sm" style="padding:0px;"><spring:theme code="basket.page.cup"/></div>
	            <div class="item__total js-item-total hidden-xs hidden-sm" style="text-align:left; padding-left:10px; padding-right:0px;">Total</div>
	            <div class="item__menu"></div> -->
					
				<!--  
		            <div class="hidden-xs hidden-sm item__toggle"></div>
					<div class="item__info"><spring:theme code="basket.page.item"/></div>
		            <div class="item__purchaseOrderNumber hidden-xs hidden-sm" style="padding:0px;"><spring:theme code="basket.page.po"/></div>
		            <div class="item__dataOrdine hidden-xs hidden-sm" style="padding:0px;"><spring:theme code="basket.page.dataOrdine"/></div>
		            <div class="item__quantity hidden-xs hidden-sm" style="padding:0px;"><spring:theme code="basket.page.cgi"/></div>
					<div class="item__quantity hidden-xs hidden-sm" style="padding:0px;"><spring:theme code="basket.page.cup"/></div>
		            <div class="item__total js-item-total hidden-xs hidden-sm" style="text-align:left; padding-left:10px; padding-right:0px;"></div>
		            <div class="item__menu"></div>
				-->		            
	
	        </li>

			</td>
		</tr>
	</table>   


    
<c:if test="${empty disableUpdate || not disableUpdate}">
    
	<table>
		<tr>
			<td>
	
				<li class="item__list--item">
	
				<c:choose>
					<c:when test="${pageType eq 'CART'}">
						<c:url value="/cart/updateAllProperties" var="updateAllProperties" />
					</c:when>
					<c:when test="${pageType eq 'QUOTE'}">
						<c:url value="/quote/updateAllProperties" var="updateAllProperties" />
					</c:when>
				</c:choose>
				<input type="hidden" id="updateAllProperties" name="updateAllProperties" value="${updateAllProperties }"/>
	
	 	        <div class="hidden-xs hidden-sm item__toggle"></div>
				<div class="item__image"></div>
				<div class="hidden-xs hidden-sm" style="padding-right: 30px;width: 350px;"><spring:theme code="basket.page.info"/></div>
 				<div class="item__info visible-xs visible-sm" style="margin:0px; padding:10px;"><spring:theme code="basket.page.info"/></div>
	            <div class="item__price"></div>
				<div class="item__quantity hidden-xs hidden-sm"></div> 
	
				<!--  	
	            <div class="hidden-xs hidden-sm item__toggle"></div>
				<div class="item__image"></div>
				<div class="item__info hidden-xs hidden-sm">Compilando questa riga le informazioni di PO Number, Data Ordine, CIG e CUP saranno salvate su tutti i prodotti del carrello</div>
				<div class="item__info visible-xs visible-sm" style="margin:0px; padding:10px;">Compilando questa riga le informazioni di PO Number, Data Ordine, CIG e CUP saranno salvate su tutti i prodotti del carrello</div>
	            <div class="item__price"></div>
				<div class="item__quantity hidden-xs hidden-sm"></div> -->

	            <div class="item__purchaseOrderNumber hidden-xs hidden-sm" style="padding:0px;">
			    	<label for="allPurchaseOrderNumber" class="visible-xs visible-sm"></label>
			    	<input id="allPurchaseOrderNumber" name="allPurchaseOrderNumber" class="form-control" type="text" value="" size="2" maxlength="15"/>
			   	</div>
		    	<div class="item__quantity__total visible-xs visible-sm">
		    		<div class="qty">
						<label for="allPurchaseOrderNumber_mobile" class="visible-xs visible-sm"><spring:theme code="basket.page.mobile.po"/></label>
				    	<input id="allPurchaseOrderNumber_mobile" name="allPurchaseOrderNumber_mobile" class="form-control" type="text" value="" size="2" maxlength="15"/>
				    	
	            	</div>
			 	</div>	            
				  	            
	            <div class="item__dataOrdine hidden-xs hidden-sm" style="padding:0px;">
			    	<div class="item__quantity hidden-xs hidden-sm form-element-icon datepicker dataOrdine_3" id="js-allDataOrdne" data-date-format-for-date-picker="dd/mm/yy" style="padding:0px;">
			        	<label for="allDataOrdine" class="visible-xs visible-sm"></label>
			        	<input id="allDataOrdine" name="allDataOrdine" class="form-control" type="text" value="" size="2" style="background-color:white; color:black; text-align:left; padding-left:10px;" maxlength="10"/>
			        	<i class="glyphicon glyphicon-calendar js-open-datepicker-allDataOrdine" style="top:13px;"></i>
			        </div>
			    </div>
		    	<div class="item__quantity__total visible-xs visible-sm">
			    	<div class="qty form-element-icon datepicker dataOrdine_3" id="js-allDataOrdne_mobile" data-date-format-for-date-picker="dd/mm/yy">
			        	<label for="allDataOrdine_mobile" class="visible-xs visible-sm"><spring:theme code="basket.page.mobile.dataOrdine"/></label>
			        	<input id="allDataOrdine_mobile" name="allDataOrdine_mobile" class="form-control" type="text" value="" size="2" readonly="readonly" style="background-color:white; color:black; padding-left:0px; padding-right:0px;" maxlength="10"/>
			        	<!--  
			        	<i class="glyphicon glyphicon-calendar js-open-datepicker-allDataOrdine"></i>
			        	-->
			        </div>
			 	</div>

				

				<!--  
				<div class="form-element-icon datepicker quote__expiration" id="js-allDataOrdine" data-date-format-for-date-picker="mm/dd/yy" data-expiration-time-url="" data-min-offer-validity-period-days="">
                	<div class="form-group">
						<input id="allDataOrdine" name="allDataOrdine" class="text quote__expiration--input form-control hasDatepicker" placeholder="dd/mm/yyyy" type="text" value="17/01/2018"></div>
						<i class="glyphicon glyphicon-calendar js-open-datepicker-allDataOrdine"></i>
                    </div>
				-->
	            
	            
	            <div class="item__quantity hidden-xs hidden-sm" style="padding:0px;">
			    	<label for="allCig" class="visible-xs visible-sm"></label>
	    			<input id="allCig" name="allCig" class="form-control" type="text" value="" size="2" minlength="10" maxlength="10"/>	    			
				</div>
		    	<div class="item__quantity__total visible-xs visible-sm">
		    	<div class="qty">
		    			<label for="allCig_mobile" class="visible-xs visible-sm"><spring:theme code="basket.page.mobile.cig"/></label>
				    	<input id="allCig_mobile" name="allCig_mobile" class="form-control" type="text" value="" size="2" minlength="10" maxlength="10"/>
	            	</div>
			 	</div>
	            
	            
	            
	            <div class="item__quantity hidden-xs hidden-sm" style="padding:0px;">
			    	<label for="allCup" class="visible-xs visible-sm"></label>
			    	<input id="allCup" name="allCup" class="form-control" type="text" value="" size="2" minlength="15" maxlength="15"/>			    	
			   	</div>
		    	<div class="item__quantity__total visible-xs visible-sm">
		    		<div class="qty">
		    			<label for="allCup_mobile" class="visible-xs visible-sm"><spring:theme code="basket.page.mobile.cup"/></label>
				    	<input id="allCup_mobile" name="allCup_mobile" class="form-control" type="text" value="" size="2" minlength="15" maxlength="15"/>
	            	</div>
			 	</div>

	            
				<div class="item__total js-item-total hidden-xs hidden-sm" style="text-align:left; padding-right:0px;">
					<c:choose>
						<c:when test="${pageType eq 'CART'}">
							<button type="button" class="btn btn-default" onclick="saveAllProperties('')"><spring:theme code="basket.page.updateAll.button"/></button>
						</c:when>
						<c:when test="${pageType eq 'QUOTE'}">
							<button type="button" class="btn btn-default" onclick="saveAllProperties('${cartData.quoteData.code}')"><spring:theme code="basket.page.updateAll.button"/></button>
						</c:when>
					</c:choose>
				</div>
		    	<div class="item__quantity__total visible-xs visible-sm">
		    		<div class="qty">
						<c:choose>
							<c:when test="${pageType eq 'CART'}">
								<button type="button" class="btn btn-default" onclick="saveAllProperties('')"><spring:theme code="basket.page.updateAll.button"/></button>
							</c:when>
							<c:when test="${pageType eq 'QUOTE'}">
								<button type="button" class="btn btn-default" onclick="saveAllProperties('${cartData.quoteData.code}')"><spring:theme code="basket.page.updateAll.button"/></button>
							</c:when>
						</c:choose>
	            	</div>
			 	</div>

	            
	            <div class="item__menu"><div class="btn-group"></div></div>
	
	        </li>
	
			
			</td>
		</tr>
	</table>    
    
</c:if>
	
	
	<c:forEach items="${cartData.rootGroups}" var="group" varStatus="loop">
    	<cart:rootEntryGroup cartData="${cartData}" entryGroup="${group}"/>
        <p></p>
    </c:forEach>
</ul>


<product:productOrderFormJQueryTemplates />


<storepickup:pickupStorePopup />
