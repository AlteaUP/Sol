<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="product" required="true" type="de.hybris.platform.commercefacades.product.data.ProductData" %>
<%@ attribute name="showName" required="false" type="java.lang.Boolean"%>
<%@ attribute name="filterSkus" required="false" type="java.util.List" %>
<%@ attribute name="readOnly" required="false" type="java.lang.Boolean"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="grid" tagdir="/WEB-INF/tags/responsive/grid" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="searchResultType" value="${param.searchResultType}"/>
<c:set var="isCreateOrderForm" value="true"/>
<c:if test="${not empty param.isCreateOrderForm}" >
	<c:set var="isCreateOrderForm" value="false"/>
</c:if>
<c:set var="loopIndexColor" value="0"/>

<div class="orderForm_grid_group">
	<div class="product-grid-container">
		<table class="product-grid-container-dim-3" >
			<!-- Header -->
			<tr class="hidden-size">
			    <th style="width:15%">
			    	<spring:theme code="text.page.cart.product"/>
			    </th>
			    <th style="width:15%">
			    	<spring:theme code="text.page.cart.info"/>
			    </th>
			    <th>
			    	<spring:theme code="text.page.cart.price"/>
			    </th>
			    <th>
			    	<spring:theme code="text.page.cart.quantity"/>
			    </th>
			    <th>
			    	<c:if test="${not empty param and isCreateOrderForm}">				    	
			    		<spring:theme code="text.page.cart.po"/>
			    	</c:if>
			    </th>
		    	<th>
		    		<c:if test="${not empty param and isCreateOrderForm}">
		    			<spring:theme code="text.page.cart.orderDate1"/>
		    		</c:if>
		    	</th>   
		    	<th>
		    		<c:if test="${not empty param and isCreateOrderForm}">
						<spring:theme code="text.page.cart.cig"/>
					</c:if>
				</th>	
		    	<th>
		    		<c:if test="${not empty param and isCreateOrderForm}">
		    			<spring:theme code="text.page.cart.cup"/>
		    		</c:if>
		    	</th>
			    <th>
			    	<c:if test="${not empty param and isCreateOrderForm}">
			    		<spring:theme code="text.page.cart.total"/>
			    	</c:if>
			    </th>
			</tr>

			<c:forEach items="${product.variantMatrix}" var="variant" varStatus="parentLoop">
				<c:if test="${empty(filterSkus) || (!empty(filterSkus) && fn:contains(filterSkus, variant.variantOption.code))}">
					<c:set var="skusId" value="${variant.variantOption.code}"/>
					
					<grid:coreTableHeader variant="${variant}"
										  product="${product}"
										  skusId="${skusId}"
										  showName="${showName}"
										  readOnly="${readOnly}" />
			
		            <grid:solCoreTable variantFirstLevel="${variantFirstLevel}"
		            				   variant="${variant}"
		                               loopIndex="${loopIndex}"
		                               readOnly="${readOnly}"
		        					   product="${product}"
									   skusId="${skusId}"
									   showName="${showName}"
									   showLastDimensionTitle="true"/>   
				</c:if>
			</c:forEach>
		</table>
	</div>
</div>