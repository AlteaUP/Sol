<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="address" required="true" type="de.hybris.platform.commercefacades.user.data.AddressData" %>
<%@ attribute name="storeAddress" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- <c:if test="${not storeAddress}">
	<div>
		<c:if test="${not empty address.title}">
		    ${fn:escapeXml(address.title)}&nbsp;
		</c:if>
		${fn:escapeXml(address.firstName)}&nbsp;${fn:escapeXml(address.lastName)}
	</div>
</c:if> --%>

<c:if test="${not empty address.lastName}">
	<div>
		<b>
			${fn:escapeXml(address.title)}&nbsp;${fn:escapeXml(address.firstName)}&nbsp;${fn:escapeXml(address.lastName)}
		</b>
	</div>
</c:if>
<c:if test="${not empty address.line1}">
	<strong>
		<spring:theme code="checkout.multi.deliveryAddress.street"></spring:theme>
	</strong>
	${fn:escapeXml(address.line1)}
</c:if>
<c:if test="${not empty address.line2}">
	,&nbsp;${fn:escapeXml(address.line2)}
</c:if>
<c:if test="${not empty address.town}">
	<div>
		<strong>
			<spring:theme code="checkout.multi.deliveryAddress.town"></spring:theme>
		</strong>	
		${fn:escapeXml(address.town)}
	</div>
</c:if>
<c:if test="${not empty address.region.name}">
	<div>
		<strong>
			<spring:theme code="checkout.multi.deliveryAddress.region"></spring:theme>
		</strong>
    	${fn:escapeXml(address.region.name)}
    </div>
</c:if>
<c:if test="${not empty address.postalCode}">
	<div>
		<strong>
			<spring:theme code="checkout.multi.deliveryAddress.postcode"></spring:theme>
		</strong>
    	${fn:escapeXml(address.postalCode)}
    </div>
</c:if>
<c:if test="${not empty address.country.name}">
	<div>
		<strong>
			<spring:theme code="checkout.multi.deliveryAddress.country"></spring:theme>
		</strong>
    	${fn:escapeXml(address.country.name)}
    </div>
</c:if>
<c:if test="${not empty address.email}">
	<div>
		<strong>
			<spring:theme code="checkout.multi.deliveryAddress.email"></spring:theme>
		</strong>
    	${fn:escapeXml(address.email)}
    </div>
</c:if>
<c:if test="${not empty address.phone}">
	<div>
		<strong>
			<spring:theme code="checkout.multi.deliveryAddress.phone"></spring:theme>
		</strong>
    	${fn:escapeXml(address.phone)}
    </div>
</c:if>