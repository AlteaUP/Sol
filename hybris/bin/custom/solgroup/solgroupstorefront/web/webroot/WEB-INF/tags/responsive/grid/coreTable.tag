<%@ tag body-content="empty" trimDirectiveWhitespaces="true"%>
<%@ attribute name="variants" required="true" type="java.util.List"%>
<%@ attribute name="inputTitle" required="true" type="java.lang.String"%>
<%@ attribute name="loopIndex" required="true" type="java.lang.Integer"%>
<%@ attribute name="readOnly" required="false" type="java.lang.Boolean"%>
<%@ attribute name="product" required="false" type="de.hybris.platform.commercefacades.product.data.ProductData" %>
<%@ attribute name="skusId" required="true" type="java.lang.String"%>
<%@ attribute name="firstVariant" required="true" type="de.hybris.platform.commercefacades.product.data.VariantMatrixElementData" %>
<%@ attribute name="showName" required="false" type="java.lang.Boolean"%>
<%@ attribute name="showLastDimensionTitle" required="false" type="java.lang.Boolean"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="grid" tagdir="/WEB-INF/tags/responsive/grid" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/responsive/product" %>

<spring:htmlEscape defaultHtmlEscape="true" />

<c:if test="${not empty sessionScope.skuIndex}">
	<c:set var="loopIndex" value="${sessionScope.skuIndex}"/>
</c:if>

<c:set var="inputDisabled" value=""/>

<c:if test="${readOnly == true}">
    <c:set var="inputDisabled" value="disabled"/>
</c:if>


<!-- Header -->
<tr class="hidden-size">
     <!--  
     <th>${fn:escapeXml(firstVariant.parentVariantCategory.name)}
        <c:if test="${firstVariant.elements ne null and firstVariant.elements.size() > 0}">
            /${fn:escapeXml(firstVariant.elements[0].parentVariantCategory.name)}
        </c:if>
    </th> 
    -->
    <th></th>
    <th><spring:theme code="text.page.cart.price"/></th>
    <th><spring:theme code="text.page.cart.quantity"/></th>
    <th><spring:theme code="text.page.cart.po"/></th>
    <th><spring:theme code="text.page.cart.orderDate2"/></th>
    <th><spring:theme code="text.page.cart.cig"/></th>
    <th><spring:theme code="text.page.cart.cup"/></th>
    
    <!-- <c:forEach items="${variants}" var="variant">
        <th>${fn:escapeXml(variant.variantValueCategory.name)}</th>
    </c:forEach> -->
</tr>

<!--  Variant rows -->
<c:forEach items="${variants}" var="variant">
<tr>
	<td class="${cssStockClass} widthReference hidden-xs">
    
        <c:set var="cssStockClass" value=""/>
        <c:if test="${product.stockable and variant.variantOption.stock.stockLevel <= 0}">
            <c:set var="cssStockClass" value="out-of-stock"/>
        </c:if>
        
        ${firstVariant.parentVariantCategory.name} : ${firstVariant.variantValueCategory.name}
        <br/>

        <c:if test="${variant ne null}">
	        <c:set var="myVariant" value="${variant}"/>
	        <c:forEach begin="0" end="10" var="count">
		        	${myVariant.parentVariantCategory.name} : ${myVariant.variantValueCategory.name} -----
		        	<br/>
	            <c:choose>
		        	<c:when test="${myVariant.elements.size() > 0}">
			        	<c:set var="myVariant" value="${myVariant.elements[0]}"/>
		        	</c:when>
		        	<c:otherwise>
		        		<c:set var="count" value="11"/>
		         	</c:otherwise>
		        </c:choose>
	        </c:forEach>
        </c:if>
        
        
		

        <!--  
        ${fn:escapeXml(variant.variantValueCategory.name)}
        -->
        
        <!-- <div class="variant-prop hidden-sm hidden-md hidden-lg" data-variant-prop="${fn:escapeXml(variant.variantValueCategory.name)}">
            <span>${fn:escapeXml(variants[0].parentVariantCategory.name)}:</span>
            ${fn:escapeXml(variant.variantValueCategory.name)}
        </div> -->
	</td>
	<td class="${cssStockClass} widthReference hidden-xs">
	
            <span class="price" data-variant-price="${variant.variantOption.priceData.value}">
                <c:set var="disableForOutOfStock" value="${inputDisabled}"/>
                <format:price priceData="${variant.variantOption.priceData}"/>
            </span>
            
            <input type=hidden id="productPrice[${loopIndex}]" value="${variant.variantOption.priceData.value}" />
    </td>    
	<td class="${cssStockClass} widthReference hidden-xs">
           <c:if test="${product.stockable and variant.variantOption.stock.stockLevel == 0}">
               <c:set var="disableForOutOfStock" value="disabled"/>
           </c:if>
           
         	<c:choose>
	    		<c:when test="${disableForOutOfStock eq 'disabled'}">
	    			<c:set var="textFieldClass" value="sku-quantityDisabled"/>
	    		</c:when>
	    		<c:otherwise>
	    			<c:set var="textFieldClass" value="sku-quantity"/>
	    		</c:otherwise>
	    	</c:choose>	
    
           <input type="hidden" class="${fn:escapeXml(skusId)} sku" name="cartEntries[${loopIndex}].sku" id="cartEntries[${loopIndex}].sku" value="${fn:escapeXml(variant.variantOption.code)}" />
           <input type="textbox" maxlength="3" class="${textFieldClass}" data-instock="${variant.variantOption.stock.stockLevel}"  data-variant-id="variant_${loopIndex}" name="cartEntries[${loopIndex}].quantity" data-product-selection='{"product":"${fn:escapeXml(variant.variantOption.code)}"}' id="cartEntries[${loopIndex}].quantity" value="0" ${disableForOutOfStock} data-parent-id="${fn:escapeXml(product.code)}"/>
	</td>  

    <td class="${cssStockClass} widthReference hidden-xs">   
    	<c:choose>
    		<c:when test="${disableForOutOfStock eq 'disabled'}">
    			<c:set var="textFieldClass" value="sku-purchaseOrderNumberDisabled"/>
    		</c:when>
    		<c:otherwise>
    			<c:set var="textFieldClass" value="sku-purchaseOrderNumber"/>
    		</c:otherwise>
    	</c:choose>	
		<input type="textbox" maxlength="3" class="${textFieldClass}" data-instock="${variant.variantOption.stock.stockLevel}"  data-variant-id="variant_${loopIndex}" name="cartEntries[${loopIndex}].purchaseOrderNumber" data-product-selection='{"product":"${fn:escapeXml(variant.variantOption.code)}"}' id="cartEntries[${loopIndex}].purchaseOrderNumber" value="" ${disableForOutOfStock} data-parent-id="${fn:escapeXml(product.code)}"/>
	</td>
	
	<td class="${cssStockClass} widthReference hidden-xs">   
    	<c:choose>
    		<c:when test="${disableForOutOfStock eq 'disabled'}">
    			<c:set var="textFieldClass" value="sku-dataOrdineDisabled"/>
    		</c:when>
    		<c:otherwise>
    			<c:set var="textFieldClass" value="sku-dataOrdine"/>
    		</c:otherwise>
    	</c:choose>	
	
		<!-- 
		<input type="textbox" maxlength="10" class="${textFieldClass}"  data-variant-id="variant_${loopIndex}" name="cartEntries[${loopIndex}].dataOrdine" data-product-selection='{"product":"${fn:escapeXml(variant.variantOption.code)}"}' id="cartEntries[${loopIndex}].dataOrdine" value="" ${disableForOutOfStock} data-parent-id="${fn:escapeXml(product.code)}"/>
		 -->
    	
    	<span class="form-element-icon datepicker cartEntries[${loopIndex}].dataOrdine" id="js-cartEntries[${loopIndex}].dataOrdine" data-date-format-for-date-picker="dd/mm/yy">
			<input data-date-format-for-date-picker="dd/mm/yy" class="${textFieldClass}" maxlength="10" type="textbox" data-variant-id="variant_${loopIndex}" name="cartEntries[${loopIndex}].dataOrdine" data-product-selection='{"product":"${fn:escapeXml(variant.variantOption.code)}"}' id="cartEntries[${loopIndex}].dataOrdine" value="" ${disableForOutOfStock} data-parent-id="${fn:escapeXml(product.code)}"/>
	    </span>
    	
    	
		
	</td>
	
	<td class="${cssStockClass} widthReference hidden-xs">   
    	<c:choose>
    		<c:when test="${disableForOutOfStock eq 'disabled'}">
    			<c:set var="textFieldClass" value="sku-cgiDisabled"/>
    		</c:when>
    		<c:otherwise>
    			<c:set var="textFieldClass" value="sku-cgi"/>
    		</c:otherwise>
    	</c:choose>	
		<input type="textbox" maxlength="3" class="${textFieldClass}" data-instock="${variant.variantOption.stock.stockLevel}"  data-variant-id="variant_${loopIndex}" name="cartEntries[${loopIndex}].cgi" data-product-selection='{"product":"${fn:escapeXml(variant.variantOption.code)}"}' id="cartEntries[${loopIndex}].cgi" value="" ${disableForOutOfStock} data-parent-id="${fn:escapeXml(product.code)}"/>
	</td>
	
	<td class="${cssStockClass} widthReference hidden-xs">   
    	<c:choose>
    		<c:when test="${disableForOutOfStock eq 'disabled'}">
    			<c:set var="textFieldClass" value="sku-cupDisabled"/>
    		</c:when>
    		<c:otherwise>
    			<c:set var="textFieldClass" value="sku-cup"/>
    		</c:otherwise>
    	</c:choose>	
		<input type="textbox" maxlength="3" class="${textFieldClass}" data-instock="${variant.variantOption.stock.stockLevel}"  data-variant-id="variant_${loopIndex}" name="cartEntries[${loopIndex}].cup" data-product-selection='{"product":"${fn:escapeXml(variant.variantOption.code)}"}' id="cartEntries[${loopIndex}].cup" value="" ${disableForOutOfStock} data-parent-id="${fn:escapeXml(product.code)}"/>
	</td>
	
	<!-- <td class="${cssStockClass} widthReference hidden-xs">	
		<span class="data-grid-total" data-grid-total-id="total_value_${loopIndex}"></span>
		
		<c:set var="loopIndex" value="${loopIndex +1}"/>
		<c:set var="skuIndex" scope="session" value="${sessionScope.skuIndex + 1}"/>
 	</td>
    <td class="mobile-cart-actions hide">
        <a href="#" class="btn btn-primary closeVariantModal"><spring:theme code="popup.done"/></a>
    </td> -->

    <td class="variant-select hidden-sm hidden-md hidden-lg">
        <a href="#" class="variant-select-btn">
            <span class="selectSize"><spring:theme code="product.grid.selectSize"/>&nbsp;${fn:escapeXml(variants[0].parentVariantCategory.name)}</span>
            <span class="editSize"><spring:theme code="product.grid.editSize"/></span>
        </a>
    </td>
</tr>
</c:forEach>