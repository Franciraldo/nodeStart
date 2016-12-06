package ionicStart;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class WebApp extends Application {

	private Scene scene;

	private static String[] args;

	public String[] getArguments() {
		return args;
	}

	public void setArguments(String[] arguments) {
		this.args = arguments;
	}

	@Override
	public void start(Stage stage) throws Exception {

		WebView browser = new WebView();
		WebEngine webEngine = browser.getEngine();

		// arguments[]: url title width height
		stage.setTitle((args.length > 1 ? args[1] : "Web View"));

		boolean debug = (args.length > 4 && args[4] != null ? true : false);

		scene = new Scene(new Browser((args.length > 0 ? args[0] : "about:blank"), debug),
				(args.length > 2 ? Integer.valueOf(args[2]) : 750), (args.length > 3 ? Integer.valueOf(args[3]) : 500),
				Color.web("#666970"));

		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		if (args != null && args.length > 0)
			launch(args);
	}

	public void run(String[] args) {
		setArguments(args);
		launch(args);
	}
}

class Browser extends Region {

	final WebView browser = new WebView();
	final WebEngine webEngine = browser.getEngine();

	public Browser(String url, boolean debug) {
		// load the web page
		webEngine.load(url);
		webEngine.setJavaScriptEnabled(true);

		// Intercept any location that includes the authorization code
		webEngine.locationProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> ov, final String oldLoc, final String loc) {
				System.out.println("Saw location changed to " + loc);

				// We have been redirected with our authorization code
				if (loc.contains("code=")) {
					try {
						URL url = new URL(loc);
						String[] params = url.getQuery().split("&");
						Map<String, String> map = new HashMap<String, String>();
						for (String param : params) {
							String name = param.split("=")[0];
							String value = param.split("=")[1];
							map.put(name, value);
						}
						System.out.println("The code is: " + map.get("code"));
						String auth_code = map.get("code");
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
			}
		});

		// show firebug: comment on release
		if (debug) {
			webEngine.documentProperty().addListener(new ChangeListener<Document>() {
				@Override
				public void changed(ObservableValue<? extends Document> prop, Document oldDoc, Document newDoc) {
					enableFirebug(webEngine);
				}
			});

			webEngine.setOnAlert(new EventHandler<WebEvent<String>>() {
				@Override
				public void handle(WebEvent<String> event) {
					System.out.println(event.getData());
				}
			});
		}

		// add the web view to the scene
		getChildren().add(browser);
	}

	private static void enableFirebug(final WebEngine engine) {
		engine.executeScript(
				"if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
	}

	private Node createSpacer() {
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		return spacer;
	}

	@Override
	protected void layoutChildren() {
		double w = getWidth();
		double h = getHeight();
		layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
	}

	public WebView getBrowser() {
		return browser;
	}

	public WebEngine getWebEngine() {
		return webEngine;
	}

	@Override
	protected double computePrefWidth(double height) {
		return 750;
	}

	@Override
	protected double computePrefHeight(double width) {
		return 500;
	}
}