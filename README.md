This is a small BurpSuite plugin that allows you to save a request or response body to a file.

By default, BurpSuite offers two options to save request/response data to files:
- copy to file: saves the content but includes the headers
- save to file: saves the request and response in an XML style format

This plugin enables you to save just the body to a file. The plugin will suggest a filename based on Content-Disposition (responses only) or the file that was requested from the server.
