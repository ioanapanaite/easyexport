var server = require('webserver').create();
var system = require('system');
var cookiejar = require('cookiejar');
var webpage = require('webpage');
var fs = require('fs');
var port = system.args[1];
var logsPath = system.args[2];

server.listen(port, function(request, response) {
    var path = logsPath + '/phantomJs-log-port-' + system.args[1] + ".log";

    /***********  Parsing the parameters for rendering the page ***********/
    var params = parseUrl(request.url);
    fs.write(path, "RECEIVED -> " + JSON.stringify(params.config.pagePaperSize) +"\r\n",'a');

    /***********  Creating the page ***********/
    var page = webpage.create();

    /***********  Creating the cookie jar for the page ***********/
    var jar = cookiejar.create();
    if(params.config.pageCookies !== undefined) {
        params.config.pageCookies.forEach(function(cookie) {
            jar.addCookie({
                'name': cookie.name,
                'domain': cookie.domain,
                'path' : cookie.path,
                'value': cookie.value,
                'expires': cookie.maxAge
            });
        });
    }
    page.cookieJar = jar;


    /***********  Adding basic authentication if the case ***********/
    if(params.config.pageBasicAuthentication !== undefined) {
        page.settings.userName = params.config.pageBasicAuthentication.username;
        page.settings.password = params.config.pageBasicAuthentication.password;
    }

    /***********  Adding page viewport if the case, or default ***********/
    if(params.config.pageViewport === undefined) {
        page.viewportSize = {
            width: 1024 * 1.25,
            height: 768 * 1.25
        };
    } else {
        page.viewportSize = {
            width: params.config.pageViewport.width * 1.25,
            height: params.config.pageViewport.height * 1.25
        };
    }

    /***********  Adding page paper size if the case, or default ***********/
    if(params.config.pagePaperSize !== undefined) {
        page.paperSize = {
            width: params.config.pagePaperSize.width === undefined ? undefined : params.config.pagePaperSize.width.value + params.config.pagePaperSize.width.unit,
            height: params.config.pagePaperSize.height === undefined ? undefined : params.config.pagePaperSize.height.value + params.config.pagePaperSize.height.unit,
            format: params.config.pagePaperSize.format === undefined ? undefined : params.config.pagePaperSize.format,
            orientation: params.config.pagePaperSize.orientation === undefined ? undefined : params.config.pagePaperSize.orientation,
            margin : params.config.pagePaperSize.margin === undefined ? undefined : params.config.pagePaperSize.margin.value + params.config.pagePaperSize.margin.unit,
            header: params.config.pagePaperSize.header === undefined ? undefined :
                {
                    height: params.config.pagePaperSize.header.height.value + params.config.pagePaperSize.header.height.unit,
                    contents: phantom.callback(customComponent(decodeURIComponent(params.config.pagePaperSize.header.content)))
                },
            footer: params.config.pagePaperSize.footer === undefined ? undefined :
                {
                    height: params.config.pagePaperSize.footer.height.value + params.config.pagePaperSize.footer.height.unit,
                    contents: phantom.callback(customComponent(decodeURIComponent(params.config.pagePaperSize.footer.content)))
                }
        };
    } else {
        page.paperSize = {
            width: '210mm',
            height : '297mm',
            margin: '20px'
        };
    }

    /***********  Opening the page at the provided url and render it ***********/
    page.open(params.url, function (status) {
        if (status !== 'success') {
            response.statusCode = 500;
            response.write('');
            page.close();
            response.close();
            fs.write(path, "ERROR ON OPEN -> " + status + " = " + params.url +" \r\n",'a');
            return;
        } 
        setTimeout(function () {
            renderPage(page, response, params, path);
        }, 1000);
    }); 
});

/***********  Utilities ***********/
function parseUrl(url) {
    var query = {};
    var a = url.substring(url.indexOf('?') + 1).split('&');
    for (var i in a) {
        var b = a[i].split('=');
        query[decodeURIComponent(b[0])] = decodeURIComponent(b[1]);
    }   
    query.config = JSON.parse(query.config);
    query.config.pageCookies = JSON.parse(decodeURIComponent(query.config.pageCookiesString));
    return query;
}

function customComponent(custom) {
    custom = custom.replace('%pageNum%', '\" + pageNum + \"');
    custom = custom.replace('%numPages%', '\" + numPages + \"');
    return new Function('pageNum', 'numPages', 'return \"' + custom + '\";');
}

function renderPage(page, response, params, path) {
    try {
        page.evaluate(function (zoom) {
            document.body.style.zoom = zoom;
        }, 0.7);
        fs.write(path, "RENDERING -> " + params.url +" \r\n",'a');
        page.render(params.file);
        response.statusCode = 200;
        response.headers = {
            'Cache': 'no-cache',
            'Content-Type': 'text/plain',
            'port': system.args[1]
        };
        response.setEncoding('UTF-8');
        response.write(system.args[1]);
        fs.write(path, "SUCCESS -> " + params.url +" \r\n",'a');
    }
    catch (err) {
        fs.write(path, "ERROR -> " + err + " = " + params.url +" \r\n",'a');
        response.statusCode = 500;
        response.headers = {
            'Cache': 'no-cache',
            'Content-Type': 'text/plain',
            'port': system.args[1]
        };
        response.setEncoding('UTF-8');
        response.write(err);
    }
    finally {
        page.close();
        response.close();
    }
}

