componentconstructors['chat'] = function(dynmap, configuration) {
	var me = this;
	// Provides 'chat'-events by monitoring the world-updates.
	$(dynmap).bind('worldupdate', function(event, update) {
		swtch(update.type, {
			chat: function() {
				$(dynmap).trigger('chat', [{source: update.source, name: update.playerName, text: update.message, account: update.account,
                channel: update.channel}]);
			}
		});
	});
	
	$(dynmap).bind('sendchat', function(event, message) {
		dynmap.sendWebMessage('chat', {
			/* TODO */
			//name: 'An anonymous username',
			message: message
		});
	});
};
