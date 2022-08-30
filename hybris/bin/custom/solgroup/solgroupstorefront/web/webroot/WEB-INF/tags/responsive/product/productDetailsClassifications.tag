<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="product" required="true" type="de.hybris.platform.commercefacades.product.data.ProductData" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>

<div class="product-classifications">
	<c:if test="${not empty product.classifications}">
		<c:forEach items="${product.classifications}" var="classification">
			<div class="headline">${fn:escapeXml(classification.name)}</div>
				<table class="table">
					<tbody>
						<c:forEach items="${classification.features}" var="feature">
							<tr>
								<td class="attrib">${fn:escapeXml(feature.name)}</td>
								<td>
									<c:forEach items="${feature.featureValues}" var="value" varStatus="status">
										
										<c:choose>
											<c:when test="${empty feature.featureUnit.symbol}">
												${value.value}
											</c:when>
											<c:otherwise>
												${fn:escapeXml(value.value)}
											</c:otherwise>
										</c:choose>
										
										<c:choose>
											<c:when test="${feature.range}">
												${not status.last ? '-' : &nbsp;fn:escapeXml(feature.featureUnit.symbol)}
											</c:when>
											<c:otherwise>
												&nbsp;${fn:escapeXml(feature.featureUnit.symbol)}
												${not status.last ? '<br/>' : ''}
											</c:otherwise>
										</c:choose>
									</c:forEach>
								</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
		</c:forEach>
	</c:if>
</div>