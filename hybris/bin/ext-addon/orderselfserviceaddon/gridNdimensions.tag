<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="product" required="true" type="de.hybris.platform.commercefacades.product.data.ProductData" %>
<%@ attribute name="showName" required="false" type="java.lang.Boolean"%>
<%@ attribute name="filterSkus" required="false" type="java.util.List" %>
<%@ attribute name="readOnly" required="false" type="java.lang.Boolean"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="grid" tagdir="/WEB-INF/tags/responsive/grid" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="loopIndex" value="0"/>
<c:set var="loopIndexColor" value="0"/>
<c:set var="searchResultType" value="${param.searchResultType}"/>

<div class="orderForm_grid_group">
	<div class="product-grid-container">
		<table class="product-grid-container-dim-3" style="table-layout:fixed">

			<!-- Header -->			
			<tr class="hidden-size">
			    <th>
			    	<spring:theme code="text.page.cart.product"/>
			    </th>
			    <th nowrap style="width:20%"></th>
			    <th>
			    	<spring:theme code="text.page.cart.price"/>
			    </th>
			    <th>
			    	<spring:theme code="text.page.cart.quantity"/>
			    </th>
			    <th>
			    	<c:if test="${searchResultType ne 'order-form'}">
			    		<spring:theme code="text.page.cart.po"/>
			    	</c:if>
			    </th>
			    <c:if test="${searchResultType ne 'order-form'}">
			    	<th>
			    		<spring:theme code="text.page.cart.orderDate1"/>
			    	</th>
			    </c:if>
			    <c:if test="${searchResultType ne 'order-form'}">
			    	<th>
						<spring:theme code="text.page.cart.cig"/>
					</th>
			    </c:if>
				<c:if test="${searchResultType ne 'order-form'}">
			    	<th>
			    		<spring:theme code="text.page.cart.cup"/>
			    	</th>
			    </c:if>
			    <th>
			    	<spring:theme code="text.page.cart.total"/>
			    </th>
			</tr>


			<c:forEach items="${product.variantMatrix}" var="variantElement" varStatus="parentLoop">
			    <c:forEach items="${variantElement.elements}" var="variant" varStatus="loop">
			
				<c:if test="${empty(filterSkus) || (!empty(filterSkus) && fn:contains(filterSkus, variant.variantOption.code) ) }">
			
						<c:set var="skusId" value="${variant.variantOption.code}"/>
						<grid:coreTableHeader variant="${variant}"
											  product="${product}"
											  skusId="${skusId}"
											  showName="${showName}"
											  readOnly="${readOnly}" />
			
						<c:set var="loopIndexColor" value="${loopIndexColor +1}"/>
			
			            <grid:solCoreTable variant="${variant}"
			                               loopIndex="${loopIndex}"
			                               readOnly="${readOnly}"
			        					   product="${product}"
										   skusId="${skusId}"
										   showName="${showName}"
										   showLastDimensionTitle="true"/>
			            
			            <c:set var="loopIndex" value="${loopIndex + fn:length(variantElement.elements)}"/>
			                     
					</c:if>
			
				</c:forEach>
			
			</c:forEach>
			
		</table>
	</div>
</div>