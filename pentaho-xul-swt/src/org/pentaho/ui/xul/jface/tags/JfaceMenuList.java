package org.pentaho.ui.xul.jface.tags;

import java.beans.Expression;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.containers.XulMenupopup;
import org.pentaho.ui.xul.dom.Element;
import org.pentaho.ui.xul.swt.AbstractSwtXulContainer;

//TODO: Move creation of combobox to late initialization to support switching from editable to non-editable.
public class JfaceMenuList<T> extends AbstractSwtXulContainer implements XulMenuList<T> {

  private Combo combobox;

  private XulDomContainer xulDomContainer;

  private static final Log logger = LogFactory.getLog(JfaceMenuList.class);

  private String binding;

  private Object previousSelectedItem = null;
  
  private JfaceMenupopup popup;
  
  private JfaceMenuitem selectedItem = null;

  private boolean editable = false;
  
  private String command;

  private XulComponent parent;
  private Collection<T> elements;

  public JfaceMenuList(Element self, XulComponent parent, XulDomContainer domContainer, String tagName) {
    super("menulist");

    this.xulDomContainer = domContainer;
    this.parent = parent;
    setupCombobox();

  }


  private void setupCombobox(){


    if(editable){
      combobox = new Combo((Composite)parent.getManagedObject(), SWT.DROP_DOWN);
    } else {
      combobox = new Combo((Composite)parent.getManagedObject(), SWT.DROP_DOWN | SWT.READ_ONLY);
    }

    setManagedObject(combobox);

    combobox.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {

      public void widgetSelected(SelectionEvent e) {
        fireSelectedEvents();

      }

    });
  }
  

  @Override
  public void addChild(Element ele) {
    super.addChild(ele);
    if(ele instanceof XulMenupopup){
      popup = (JfaceMenupopup) ele;
      for( XulComponent child : popup.getChildNodes() ) {
    	  if( child instanceof JfaceMenuitem ) {
    		  if(((JfaceMenuitem) child).isSelected()) {
   				  setSelectedItem((T) child);
    		  }
    	  }
      }
    }
  }


  public void layout() {
    combobox.removeAll();
    selectedItem = null; //clear selection
    
    for (XulComponent item : popup.getChildNodes()) {
    	JfaceMenuitem mItem = (JfaceMenuitem) item;
      if(mItem.isSelected()){
        this.selectedItem = mItem;
      }
      if(mItem.isVisible()){	
    	  combobox.add(mItem.getLabel());
      }
    }
    int idx = -1;
    if(selectedItem != null){
      idx = combobox.indexOf(selectedItem.toString());
    } else if( popup.getChildNodes().size() > 0){
      idx = 0;
    }
    this.setSelectedIndex(idx);
    this.combobox.update();
  }

  /*
   * Swaps out the managed list.  Effectively replaces the SwingMenupopup child component.
   * (non-Javadoc)
   * @see org.pentaho.ui.xul.components.XulMenuList#replaceAll(java.util.List)
   */
  @Deprecated
  public void replaceAllItems(Collection<T> tees) {
    setElements(tees);
  }

  public String getSelectedItem() {
    int idx = combobox.getSelectionIndex();
    return (idx > -1 && idx < this.combobox.getItemCount())? this.combobox.getItem(idx) : null;
  }

  public void setSelectedItem(T t) {
    if(t == null){
      this.combobox.deselectAll();
      return;
    }
    if(this.elements != null){
      this.combobox.select(new ArrayList(this.elements).indexOf(t));
    } else {
      this.combobox.select(combobox.indexOf(t.toString()));
    }
    this.previousSelectedItem = null;
  }

  public void setOncommand(final String command) {
    this.command = command;
  }

  public Collection<T> getElements() {
    return (Collection) popup.getChildNodes();
  }

  public String getBinding() {
    return binding;
  }

  public void setBinding(String binding) {
    this.binding = binding;
  }

  private String extractLabel(T t) {
    String attribute = getBinding();
    if (StringUtils.isEmpty(attribute)) {
      return t.toString();
    } else {
      String getter = "get" + (String.valueOf(attribute.charAt(0)).toUpperCase()) + attribute.substring(1);
      try {
        return new Expression(t, getter, null).getValue().toString();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void setElements(Collection<T> tees) {
	  
	    String selected = this.getSelectedItem();
	    this.elements = tees;
    
    for (XulComponent menuItem : popup.getChildNodes()) {
      popup.removeChild(menuItem);
    }

    if(tees == null || tees.size() == 0){
      return;
    }
    
    List<XulComponent> items = popup.getChildNodes();
    Map<String,JfaceMenuitem> itemMap = new HashMap<String,JfaceMenuitem>();
    for( XulComponent item : items ) {
    	if( item instanceof JfaceMenuitem ) {
        	itemMap.put(((JfaceMenuitem) item).getLabel(), (JfaceMenuitem) item);
    	}
    }
    for (T t : tees) {
      try{
    	  String label = extractLabel(t);
    	  if( itemMap.get(label) != null ) {
    		  // make it visible
    		  itemMap.get(label).setVisible(true);
    	  } else {
    	        XulMenuitem item = (XulMenuitem) xulDomContainer.getDocumentRoot().createElement("menuitem");

    	        String attribute = getBinding();
    	        item.setLabel( label );
    	  
    	        popup.addChild(item);
    	  }
      } catch(XulException e){
        logger.error("Unable to create new menulist menuitem: ", e);
      }
    }

    if( selected != null ) {
    	Collection<T> elist = getElements();
    	for( T t : elist ) {
    		if( selected.equals(t.toString() )) {
    	    	setSelectedItem(t);
    		}
    	}
    }
    layout();
  }
  public int getSelectedIndex() {
    return this.combobox.getSelectionIndex();
  }

  public void setSelectedIndex(int idx) {
    if(idx == -1){
      this.combobox.clearSelection();
    } else {
      this.combobox.select(idx);
    }
    fireSelectedEvents();
  }
  
  @Override
  public void removeChild(Element ele) {
	    String selected = this.getSelectedItem();
	  super.removeChild(ele);
	    if( selected != null ) {
	    	Collection<T> elist = getElements();
	    	for( T t : elist ) {
	    		if( selected.equals(t.toString() )) {
	    	    	setSelectedItem(t);
	    		}
	    	}
	    }
	  
  }

  private void fireSelectedEvents(){
    int idx = getSelectedIndex();
    if(idx >= 0){
      Object newSelectedItem = (idx >= 0) ? (elements != null)? elements.toArray()[idx] : getSelectedItem() : getSelectedItem();

      changeSupport.firePropertyChange("selectedItem",
          previousSelectedItem, newSelectedItem
      );
      this.previousSelectedItem = newSelectedItem;
    }
    changeSupport.firePropertyChange("selectedIndex", null
        , combobox.getSelectionIndex());

    if(JfaceMenuList.this.command != null
        && getDocument() != null){    //make sure we're on the dom tree (initialized)
      invoke(JfaceMenuList.this.command, new Object[] {});
    }
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  public boolean getEditable() {
    return editable;
  }

  public String getValue() {
    return getSelectedItem();
  }

  public void setValue(String value) {
    combobox.setText(value);
  }


  @Override
  public void setDisabled(boolean disabled) {
    super.setDisabled(disabled);
    combobox.setEnabled(!disabled);
  }
  
  
}
