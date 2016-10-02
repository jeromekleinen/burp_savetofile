This is a small BurpSuite plugin that allows you to save a request or response body to a file.

By default, BurpSuite offers two options to save request/response data to files:
- copy to file: saves the content but includes the headers
- save to file: saves the request and response in an XML style format

This plugin enables you to save just the body to a file. The plugin will suggest a filename based on Content-Disposition (responses only) or the file that was requested from the server.

Usage: right click in a request or response viewer/editor and click on "Save body to file" and select an output file using the standard dialog. If there is no body that can be saved, the dialog will simply not be shown.

This plugin was only tested against BurpSuite 1.7.07 on Windows 10 but should work on a wide variety of versions and platforms.

If you encounter any problems, please file an issue and I'll try to look into it.
