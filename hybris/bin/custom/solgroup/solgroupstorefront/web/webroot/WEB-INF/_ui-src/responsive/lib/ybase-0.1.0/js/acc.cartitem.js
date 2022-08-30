ACC.cartitem = {

	_autoload: [
		"bindCartItem"
	],

	submitTriggered: false,

	bindCartItem: function ()
	{

		$('.js-execute-entry-action-button').on("click", function ()
		{
			var entryAction = $(this).data("entryAction");
			var entryActionUrl =  $(this).data("entryActionUrl");
			var entryProductCode =  $(this).data("entryProductCode");
			var entryInitialQuantity =  $(this).data("entryInitialQuantity");
			var entryInitialQuantity =  $(this).data("entryInitialPo");
			var actionEntryNumbers =  $(this).data("actionEntryNumbers");

			if(entryAction == 'REMOVE')
			{
				ACC.track.trackRemoveFromCart(entryProductCode, entryInitialQuantity);
			}

			var cartEntryActionForm = $("#cartEntryActionForm");
			var entryNumbers = actionEntryNumbers.toString().split(';');
			entryNumbers.forEach(function(entryNumber) {
				var entryNumbersInput = $("<input>").attr("type", "hidden").attr("name", "entryNumbers").val(entryNumber);
				cartEntryActionForm.append($(entryNumbersInput));
			});
			cartEntryActionForm.attr('action', entryActionUrl).submit();
		});

		$('.js-update-entry-quantity-input').on("blur", function (e)
		{
			ACC.cartitem.handleUpdateQuantity(this, e);

		}).on("keyup", function (e)
		{
			return ACC.cartitem.handleKeyEvent(this, e);
		}
		).on("keydown", function (e)
		{
			return ACC.cartitem.handleKeyEvent(this, e);
		}
		);
		
		$('.js-update-entry-po-input').on("blur", function (e)
		{
			ACC.cartitem.handleUpdatePo(this, e);
	
		}).on("keyup", function (e)
		{
			return ACC.cartitem.handlePOKeyEvent(this, e);
		}
		).on("keydown", function (e)
		{
			return ACC.cartitem.handlePOKeyEvent(this, e);
		}
		);
		
		$('.js-update-entry-cig-input').on("blur", function (e)
		{
			ACC.cartitem.handleUpdateCig(this, e);
	
		}).on("keyup", function (e)
		{
			return ACC.cartitem.handleCigKeyEvent(this, e);
		}
		).on("keydown", function (e)
		{
			return ACC.cartitem.handleCigKeyEvent(this, e);
		}
		);
		
		$('.js-update-entry-cup-input').on("blur", function (e)
		{
			ACC.cartitem.handleUpdateCup(this, e);
	
		}).on("keyup", function (e)
		{
			return ACC.cartitem.handleCupKeyEvent(this, e);
		}
		).on("keydown", function (e)
		{
			return ACC.cartitem.handleCupKeyEvent(this, e);
		}
		);
		
		$('.js-update-entry-dataOrdine-input').on("change", function (e)
		{
			ACC.cartitem.handleUpdateDataOrdine(this, e);
	
		}).on("keyup", function (e)
		{
			return ACC.cartitem.handleDataOrdineKeyEvent(this, e);
		}
		).on("keydown", function (e)
		{
			return ACC.cartitem.handleDataOrdineKeyEvent(this, e);
		}
		);
	},

	handleKeyEvent: function (elementRef, event)
	{
		//console.log("key event (type|value): " + event.type + "|" + event.which);

		if (event.which == 13 && !ACC.cartitem.submitTriggered)
		{
			ACC.cartitem.submitTriggered = ACC.cartitem.handleUpdateQuantity(elementRef, event);
			return false;
		}
		else 
		{
			// Ignore all key events once submit was triggered
			if (ACC.cartitem.submitTriggered)
			{
				return false;
			}
		}

		return true;
	},
	
	handlePOKeyEvent: function (elementRef, event)
	{
		//console.log("key event (type|value): " + event.type + "|" + event.which);

		if (event.which == 13 && !ACC.cartitem.submitTriggered)
		{
			ACC.cartitem.submitTriggered = ACC.cartitem.handleUpdatePo(elementRef, event);
			return false;
		}
		else 
		{
			// Ignore all key events once submit was triggered
			if (ACC.cartitem.submitTriggered)
			{
				return false;
			}
		}

		return true;
	},
	
	handleCigKeyEvent: function (elementRef, event)
	{
		//console.log("key event (type|value): " + event.type + "|" + event.which);

		if (event.which == 13 && !ACC.cartitem.submitTriggered)
		{
			ACC.cartitem.submitTriggered = ACC.cartitem.handleUpdateCig(elementRef, event);
			return false;
		}
		else 
		{
			// Ignore all key events once submit was triggered
			if (ACC.cartitem.submitTriggered)
			{
				return false;
			}
		}

		return true;
	},
	
	handleCupKeyEvent: function (elementRef, event)
	{
		//console.log("key event (type|value): " + event.type + "|" + event.which);

		if (event.which == 13 && !ACC.cartitem.submitTriggered)
		{
			ACC.cartitem.submitTriggered = ACC.cartitem.handleUpdateCup(elementRef, event);
			return false;
		}
		else 
		{
			// Ignore all key events once submit was triggered
			if (ACC.cartitem.submitTriggered)
			{
				return false;
			}
		}

		return true;
	},
	
	handleDataOrdineKeyEvent: function (elementRef, event)
	{
		//console.log("key event (type|value): " + event.type + "|" + event.which);
		if (event.which == 13 && !ACC.cartitem.submitTriggered)
		{
			
			ACC.cartitem.submitTriggered = ACC.cartitem.handleUpdateDataOrdine(elementRef, event);
			return false;
		}
		else 
		{
			// Ignore all key events once submit was triggered
			if (ACC.cartitem.submitTriggered)
			{
				return false;
			}
		}

		return true;
	},

	handleUpdateQuantity: function (elementRef, event)
	{

		var form = $(elementRef).closest('form');

		var productCode = form.find('input[name=productCode]').val();
		var initialCartQuantity = form.find('input[name=initialQuantity]').val();
		var newCartQuantity = form.find('input[name=quantity]').val();

		if(initialCartQuantity != newCartQuantity)
		{
			ACC.track.trackUpdateCart(productCode, initialCartQuantity, newCartQuantity);
			form.submit();

			return true;
		}

		return false;
	},
	
	handleUpdatePo: function (elementRef, event)
	{

		console.log("handle updatePo for cartEntry without variants");

		var product = $(elementRef).data("product");
		var purchaseOrderNumber = $(elementRef).val();
		var url = $(elementRef).data("url") + "?purchaseOrderNumber=" + purchaseOrderNumber + "&product=" + product;
		console.log("HTTP GET to url " + url);

		// Remove previous global alerts
        $('.global-alerts').remove();
        $('div').removeClass('has-error');
        $('td').removeClass('has-error');

        // send ajax
		$.ajax({
                url: url, // url where to submit the request
                type : "GET", // type of action POST || GET
                data : "",
                success : function(result) {
                	console.log("HTTP GET completed with success");
                	console.log("Result.error = " + result.error);
                	if(result.error) {
                		$(elementRef).parent().addClass("has-error")
                		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-danger alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.errorDescription + '</div></div>');
                	}
                	else {
                		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-info alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.successDescription + '</div></div>');
                	}
                },
                error: function(xhr, resp, text) {
                	console.log("HTTP POST completed with error");
                	console.log(xhr, resp, text);
                }
            })

		return true;
		
	},
	
	handleUpdateCig: function (elementRef, event)
	{

		console.log("handle updateCig for cartEntry without variants");

		var product = $(elementRef).data("product");
		var cig = $(elementRef).val();
		var url = $(elementRef).data("url") + "?cig=" + cig + "&product=" + product;
		console.log("HTTP GET to url " + url);

		// Remove previous global alerts
        $('.global-alerts').remove();
        $('div').removeClass('has-error');
        $('td').removeClass('has-error');

		
        // send ajax
		$.ajax({
                url: url, // url where to submit the request
                type : "GET", // type of action POST || GET
                data : "",
                success : function(result) {
                	console.log("HTTP GET completed with success");
                	console.log("Result.error = " + result.error);
                	if(result.error) {
                		$(elementRef).parent().addClass("has-error")
                		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-danger alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.errorDescription + '</div></div>');
                	}
                	else {
                		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-info alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.successDescription + '</div></div>');
                	}
                },
                error: function(xhr, resp, text) {
                	console.log("HTTP POST completed with error");
                	console.log(xhr, resp, text);
                }
            })

		return true;
		
	},
	
	handleUpdateCup: function (elementRef, event)
	{

		console.log("handle updateCup for cartEntry without variants");

		var product = $(elementRef).data("product");
		var cup = $(elementRef).val();
		var url = $(elementRef).data("url") + "?cup=" + cup + "&product=" + product;
		console.log("HTTP GET to url " + url);

		// Remove previous global alerts
        $('.global-alerts').remove();
        $('div').removeClass('has-error');
        $('td').removeClass('has-error');

		
        // send ajax
		$.ajax({
                url: url, // url where to submit the request
                type : "GET", // type of action POST || GET
                data : "",
                success : function(result) {
                	console.log("HTTP GET completed with success");
                	console.log("Result.error = " + result.error);
                	if(result.error) {
                		$(elementRef).parent().addClass("has-error")
                		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-danger alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.errorDescription + '</div></div>');
                	}
                	else {
                		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-info alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.successDescription + '</div></div>');
                	}
                },
                error: function(xhr, resp, text) {
                	console.log("HTTP POST completed with error");
                	console.log(xhr, resp, text);
                }
            })

		return true;
		
	},
	
	handleUpdateDataOrdine: function (elementRef, event)
	{

		console.log("handle updateDataOrdine for cartEntry without variants");

		var product = $(elementRef).data("product");
		var dataOrdine = $(elementRef).val();
		var url = $(elementRef).data("url") + "?orderDate=" + dataOrdine + "&product=" + product;
		console.log("HTTP GET to url " + url);

		// Remove previous global alerts
        $('.global-alerts').remove();
        $('div').removeClass('has-error');
        $('td').removeClass('has-error');

		
        // send ajax
		$.ajax({
                url: url, // url where to submit the request
                type : "GET", // type of action POST || GET
                data : "",
                success : function(result) {
                	console.log("HTTP GET completed with success");
                	console.log("Result.error = " + result.error);
                	if(result.error) {
                		$(elementRef).parent().addClass("has-error")
                		$(elementRef).css("background-color","");
                		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-danger alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.errorDescription + '</div></div>');
                	}
                	else {
                		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-info alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.successDescription + '</div></div>');
                	}
                },
                error: function(xhr, resp, text) {
                	console.log("HTTP POST completed with error");
                	console.log(xhr, resp, text);
                }
            })

		return true;
		
	}
	
	
};

function saveAllProperties(quoteId){
	console.log("saveAllProperties");
	var purchaseOrderNumber = "";
	if($("#allPurchaseOrderNumber").is(":visible")) {
		purchaseOrderNumber = $("#allPurchaseOrderNumber").val();
	}
	else {
		purchaseOrderNumber = $("#allPurchaseOrderNumber_mobile").val();
	}
	
	var dataOrdine = "";
	if($("#allDataOrdine").is(":visible")) {
		dataOrdine = $("#allDataOrdine").val();
	}
	else {
		dataOrdine = $("#allDataOrdine_mobile").val();
	}

	var cig = "";
	if($("#allCig").is(":visible")) {		
			cig = $("#allCig").val();			
	}
	else {
			cig = $("#allCig_mobile").val();			
	}
	
	var cup = "";
	if($("#allCup").is(":visible")) {		
		cup = $("#allCup").val();		
	}
	else {		
		cup = $("#allCup_mobile").val();		
	}
	
	var urlFunction = $("#updateAllProperties").val();
	var urlParams = "?purchaseOrderNumber=" + purchaseOrderNumber + "&dataOrdine=" + dataOrdine+ "&cig=" + cig+ "&cup=" + cup;
	if(quoteId) {
		urlParams = urlParams + "&quoteId=" + quoteId;
	}
	var url = urlFunction + urlParams;
	console.log("Send HTTP GET to : " + url);
	
	$.ajax({
        url: url, // url where to submit the request
        type : "GET", // type of action POST || GET
        data : "", // post data || get data
        success : function(result) {
            console.log("HTTP GET completed with success");
            
            // Remove previous global alerts
            $('.global-alerts').remove();
            $('div').removeClass('has-error');
            
            if(result.error === true) {
            	for(i=0; i<result.errorDescriptions.length; i++) {
            		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-danger alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.errorDescriptions[i] + '</div></div>');
            	}
            	if(result.po_error === true) {
            		if($("#allPurchaseOrderNumber").is(":visible")) {
            			$("#allPurchaseOrderNumber").parent().addClass("has-error");
            		}
            		// TODO : gestire la aprte per il mobile
            	}

            	if(result.cig_error === true) {
            		if($("#allCig").is(":visible")) {
            			$("#allCig").parent().addClass("has-error");
            		}
            		// TODO : gestire la aprte per il mobile
            	}

            	if(result.cup_error === true) {
            		if($("#allCup").is(":visible")) {
            			$("#allCup").parent().addClass("has-error");
            		}
            		// TODO : gestire la aprte per il mobile
            	}

            	if(result.dataOrdine_error === true) {
            		if($("#allDataOrdine").is(":visible")) {
            			$("#allDataOrdine").parent().addClass("has-error");
            			$("#allDataOrdine").css("background-color","");
            		}
            		// TODO : gestire la aprte per il mobile
            	}

            
            }
            
            else {

        	
	        	// Update input fields for order entries without variants
	        	var inputs_quantity = $("input[name=quantity]");
	        	var inputs_purchaseOrderNumber = $("input[name^=purchaseOrderNumber]");
	            var inputs_cig = $("input[name^=cig]");
	            var inputs_cup = $("input[name^=cup]");
	            var inputs_orderData = $("input[name^=dataOrdine]");
	            
	            for(i=0; i<inputs_quantity.length; i++) {
	            	var qty = inputs_quantity[i].value;
	            	if(qty > 0) {
	            		if(inputs_purchaseOrderNumber[i]){
	            			inputs_purchaseOrderNumber[i].value=result.purchaseOrderNumber;
		            	}
		            	if(inputs_cig[i]) {
		            		inputs_cig[i].value=result.cig;
		            	}
		            	if(inputs_cup[i]) {
		            		inputs_cup[i].value=result.cup;
		            	}
		            	if(inputs_orderData[i]) {
		            		inputs_orderData[i].value=result.dataOrdine;
		            	}
	            	}
	            }
	
	        	
	            // Update input fields for open order entries with variants
	        	var inputs_quantity_v = $('input[name^=cartEntries][name$=quantity]');
	        	var inputs_purchaseOrderNumber_v = $('input[name^=cartEntries][name$=purchaseOrderNumber]');
	            var inputs_cig_v = $('input[name^=cartEntries][name$=cig]');
	            var inputs_cup_v = $('input[name^=cartEntries][name$=cup]');
	            var inputs_orderData_v = $('input[name^=cartEntries][name$=dataOrdine]');
	            
	            for(i=0; i<inputs_quantity_v.length; i++) {
	            	var qty = inputs_quantity_v[i].value;
	            	if(qty > 0) {
	            		if(inputs_purchaseOrderNumber_v[i]){
	            			inputs_purchaseOrderNumber_v[i].value=result.purchaseOrderNumber;
		            	}
		            	if(inputs_cig_v[i]) {
		            		inputs_cig_v[i].value=result.cig;
		            	}
		            	if(inputs_cup_v[i]) {
		            		inputs_cup_v[i].value=result.cup;
		            	}
		            	if(inputs_orderData_v[i]) {
		            		inputs_orderData_v[i].value=result.dataOrdine;
		            	}
	            	}
	            }
	            
	            
	            
	            // Update data attributes for productVariantMatrix
	            var grids = $("div[id^=grid]");
	            for(j=0; j<grids.length; j++) {
	            	var data = $(grids[j]).data('sub-entries');
	            	var newTokens = '';
	            	var tokens = data.split(',');
	            	for(t=0; t<tokens.length; t++) {
	            		var subTokens = tokens[t].split(':');
	            		var newToken = subTokens[0] + ":" + subTokens[1] + ":";
	            		if(result.purchaseOrderNumber) {
	            			newToken = newToken + result.purchaseOrderNumber;
	            		}
	            		newToken = newToken + ":"
	            		if(result.cig) {
	            			newToken = newToken + result.cig;
	            		}
	            		newToken = newToken + ":"
	            		if(result.cup) {
	            			newToken = newToken + result.cup;
	            		}
	            		newToken = newToken + ":"
	            		if(result.dataOrdine) {
	            			newToken = newToken + result.dataOrdine;
	            		}
	            		newToken = newToken + ","
	            		newTokens = newTokens + newToken;
	            	}
	            	$(grids[j]).data('sub-entries',newTokens);
	            }
	            
            
	            $("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-info alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.successDescription + '</div></div>');
            }
            
            
        },
        error: function(xhr, resp, text) {
            console.log(xhr, resp, text);
        }
    });
}

