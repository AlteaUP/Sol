ACC.mydocuments = {

	_autoload: [
	    "bindWamStartDateData",
	    "bindWamEndDateData"
	],
	reloadPage: function (){
		var selectedOption = $('select[name=documentType]').find(":selected").val().trim();
		var uri = window.location.toString();
		if (uri.indexOf("?") > 0) {
		    var clean_uri = uri.substring(0, uri.indexOf("?"));
		    window.history.replaceState({}, document.title, clean_uri);
		}
		window.location.href = window.location + "?documentType="  +  selectedOption;
	},
    downloadDocument: function(documentId, documentKey, documentType) {
	    var uri = window.location.toString();
        if (uri.indexOf("?") > 0) {
            var clean_uri = uri.substring(0, uri.indexOf("?"));
            window.history.replaceState({}, document.title, clean_uri);
        }
        var encodedDocumentKey = ACC.mydocuments.fixedEncodeURIComponent(documentKey);
        var encodedUri = window.location + "/downloadWamDocument?documentId=" + documentId + "&documentKey=" + encodedDocumentKey + "&documentType=" + documentType;
        window.open(encodedUri);
	},
	fixedEncodeURIComponent: function (str){
        return encodeURIComponent(str).replace(/[!'()*]/g, function (c) {
        return '%' + c.charCodeAt(0).toString(16);
        });
	},
    bindWamStartDateData: function(e) {
        $("#wamStartDate").datepicker({
            dateFormat: 'dd/mm/yy',
            changeMonth: true,
            changeYear: true,
            constrainInput: true,
            minDate: new Date("01/01/2000"),
            onSelect: function(dateText) {
                var startDate = $('#wamStartDate').datepicker('getDate');
                var endDate = $('#wamEndDate').datepicker('getDate');
                var daysRange = parseInt($('#defaultDaysRange').val(), 10);
                const diffTime = endDate - startDate;
                const diffDays = Math.round(diffTime / (1000 * 60 * 60 * 24));
                if(diffDays < 0 || diffDays > daysRange){
                    var newDate = new Date(startDate);
                    newDate.setDate(startDate.getDate() + daysRange)
                    $('#wamEndDate').datepicker('setDate', newDate);
                }

            }
        });
        $("#wamStartDate").attr('readonly','readonly');
        $("#wamStartDate").css('background-color','white');
        $("#wamStartDate").css('color','black');
        $(".ui-datepicker").css('font-size', '135%');
    },
    bindWamEndDateData: function(e) {
        $("#wamEndDate").datepicker({
            dateFormat: 'dd/mm/yy',
            changeMonth: true,
            changeYear: true,
            constrainInput: true,
            minDate: new Date("01/01/2000"),
            onSelect: function(dateText) {
                var startDate = $('#wamStartDate').datepicker('getDate');
                var endDate = $('#wamEndDate').datepicker('getDate');
                var daysRange = parseInt($('#defaultDaysRange').val(), 10);
                const diffTime = endDate - startDate;
                const diffDays = Math.round(diffTime / (1000 * 60 * 60 * 24));
                if(diffDays < 0 || diffDays > daysRange){
                    var newDate = new Date(endDate);
                    newDate.setDate(endDate.getDate() - daysRange)
                    $('#wamStartDate').datepicker('setDate', newDate);
                }
            }
        });
        $("#wamEndDate").attr('readonly','readonly');
        $("#wamEndDate").css('background-color','white');
        $("#wamEndDate").css('color','black');
        $(".ui-datepicker").css('font-size', '135%');
    },
    searchWamDocuments: function(){
        var startDate = $('#wamStartDate').datepicker('getDate');
        var endDate = $('#wamEndDate').datepicker('getDate');
        if (startDate !== null && endDate != null) {
            var fStartDate = $.datepicker.formatDate("dd-mm-yy", startDate);
            var fEndDate = $.datepicker.formatDate("dd-mm-yy", endDate);
            var documentType = $('select[name=documentType]').find(":selected").val().trim();
            var uri = window.location.toString();
            if (uri.indexOf("?") > 0) {
                var clean_uri = uri.substring(0, uri.indexOf("?"));
                window.history.replaceState({}, document.title, clean_uri);
            }
            window.location.href = window.location + "?documentType="  +  documentType + "&searchFromDate=" + ACC.mydocuments.fixedEncodeURIComponent(fStartDate) + "&searchToDate=" + ACC.mydocuments.fixedEncodeURIComponent(fEndDate);
        }
    }
};
