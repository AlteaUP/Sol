<%@ tag body-content="empty" trimDirectiveWhitespaces="true"%>
<%@ attribute name="product" required="true"
	type="de.hybris.platform.commercefacades.product.data.ProductData"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="product-classifications">
	<c:choose>
		<c:when test="${not empty product.medicalAttachments}">
			<c:forEach items="${product.medicalAttachments}" var="medicalAttachments">
				<c:if test="${fn:length(fn:escapeXml(medicalAttachments.title))!=0}">
					<div class="headline">${fn:escapeXml(medicalAttachments.title)}</div>
				</c:if>
				<table class="table">
					<tbody>
						<tr>
							<td class="attrib" style="border-top:0px;">
								<a href="<c:if test="${medicalAttachments.external}">http://</c:if>${fn:escapeXml(medicalAttachments.url)}"
									<c:if test="${medicalAttachments.target}">target="_blank"</c:if> ${medicalAttachments.styleAttributes}>
										<c:choose>
											<c:when
												test="${fn:length(fn:escapeXml(medicalAttachments.linkName))==0}">${fn:escapeXml(medicalAttachments.url)}
											</c:when>
											<c:otherwise>${fn:escapeXml(medicalAttachments.linkName)}</c:otherwise>
										</c:choose>
								</a>
							</td>
							<td style="border-top:0px;">
								${fn:escapeXml(medicalAttachments.description)}
							</td>
						</tr>
					</tbody>
				</table>
			</c:forEach>
		</c:when>
		<c:otherwise>
			<spring:theme code="product.medicalAttachments.empty"
				arguments="${product.code},${product.erpCode}" />
		</c:otherwise>
	</c:choose>
</div>

