package com.binaryinspector.views;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.*;

import com.binaryinspector.Activator;
import com.binaryinspector.decoders.*;

public class ConfigureDecodersAction extends Action {
	private Shell shell;
	private boolean cancelled;
	
	public ConfigureDecodersAction(Shell shell) {
		super.setText("Configure Decoders...");
		setToolTipText("Configure decoders");
		setImageDescriptor(Activator.getImageDescriptor("icons/wrench_orange.png"));
		this.shell = shell;
	}
	
	@Override
	public void run() {
		ArrayList<Decoder> decodersCopy = DecodeUtils.decodeFactory.cloneDecoders();
		ConfigureDecodersDialog dialog = new ConfigureDecodersDialog(shell, decodersCopy);
		dialog.create();
		if (dialog.open() == Window.OK) {
			DecodeUtils.decodeFactory.saveDecoders(decodersCopy);
			cancelled = false;
		} else {
			cancelled = true;
		}
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}
}
