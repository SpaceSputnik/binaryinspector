package com.binaryinspector.views;


import java.util.ResourceBundle;

import org.eclipse.jface.action.*;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.FindReplaceAction;


public class MdaView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.binaryinspector.MdaView";
	private MdaComposite mdaComp;

	/**
	 * The constructor.
	 */
	public MdaView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		mdaComp = new MdaComposite(this, parent, SWT.NONE);
		
		makeActions();
		contributeToActionBars();
	}

	public void setFocus() {
		mdaComp.setFocus();
	}
	
	private void fillLocalPullDown(IMenuManager manager) {
		IAction[] actions = mdaComp.getPulldownActions();
		for (IAction action : actions) {
			if (action == null) {
				manager.add(new Separator());
			} else {
				manager.add(action);
			}
		}
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		for (IAction a : mdaComp.getToolbarActions()) {
			manager.add(a);
		}
	}	
	
	private void makeActions() {
        ResourceBundle bundle = ResourceBundle.getBundle("org.eclipse.ui.texteditor.ConstructedTextEditorMessages");
        FindReplaceAction findReplaceAction = new FindReplaceAction(bundle, "Editor.FindReplace.", this);
        findReplaceAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE);
        
		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(), findReplaceAction);
 	}
	
	public void setStatisLine(String message, boolean error) {
		IStatusLineManager slm = getViewSite().getActionBars().getStatusLineManager();
		slm.setErrorMessage(null);
		if (error) {
			slm.setErrorMessage(message);
		} else {
			slm.setMessage(message);
		}
	}
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (IFindReplaceTarget.class.equals(adapter)) {
			return mdaComp.getFindReplaceAdapter();
		}
		return super.getAdapter(adapter);
	}
}