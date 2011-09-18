componentconstructors['authentication'] = function(dynmap, configuration) {
	var legend;
	var fieldset = $('<fieldset/>')
		.addClass('authentication')
		.append(legend = $('<legend/>').text('Loading...'))
		.prependTo($('.sidebar > .panel', dynmap.options.container)[0]);
	$.getJSON('/up/authentication/test', function(result) {
		if (result) {
			dynmap.user = result;
			legend.text('Logged in');
			build_loggedIn(fieldset);
		} else {
			dynmap.user = undefined;
			legend.text('Login');
			build_loggedOut(fieldset);
		}
	});
	
	function login(playername) {
		var baseurl = document.URL.match(/^[^\?#]+/)[0];
		
		var url = baseurl + 'up/authentication/request?playername=' + escape(playername) +
			'&verifyurl=' + escape(baseurl + 'up/authentication/verify') +
			'&originalurl=' + escape(document.URL) +
			'&json=1';
		$.getJSON(url, function(obj) {
			if (obj.error) {
				alert(obj.error);
			} else {
				// Do a POST or GET?
				if (obj.parameters) {
					var f = $('<form>')
						.attr({action:obj.url,method:'POST'});
					$.each(obj.parameters, function(key, value) {
						$('<input>').attr({type:'hidden',name:key,value:value}).appendTo(f);
					});
					f.hide()
						.appendTo(document.body)
						.submit();
				} else {
					window.location.replace(obj.url);
				}
			}
		});
	}
	
	function logout() {
		$.getJSON('/up/authentication/logout', function() {
			window.location.reload(false);
		});
	}
	
	function build_loggedIn(fieldset) {
		var iconContainer;
		fieldset.append(
			$('<input>').addClass('logout').attr({type: 'button', value: 'Logout'}).click(logout),
			iconContainer = $('<span>').addClass('iconcontainer'),
			$('<span>').addClass('playername').text(dynmap.user.playername)
			);
		getMinecraftHead(dynmap.user.playername, 16, function(result) {
			iconContainer.append(result);
		});
	}
	
	function build_loggedOut(fieldset) {
		var playerNameInput = $('<input/>')
			.addClass('playername')
			.focus()
			.keypress(function(e){
				if(e.which == 13) {
					authenticate_click();
					e.preventDefault();
				}
			});
		var authenticateButton = $('<input type="button" value="Login" />')
			.addClass('login')
			.click(authenticate_click);
		
		function authenticate_click() {
			login(playerNameInput.val());
		}
		
		
		fieldset.append(playerNameInput, authenticateButton);
	}
};