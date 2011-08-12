componentconstructors['authentication'] = function(dynmap, configuration) {
	loadjs('js/openid-jquery.js', function() {
		loadjs('js/openid-en.js', function() {
			$('<fieldset/>')
				.append($('<legend/>').text('Login'))
				.append($('<input/>').attr({ type: 'button', value: 'Login' }).click(function() {
					var dialog = $('<div/>')
						.css({
							'font-size': '1em',
							color: 'black',
							position: 'absolute',
							padding: '10px',
							width: '550px',
							'margin-left': '-275px',
							top: '0px',
							left: '50%',
							'z-index': '999999',
							'background-color': 'white',
							'border-radius': '10px',
							'box-shadow': '0px 0px 100px #000'
						}).appendTo(document.body);
					var openidForm = $('<form action="examples/consumer/try_auth.php" method="get" id="openid_form"><input type="hidden" name="action" value="verify" /><div id="openid_choice"><p>Please click your account provider:</p><div id="openid_btns"></div></div><div id="openid_input_area"><input id="openid_identifier" name="openid_identifier" type="text" value="http://" /><input id="openid_submit" type="submit" value="Sign-In"/></div></form>');
					dialog.append(openidForm);
					openid.init('openid_identifier');
					openidForm.submit(function() {
						var identifier = $('#openid_identifier', openidForm).val();
						openidForm.remove();
						
						dialog.text('Verifying...');
						
						// Send to server.
						
						return false;
					});
				}))
				.prependTo($('.sidebar > .panel', dynmap.options.container)[0]);
		});
	});
	$(document.head).append($('<style> #openid_form { width: 580px; } #openid_form legend { font-weight: bold; } #openid_choice { display: none; } #openid_input_area { clear: both; padding: 10px; } #openid_btns, #openid_btns br { clear: both; } #openid_highlight { padding: 3px; background-color: #FFFCC9; float: left; } .openid_large_btn { width: 100px; height: 60px; _width: 102px; _height: 62px; border: 1px solid #DDD; margin: 3px; float: left; } .openid_small_btn { width: 24px; height: 24px; _width: 26px; _height: 26px; border: 1px solid #DDD; margin: 3px; float: left; } a.openid_large_btn:focus { outline: none; } a.openid_large_btn:focus { -moz-outline-style: none; } .openid_selected { border: 4px solid #DDD; } </style>'));
};