package com.binaryinspector.views;

import org.eclipse.core.runtime.preferences.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Button;

import com.binaryinspector.Activator;

public class EntrySavingSelectionListener implements SelectionListener {
	private final String entryName;
	
	public EntrySavingSelectionListener(String entryName) {
		this.entryName = entryName;
	}
	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.widget instanceof Button) {
			save(entryName, ((Button)e.widget).getSelection());
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}
	
	public static void save(String entryName, boolean selected) {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID); 
		prefs.put(entryName, String.valueOf(selected));
	}
}
