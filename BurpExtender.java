package burp;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;

public class BurpExtender implements IBurpExtender
{

    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
	private PrintWriter stdout;
	
	
	@Override
	public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks)
	{
        
        // keep a reference to our callbacks object
        this.callbacks = callbacks;
        helpers = callbacks.getHelpers();
        
        stdout = new PrintWriter(callbacks.getStdout(), true);
        
        // set our extension name
        callbacks.setExtensionName("Save body to file");
        
        // register context menu
        callbacks.registerContextMenuFactory(new ContextMenu(helpers, stdout));
	
	}    
}

class ContextMenu implements IContextMenuFactory {
    
	private final IExtensionHelpers helpers;
	private PrintWriter stdout;
	private boolean req;
	
    public ContextMenu(IExtensionHelpers helpers, PrintWriter stdout) 
    {
        this.helpers = helpers;
        this.stdout = stdout;
    }

    public List<JMenuItem> createMenuItems(final IContextMenuInvocation invocation) {
        List<JMenuItem> menus = new ArrayList();
                
		byte invocationContext = invocation.getInvocationContext();
		
		req = false;
		
		if(invocationContext==invocation.CONTEXT_MESSAGE_EDITOR_REQUEST || invocationContext == invocation.CONTEXT_MESSAGE_VIEWER_REQUEST)
		{
			req = true;
		}
		else if(invocationContext==invocation.CONTEXT_MESSAGE_EDITOR_RESPONSE || invocationContext == invocation.CONTEXT_MESSAGE_VIEWER_RESPONSE)
		{
			req = false;
		}      
        
        JMenuItem saveBodyToFile = new JMenuItem("Save body to file");

        saveBodyToFile.addMouseListener(new MouseListener() {

	        public void mouseClicked(MouseEvent arg0) {	
	        }
		
	        public void mouseEntered(MouseEvent arg0) {
	        }	
	
	        public void mouseExited(MouseEvent arg0) {
	        }	
	
	        public void mousePressed(MouseEvent arg0) {	
	        }

	        public void mouseReleased(MouseEvent arg0) {
	            
	        	IHttpRequestResponse iReqResp = invocation.getSelectedMessages()[0];
	            
	            byte[] bytes;
	            byte[] body;
	            String filename = "";
        		
            	// get request bytes
            	bytes = iReqResp.getRequest();
            	
            	// analyze request
        		IRequestInfo reqInfo = helpers.analyzeRequest(bytes);
        		
        		// get first line of headers
        		String fh = reqInfo.getHeaders().get(0);
        		
        		//split on space, second part should be path
	            String pieces[] = fh.split("\\s+");
	            
	            if(!pieces[1].startsWith("/"))
	            	pieces[1] = "/" + pieces[1];
	            
	            if(pieces[1].length() > 1) // if this is equal to / we will cause a nullpointerexception
	            {	            
		            // build fake url
		            String url = "http://whatever.com" + pieces[1];
		            
		            // get filename
		            try {
						filename = Paths.get(new URI(url).getPath()).getFileName().toString();
					} catch (Exception e) {
						e.printStackTrace(stdout);
					}
	            }
        		
	            if(req)
	            {        		
	        		// set body
	        		body = Arrays.copyOfRange(bytes, reqInfo.getBodyOffset(), bytes.length);
	            }
	            else
	            {	            	
	            	// response
	            	bytes = iReqResp.getResponse();
	            	
	            	// analyze response
	        		IResponseInfo respInfo = helpers.analyzeResponse(bytes);
	        		
	        		// set body
	        		body = Arrays.copyOfRange(bytes, respInfo.getBodyOffset(), bytes.length);
	        		
	        		// search headers for filename suggestion
	        		List<String> headers = respInfo.getHeaders();
	        		
	        		for(String header : headers)
	        		{
	        			if(header.startsWith("Content-Disposition:"))
	        			{
	        				// use regex to get filename, best effort
	        				Pattern pattern = Pattern.compile("filename\\*?=['\"]?([^'\";\\n]*'')?([^'\";\\n]*)", Pattern.CASE_INSENSITIVE);
	        				Matcher matcher = pattern.matcher(header);
	                		
	        				// best effort: take last match as suggestion
	                		while (matcher.find())
	                		{
	                			filename = matcher.group(2);	                			
	                		}
	        			}
	        		}
	            }
	            
            	if(body.length > 0)
            	{
            		// show save file dialog
            		final JFileChooser fc = new JFileChooser();
            		if(filename.length() > 0)
            			fc.setSelectedFile(new File(filename));
            		int returnVal = fc.showSaveDialog(null);
            		if (returnVal == JFileChooser.APPROVE_OPTION)
            		{ // write file
            			try (FileOutputStream fos = new FileOutputStream(fc.getSelectedFile())) {
            			    fos.write(body);
            			    fos.close();
            			} catch (IOException ioe) {
            			    ioe.printStackTrace();
            			}
            		}
            	} 
	        }
        });

        menus.add(saveBodyToFile);

        return menus;
    }

}
