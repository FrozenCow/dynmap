var config = {
	// For internal server or proxying webserver.
	url : {
		configuration : 'up/configuration',
		update : 'up/world/{world}/{timestamp}',
		sendmessage : 'up/sendmessage',
		sendwebmessage: 'up/sendwebmessage'
	},

	// For proxying webserver through php.
	// url: {
	// configuration: 'up.php?path=configuration',
	// update: 'up.php?path=world/{world}/{timestamp}',
	// sendmessage: 'up.php?path=sendmessage',
	// sendwebmessage: 'up.php?path=sendwebmessage'
	// },

	// For proxying webserver through aspx.
	// url: {
	// configuration: 'up.aspx?path=configuration',
	// update: 'up.aspx?path=world/{world}/{timestamp}',
	// sendmessage: 'up.aspx?path=sendmessage'
	// sendwebmessage: 'up.aspx?path=sendwebmessage'
	// },

	// For standalone (jsonfile) webserver.
	// url: {
	// configuration: 'standalone/dynmap_config.json',
	// update: 'standalone/dynmap_{world}.json',
	// sendmessage: 'standalone/sendmessage.php'
	// sendwebmessage: 'standalone/sendwebmessage.php'
	// },

	tileUrl : 'tiles/',
	tileWidth : 128,
	tileHeight : 128
};
