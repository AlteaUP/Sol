<%@ tag body-content="empty" trimDirectiveWhitespaces="true"%>

<%@ attribute name="variantFirstLevel" required="true" type="de.hybris.platform.commercefacades.product.data.VariantMatrixElementData"%>
<%@ attribute name="variant" required="true" type="de.hybris.platform.commercefacades.product.data.VariantMatrixElementData"%>
<%@ attribute name="loopIndex" required="true" type="java.lang.Integer"%>
<%@ attribute name="readOnly" required="false" type="java.lang.Boolean"%>
<%@ attribute name="product" required="false" type="de.hybris.platform.commercefacades.product.data.ProductData" %>
<%@ attribute name="skusId" required="true" type="java.lang.String"%>
<%@ attribute name="showName" required="false" type="java.lang.Boolean"%>
<%@ attribute name="showLastDimensionTitle" required="false" type="java.lang.Boolean"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="grid" tagdir="/WEB-INF/tags/responsive/grid" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/responsive/product" %>

<spring:htmlEscape defaultHtmlEscape="true" />

<c:set var="searchResultType" value="${param.searchResultType}"/>
<c:set var="isCreateOrderForm" value="true"/>
<c:if test="${not empty param.isCreateOrderForm}" >
	<c:set var="isCreateOrderForm" value="false"/>
</c:if>

<c:if test="${not empty sessionScope.skuIndex}">
	<c:set var="loopIndex" value="${sessionScope.skuIndex}"/>
</c:if>

<c:set var="inputDisabled" value=""/>

<c:if test="${readOnly == true}">
    <c:set var="inputDisabled" value="disabled"/>
</c:if>

<c:set var="readonly" value="readonly"/>
<c:if test="${empty variant.showSearchedVariant or variant.showSearchedVariant}">
	<c:set var="readonly" value=""/>
</c:if>

<tr>
	<td class="${cssStockClass} widthReference hidden-xs erpCode" data-erpCode="${fn:escapeXml(variant.variantOption.erpCode)}">
		<span class="labelVariantCode"><spring:theme code="text.page.cart.product.mobile"/></span> ${fn:escapeXml(variant.variantOption.erpCode)}
	</td>
	
	<td nowrap class="${cssStockClass} widthReference variant-criterias hidden-xs">
    
        <c:set var="cssStockClass" value=""/>
        <c:if test="${variant.variantOption.stock.stockLevel <= 0}">
            <c:set var="cssStockClass" value="out-of-stock"/>
        </c:if>
        <c:if test="${not empty readonly}">
            <c:set var="cssStockClass" value="out-of-stock"/>
        </c:if>
        
        <c:set var="currentVariant" value="${variant}"/>
        <c:set var="lastVariant" value="${variant}"/>
        
		<%-- ${fn:escapeXml(variantFirstLevel.parentVariantCategory.name)} : ${fn:escapeXml(variantFirstLevel.variantValueCategory.name)} --%>
        <!-- <br/> -->

        <c:forEach begin="0" end="10" varStatus="count">

			<c:if test="${currentVariant ne null}">
				${fn:escapeXml(currentVariant.parentVariantCategory.name)} : ${fn:escapeXml(currentVariant.variantValueCategory.name)}
		        <br/>
			</c:if>		        
		    
            <c:choose>
	        	<c:when test="${currentVariant.elements.size() > 0}">
		        	<c:set var="currentVariant" value="${currentVariant.elements[0]}"/>
		        	<c:set var="lastVariant" value="${currentVariant}"/>
	        	</c:when>
	        	<c:otherwise>
	        		<c:set var="currentVariant" value="${null}"/>
	         	</c:otherwise>
	        </c:choose>
	        
	     </c:forEach>
        
        <!--  
        ${fn:escapeXml(variant.variantValueCategory.name)}
        -->
        
        <!-- <div class="variant-prop hidden-sm hidden-md hidden-lg" data-variant-prop="${fn:escapeXml(variant.variantValueCategory.name)}">
            <span>${fn:escapeXml(variants[0].parentVariantCategory.name)}:</span>
            ${fn:escapeXml(variant.variantValueCategory.name)}
        </div> -->
	</td>
	<td class="${cssStockClass} widthReference hidden-xs">
		<span class="labelVariantCode"><spring:theme code="text.page.cart.price.mobile"/></span>
        <span class="price" data-variant-price="${lastVariant.variantOption.priceData.value}">
            <c:set var="disableForOutOfStock" value="${inputDisabled}"/>
            <format:price priceData="${lastVariant.variantOption.priceData}"/>
        </span>
        <input type=hidden id="productPrice[${loopIndex}]" value="${lastVariant.variantOption.priceData.value}"/>
    </td>    
	<td class="${cssStockClass} widthReference hidden-xs">
           <c:if test="${product.stockable and variant.variantOption.stock.stockLevel == 0}">
               <c:set var="disableForOutOfStock" value="disabled"/>
           </c:if>
           <c:choose>
    			<c:when test="${not empty readonly}">
	    			<c:set var="textFieldClass" value="sku-quantityDisabled"/>
	    		</c:when>
    			<c:when test="${empty readonly}">
    				<c:set var="textFieldClass" value="sku-quantity"/>
    			</c:when>
	       </c:choose>
           <input type="hidden" class="${fn:escapeXml(skusId)} sku" name="cartEntries[${loopIndex}].sku" id="cartEntries[${loopIndex}].sku" value="${fn:escapeXml(variant.variantOption.code)}" />
           <label class="labelPo"><spring:theme code="text.page.cart.quantity.mobile"/></label>
           <input type="textbox" maxlength="3" class="${textFieldClass}" data-instock="${variant.variantOption.stock.stockLevel}"  data-variant-id="variant_${loopIndex}" name="cartEntries[${loopIndex}].quantity" data-product-selection='{"product":"${fn:escapeXml(variant.variantOption.code)}"}' id="cartEntries[${loopIndex}].quantity" value="0" ${disableForOutOfStock} data-parent-id="${fn:escapeXml(product.code)}" ${readonly}/>
	</td>  
    <td class="${cssStockClass} widthReference hidden-xs">
    	<c:if test="${not empty param and isCreateOrderForm}">
	    	<c:choose>
    		<c:when test="${disableForOutOfStock eq 'disabled'}">
    			<c:set var="textFieldClass" value="sku-purchaseOrderNumberDisabled"/>
    		</c:when>
    		<c:otherwise>
    			<c:set var="textFieldClass" value="form-control sku-purchaseOrderNumber"/>
    		</c:otherwise>
    		</c:choose>
	    	<label class="labelPo"><spring:theme code="text.page.cart.po.mobile"/></label>
			<input type="textbox" maxlength="15" class="${textFieldClass}" name="cartEntries[${loopIndex}].purchaseOrderNumber" data-product='${fn:escapeXml(variant.variantOption.code)}' id="cartEntries[${loopIndex}].purchaseOrderNumber" value="" ${disableForOutOfStock} data-parent-id="${fn:escapeXml(product.code)}"/>
		</c:if>
	</td>
	
	<td class="${cssStockClass} widthReference hidden-xs">			
		<c:if test="${not empty param and isCreateOrderForm}">
	    <c:choose>
    		<c:when test="${disableForOutOfStock eq 'disabled'}">
    			<c:set var="textFieldClass" value="sku-dataOrdineDisabled"/>
    		</c:when>
    		<c:otherwise>
    			<c:set var="textFieldClass" value="sku-dataOrdine"/>
    		</c:otherwise>
    	</c:choose>	
	
    	<span class="form-element-icon datepicker cartEntries[${loopIndex}].dataOrdine" id="js-cartEntries[${loopIndex}].dataOrdine" data-date-format-for-date-picker="dd/mm/yy">
			<label class="labelPo"><spring:theme code="text.page.cart.date.mobile"/></label>
			<input data-date-format-for-date-picker="dd/mm/yy" class="${textFieldClass}" maxlength="10" type="textbox" data-product='${fn:escapeXml(variant.variantOption.code)}' name="cartEntries[${loopIndex}].dataOrdine"  id="cartEntries[${loopIndex}].dataOrdine" value="" ${disableForOutOfStock} data-parent-id="${fn:escapeXml(product.code)}" style="padding-left:10px; padding-right:0px; text-align:left;"/>
			<i class="glyphicon glyphicon-calendar js-open-datepicker-dataOrdine" style="top:4px;"></i>
	    </span>
		</c:if>
	</td>	 
	
	<td class="${cssStockClass} widthReference hidden-xs">   
		<c:if test="${not empty param and isCreateOrderForm}">
	    	    	<c:choose>
    		<c:when test="${disableForOutOfStock eq 'disabled'}">
    			<c:set var="textFieldClass" value="sku-cigDisabled"/>
    		</c:when>
    		<c:otherwise>
    			<c:set var="textFieldClass" value="form-control sku-cig"/>
    		</c:otherwise>
    	</c:choose>	
    	<label class="labelPo"><spring:theme code="text.page.cart.cgi.mobile"/></label>
		<input type="textbox" maxlength="10" class="${textFieldClass}" name="cartEntries[${loopIndex}].cig" data-product='${fn:escapeXml(variant.variantOption.code)}' id="cartEntries[${loopIndex}].cig" value="" ${disableForOutOfStock} data-parent-id="${fn:escapeXml(product.code)}"/>
		</c:if>
	</td>
	
	<td class="${cssStockClass} widthReference hidden-xs">  
		<c:if test="${not empty param and isCreateOrderForm}"> 
	    	<c:choose>
    		<c:when test="${disableForOutOfStock eq 'disabled'}">
    			<c:set var="textFieldClass" value="sku-cigDisabled"/>
    		</c:when>
    		<c:otherwise>
    			<c:set var="textFieldClass" value="form-control sku-cup"/>
    		</c:otherwise>
    	</c:choose>	
    	<label class="labelPo"><spring:theme code="text.page.cart.cup.mobile"/></label>
		<input type="textbox" maxlength="15" class="${textFieldClass}" name="cartEntries[${loopIndex}].cup" data-product='${fn:escapeXml(variant.variantOption.code)}' id="cartEntries[${loopIndex}].cup" value="" ${disableForOutOfStock} data-parent-id="${fn:escapeXml(product.code)}"/>
		</c:if>
	</td>
	
	<td class="${cssStockClass} widthReference hidden-xs">
		<c:if test="${not empty param and isCreateOrderForm}">
			<c:if test="${!variant.variantOption.refill}">
				<span class="data-grid-total" data-grid-total-id="total_value_${loopIndex}"></span>
			</c:if>
			<c:set var="loopIndex" value="${loopIndex +1}"/>
			<c:set var="skuIndex" scope="session" value="${sessionScope.skuIndex + 1}"/>
		</c:if>
	 </td>
	 <td class="mobile-cart-actions hide">
	 	<c:if test="${not empty param and isCreateOrderForm}">
	        <a href="#" class="btn btn-primary closeVariantModal"><spring:theme code="popup.done"/></a>
	    </c:if>
	 </td>

    <!--  
	<td class="variant-select hidden-sm hidden-md hidden-lg">
	-->

    <td class="variant-select hidden-md hidden-lg">
        <a href="#" class="variant-select-btn">
            <span class="selectSize">Add</span>
            <span class="editSize">Edit</span>
        </a>
    </td>
</tr>
