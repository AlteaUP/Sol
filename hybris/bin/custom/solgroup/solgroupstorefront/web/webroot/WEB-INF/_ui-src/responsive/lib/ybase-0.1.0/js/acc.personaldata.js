ACC.personaldata = {

	bindPersonalDataLink: function() {
		$(document).on("click",".personalDataLink",function(e) {
			e.preventDefault();
			var url = $(".personalDataLink").attr('href'); 
			var win = window.open(url, '_blank');
			if (win) {
				$("#Terms2").removeAttr('disabled');
			} 
		});
	}
}

$(function(){
	with(ACC.personaldata) {
		bindPersonalDataLink();
	}
});
