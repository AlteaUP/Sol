<%@ tag body-content="empty" trimDirectiveWhitespaces="true"%>
<%@ attribute name="product" required="true"
	type="de.hybris.platform.commercefacades.product.data.ProductData"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="product-classifications">
	<c:choose>
		<c:when test="${not empty product.techAttachments}">
			<c:forEach items="${product.techAttachments}" var="techAttachment">
				<c:if test="${fn:length(fn:escapeXml(techAttachment.title))!=0}">
					<div class="headline">${fn:escapeXml(techAttachment.title)}</div>
				</c:if>
				<table class="table">
					<tbody>
						<tr>
							<td class="attrib" style="border-top:0px;">
								<a href="<c:if test="${techAttachment.external}">http://</c:if>${fn:escapeXml(techAttachment.url)}"
									<c:if test="${techAttachment.target}">target="_blank"</c:if> ${techAttachment.styleAttributes}>
										<c:choose>
											<c:when
												test="${fn:length(fn:escapeXml(techAttachment.linkName))==0}">${fn:escapeXml(techAttachment.url)}
											</c:when>
											<c:otherwise>${fn:escapeXml(techAttachment.linkName)}</c:otherwise>
										</c:choose>
								</a>
							</td>
							<td style="border-top:0px;">
								${fn:escapeXml(techAttachment.description)} : ${product.baseProductName}
							</td>
						</tr>
					</tbody>
				</table>
			</c:forEach>
		</c:when>
		<c:otherwise>
			<spring:theme code="product.techAttachment.empty"
				arguments="${product.code},${product.erpCode}" />
		</c:otherwise>
	</c:choose>
</div>