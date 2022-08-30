<%@ tag body-content="empty" trimDirectiveWhitespaces="true"%>
<%@ attribute name="product" required="true" type="de.hybris.platform.commercefacades.product.data.ProductData"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<div class="tab-details">
	<ycommerce:testId code="productDetails_content_label">
		<p>
			${ycommerce:sanitizeHTML(product.description)}
		<p>
		<c:forEach items="${product.categories}" var="category">
        <c:if test="${category.code == 'solgroupIT_containerType_01'}"> <spring:theme code="product.description.disclaimer"></spring:theme></c:if>
    </c:forEach>
	</ycommerce:testId>
</div>