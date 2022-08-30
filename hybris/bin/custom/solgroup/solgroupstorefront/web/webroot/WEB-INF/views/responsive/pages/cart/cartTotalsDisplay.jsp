<%@ taglib prefix="cart" tagdir="/WEB-INF/tags/responsive/cart" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Verified that there's a pre-existing bug regarding the setting of showTax; created issue  --%>
<div class="cart-header border">
	<div class="row"> 
		<div class="col-xs-12 col-md-5 col-lg-6">
			<%--
	        <div class="cart-voucher">
	            <cart:cartVoucher cartData="${cartData}"/>
	        </div>
	        --%>
	    </div>
	     
	    <c:if test="${!cartData.refill}">
		    <div class="col-xs-12 col-md-7 col-lg-6">
		        <div class="cart-totals">
		            <cart:cartTotals cartData="${cartData}" showTax="false"/>
		            <cart:ajaxCartTotals/>
		        </div>
		    </div>
		</c:if>
	</div>
</div>