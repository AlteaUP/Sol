ACC.multidgrid = {

	populateAndShowGridOverlay: function(element, event)
	{
		event.preventDefault();
		
    	var htmls = document.getElementsByTagName('html');
    	htmls[0].style.height = "auto";
    	console.log("Set html height to auto");

		var itemIndex = $(element).data("index");
		// FIX SOL404
		var from = $(element).data("from");
		var gridEntries = $('#grid' + itemIndex);

		var strSubEntries = gridEntries.data("sub-entries");
		var productName = gridEntries.data("product-name");
		var arrSubEntries= strSubEntries.split(',');
		var firstVariantCode = arrSubEntries[0].split(':')[0];

		var targetUrl = gridEntries.data("target-url") + '?productCode=' + firstVariantCode + '&from=' + from;

		ACC.colorbox.open(productName,{
			href:   targetUrl,
			className: 'read-only-grid',
			close:'<span class="glyphicon glyphicon-remove"></span>',
			width: window.innerWidth > parseInt(cboxOptions.maxWidth) ? cboxOptions.maxWidth : cboxOptions.width,
			height: window.innerHeight > parseInt(cboxOptions.maxHeight) ? cboxOptions.maxHeight : cboxOptions.height,
			onComplete: function() {

				$('body').addClass('offcanvas');
				var oH = $('#cboxLoadedContent').height();
				$('#cboxLoadedContent').height((oH - $('#cboxTitle').height()) +'px');

			},

			onClosed: function() {
				$('body').removeClass('offcanvas');
			}

		});
	},
	
	populateAndShowGrid: function(element, event, readOnly)
	{

    	var htmls = document.getElementsByTagName('html');
    	htmls[0].style.height = "auto";
    	console.log("Set html height to auto");
		
		var itemIndex = $(element).data("index");
		grid = $("#ajaxGrid" + itemIndex);
		var gridEntries = $('#grid' + itemIndex);
		
		$(element).toggleClass('open');
		
		if (!grid.is(":hidden")) {
        	grid.slideUp();
        	return;
        }

		if(grid.html() != "") {
			grid.slideToggle("slow");
			return;
		}

		var strSubEntries = gridEntries.data("sub-entries");
		var arrSubEntries= strSubEntries.split(',');
		var firstVariantCode = arrSubEntries[0].split(':')[0];

		var targetUrl = gridEntries.data("target-url");

		var method = "GET";
		$.ajax({
			url: targetUrl,
			data: {productCode: firstVariantCode},
			type: method,
			success: function(data)
			{
				grid.html(data);
				grid.slideDown("slow");
			},
			error: function(xht, textStatus, ex)
			{
				alert("Failed to get variant matrix. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
			}

		});
	}
};