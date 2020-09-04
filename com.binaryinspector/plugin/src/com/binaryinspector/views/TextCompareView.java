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


public class TextCompareView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.binaryinspector.TextCompareView";
	private TextCompareComposite rootComposite;

	/**
	 * The constructor.
	 */
	public TextCompareView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		rootComposite = new TextCompareComposite(parent, SWT.NONE);
		
		makeActions();
		contributeToActionBars();
	}

	public void setFocus() {
		rootComposite.setFocus();
	}
	
	private void fillLocalPullDown(IMenuManager manager) {
		IAction[] actions = new IAction[0];
		for (IAction action : actions) {
			if (action == null) {
				manager.add(new Separator());
			} else {
				manager.add(action);
			}
		}
		manager.add(new Separator());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		IAction[] actions = new IAction[0];
		for (IAction a : actions) {
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
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (IFindReplaceTarget.class.equals(adapter)) {
			return rootComposite.getFindReplaceAdapter();
		}
		return super.getAdapter(adapter);
	}
	
}