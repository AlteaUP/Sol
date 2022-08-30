<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>

<script type="text/javascript" src="/_ui/responsive/common/js/jquery-2.1.1.min.js"></script>
<script type="text/javascript" src="/_ui/responsive/common/js/jquery.hoverIntent.js"></script>
<script type="text/javascript" src="${sharedResourcePath}/js/superfish.js"></script>

<script>

		(function($){ //create closure so we can safely use $ as alias for jQuery

			$(document).ready(function(){

				// initialise plugin
				var example = $('.sf-menu').superfish({
					//add options here if required
				});

			});

		})(jQuery);


</script>
