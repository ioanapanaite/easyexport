# easyexport
Java 8 Library for exporting an url into different formats like PDF, PNG, GIF etc. using PhantomJs.

## Description

EasyExport is a Java 8 library used for rendering any given URL into the desired format (from PDF, PNG, JPEG, GIF etc.). Using PhantomJs (link) for rendering and Finagle library from Twitter (link) to load balance the requests to the PhantomJs instances, the library will render your URLs fast and asynchronously. 
To use this library you will need to have downloaded the latest version of PhantomJs (link). This library provides the script used by PhanomJs to render your URLs.
     
The idea: when your application starts, a configurable number of PhantomJs instances (servers) are started and will listen to a number of configurable ports the requests for rendering URLs. When a request to render a page is made, the library load balances which PhantomJs instance is the best to serve your request the fastest and will choose it. Your page gets rendered. The PhantomJs instances are managed by the library to always have alive the number of servers you decided (in case of them crashes or needs to be restarted in case of incapacity to render any more pages).
    
EasyExport can be used by including it into your project and use it as it is or build you microservice from it and scale it as you need.

## Installation

1. Clone the GitHub repository and import it into your project.
2. Download [PhantomJs](http://phantomjs.org) somewhere on you system/server where your application is deployed.
3. Use it.

## Usage

1. The first step before starting to to the rendering of your URLs is to start the library by calling ```EasyExporter.init``` method. Based on a configuration you will need to define, the method will start the PhantomJs instances that will listen to your requests. See example below.
2. Render your URLs by calling ```EasyExporter.render``` method with the page configuration for your output. See examples below.
3. Before shutting down your application that uses EasyExport library, you will need to call the method ```EasyExporter.destroy```. This will wait for all the rendering requests in progress to finish and then shuts down all the PhantomJs instances. See example below

## Examples

#### 1. Init
```
// injected instance of exporter
private EasyExporter easyExporter;

EasyExportConfig config = new EasyExportConfig.Builder()
        .withPhantomJsExecutablePath("/usr/local/phantomjs/bin/phantomjs") // required to point to the location you downloaded PhantomJs
        .withPhantomJsScriptPath("/path/to/your/own/phantomjs/script") // if not defined, will point to the script provided by the library from resources/phantomjs/run.js
        .withPhantomJsHost("135.768.89") // default: localhost
        .withPhantomJsPortList(8880, 8881, 8882, 8883, 8884, 8885, 8886) // default: 9000,9001,9002,9003,9004,9005,9006,9007,9008,9009
        .withPhantomJsNoOfAliveInstances(2) // default: 3
        .withMaxRequestsPerRenderer(500) // default: 1000
        .withMaxRetriesPerRequest(2) // default: 5
        .withRequestTimeout(120) // in seconds; default: 60
        .withRendererLogFolderPath("/tmp/randomlocation/phantomjs/logs") //default: /tmp/easyexporter/logs
        .build();
easyExporter.init(config);
```
#### 2. Rendering

Default page configuration:    
  - Page viewport size: 
    - width = 1024
    - height = 768
    
  - Paper size:
    - width = 210mm
    - heght = 297mm
    - margin = 20px
  - No header
  - No footer
  - No basic authentication
  - No cookies
  
  
##### 2.1. PDF with default configuration
```
EasyExportPageConfig pageConfig = new EasyExportPageConfig.Builder()
    .exportType(PageExportType.PDF)
    .build();
File result = easyExporter.render(pageConfig, "http://www.google.com", "/tmp/randomdirectory/resultFile.pdf");
```

##### 2.2 PNG with custom page viewport size
  
```
EasyExportPageConfig pageConfig = new EasyExportPageConfig.Builder()
    .exportType(PageExportType.PNG)
    .pageViewportSize(1500, 1200);
File result = easyExporter.render(pageConfig, "http://www.google.com", "/tmp/randomdirectory/resultFile.png");
```
##### 2.3. PDF with custom paper size

Note: You can either define width + height, or format

##### 2.3.1. Width, height and margin
```
PagePaperSize pagePaperSize = new PagePaperSize.Builder()
    .withDimensions(new PageAttribute(140, mm), new PageAttribute(310, mm))
    .withPageMargin(new PageAttribute(10, PageUnit.px))
    .build();
EasyExportPageConfig pageConfig = new EasyExportPageConfig.Builder()
    .exportType(PageExportType.PDF)
    .pagePaperSize(pagePaperSize)
    .build();
File result = easyExporter.render(pageConfig, "http://www.google.com", "/tmp/randomdirectory/resultFile.pdf");
```

##### 2.3.2. Format
```
PagePaperSize pagePaperSize = new PagePaperSize.Builder()
    .withFormat(PageFormat.A4, PageOrientation.portrait)
    .build();
EasyExportPageConfig pageConfig = new EasyExportPageConfig.Builder()
    .exportType(PageExportType.PDF)
    .pagePaperSize(pagePaperSize)
    .build();
File result = easyExporter.render(pageConfig, "http://www.google.com", "/tmp/randomdirectory/resultFile.pdf");
```
##### 2.3.3. Header and footer

Both header and footer are represented the same by height and content in HTML format. If you want to include in your header/footer the current page number or the number of pages use the constants ```PageComponent.PAGE_NUM``` and ```PageComponent.NUM_PAGES``` when you compose your content text.
```
PagePaperSize pagePaperSize = new PagePaperSize.Builder()
    .withHeader(new PageComponent(new PageAttribute(30,PageUnit.px), "<div style='color:blue'>Header: current page number is: "+ PageComponent.PAGE_NUM + " from a number of " + PageComponent.NUM_PAGES + " pages <div>")
    .withFooter(new PageComponent(new PageAttribute(30,PageUnit.px), "<div style='color:red'>Footer: current page number is: "+ PageComponent.PAGE_NUM + " from a number of " + PageComponent.NUM_PAGES + " pages <div>")
    .build();
EasyExportPageConfig pageConfig = new EasyExportPageConfig.Builder()
    .exportType(PageExportType.PDF)
    .pagePaperSize(pagePaperSize)
    .build();
File result = easyExporter.render(pageConfig, "http://www.google.com", "/tmp/randomdirectory/resultFile.pdf");
```
##### 2.4. Basic authentication
```
EasyExportPageConfig pageConfig = new EasyExportPageConfig.Builder()
    .exportType(PageExportType.PDF)
    .withBasicAuthentication(new PageBasicAuthentication("username", "password"))
    .build();
File result = easyExporter.render(pageConfig, "http://www.google.com", "/tmp/randomdirectory/resultFile.pdf");
```

##### 2.5. Cookies
```
List<Cookie> cookieList = ... // List of javax.servlet.http.Cookie

EasyExportPageConfig pageConfig = new EasyExportPageConfig.Builder()
    .exportType(PageExportType.PDF)
    .withPageCookies(cookieList)
    .build();
File result = easyExporter.render(pageConfig, "http://www.google.com", "/tmp/randomdirectory/resultFile.pdf");
```

#### 3. Destroy
```
// Injected EasyExporter instance
private EasyExporter easyExporter;

easyExporter.destroy();
```
