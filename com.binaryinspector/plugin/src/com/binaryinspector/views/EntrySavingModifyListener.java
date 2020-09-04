package com.binaryinspector.views;

import org.eclipse.core.runtime.preferences.*;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

import com.binaryinspector.Activator;

class EntrySavingModifyListener implements ModifyListener {
	private final String entryName;
	
	public EntrySavingModifyListener(String entryName) {
		this.entryName = entryName;
	}
	
	@Override
	public void modifyText(ModifyEvent e) {
		Object source = e.getSource();
		String text = null;
		if (source instanceof StyledText) {
			text = ((StyledText)source).getText();
		} else if (source instanceof Text) {
			text = ((Text)source).getText();
		} else if (source instanceof Combo) {
			text = ((Combo)source).getText(); 
		}
		if (text == null) {
			return;
		}
		save(entryName, text);
	}

	public static void save(String entryName, String text) {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID); 
		prefs.put(entryName, text);
	}
}