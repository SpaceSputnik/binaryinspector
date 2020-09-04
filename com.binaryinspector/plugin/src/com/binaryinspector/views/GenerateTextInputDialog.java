package com.binaryinspector.views;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.binaryinspector.Activator;
import com.binaryinspector.decoders.DecodeUtils;
import com.binaryinspector.decoders.text.TextDecoder;
import com.binaryinspector.encoding.Encoding;

public class GenerateTextInputDialog extends TitleAreaDialog {
	private static final String PREF_ENTRY_VALUE = "compareDataText";
	private static final String PREF_ENTRY_CHARSET = "compareDataCharset";

	private StyledText inputText;
	private StyledText outputText;
	
	private Button charsetButton;
	private String charset;

	public GenerateTextInputDialog(Shell parentShell) {
		super(parentShell);
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID); 
		charset = prefs.get(PREF_ENTRY_CHARSET, "cp037");
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("Input");
		setMessage("Enter text and select charset", IMessageProvider.INFORMATION);
		setTitle("Input");
		UiUtils.setDialigInitialBounds(getShell());
		setHelpAvailable(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		final Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(layout);

		inputText = new StyledText(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		inputText.setWordWrap(true);
		
		inputText.setLayoutData(new GridData(GridData.FILL_BOTH));
		inputText.setFont(JFaceResources.getTextFont());
		
		IEclipsePreferences prefs1 = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID); 
		String s = prefs1.get(PREF_ENTRY_VALUE, "");
		inputText.setText(s);
		
		inputText.addModifyListener(new EntrySavingModifyListener(PREF_ENTRY_VALUE));
		inputText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				generate();
			}
		});
		
		charsetButton = new Button(container, SWT.PUSH);
		charsetButton.setText(charset);

		outputText = new StyledText(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		outputText.setEditable(false);
		outputText.setWordWrap(true);
		
		outputText.setFont(JFaceResources.getTextFont());
		outputText.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		charsetButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(
						getShell(), new LabelProvider());
				dialog.setElements(TextDecoder.getEnumValues());
				dialog.setTitle("Select Character Set");
				dialog.setImage(Activator.getImageDescriptor("icons/bytes.gif").createImage());
				if (dialog.open() != Window.OK) {
					return;
				}
				charset = (String)dialog.getResult()[0];
				charsetButton.setText(charset);
				container.layout(true, true);

				IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID); 
				prefs.put(PREF_ENTRY_CHARSET, charset);
				
				generate();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		generate();
		
		return area;
	}
	
	@Override
    protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
	}
	
	public void generate() {
		try {
			Encoding enc = Encoding.create(charset);
			byte b[] = enc.getBytes(inputText.getText());
			String hex = DecodeUtils.getHex(b, true);
			outputText.setText(hex);
		} catch (RuntimeException e) {
			String message = e.getMessage();
			outputText.setText(message == null ? "" : message);
		}
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}