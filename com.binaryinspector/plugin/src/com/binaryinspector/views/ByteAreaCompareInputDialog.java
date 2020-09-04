package com.binaryinspector.views;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.binaryinspector.Activator;

public class ByteAreaCompareInputDialog extends TitleAreaDialog {
	private static final String PREF_ENTRY = "compareData";

	private StyledText text;

	private String data;
	
	private final boolean hex;

	public ByteAreaCompareInputDialog(Shell parentShell, boolean hex) {
		super(parentShell);
		this.hex = hex;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("Input");
		setMessage("Enter hex data or paste base64Binary data. This data will be compared with the current data in the view.", 
				IMessageProvider.INFORMATION);
		setTitle("Input");
		UiUtils.setDialigInitialBounds(getShell());
		setHelpAvailable(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(2, false);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(layout);

		createText(container);
		return area;
	}

	private void createText(Composite container) {
		GridData ld = new GridData();
		ld.grabExcessHorizontalSpace = true;
		ld.grabExcessVerticalSpace = true;
		ld.horizontalAlignment = GridData.FILL;
		ld.verticalAlignment = GridData.FILL;

		text = new StyledText(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		text.setWordWrap(true);
		
		text.setLayoutData(ld);
		text.setFont(JFaceResources.getTextFont());

		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID); 
		String s = prefs.get(PREF_ENTRY, "");
		text.setText(s);

		if (hex) {
			Base64ConvertingListener.add(text);
		}
		text.addModifyListener(new EntrySavingModifyListener(PREF_ENTRY));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	// save content of the Text fields because they get disposed
	// as soon as the Dialog closes
	private void saveInput() {
		data = text.getText();
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	public String getData() {
		return data;
	}
}