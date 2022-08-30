<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:htmlEscape defaultHtmlEscape="true" />

<spring:url value="/cart/exportTemplate" var="exportTemplateUrl" htmlEscape="false"/>
<div class="col-xs-12  col-sm-4 col-md-4 col-lg-2 pull-left">
	<a href="${exportTemplateUrl}" class="exportTemplate__cart--link">
		<spring:theme code="saved.cart.export.template.csv.file" />
	</a>
</div>