# bravo-grid
This project adds selenium grid extension to the official [docker-selenium](https://github.com/SeleniumHQ/docker-selenium) images to enable remote Sikuli operations and files transfer with grid nodes. Extensions are based on great project [Selenium Grid Extensions](https://github.com/sterodium/selenium-grid-extensions).

Images included:

- __bravo/grid/hub__: Image for running a Selenium Grid Hub
- __bravo/grid/node-chrome__: Selenium node with Chrome installed, needs to be connected to a Selenium Grid Hub
- __bravo/grid/node-firefox__: Selenium node with Firefox installed, needs to be connected to a Selenium Grid Hub
- __bravo/grid/node-chrome-debug__: Selenium node with Chrome installed and runs a VNC server, needs to be connected to a Selenium Grid Hub
- __bravo/grid/node-firefox-debug__: Selenium node with Firefox installed and runs a VNC server, needs to be connected to a Selenium Grid Hub

## Building the images
```bash
build.sh
```
## Running the images
```bash
bravogridctl.sh start
```
To stop running container use command:
```bash
bravogridctl.sh start
```

## Using extensions
Extension proxy objects are instantiated through io.sterodium.rmi.protocol.client.RemoteNavigator as follows:

```java
String urlHub = "http://192.168.99.100:4444/wd/hub"
RemoteWebDriver driver = new RemoteWebDriver(urlHub, caps);
URL urlHubParsed = new URL(urlHub);
String driverSessionId = driver.getSessionId().toString();
String EXTENSION_PATH_FMT = "/grid/admin/HubRequestsProxyingServlet/session/%s/BravoExtensionServlet"
String extensionPath = String.format(EXTENSION_PATH, driverSessionId);
RemoteNavigator = new RemoteNavigator(urlHubParsed.getHost(), urlHubParsed.getPort(), extensionPath);
SikuliScreen screen = navigator.createProxy(SikuliScreen.class, "screen");  
FileTransfer files = navigator.createProxy(FileTransfer.class, "files");
```
To remotelly control sikuli, first add base64 encoded png image content as sikuli target with choosen name and then search this target via sikuli using same name.

```java
String b64PNG = DatatypeConverter.printBase64Binary(Files.readAllBytes(pngPath));
Strign myTargetName = "SomeUniqueName";
screen.addTarget(myTargetName, b64PNG);
ScreenRegion target = screen.find(myTargetName);
target.click()
```
To upload file call method saveFile on FileTransfer object, eg:

```java
String remotePath = files.saveFile(".html", "/path/to/test.hml");
driver.get("file://" + remotePath);

```

### Debugging
Install RealVNC or any other VNC client, and connect to 127.0.0.1:6000 (Chrome) or 127.0.0.1:7000 (Firefox)