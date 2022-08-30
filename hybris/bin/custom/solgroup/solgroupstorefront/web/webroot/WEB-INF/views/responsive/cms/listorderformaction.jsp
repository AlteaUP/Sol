<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="urlProductQuote">/my-account/add-support-ticket</c:set>

<!--  Order form button : only for multidimensional products with price -->
<c:if test="${product.multidimensional and (not empty product.priceRange.minPrice.value) and (not empty product.priceRange.maxPrice.value)}" >
	<c:url value="${product.url}/orderForm" var="productOrderFormUrl"/>
	<form:form id="orderForm${fn:escapeXml(product.code)}" action="${productOrderFormUrl}" method="get">		
		<button id="productOrderFormButton" type="submit" class="btn btn-block btn-default glyphicon glyphicon-list-alt productOrderFormButton"></button>
	</form:form>
</c:if>


<!-- Product price quote : only for purchasable base products with default price -->

<c:if test="${not product.multidimensional}">

	<c:choose>
		<%-- Product without price --%>
		<c:when test="${empty product.price.value}">
			<form:form id="productQuoteForm${fn:escapeXml(product.code)}" action="${urlProductQuote}" method="get">	
				<input type="hidden" name="c" value="${product.erpCode}" />
				<input type="hidden" name="n" value="${product.name}" />
				<input type="hidden" name="i" value="${product.code}" />
				<button id="productOrderFormButton" type="submit" class="btn btn-block btn-default glyphicon glyphicon-euro productOrderFormButton"></button>
			</form:form>
		</c:when>
		<%-- Product with default price --%>
		<c:when test="${not empty product.price.value and not product.price.customPrice}">
				<form:form id="productQuoteForm${fn:escapeXml(product.code)}" action="${urlProductQuote}" method="get">	
					<input type="hidden" name="c" value="${product.erpCode}" />
					<input type="hidden" name="n" value="${product.name}" />
					<input type="hidden" name="i" value="${product.code}" />
					<button id="productOrderFormButton" type="submit" class="btn btn-block btn-default glyphicon glyphicon-euro productOrderFormButton"></button>
				</form:form>
		</c:when>
	




	
	
		<%-- 	
		<c:when test="${empty product.price.value}">
			<c:choose>
				<c:when test="${empty product.priceRange.minPrice.value}">
					<form:form id="productQuoteForm${fn:escapeXml(product.code)}" action="${urlProductQuote}" method="get">	
						<input type="hidden" name="c" value="${product.erpCode}" />
						<input type="hidden" name="n" value="${product.name}" />
						<input type="hidden" name="i" value="${product.code}" />
						<button id="productOrderFormButton" type="submit" class="btn btn-block btn-default glyphicon glyphicon-euro productOrderFormButton"></button>
					</form:form>
				</c:when>
				<c:otherwise>
					<c:if test="${not empty product.priceRange.minPrice.value and not product.priceRange.minPrice.customPrice}">
						<form:form id="productQuoteForm${fn:escapeXml(product.code)}" action="${urlProductQuote}" method="get">	
							<input type="hidden" name="c" value="${product.erpCode}" />
							<input type="hidden" name="n" value="${product.name}" />
							<input type="hidden" name="i" value="${product.code}" />
							<button id="productOrderFormButton" type="submit" class="btn btn-block btn-default glyphicon glyphicon-euro productOrderFormButton"></button>
						</form:form>
					</c:if>
				</c:otherwise>
			</c:choose>
		</c:when>
		<c:otherwise>
			<c:if test="${not product.price.customPrice}">
				<form:form id="productQuoteForm${fn:escapeXml(product.code)}" action="${urlProductQuote}" method="get">	
					<input type="hidden" name="c" value="${product.erpCode}" />
					<input type="hidden" name="n" value="${product.name}" />
					<input type="hidden" name="i" value="${product.code}" />
					<button id="productOrderFormButton" type="submit" class="btn btn-block btn-default glyphicon glyphicon-euro productOrderFormButton"></button>
				</form:form>
			</c:if>
		</c:otherwise>
		--%>

	</c:choose>
	
	

</c:if>