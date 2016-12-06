package ionicStart;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.resource.Resource;

import br.com.javatools.Utils;

public class run {

	private static Hashtable<String, String> restUrls = new Hashtable<>();

	public static class StaticResourceHandler extends ResourceHandler {

		@Override
		public void handle(String arg0, Request req, HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {

			System.out.println(req.getRequestURI());

			response.addHeader("Access-Control-Allow-Origin", "*");
			response.addHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization");
			response.addHeader("Access-Control-Allow-Credentials", "true");
			response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");

			super.handle(arg0, req, request, response);
		}

		@Override
		protected void doResponseHeaders(HttpServletResponse response, Resource resource, String mimeType) {
			super.doResponseHeaders(response, resource, mimeType);

			System.out.println(resource.getName());

			response.addHeader("Access-Control-Allow-Origin", "*");
			response.addHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization");
			response.addHeader("Access-Control-Allow-Credentials", "true");
			response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
		}
	}

	public static void main(String[] args) throws Exception {
		if (args == null) {
			showUsage();
			System.exit(1);
		}

		runServer(args);
	}

	private static void showUsage() {
		System.out.println("noteStart:\nUsage:\n\t");
		System.out.println("Arguments:\n\t[html home dir]: Home app html files (Default: Current directory).");
		System.out.println("\t[shell]: Use shell exec to open browser or internal Desktop object, Default: Shell).");
		System.out.println("\t[debug]: Enable debug, Default: false).");
		System.out.println("\t[w:NNN]: Window width in pixels(e.g.: w:800, Default: 400).");
		System.out.println("\t[h:NNN]: Window height in pixels(e.g.: g:800, Default: 600).");
		System.out.println("\t[title:XXX]: Window title (e.g.: title:My App, Default: App).");
		System.out.println("\t[port:NNN]: Internal Jetty Embedded Server port (e.g.: port:8088, Default: 8100).");
		System.out.println("\t[start:NNN]: Default start page (e.g.: start:home.html, Default: index.html).");
		System.out.println(
				"\t[about:XXX]: Custom about message showed in tray icon (e.g.: about:My app about, Default: {{title}} + \\n\\n {{YEAR}} All rights reserved).");
		System.out.println(
				"\t[trayimg:XXX]: Path to custom icon tray (e.g.: trayimg:./app.png, Default: internal icon image).");
		System.out.println(
				"\t[webview:true|false]: Use WebView or JavaFX embedded browser (e.g.: webview:true, Default: JavaFx).");
		System.out.println(
				"\t[rest:XXX=URL;XXX2=URL2...]: REST URL´s for internal url forwarding to prevent cross domain errors "
						+ "(e.g.: rest:service1=http://other.domain/services/service1;service2=http://another.domain/services/service2).");
	}

	@SuppressWarnings("serial")
	public static class ForwardServlet extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {

			try {
				String rest = request.getRequestURI().split("/")[1];

				if (restUrls.get(rest) == null)
					return;

				String url = request.getScheme() + "://" + restUrls.get(rest)
						+ new StringBuffer(request.getRequestURI()).insert(1, "?");

				System.out.println("URL: " + url);

				StringBuilder sb = new StringBuilder();

				try {
					final URL rUrl = new URL(request.getScheme() + "://" + restUrls.get(rest) + request.getRequestURI()
							+ (request.getQueryString() != null ? "?" + request.getQueryString() : ""));

					HttpURLConnection conn = (HttpURLConnection) rUrl.openConnection();
					conn.setRequestMethod(request.getMethod());

					System.out.println("Proxy Url: " + rUrl.getPath());

					System.out.println("Headers:");

					final Enumeration<String> headers = request.getHeaderNames();
					while (headers.hasMoreElements()) {
						final String header = headers.nextElement();
						System.out.println("header:" + header);
						final Enumeration<String> values = request.getHeaders(header);
						while (values.hasMoreElements()) {
							String value = values.nextElement();
							if (header.equals("Accept"))
								value = "*/*";
							conn.addRequestProperty(header, value);
							System.out.println("Addding header:" + header + "=" + value);
						}
					}

					conn.addRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");

					// conn.setFollowRedirects(false);
					conn.setUseCaches(false);
					conn.setDoInput(true);
					conn.setDoOutput(true);
					conn.connect();

					System.out.println("Method: " + request.getMethod());

					final byte[] buffer = new byte[16384];
					final boolean hasoutbody = (request.getMethod().equals("POST"));
					while (hasoutbody) {
						final int read = request.getInputStream().read(buffer);
						if (read <= 0)
							break;

						String s = new String(buffer, 0, read);
						System.out.println(s);

						conn.getOutputStream().write(buffer, 0, read);
					}

					response.setStatus(conn.getResponseCode());

					System.out.println("Status: " + conn.getResponseMessage() + "\n" + conn.getResponseMessage());
					System.out.println("Adding Response headers:");

					for (Iterator iterator = conn.getHeaderFields().keySet().iterator(); iterator.hasNext();) {
						String header = (String) iterator.next();
						if (header == null)
							continue;

						final String value = conn.getHeaderField(header);
						response.setHeader(header, value);

						System.out.println("header:" + header + "=" + value);
					}

					System.out.println("Body:");
					while (true) {
						final int read = conn.getInputStream().read(buffer);
						if (read <= 0)
							break;

						String s = new String(buffer, 0, read);
						sb.append(s);

						System.out.println(s);

						response.getOutputStream().write(buffer, 0, read);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				// response.setStatus(HttpServletResponse.SC_OK);
				// response.getWriter().write(sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			doGet(req, resp);
		}
	}

	private static void runServer(String[] args) throws IOException {

		System.out.println("App starting...");

		String webappDirLocation = args.length > 0 ? args[0] : Utils.getPathOfClass(run.class);

		System.out.println("Home directory: " + webappDirLocation);

		String useShell = args.length > 0 && Arrays.asList(args).contains("shell") ? "true" : null;
		String debug = args.length > 0 && Arrays.asList(args).contains("debug") ? "true" : null;

		if (debug != null)
			System.setProperty("org.eclipse.jetty.LEVEL", "DEBUG");
		else
			System.setProperty("org.eclipse.jetty.LEVEL", "INFO");

		System.out.println("Using shell exec: " + (useShell != null ? useShell : "none"));
		System.out.println("Debug model: " + (debug != null ? "DEBUG (firebug)" : "INFO"));

		String width = "400";
		String height = "600";
		String title = "App";
		String serverPort = "8100";
		String startPage = "start.html";
		String about = null;
		String trayImage = null;
		String webview = "true";
		String restServices = null;

		for (String arg : args) {
			if (arg.startsWith("w:"))
				width = arg.substring(2);
			else if (arg.startsWith("h:"))
				height = arg.substring(2);
			else if (arg.startsWith("title:"))
				title = arg.substring(6);
			else if (arg.startsWith("port:"))
				serverPort = arg.substring(5);
			else if (arg.startsWith("start:"))
				startPage = arg.substring(6);
			else if (arg.startsWith("about:"))
				about = arg.substring(6);
			else if (arg.startsWith("trayimg:"))
				trayImage = arg.substring(8);
			else if (arg.startsWith("webview:"))
				webview = arg.substring(8);
			else if (arg.startsWith("rest:")) {
				restServices = arg.substring(5);
				String[] aRest = restServices.split(";");
				for (int i = 0; i < aRest.length; i++) {
					restUrls.put(aRest[i].split("=")[0], aRest[i].split("=")[1]);
				}
			}
		}

		if (about == null)
			about = title + "\n\n" + Calendar.getInstance().get(Calendar.YEAR) + " All rights reserved";

		System.out.println("Window Title: " + title);
		System.out.println("Window Width: " + width);
		System.out.println("Window Height: " + height);
		System.out.println("Server Port: " + serverPort);
		System.out.println("Tray image: " + trayImage);

		System.out.println("WebView: " + webview);

		if (webview.equals("true"))
			startPage = "index.html";

		// if other, ignored
		if (startPage.equals("start.html")) {
			System.out.println("Copying template start file" + startPage + "...");

			Files.copy(
					new File(Utils.getPathOfClass(ionicStart.run.class).replaceAll("/bin", "") + "template/start.html")
							.toPath(),
					new File(webappDirLocation + "start.html").toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		if (!Files.exists(new File(webappDirLocation + File.separatorChar + startPage).toPath(),
				LinkOption.NOFOLLOW_LINKS)) {

			showUsage();
			System.out.println(
					"\nInvalid Html home dir: no " + startPage + " found in " + webappDirLocation + ", aborting...");
			return;
		}
		// search for available port number
		int port = Integer.valueOf(serverPort);

		Server server = new Server(port);

		StaticResourceHandler resource_handler = new StaticResourceHandler();
		resource_handler.setDirectoriesListed(true);
		resource_handler.setWelcomeFiles(new String[] { startPage });

		resource_handler.setResourceBase(webappDirLocation);

		ServletHandler servletHandler = new ServletHandler();

		ServletContextHandler context = new ServletContextHandler();
		context.addServlet(ForwardServlet.class, "/*");

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resource_handler, context });

		server.setHandler(handlers);

		try {
			System.out.println("Starting server: http://localhost:" + port);
			server.start();
			server.dump(System.err);

			String url = "http://localhost:" + port + "/" + startPage + "?q=open&w=" + width + "&h=" + height
					+ "&title=" + title;

			if (webview.equals("true")) {
				url = "http://localhost:" + port + "/" + startPage;

				new WebApp().run(new String[] { url, title, width, height, debug });

			} else {
				Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
				if (useShell == null && desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
					try {
						desktop.browse(new URI(url));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					Runtime runtime = Runtime.getRuntime();
					String os = System.getProperty("os.name").toLowerCase();
					try {
						if (os.indexOf("win") >= 0)
							// runtime.exec("cmd /c start " + url);
							runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
						else if (os.indexOf("mac") >= 0)
							runtime.exec("open " + url);
						else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0)
							runtime.exec(new String[] { "sh", "-c", url });
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			showTray(title, about, trayImage);

			server.join();
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}
	}

	private static void showTray(String tooltip, String about, String image) {
		if (!SystemTray.isSupported()) {
			System.out.println("System tray is not supported !!! ");
			return;
		}

		SystemTray systemTray = SystemTray.getSystemTray();

		if (image == null)
			image = "template/webapp.png";

		Image img = Toolkit.getDefaultToolkit().getImage(image);

		PopupMenu trayPopupMenu = new PopupMenu();

		MenuItem action = new MenuItem("About");
		action.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, about);
			}
		});
		trayPopupMenu.add(action);

		MenuItem close = new MenuItem("Close " + tooltip);
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				JOptionPane.showMessageDialog(null,
						"Application " + tooltip + " will be closed, close all opened windows after that, bye!!");

				System.exit(0);
			}
		});
		trayPopupMenu.add(close);

		TrayIcon trayIcon = new TrayIcon(img, tooltip, trayPopupMenu);
		trayIcon.setImageAutoSize(true);

		try {
			systemTray.add(trayIcon);
		} catch (AWTException awtException) {
			awtException.printStackTrace();
		}
	}

	private static boolean testServerConnection(String host, String port) {
		boolean connected = false;

		try {
			@SuppressWarnings("resource")
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(host, Integer.valueOf(port)), 500);

			connected = true;
		} catch (Exception ex) {
			System.out.println("Port available: " + host + ":" + port);
		}

		return connected;
	}
}
