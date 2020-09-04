package com.binaryinspector.views;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

import com.binaryinspector.Activator;
import com.binaryinspector.decoders.*;

final class Base64ConvertingListener implements ModifyListener, VerifyListener {
	private final StyledText text;
	private boolean enableAutoConvert = false;
	private boolean disarmAutoConvert = false;
	
	Base64ConvertingListener(StyledText text) {
		this.text = text;
	}
	
	@Override
	public void verifyText(VerifyEvent e) {
		enableAutoConvert = e.text.length() > 1;
	}

	@Override
	public void modifyText(ModifyEvent e) {
		if (disarmAutoConvert || ! enableAutoConvert) {
			disarmAutoConvert = false;
			return;
		}
		String s = text.getText();
		try {
			DecodeUtils.parseHexString(s);
		} catch (EnvelopeException ex) {
			// base64?
			try {
				byte[] b = DatatypeConverter.parseBase64Binary(s);
				if (b != null && b.length != 0) {
					MessageBox dialog = new MessageBox(text.getShell(), SWT.ICON_QUESTION | SWT.OK| SWT.CANCEL);
					dialog.setText("Confirm");
					dialog.setMessage("Decode base64Binary ?");
					if (SWT.OK == dialog.open()) { 
						convertBase64(text);
					}
				}
			} catch (IllegalArgumentException exx) {
				// nope, not base64
			}
		}
	}
	
	public static void convertBase64(StyledText text) {
		try {
			byte [] bytes = DatatypeConverter.parseBase64Binary(text.getText());
			text.setText(DecodeUtils.getHex(bytes, true).toUpperCase());
		} catch (Exception e) {
	        Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, e.getMessage(), e);
	        //ILogger logger = Policy.getLog();
	        //logger.log(status);
	        ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Could not convert from base64Binary", status);
		}
	}
	
	public void convertToBase64(StyledText text) {
		try {
			byte [] bytes = DecodeUtils.parseHexString(text.getText());
			String base64 = DatatypeConverter.printBase64Binary(bytes);
			disarmAutoConvert = true;
			text.setText(base64);
		} catch (Exception e) {
	        Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, e.getMessage(), e);
	        //ILogger logger = Policy.getLog();
	        //logger.log(status);
	        ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Could not convert to base64Binary", status);
		}
	}
	
	public static Base64ConvertingListener add(StyledText text) {
		Base64ConvertingListener listener = new Base64ConvertingListener(text);
		text.addVerifyListener(listener);
		text.addModifyListener(listener);
		return listener;
	}
	
	public void disarmAutoConvert() {
		disarmAutoConvert = true;
	}
	
}