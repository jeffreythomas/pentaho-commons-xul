package org.pentaho.ui.xul.gwt;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.gwt.widgets.client.utils.MessageBundle;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.gwt.util.EventHandlerWrapper;
import org.pentaho.ui.xul.impl.XulEventHandler;

import com.google.gwt.core.client.GWT;

public class GwtXulDomContainer implements XulDomContainer {

  Document document;
  Map<String, XulEventHandler> handlers = new HashMap<String, XulEventHandler>();
  Map<XulEventHandler, EventHandlerWrapper> handlerWrapers = new HashMap<XulEventHandler, EventHandlerWrapper>();
  GwtXulLoader loader;
  
  
  public void addDocument(Document document) {
    this.document = document;
  }

  public Document getDocumentRoot() {
    return document;
  }

  public XulDomContainer loadFragment(String xulLocation) throws XulException {
    // TODO Auto-generated method stub
    return null;
  }
  
  Map<String, XulEventHandler> eventHandlers = new HashMap<String, XulEventHandler>();
 
  //The following does not work outside of "hosted mode" 
  public void addEventHandler(XulEventHandler handler) {

    //ref: http://google-web-toolkit.googlecode.com/svn/javadoc/1.5/com/google/gwt/core/client/GWT.html#create(java.lang.Class)
    //ref: http://groups.google.com/group/Google-Web-Toolkit/browse_thread/thread/cf36c64ff48b3e19
    //ref: http://code.google.com/p/google-web-toolkit/issues/detail?id=2243
//    EventHandlerWrapper wrapper = GWT.create(handler.getClass());
//    
//    
//    handler.setXulDomContainer(this);
//    wrapper.setHandler(handler);
//    this.handlerWrapers.put(handler, wrapper);
//    this.handlers.put(handler.getName(), handler);
  }
  
  public void addEventHandler(EventHandlerWrapper wrapper){
    
    XulEventHandler handler = wrapper.getHandler();
    this.handlerWrapers.put(handler, wrapper);
    handler.setXulDomContainer(this);
    this.handlers.put(handler.getName(), handler);
    
  }

  public void addEventHandler(String id, String eventClassName) {
    throw new UnsupportedOperationException("use addEventHandler(XulEventHandler handler)");
  }
  
  public XulEventHandler getEventHandler(String key) throws XulException {
    return handlers.get(key);
  }

  public XulMessageBox createMessageBox(String message) {
    // TODO Auto-generated method stub
    return null;
  }

  public void initialize() {
    // TODO Auto-generated method stub

  }

  public void close() {
    // TODO Auto-generated method stub

  }


  public Document getDocument(int idx) {
    
    return document;
      
  }

  public Map<String, XulEventHandler> getEventHandlers() {
    return eventHandlers;
  }

  public Object getOuterContext() {
    return "";
  }


  private Object[] getArgs(String methodCall){
    if(methodCall.indexOf("()") > -1){
      return null;
    }
    String argsList = methodCall.substring(methodCall.indexOf("(")+1, methodCall.indexOf(")"));
    String[] stringArgs = argsList.split(",");
    Object[] args = new Object[ stringArgs.length ];
    int i=-1;
    for(String obj : stringArgs){
      i++;
      obj = obj.trim();
      try{
        Integer num = Integer.valueOf(obj);
        args[i] = num;
        continue;
      } catch(NumberFormatException e){
        try{
          Double num = Double.valueOf(obj);
          args[i] = num;
          continue;
        } catch(NumberFormatException e2){
          try{
            if(obj.indexOf('\'') == -1 && obj.indexOf('\"') == -1){
              throw new IllegalArgumentException("Not a string");
            }
            String str = obj.replaceAll("'", "");
            str = str.replaceAll("\"", "");
            args[i] = str;
            continue;
          } catch(IllegalArgumentException e4){
            try{
              Boolean flag = Boolean.parseBoolean(obj);
              args[i] = flag;
              continue;
            } catch(NumberFormatException e3){
              continue;
            }
          }
        }
      }
    }
    return args;
    
  }

  private Class unBoxPrimative(Class clazz){
    return clazz;
  }
  
  public Object invoke(String method, Object[] args) throws XulException {
    try {
      if (method == null || method.indexOf('.') == -1) {
        throw new XulException("method call does not follow the pattern [EventHandlerID].methodName()");
      }

      String eventID = method.substring(0, method.indexOf("."));
      String methodName = method.substring(method.indexOf(".")+1);
      
      Object[] arguments = getArgs(methodName);
      if(arguments != null){
        return invoke(method.substring(0,method.indexOf("("))+"()", arguments);
      } else {
        methodName = methodName.substring(0,methodName.indexOf("("));
      }
      
      EventHandlerWrapper wrapper = this.handlerWrapers.get(this.handlers.get(eventID));
      
      if(args.length > 0){
        Class[] classes = new Class[args.length];
        
        for(int i=0; i<args.length; i++){
          classes[i] = unBoxPrimative(args[i].getClass());
        }
        //Add in parameter support
        wrapper.execute(methodName);
        return null;
      } else {
        wrapper.execute(methodName);
        return null;
      }
    } catch (Exception e) {
      throw new XulException("Error invoking method: " + method, e);
    }
  }

  public void invokeLater(Runnable runnable) {
    
        // TODO Auto-generated method stub 
      
  }

  public boolean isClosed() {
    return false;
  }

  public boolean isRegistered(String widgetHandlerName) {
    return this.loader.isRegistered(widgetHandlerName);
  }

  public void loadFragment(String id, String src) throws XulException {
  }

  public void mergeContainer(XulDomContainer container) {
    
        // TODO Auto-generated method stub 
      
  }
  
  public void loadOverlay(com.google.gwt.xml.client.Document overlayDoc) throws XulException {
    XulDomContainer overlayContainer = this.loader.loadXul(overlayDoc);
    applyOverlay(overlayContainer.getDocumentRoot());
    
  }
  
  private void applyOverlay(Document overlay){
    for(XulComponent child : overlay.getChildNodes()){
      
      XulComponent sourceDocumentNodeMatch;
      if((sourceDocumentNodeMatch = this.document.getElementById(child.getId())) != null){
        sourceDocumentNodeMatch.adoptAttributes(child);
        
        for(XulComponent overlayChild : child.getChildNodes()){
          String position = overlayChild.getAttributeValue("position");
          String insertBefore = overlayChild.getAttributeValue("insertbefore");
          String insertAfter = overlayChild.getAttributeValue("insertafter");
          
          if(position != null){
            int pos = Integer.parseInt(position);
            sourceDocumentNodeMatch.addChildAt(overlayChild, pos);
          } else if(insertBefore != null){
            XulComponent relativeTo = document.getElementById(insertBefore);
            if(relativeTo != null && sourceDocumentNodeMatch.getChildNodes().contains(relativeTo)){
              int relativePos = sourceDocumentNodeMatch.getChildNodes().indexOf(relativeTo);
              relativePos--;
              Math.abs(relativePos);
              sourceDocumentNodeMatch.addChildAt(overlayChild, relativePos);
            } else {
              sourceDocumentNodeMatch.addChild(overlayChild);
            }
          } else if(insertAfter != null){
            XulComponent relativeTo = document.getElementById(insertBefore);
            if(relativeTo != null && sourceDocumentNodeMatch.getChildNodes().contains(relativeTo)){
              int relativePos = sourceDocumentNodeMatch.getChildNodes().indexOf(relativeTo);
              relativePos++;
              sourceDocumentNodeMatch.addChildAt(overlayChild, relativePos);
            } else {
              sourceDocumentNodeMatch.addChild(overlayChild);
            }
          } else {
            sourceDocumentNodeMatch.addChild(overlayChild);
          }
          
        }
        
      }
    }
  }
  
  public void loadOverlay(com.google.gwt.xml.client.Document overlayDoc, MessageBundle bundle) throws XulException {
    XulDomContainer overlayContainer = this.loader.loadXul(overlayDoc, bundle);
    applyOverlay(overlayContainer.getDocumentRoot());
    
  }
  
  public void removeOverlay(com.google.gwt.xml.client.Document overlayDoc) throws XulException {
    XulDomContainer overlayContainer = this.loader.loadXul(overlayDoc);
    
    for(XulComponent child : overlayContainer.getDocumentRoot().getChildNodes()){
      
      XulComponent insertedNode = document.getElementById(child.getId());
      insertedNode.getParent().removeChild(insertedNode);
    }
  }

  public void setOuterContext(Object context) {
    
        // TODO Auto-generated method stub 
      
  }

}