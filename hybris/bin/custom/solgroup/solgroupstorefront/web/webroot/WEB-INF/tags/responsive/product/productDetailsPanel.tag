<%@ tag body-content="empty" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/responsive/product"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<div class="product-details page-title">
	<ycommerce:testId code="productDetails_productNamePrice_label_${product.code}">
		<div class="name">${fn:escapeXml(product.name)}<span class="sku">ID</span><span class="code">${fn:escapeXml(product.erpCode)}</span></div>
	</ycommerce:testId>
	<%-- <product:productReviewSummary product="${product}" showLinks="true"/> --%>
</div>
<div class="row">
	<div class="col-xs-10 col-xs-push-1 col-sm-6 col-sm-push-0 col-lg-4">
		<product:productImagePanel galleryImages="${galleryImages}" />
	</div>
	<div class="clearfix hidden-sm hidden-md hidden-lg"></div>
	<div class="col-sm-6 col-lg-8">
		<div class="product-main-info">
			<div class="row">
				<div class="col-lg-6">
					<div class="product-details">
						<product:productPromotionSection product="${product}"/>
						<ycommerce:testId code="productDetails_productNamePrice_label_${product.code}">
							<product:productPricePanel product="${product}" />
						</ycommerce:testId>
						<div class="description">${ycommerce:sanitizeHTML(product.summary)}</div>
					</div>
				</div>

				<div class="col-sm-12 col-md-9 col-lg-6">
					<cms:pageSlot position="VariantSelector" var="component" element="div" class="page-details-variants-select">
						<cms:component component="${component}" element="div" class="yComponentWrapper page-details-variants-select-component"/>
					</cms:pageSlot>
					<!-- CR sulla visualizzazione della disponibilità residua dello stock -->
					<c:if test="${product.stockable}">
						<c:choose>
							<c:when test="${product.stock.stockLevelStatus.code ne 'outOfStock'}">				
								<c:set var="productStockLevel">
									${product.stock.stockLevel}&nbsp;<spring:theme code="product.variants.in.stock"/>
								</c:set>
								<p class="stock-wrapper clearfix stock-status">${productStockLevel}</p> 
							</c:when>
							<c:otherwise>
								<cms:pageSlot position="ProductDetailsAvailabilityInfo" var="component" element="div" class="stock-wrapper clearfix">
	    							<cms:component component="${component}" element="div" class="stock-status"/>
	    						</cms:pageSlot>							
							</c:otherwise>
						</c:choose>
					</c:if>
					<cms:pageSlot position="AddToCart" var="component" element="div" class="page-details-variants-select">
						<cms:component component="${component}" element="div" class="yComponentWrapper page-details-add-to-cart-component"/>
					</cms:pageSlot>
				</div>
			</div>
		</div>

	</div>
</div>