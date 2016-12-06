# nodeStart

Usage:

Arguments:

	[html home dir]: Home app html files (Default: Current directory).
	[shell]: Use shell exec to open browser or internal Desktop object, Default: Shell).
	[debug]: Enable debug, Default: false).
	[w:NNN]: Window width in pixels(e.g.: w:800, Default: 400).
	[h:NNN]: Window height in pixels(e.g.: g:800, Default: 600).
	[title:XXX]: Window title (e.g.: title:My App, Default: App).
	[port:NNN]: Internal Jetty Embedded Server port (e.g.: port:8088, Default: 8100).
	[start:NNN]: Default start page (e.g.: start:home.html, Default: index.html).
	[about:XXX]: Custom about message showed in tray icon (e.g.: about:My app about, Default: {{title}} + \n\n {{YEAR}} All rights reserved).
	[trayimg:XXX]: Path to custom icon tray (e.g.: trayimg:./app.png, Default: internal icon image).
	[webview:true|false]: Use WebView or JavaFX embedded browser (e.g.: webview:true, Default: JavaFx).
	[rest:XXX=URL;XXX2=URL2...]: REST URL´s for internal url forwarding to prevent cross domain errors (e.g.: rest:service1=http://other.domain/services/service1;service2=http://another.domain/services/service2).

Run:

java -classpath .\bin;./lib\jetty-all.jar;./lib\javaee-api-7.0.jar;./lib\servlet-api.jar;./lib\fts-utils.jar;./lib\javatools.jar;./lib\log4j-1.2.9.jar ionicStart.run . shell title:My-App start:index.html
