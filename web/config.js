var config = {
	// For internal server or proxying webserver.
	url : {
		configuration : 'up/configuration',
		update : 'up/world/{world}/{timestamp}',
		sendwebmessage: 'up/sendwebmessage'
	},

	// For proxying webserver through php.
	// url: {
	// configuration: 'up.php?path=configuration',
	// update: 'up.php?path=world/{world}/{timestamp}',
	// sendwebmessage: 'up.php?path=sendwebmessage'
	// },

	// For proxying webserver through aspx.
	// url: {
	// configuration: 'up.aspx?path=configuration',
	// update: 'up.aspx?path=world/{world}/{timestamp}',
	// sendwebmessage: 'up.aspx?path=sendwebmessage'
	// },

	// For standalone (jsonfile) webserver.
	// url: {
	// configuration: 'standalone/dynmap_config.json',
	// update: 'standalone/dynmap_{world}.json',
	// sendwebmessage: 'standalone/sendwebmessage.php'
	// },

	tileUrl : 'tiles/',
	tileWidth : 128,
	tileHeight : 128
};
