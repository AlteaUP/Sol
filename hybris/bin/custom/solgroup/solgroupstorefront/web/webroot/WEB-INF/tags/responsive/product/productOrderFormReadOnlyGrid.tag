<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="readOnlyMultiDMap" required="true" type="java.util.Map"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/responsive/product" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>	

<spring:htmlEscape defaultHtmlEscape="true" />
<ul class="variant_item__list">
    <li class="hidden-xs hidden-sm">
        <ul class="variant_item__list--header">
        	<!-- <li class="item__toggle"></li> -->
            <li class="item__variant code"><spring:theme code="basket.page.erpCode"/></li>
            <li class="item__variant long_item"><spring:theme code="basket.page.item"/></li>
            <li class="item__variant item"><spring:theme code="basket.page.qty"/></li>
            <li class="item__variant item"><spring:theme code="basket.page.price"/></li>
            <c:if test="${empty param.from}">
	            <li class="item__variant item"><spring:theme code="basket.page.po"/></li>
	            <li class="item__variant item"><spring:theme code="basket.page.dataOrdine"/></li>
	            <li class="item__variant item"><spring:theme code="basket.page.cgi"/></li>
	            <li class="item__variant item"><spring:theme code="basket.page.cup"/></li>
	        </c:if>
            <li class="item__variant item"><spring:theme code="basket.page.total"/></li>
        </ul>
    </li>
</ul>

<c:forEach items="${readOnlyMultiDMap}" var="readonlyGridData">	 
	<ul class="variant_item__list">
		<c:forEach items="${readonlyGridData.value.leafDimensionDataSet}" var="leafDimensionData">
			<li class="hidden-xs hidden-sm">
				<ul class="variant_item__list--item" > 
					<!-- <div class="hidden-xs hidden-sm item__toggle">
					</div> -->
			    
				    <%-- ERP Code --%>
				    <div class="item__variant code">
				     	<%-- <c:if test="${fn:length(leafDimensionData.leafDimensionHeader) > 0}">
							${fn:escapeXml(leafDimensionData.leafDimensionHeader)} - ${fn:escapeXml(leafDimensionData.leafDimensionValue)}<br/>
						</c:if>	 --%>
						<ycommerce:testId code="orderDetail_productThumbnail_link">
							${readonlyGridData.value.product.erpCode}
						</ycommerce:testId>
				    </div> 
				
				   	<%-- product name, code, promotions --%>		   	
			    	<div class="item__variant long_item">			
			    		<ycommerce:testId code="orderDetails_productCode">
			        		<c:forEach items="${readonlyGridData.value.dimensionHeaderMap}" var="dimension">
								${fn:escapeXml(dimension.key)} - ${fn:escapeXml(dimension.value)}<br>	
							</c:forEach>
						</ycommerce:testId>						
						<!-- <div class="item__variant">
						</div> -->
			    	</div>
				    
				    <%-- quantity --%>
				    <div class="item__variant item">	
				    	<span class="visible-xs visible-sm"><spring:theme code="basket.page.qty"/>:</span>	
				        <ycommerce:testId code="orderDetails_productItemPrice_label">
				           <%--  <label class="visible-xs visible-sm"><spring:theme code="text.account.order.qty"/>:</label> --%>
				            <!-- <span class="qtyValue"> -->
				                ${fn:escapeXml(leafDimensionData.leafDimensionData)}
				            <!-- </span> -->
				        </ycommerce:testId>
				    </div> 
				    
				    <%-- price --%>
			    	<div class="item__variant item">
			        	<span class="visible-xs visible-sm"><spring:theme code="basket.page.price"/>:</span>
			        	<ycommerce:testId code="orderDetails_productItemPrice_label">
			            	<format:price priceData="${leafDimensionData.basePrice}" />
			        	</ycommerce:testId>
			    	</div>
				    
				    <%-- po --%>
				    <c:if test="${empty param.from}">
				    	<div class="item__variant item">
					        <span class="visible-xs visible-sm"><spring:theme code="basket.page.po"/>:</span>
					        <ycommerce:testId code="orderDetails_productItemPo_label">
					           ${fn:escapeXml(leafDimensionData.purchaseOrderNumber)}
					        </ycommerce:testId> 
				    	</div>
				     
				    	<%-- data ordine --%>
				   		<div class="item__variant item">
					    	<span class="visible-xs visible-sm"><spring:theme code="basket.page.dataOrdine"/>:</span>
					    	<ycommerce:testId code="orderDetails_productItemOrderDate_label">
					    		${fn:escapeXml(leafDimensionData.dataOrdine)}
					    	</ycommerce:testId>
				    	</div>
				    
				    	<%-- cig --%>
				    	<div class="item__variant item">
					    	<span class="visible-xs visible-sm"><spring:theme code="basket.page.cgi"/>:</span>
					    	<ycommerce:testId code="orderDetails_productItemCgi_label">
					    		${fn:escapeXml(leafDimensionData.cgi)}
					    	</ycommerce:testId>			    
				    	</div>
				    
					    <%-- cup --%>	   
				    	<div class="item__variant item">    	
					    	<span class="visible-xs visible-sm"><spring:theme code="basket.page.cup"/>:</span>
					   		<ycommerce:testId code="orderDetails_productItemCup_label">
					    	 	${fn:escapeXml(leafDimensionData.cup)}
					    	</ycommerce:testId>
				    	</div>
				    </c:if>
				
				    <%-- total --%>
					<div class="item__variant item">
						<span class="visible-xs visible-sm"><spring:theme code="basket.page.total"/>:</span>
							<c:choose>
								<c:when test="${!readonlyGridData.value.product.refill}">
				        			<ycommerce:testId code="orderDetails_productTotalPrice_label">
				        				<format:price priceData="${leafDimensionData.price}"/>
				       				 </ycommerce:testId>
								</c:when>
								<c:otherwise>
									<ycommerce:testId code="orderDetails_productTotalPrice_label"></ycommerce:testId>
								</c:otherwise>
							</c:choose>
					</div>
				</ul>	
				<c:if test="${not empty param.from and 'checkout' eq param.from}">	
					<ul class="variant_item__list--item see_details">
						<c:if test="${not empty leafDimensionData.purchaseOrderNumber}">
					        <span>
						        <ycommerce:testId code="orderDetails_productItemPo_label">
						      		<spring:theme code="basket.page.po"/>:&ensp;${fn:escapeXml(leafDimensionData.purchaseOrderNumber)}
						        </ycommerce:testId> 
					        </span>
				    	</c:if>
				    	<c:if test="${not empty leafDimensionData.dataOrdine}">
					    	<span>
						    	<ycommerce:testId code="orderDetails_productItemOrderDate_label">
						    		<spring:theme code="basket.page.dataOrdine"/>:&ensp;${fn:escapeXml(leafDimensionData.dataOrdine)}
						    	</ycommerce:testId>
					    	</span>
					    </c:if>
					    <c:if test="${not empty leafDimensionData.cgi}">
					    	<span>
						    	<ycommerce:testId code="orderDetails_productItemCgi_label">
						    		<spring:theme code="basket.page.cgi"/>:&ensp;${fn:escapeXml(leafDimensionData.cgi)}
						    	</ycommerce:testId>	
					    	</span>		    
					    </c:if>
					    <c:if test="${not empty leafDimensionData.cup}">	
					    	<span>
						   		<ycommerce:testId code="orderDetails_productItemCup_label">
						    	 	<spring:theme code="basket.page.cup"/>:&ensp;${fn:escapeXml(leafDimensionData.cup)}
						    	</ycommerce:testId>
					    	</span>
					    </c:if>
					</ul>	
				</c:if>
			</li>
		</c:forEach>
	</ul>
</c:forEach>

