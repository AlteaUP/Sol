ACC.termsandconditions = {

	bindTermsAndConditionsLink: function() {
		$(document).on("click",".termsAndConditionsLink",function(e) {
			e.preventDefault();
			var data = $("#terms-and-condition").html();
			var title = $("#termsTitle").data("title");
			
			$.colorbox({
				maxWidth:"100%",
				maxHeight:"80%",
				width:"870px",
				scrolling:true,
				href: $(this).attr("href"),
				close:'<span class="glyphicon glyphicon-remove"></span>',
				title:title,
				html: data,
				onComplete: function() {
					ACC.common.refreshScreenReaderBuffer();
				},
				onClosed: function() {
					ACC.common.refreshScreenReaderBuffer();
				}
			});
		});
	}
}

$(function(){
	with(ACC.termsandconditions) {
		bindTermsAndConditionsLink();
	}
});
