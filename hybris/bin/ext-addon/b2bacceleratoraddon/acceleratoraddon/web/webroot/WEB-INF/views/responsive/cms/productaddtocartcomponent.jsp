<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="action" tagdir="/WEB-INF/tags/responsive/action"%>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/responsive/product"%>
<%@ taglib prefix="b2b-product"
	tagdir="/WEB-INF/tags/addons/b2bacceleratoraddon/responsive/product"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>

<c:choose>
	<c:when
		test="${product.stock.stockLevelStatus.code eq 'inStock' and empty product.stock.stockLevel}">
		<c:set var="maxQty" value="FORCE_IN_STOCK" />
	</c:when>
	<c:otherwise>
		<c:set var="maxQty" value="${product.stock.stockLevel}" />
	</c:otherwise>
</c:choose>

<c:set var="qtyMinus" value="1" />
<c:set var="showDisclaimer" value="false" />

<div class="addtocart-component">
	<c:if test="${product.showQuantitySelector}">
		<c:if test="${empty showAddToCart ? true : showAddToCart}">
			<c:if
				test="${product.purchasable and product.stock.stockLevelStatus.code ne 'outOfStock' }">
				<c:forEach items="${product.categories}" var="category">
					<c:if test="${category.code == 'solgroupIT_containerType_01'}">
						<c:set var="showDisclaimer" value="true" />
					</c:if>
				</c:forEach>
				<c:if test="${showDisclaimer eq 'true'}"> <spring:theme code="product.description.shortDisclaimer" var="shortDisclaimer"/></c:if>
				<div class="qty-selector input-group js-qty-selector" title="${shortDisclaimer}">					
					<span class="input-group-btn">
						<button class="btn btn-default js-qty-selector-minus"
							type="button"
							<c:if test="${qtyMinus <= 1}"><c:out value="disabled='disabled'"/></c:if>>
							<span class="glyphicon glyphicon-minus" aria-hidden="true"></span>
						</button>
					</span> <input type="text" maxlength="3"
						class="form-control js-qty-selector-input" size="1"
						value="${qtyMinus}" data-max="${maxQty}" data-min="1"
						name="pdpAddtoCartInput" id="pdpAddtoCartInput" /> <span
						class="input-group-btn">
						<button class="btn btn-default js-qty-selector-plus" type="button">
							<span class="glyphicon glyphicon-plus" aria-hidden="true"></span>
						</button>
					</span>
				</div>
			</c:if>
		</c:if>
	</c:if>
	<div class="stock-wrapper clearfix">
		<%-- <ycommerce:testId code="productDetails_productInStock_label">
            <b2b-product:productStockThreshold product="${product}"/>
        </ycommerce:testId>
        <product:productFutureAvailability product="${product}" futureStockEnabled="${futureStockEnabled}" /> --%>
	</div>

	<div class="actions">
		<c:if test="${multiDimensionalProduct}">
			<c:if
				test="${product.purchasable and product.stock.stockLevelStatus.code ne 'outOfStock' }">
				<c:url value="${product.url}/orderForm" var="productOrderFormUrl" />
				<a href="${productOrderFormUrl}"
					class="btn btn-default btn-block btn-icon js-add-to-cart glyphicon-list-alt">
					<spring:theme code="order.form" />
				</a>
			</c:if>
		</c:if>
	</div>

	<c:if test="${empty product.price or !product.customPrice}">
		<div class="actions">
			<a
				href="/my-account/add-support-ticket?c=${product.erpCode}&n=${product.name}&i=${product.code}"
				class="btn btn-default btn-block btn-icon js-add-to-cart glyphicon-euro"
				id="add-support-ticket-btn"> <spring:theme
					code="text.account.productquote.quoteRequest" text="Quote Request" />
			</a>
	</c:if>
	<action:actions element="div" parentComponent="${component}" />
	<c:if test="${empty product.price or !product.customPrice}">
</div>
</c:if>
</div>