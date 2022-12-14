<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ attribute name="product" required="true" type="de.hybris.platform.commercefacades.product.data.ProductData" %>

<spring:htmlEscape defaultHtmlEscape="true" />


<c:if test="${product.stockable and product.purchasable}">
	<c:choose>
		<c:when test="${product.stock.stockLevelStatus.code eq 'inStock'}">
			<c:set var="productStockLevel">
				<c:if test="${stockQuantityEnabled}">${product.stock.stockLevel}&nbsp;</c:if><spring:theme code="product.variants.available"/>
			</c:set>
		</c:when>
		<%-- 
		<c:otherwise>
			<c:set var="productStockLevel">
				<spring:theme code="product.variants.out.of.stock"/>
			</c:set>
		</c:otherwise>
		--%>
	</c:choose>
	<!-- FIX SOL601 -->
	<%-- <p class="stock-status">${productStockLevel}</p> --%>
</c:if>










<%-- 
<c:if test="${product.purchasable and product.stock.stockLevel le 0}">
	<c:set var="productStockLevel"><spring:theme code="product.variants.out.of.stock"/></c:set>
</c:if>
<c:if test="${product.stock.stockLevel gt 0 and empty product.stock.stockThreshold}">
	<c:set var="productStockLevel">${product.stock.stockLevel}&nbsp;<spring:theme code="product.variants.in.stock"/></c:set>
</c:if>
<c:if test="${product.stock.stockLevel gt 0 and product.stock.stockLevel le product.stock.stockThreshold}">
	<c:set var="productStockLevel">${product.stock.stockLevel}&nbsp;<spring:theme code="product.variants.in.stock"/></c:set>
</c:if>
<c:if test="${product.stock.stockLevel gt 0 and product.stock.stockLevel gt product.stock.stockThreshold}">
	<c:set var="productStockLevel">${product.stock.stockThreshold}&nbsp;<spring:theme code="product.variants.in.stock"/></c:set>
</c:if>
<c:if test="${product.stock.stockLevelStatus.code eq 'lowStock'}">
	<c:set var="productStockLevel"><spring:theme code="product.variants.only.left" arguments="${product.stock.stockLevel}"/></c:set>
</c:if>
<c:if test="${product.stock.stockLevelStatus.code eq 'inStock' and empty product.stock.stockLevel}">
	<c:set var="productStockLevel"><spring:theme code="product.variants.available"/></c:set>
</c:if>
<p class="stock-status">${productStockLevel}</p> 
--%>