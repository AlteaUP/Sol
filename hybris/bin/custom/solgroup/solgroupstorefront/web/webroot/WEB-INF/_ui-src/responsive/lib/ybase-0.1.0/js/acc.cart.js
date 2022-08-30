ACC.cart = {

    _autoload: [
        "bindHelp",
        "cartRestoration",
        "bindCartPage",
        "bindMultiDEntryRemoval",
        "bindMultidCartProduct",
        ["bindApplyVoucher", $("#js-voucher-apply-btn").length != 0],
        ["bindToReleaseVoucher", $("#js-applied-vouchers").length != 0],
        "bindAllDataOrdine",
        "bindDataOrdine",
        "bindOrderHistoryData"
    ],

    bindHelp: function () {
        $(document).on("click", ".js-cart-help", function (e) {
            e.preventDefault();
            var title = $(this).data("help");
            ACC.colorbox.open(title, {
                html: $(".js-help-popup-content").html(),
                width: "300px"
            });
        })
    },

    cartRestoration: function () {
        $('.cartRestoration').click(function () {
            var sCartUrl = $(this).data("cartUrl");
            window.location = sCartUrl;
        });
    },

    bindCartPage: function () {
        // link to display the multi-d grid in read-only mode
        $(document).on("click", '.js-show-editable-grid', function (event) {
        	ACC.cart.populateAndShowEditableGrid(this, event);
        });
    },

    bindMultiDEntryRemoval: function () {
        $(document).on("click", '.js-submit-remove-product-multi-d', function () {
            var itemIndex = $(this).data("index");
            var $form = $('#updateCartForm' + itemIndex);
            var initialCartQuantity = $form.find('input[name=initialQuantity]');
            var cartQuantity = $form.find('input[name=quantity]');
            var entryNumber = $form.find('input[name=entryNumber]').val();
            var productCode = $form.find('input[name=productCode]').val();

            cartQuantity.val(0);
            initialCartQuantity.val(0);

            ACC.track.trackRemoveFromCart(productCode, initialCartQuantity, cartQuantity.val());

            var method = $form.attr("method") ? $form.attr("method").toUpperCase() : "GET";
            $.ajax({
                url: $form.attr("action"),
                data: $form.serialize(),
                type: method,
                success: function (data) {
                    location.reload();
                },
                error: function () {
                    alert("Failed to remove quantity. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
                }

            });

        });
    },


    populateAndShowEditableGrid: function (element, event) {
    	
    	var htmls = document.getElementsByTagName('html');
    	htmls[0].style.height = "auto";
    	console.log("Set html height to auto");

    	
        var readOnly = $(element).data("readOnlyMultidGrid");
        var itemIndex = $(element).data("index");
        grid = $("#ajaxGrid" + itemIndex);

        var gridEntries = $('#grid' + itemIndex);
        var strSubEntries = gridEntries.data("sub-entries");
        var arrSubEntries = strSubEntries.split(',');
        var firstVariantCode = arrSubEntries[0].split(':')[0];
        var baseProduct = gridEntries.data("base-product");

        $(element).toggleClass('open');

        var targetUrl = gridEntries.data("target-url");

        var mapCodeQuantity = new Object();
        var mapCodePurchaseOrderNumber = new Object();
        var mapCodeCig = new Object();
        var mapCodeCup = new Object();
        var mapCodeDataOrdine = new Object();
        var mapCodeErp = new Object();
        for (var i = 0; i < arrSubEntries.length; i++) {
            var arrValue = arrSubEntries[i].split(":");
            mapCodeQuantity[arrValue[0]] = arrValue[1];
            mapCodePurchaseOrderNumber[arrValue[0]] = arrValue[2];
            mapCodeCig[arrValue[0]] = arrValue[3];
            mapCodeCup[arrValue[0]] = arrValue[4];
            mapCodeDataOrdine[arrValue[0]] = arrValue[5];
            mapCodeErp[arrValue[0]] = arrValue[6];
        }

        if (grid.children('#cartOrderGridForm').length > 0) {
            grid.slideToggle("slow");
        }
        else {
            var method = "GET";
            $.ajax({
                url: targetUrl,
                data: {productCode: baseProduct, readOnly: readOnly},
                type: method,
                success: function (data) {
                    grid.html(data);
                    $("#ajaxGrid").removeAttr('id');
                    var $gridContainer = grid.find(".product-grid-container");
                    var numGrids = $gridContainer.length;
                    for (var i = 0; i < numGrids; i++) {
                        ACC.cart.getProductQuantity($gridContainer.eq(i), mapCodeQuantity, mapCodePurchaseOrderNumber, mapCodeCig, mapCodeCup, mapCodeDataOrdine, mapCodeErp, i);
                    }
                    grid.slideDown("slow");
                    ACC.cart.coreCartGridTableActions(element, mapCodeQuantity);
                    ACC.cart.coreCartPOGridTableActions(element, mapCodePurchaseOrderNumber);
                    ACC.cart.coreCartCigGridTableActions(element, mapCodeCig);
                    ACC.cart.coreCartCupGridTableActions(element, mapCodeCup);
                    ACC.cart.coreCartDataOrdineGridTableActions(element, mapCodeDataOrdine);
                    ACC.productorderform.coreTableScrollActions(grid.children('#cartOrderGridForm'));

                    if($(window).width() >=1024) {
	            		//var minDate = new Date();
	            		
	                    var dataOridneWrapperElements = grid.find("span[id^=js-cartEntries][id$=dataOrdine]");
	                    var dateFormatForDatePicker = $(dataOridneWrapperElements[0]).data("date-format-for-date-picker");
	                    
	                    var inputs = grid.find("input[id^=cartEntries][id$=dataOrdine]");
	                    
	                    for(var i=0; i<inputs.length; i++) {
	                		console.log("define date picker");
	                    	$(inputs[i]).datepicker({
	                			dateFormat: dateFormatForDatePicker,
	                			constrainInput: true
	                		});
	                		$(inputs[i]).attr('readonly','readonly');
	                		
	                		$(inputs[i]).siblings(".js-open-datepicker-dataOrdine").click(function(){$(this).siblings("input").datepicker('show');});
	                		
	                    
	                    }
                    }
                    
                },
                error: function (xht, textStatus, ex) {
                    alert("Failed to get variant matrix. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
                }

            });
        }
    },
    
    coreCartDataOrdineGridTableActions: function (element, mapCodeDataOrdine) {
        ACC.productorderform.bindVariantSelect($(".variant-select-btn"), 'cartOrderGridForm');
        var itemIndex = $(element).data("index");
        var skuDataOrdineClass = '.sku-dataOrdine';

        var dataOrdineBefore = "";
        var grid = $('#ajaxGrid' + itemIndex + " .product-grid-container");


        grid.on('change', skuDataOrdineClass, function (event) {
            var code = event.keyCode || event.which || event.charCode;

            if (code != 13 && code != undefined) {
                return;
            }

            var dataOrdine = this.value
            var productCode = $(this).data("product");

            console.log("Data ordine = " + dataOrdine);
            console.log("Product code = " + productCode);

            var _this = this;

    		// Remove previous global alerts
            $('.global-alerts').remove();
            $('div').removeClass('has-error');
            $('td').removeClass('has-error');


                $.ajax({
                url: ACC.config.encodedContextPath + '/cart/updateOrderDate?orderDate=' + dataOrdine + '&product=' + productCode ,
                //data: {productCode: variantCode, purchaseOrderNumber: purchaseOrderNumberAfter, entryNumber: -1},
                type: "GET",
                success: function (result) {
                	console.log("HTTP GET completed with success");
                	console.log("Result.error = " + result.error);
                	if(result.error) {
                		console.log($(_this));
                		$(_this).parent().addClass('has-error');
                		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-danger alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.errorDescription + '</div></div>');
	                	}
	                	else {
                		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-info alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.successDescription + '</div></div>');
	                	}
                    },
                    error: function (xhr, textStatus, error) {
                        var redirectUrl = xhr.getResponseHeader("redirectUrl");
                        var connection = xhr.getResponseHeader("Connection");
                        // check if error leads to a redirect
                        if (redirectUrl !== null) {
                            window.location = redirectUrl;
                            // check if error is caused by a closed connection
                        } else if (connection === "close") {
                            window.location.reload();
                        }
                    }
                });
        });
    },
    
    
    
    coreCartPOGridTableActions: function (element, mapCodePurchaseOrderNumber) {
        ACC.productorderform.bindVariantSelect($(".variant-select-btn"), 'cartOrderGridForm');
        var itemIndex = $(element).data("index");
        var skuPurchaseOrderNumberClass = '.sku-purchaseOrderNumber';

        var grid = $('#ajaxGrid' + itemIndex + " .product-grid-container");


        grid.on('focusout keypress', skuPurchaseOrderNumberClass, function (event) {
            var code = event.keyCode || event.which || event.charCode;

            if (code != 13 && code != undefined) {
                return;
            }

            var purchaseOrderNumber = this.value
            var productCode = $(this).data("product");

            console.log("Purchase order number = " + purchaseOrderNumber);
            console.log("Product code = " + productCode);
            
            var _this = this;

    		// Remove previous global alerts
            $('.global-alerts').remove();
            $('div').removeClass('has-error');
            $('td').removeClass('has-error');


                $.ajax({
                url: ACC.config.encodedContextPath + '/cart/updatePo?purchaseOrderNumber=' + purchaseOrderNumber + '&product=' + productCode ,
                //data: {productCode: variantCode, purchaseOrderNumber: purchaseOrderNumberAfter, entryNumber: -1},
                type: "GET",
                success: function (result) {
                	console.log("HTTP GET completed with success");
                	console.log("Result.error = " + result.error);
                	if(result.error) {
                		console.log($(_this));
                		$(_this).parent().addClass('has-error');
                		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-danger alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.errorDescription + '</div></div>');
	                	}
	                	else {
                		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-info alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.successDescription + '</div></div>');
	                	}
                    },
                    error: function (xhr, textStatus, error) {
                        var redirectUrl = xhr.getResponseHeader("redirectUrl");
                        var connection = xhr.getResponseHeader("Connection");
                        // check if error leads to a redirect
                        if (redirectUrl !== null) {
                            window.location = redirectUrl;
                            // check if error is caused by a closed connection
                        } else if (connection === "close") {
                            window.location.reload();
                        }
                    }
                });
        });
    },
    
    
    coreCartCigGridTableActions: function (element, mapCodePurchaseOrderNumber) {
        ACC.productorderform.bindVariantSelect($(".variant-select-btn"), 'cartOrderGridForm');
        var itemIndex = $(element).data("index");
        var skuCigClass = '.sku-cig';
        
        var grid = $('#ajaxGrid' + itemIndex + " .product-grid-container");


        grid.on('focusout keypress', skuCigClass, function (event) {
            var code = event.keyCode || event.which || event.charCode;

            if (code != 13 && code != undefined) {
                return;
            }

            var cig = this.value
            var productCode = $(this).data("product");

            console.log("Cigr = " + cig);
            console.log("Product code = " + productCode);

            var _this = this;

    		// Remove previous global alerts
            $('.global-alerts').remove();
            $('div').removeClass('has-error');
            $('td').removeClass('has-error');


                $.ajax({
                url: ACC.config.encodedContextPath + '/cart/updateCig?cig=' + cig + '&product=' + productCode ,
                type: "GET",
                success: function (result) {
                	console.log("HTTP GET completed with success");
                	console.log("Result.error = " + result.error);
                	if(result.error) {
                		$(_this).parent().addClass('has-error');
                		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-danger alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.errorDescription + '</div></div>');
	                	}
	                	else {
                		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-info alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.successDescription + '</div></div>');
	                	}
                    },
                    error: function (xhr, textStatus, error) {
                        var redirectUrl = xhr.getResponseHeader("redirectUrl");
                        var connection = xhr.getResponseHeader("Connection");
                        // check if error leads to a redirect
                        if (redirectUrl !== null) {
                            window.location = redirectUrl;
                            // check if error is caused by a closed connection
                        } else if (connection === "close") {
                            window.location.reload();
                        }
                    }
                });
        });
    },
    
    coreCartCupGridTableActions: function (element, mapCodePurchaseOrderNumber) {
        ACC.productorderform.bindVariantSelect($(".variant-select-btn"), 'cartOrderGridForm');
        var itemIndex = $(element).data("index");
        var skuCupClass = '.sku-cup';

        var grid = $('#ajaxGrid' + itemIndex + " .product-grid-container");


        grid.on('focusout keypress', skuCupClass, function (event) {
            var code = event.keyCode || event.which || event.charCode;

            if (code != 13 && code != undefined) {
                return;
            }

            var cup = this.value
            var productCode = $(this).data("product");

            console.log("Cup = " + cup);
            console.log("Product code = " + productCode);

            var _this = this;

    		// Remove previous global alerts
            $('.global-alerts').remove();
            $('div').removeClass('has-error');
            $('td').removeClass('has-error');


                $.ajax({
                url: ACC.config.encodedContextPath + '/cart/updateCup?cup=' + cup + '&product=' + productCode ,
                type: "GET",
                success: function (result) {
                	console.log("HTTP GET completed with success");
                	console.log("Result.error = " + result.error);
                	if(result.error) {
                		$(_this).parent().addClass('has-error');
                		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-danger alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.errorDescription + '</div></div>');
	                	}
	                	else {
                		$("#pageContentContainer").prepend('<div class="global-alerts"><div class="alert alert-info alert-dismissable"><button class="close" aria-hidden="true" data-dismiss="alert" type="button">×</button>' + result.successDescription + '</div></div>');
	                	}
                    },
                    error: function (xhr, textStatus, error) {
                        var redirectUrl = xhr.getResponseHeader("redirectUrl");
                        var connection = xhr.getResponseHeader("Connection");
                        // check if error leads to a redirect
                        if (redirectUrl !== null) {
                            window.location = redirectUrl;
                            // check if error is caused by a closed connection
                        } else if (connection === "close") {
                            window.location.reload();
                        }
                    }
                });
        });
    },


    coreCartGridTableActions: function (element, mapCodeQuantity) {
        ACC.productorderform.bindUpdateFutureStockButton(".update_future_stock_button");
        ACC.productorderform.bindVariantSelect($(".variant-select-btn"), 'cartOrderGridForm');
        var itemIndex = $(element).data("index");
        var skuQuantityClass = '.sku-quantity';

        var quantityBefore = 0;
        var grid = $('#ajaxGrid' + itemIndex + " .product-grid-container");

        grid.on('focusin', skuQuantityClass, function (event) {
            quantityBefore = jQuery.trim(this.value);

            $(this).parents('tr').next('.variant-summary').remove();
            if ($(this).parents('table').data(ACC.productorderform.selectedVariantData)) {
                ACC.productorderform.selectedVariants = $(this).parents('table').data(ACC.productorderform.selectedVariantData);
            } else {
                ACC.productorderform.selectedVariants = [];
            }

            if (quantityBefore == "") {
                quantityBefore = 0;
                this.value = 0;
            }
        });

        grid.on('focusout keypress', skuQuantityClass, function (event) {
            var code = event.keyCode || event.which || event.charCode;

            if (code != 13 && code != undefined) {
                return;
            }

            var quantityAfter = 0;
            var gridLevelTotalPrice = "";

            var indexPattern = "[0-9]+";
            var currentIndex = parseInt($(this).attr("id").match(indexPattern));

            this.value = ACC.productorderform.filterSkuEntry(this.value);

            quantityAfter = jQuery.trim(this.value);
            var variantCode = $("input[id='cartEntries[" + currentIndex + "].sku']").val();

            if (isNaN(jQuery.trim(this.value))) {
                this.value = 0;
            }

            if (quantityAfter == "") {
                quantityAfter = 0;
                this.value = 0;
            }

            var $gridTotalValue = grid.find("[data-grid-total-id=" + 'total_value_' + currentIndex + "]");
            var currentPrice = $("input[id='productPrice[" + currentIndex + "]']").val();

            if (quantityAfter > 0) {
                gridLevelTotalPrice = ACC.productorderform.formatTotalsCurrency(parseFloat(currentPrice) * parseInt(quantityAfter));
            }

            $gridTotalValue.html(gridLevelTotalPrice);

            var _this = this;
            var priceSibling = $(this).siblings('.price');
            var propSibling = $(this).siblings('.variant-prop');
            var currentSkuId = $(this).next('.td_stock').data('sku-id');
            var currentBaseTotal = $(this).siblings('.data-grid-total');

            if (this.value != quantityBefore) {
                var newVariant = true;
                ACC.productorderform.selectedVariants.forEach(function (item, index) {
                    if (item.id === currentSkuId) {
                        newVariant = false;

                        if(_this.value === '0' || _this.value === 0){
                            ACC.productorderform.selectedVariants.splice(index, 1);
                        } else {
                            ACC.productorderform.selectedVariants[index].quantity = _this.value;
                            ACC.productorderform.selectedVariants[index].total = ACC.productorderform.updateVariantTotal(priceSibling, _this.value, currentBaseTotal);
                        }
                    }
                });

                if(newVariant && this.value > 0){
                    // update variantData
                    ACC.productorderform.selectedVariants.push({
                        id: currentSkuId,
                        size: propSibling.data('variant-prop'),
                        quantity: _this.value,
                        total: ACC.productorderform.updateVariantTotal(priceSibling, _this.value, currentBaseTotal)
                    });
                }
            }
            ACC.productorderform.showSelectedVariant($(this).parents('table'));
            if (this.value > 0 && this.value != quantityBefore) {
                $(this).parents('table').addClass('selected');
            } else {
                if (ACC.productorderform.selectedVariants.length === 0) {
                    $(this).parents('table').removeClass('selected').find('.variant-summary').remove();

                }
            }

            if (quantityBefore != quantityAfter) {
                var method = "POST";
                $.ajax({
                    url: ACC.config.encodedContextPath + '/cart/updateMultiD',
                    data: {productCode: variantCode, quantity: quantityAfter, entryNumber: -1},
                    type: method,
                    success: function (data, textStatus, xhr) {
                        ACC.cart.refreshCartData(data, -1, quantityAfter, itemIndex);
                        mapCodeQuantity[variantCode] = quantityAfter;
                    },
                    error: function (xhr, textStatus, error) {
                        var redirectUrl = xhr.getResponseHeader("redirectUrl");
                        var connection = xhr.getResponseHeader("Connection");
                        // check if error leads to a redirect
                        if (redirectUrl !== null) {
                            window.location = redirectUrl;
                            // check if error is caused by a closed connection
                        } else if (connection === "close") {
                            window.location.reload();
                        }
                    }
                });
            }
        });
    },

    refreshCartData: function (cartData, entryNum, quantity, itemIndex) {
        // if cart is empty, we need to reload the whole page
        if (cartData.entries.length == 0) {
            location.reload();
        }
        else {
            var form;

            if (entryNum == -1) // grouped item
            {
                form = $('.js-qty-form' + itemIndex);
                var productCode = form.find('input[name=productCode]').val();

                var quantity = 0;
                var entryPrice = 0;
                for (var i = 0; i < cartData.entries.length; i++) {
                    var entry = cartData.entries[i];
                    if (entry.product.code == productCode) {
                        quantity = entry.quantity;
                        entryPrice = entry.totalPrice;
                        ACC.cart.updateEntryNumbersForCartMenuData(entry);
                        break;
                    }
                }

                if (quantity == 0) {
                    location.reload();
                }
                else {
                    form.find(".qtyValue").html(quantity);
                    form.parent().parent().find(".js-item-total").html(entryPrice.formattedValue);
                }
            }

            ACC.cart.refreshCartPageWithJSONResponse(cartData);
        }
    },

    refreshCartPageWithJSONResponse: function (cartData) {
        // refresh mini cart
        ACC.minicart.updateMiniCartDisplay();
        $('.js-cart-top-totals').html($("#cartTopTotalSectionTemplate").tmpl(cartData));
        $('div .cartpotproline').remove();
        $('div .cartproline').remove();
        $('.js-cart-totals').remove();
        $('#ajaxCartPotentialPromotionSection').html($("#cartPotentialPromotionSectionTemplate").tmpl(cartData));
        $('#ajaxCartPromotionSection').html($("#cartPromotionSectionTemplate").tmpl(cartData));
        $('#ajaxCart').html($("#cartTotalsTemplate").tmpl(cartData));
        ACC.quote.bindQuoteDiscount();
    },
    
    updateEntryNumbersForCartMenuData: function (entry) {
    	var entryNumbers = "";
        $.each(entry.entries, function(index, subEntry) {
        	if(index != 0){
        		entryNumbers = entryNumbers + ";";
        	}
        	entryNumbers = entryNumbers + subEntry.entryNumber;
        });
        $('.js-execute-entry-action-button').data('actionEntryNumbers',entryNumbers); 
    },

    getProductQuantity: function (gridContainer, mapData, mapData2, mapData3, mapData4, mapData5, mapData6, i) {
        var tables = gridContainer.find("table");

        $.each(tables, function (index, currentTable) {
        	
        	
            var skus = jQuery.map($(currentTable).find("input[type='hidden'].sku"), function (o) {
                return o.value
            });
            var quantities = jQuery.map($(currentTable).find("input[type='textbox'].sku-quantity"), function (o) {
                return o
            });
            if(quantities.length===0) {
            	quantities = jQuery.map($(currentTable).find("input[type='textbox'].sku-quantityDisabled"), function (o) {
                    return o
                });
            }
            
            
            var purchaseOrderNumbers = jQuery.map($(currentTable).find("input[type='textbox'].sku-purchaseOrderNumber"), function (o) {
                return o
            });
            if(purchaseOrderNumbers.length===0) {
            	purchaseOrderNumbers = jQuery.map($(currentTable).find("input[type='textbox'].sku-purchaseOrderNumberDisabled"), function (o) {
                    return o
                });
            }

            
            var cigs = jQuery.map($(currentTable).find("input[type='textbox'].sku-cig"), function (o) {
                return o
            });
            if(cigs.length===0) {
            	cigs = jQuery.map($(currentTable).find("input[type='textbox'].sku-cgiDisabled"), function (o) {
                    return o
                });
            }

            
            var cups = jQuery.map($(currentTable).find("input[type='textbox'].sku-cup"), function (o) {
                return o
            });
            if(cups.length===0) {
            	cups = jQuery.map($(currentTable).find("input[type='textbox'].sku-cupDisabled"), function (o) {
                    return o
                });
            }
            
            var dataOrdines = jQuery.map($(currentTable).find("input[type='textbox'].sku-dataOrdine"), function (o) {
                return o
            });
            if(dataOrdines.length===0) {
            	dataOrdines = jQuery.map($(currentTable).find("input[type='textbox'].sku-dataOrdineDisabled"), function (o) {
                    return o
                });
            }
            
            var erpCodes = jQuery.map($(currentTable).find(".erpCode"), function (o) {
                return o
            });
            	
            var selectedVariants = [];
            
            $.each(skus, function (index, skuId) {
                var quantity = mapData[skuId];
                var purchaseOrderNumber =  mapData2[skuId];
                var cig =  mapData3[skuId];
                var cup =  mapData4[skuId];
                var dataOrdine =  mapData5[skuId];
                var erpCode = $($(erpCodes[index])[0]).data('erpcode');
                
                if (quantity != undefined) {
                	quantities[index].value = quantity;
                    purchaseOrderNumbers[index].value = purchaseOrderNumber;
                    cigs[index].value = cig;
                    cups[index].value = cup;
                    dataOrdines[index].value = dataOrdine;

                    var indexPattern = "[0-9]+";
                    var currentIndex = parseInt(quantities[index].id.match(indexPattern));
                    var gridTotalValue = gridContainer.find("[data-grid-total-id=" + 'total_value_' + currentIndex + "]");
                    var gridLevelTotalPrice = "";
                    var currentPrice = $("input[id='productPrice[" + currentIndex + "]']").val();
                    if (quantity > 0) {
                        gridLevelTotalPrice = ACC.productorderform.formatTotalsCurrency(parseFloat(currentPrice) * parseInt(quantity));
                    }
                    gridTotalValue.html(gridLevelTotalPrice);

                    selectedVariants.push({
                        id: skuId,
                        size: $(quantities[index]).siblings('.variant-prop').data('variant-prop'),
                        quantity: quantity,
                        purchaseOrderNumber: purchaseOrderNumber,
                        cig: cig,
                        cup: cup,
                        dataOrdine: dataOrdine,
                        total: gridLevelTotalPrice,
                        erpCode : erpCode
                    });
                }
                
                else {
                	
                	  	  quantities[index].value = 0;
                          purchaseOrderNumbers[index].value = "";
                          cigs[index].value = "";
                          cups[index].value = "";
                          dataOrdines[index].value = "";

                          var indexPattern = "[0-9]+";
                          var currentIndex = parseInt(quantities[index].id.match(indexPattern));
                          var gridTotalValue = gridContainer.find("[data-grid-total-id=" + 'total_value_' + currentIndex + "]");
                          var gridLevelTotalPrice = "";
                          var currentPrice = $("input[id='productPrice[" + currentIndex + "]']").val();
                          if (quantity > 0) {
                              gridLevelTotalPrice = ACC.productorderform.formatTotalsCurrency(parseFloat(currentPrice) * parseInt(quantity));
                          }
                          gridTotalValue.html(gridLevelTotalPrice);

                          selectedVariants.push({
                              id: skuId,
                              size: $(quantities[index]).siblings('.variant-prop').data('variant-prop'),
                              quantity: 0,
                              purchaseOrderNumber: "",
                              cig: "",
                              cup: "",
                              dataOrdine: "",
                              total: gridLevelTotalPrice,
                              erpCode : erpCode
                          });
                      
                }
                
            });

            if (selectedVariants.length != 0) {
                $.tmpl(ACC.productorderform.$variantSummaryTemplate, {
                    variants: selectedVariants
                }).appendTo($(currentTable).addClass('selected'));
                $(currentTable).find('.variant-summary .variant-property').html($(currentTable).find('.variant-detail').data('variant-property'));
                $(currentTable).data(ACC.productorderform.selectedVariantData, selectedVariants);
            }
        });

    },

    bindMultidCartProduct: function () {
        // link to display the multi-d grid in read-only mode
        $(document).on("click",'.showQuantityProduct', function (event){
            ACC.multidgrid.populateAndShowGrid(this, event, true);
        });

        // link to display the multi-d grid in read-only mode
        $(document).on("click",'.showQuantityProductOverlay', function (event){
            ACC.multidgrid.populateAndShowGridOverlay(this, event);
        });

    },

    bindApplyVoucher: function () {

        $("#js-voucher-apply-btn").on("click", function (e) {
            ACC.cart.handleApplyVoucher(e);
        });

        $("#js-voucher-code-text").on("keypress", function (e) {
            var code = (e.keyCode ? e.keyCode : e.which);
            if (code == 13) {
                ACC.cart.handleApplyVoucher(e);
            }
        });
    },

    handleApplyVoucher: function (e) {
        var voucherCode = $.trim($("#js-voucher-code-text").val());
        if (voucherCode != '' && voucherCode.length > 0) {
            $("#applyVoucherForm").submit();
        }
    },

    bindToReleaseVoucher: function () {
        $('.js-release-voucher-remove-btn').on("click", function (event) {
            $(this).closest('form').submit();
        });
    },
    
	bindAllDataOrdine: function(e) {
		
			var allDataOrdineWrapperElement = $("#js-allDataOrdne");
			var dateFormatForDatePicker = allDataOrdineWrapperElement.data("date-format-for-date-picker");
			//var minDate = new Date();
			$("#allDataOrdine").datepicker({
				dateFormat: dateFormatForDatePicker,
				constrainInput: true
			});
			$("#allDataOrdine").attr('readonly','readonly');
			

			// MOBILE
			var allDataOrdineWrapperElement_mobile = $("#js-allDataOrdne_mobile");
			var dateFormatForDatePicker_mobile = allDataOrdineWrapperElement_mobile.data("date-format-for-date-picker");
			$("#allDataOrdine_mobile").datepicker({
				dateFormat: dateFormatForDatePicker_mobile,
				constrainInput: true
			});
			$("#allDataOrdine_mobile").attr('readonly','readonly');
	
			$(document).on("click", ".js-open-datepicker-allDataOrdine", function() {
				$("#allDataOrdine").datepicker('show');
			});
		
		
	},
    
	bindDataOrdine: function(e) {
	        var dataOridneWrapperElements = $("div[id^=js-dataOrdine_]");
	        var dateFormatForDatePicker = $(dataOridneWrapperElements[0]).data("date-format-for-date-picker");
	        
	        var inputs = $("input[id^=dataOrdine_]");
	        
	        for(var i=0; i<inputs.length; i++) {
	        	$(inputs[i]).datepicker({
	    			dateFormat: dateFormatForDatePicker,
	    			constrainInput: true
	    		});
	    		
	    		$(inputs[i]).attr('readonly','readonly');
	    		$(inputs[i]).css('background-color','white');
	    		$(inputs[i]).css('color','black');

	    		$(inputs[i]).siblings(".js-open-datepicker-dataOrdine").click(function(){$(this).siblings("input").datepicker('show');});
	        
	        }
		
	},
	
	bindOrderHistoryData: function(e) {
		$("#orderDate").datepicker({
			dateFormat: 'dd/mm/yy',
			constrainInput: true,
			minDate: new Date("01/01/2000")
		});
		$("#orderDate").attr('readonly','readonly');
		$("#orderDate").css('background-color','white');
		$("#orderDate").css('color','black');		
		$(".ui-datepicker").css('font-size', '135%');
		
		$('#reset').on('click', function() { 
	        $('#orderHistorySearchForm').find('input:text, input:password, select, textarea').val('');
	        $('#orderHistorySearchForm').find('input:radio, input:checkbox').prop('checked', false);
		});
		
		$('#orderHistorySearchForm').on('keyup keypress', function(e) {
			  var keyCode = e.keyCode || e.which;
			  if (keyCode === 13) { 
			    e.preventDefault();
			    return false;
			  }
		});
	}

};