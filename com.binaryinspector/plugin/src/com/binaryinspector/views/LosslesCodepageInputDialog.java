package com.binaryinspector.views;

import org.eclipse.core.runtime.preferences.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import com.binaryinspector.Activator;
import com.binaryinspector.decoders.DecodeUtils;
import com.binaryinspector.encoding.Encoding;

public class LosslesCodepageInputDialog extends TitleAreaDialog {
	private static final String PREF_ENTRY_LABEL = "losslesCodepageTestText";

	private StyledText losslessTestText;
	private StyledText codepageNamesOutput;
	private StyledText logText;
	private Button testButton;
	
	public LosslesCodepageInputDialog(Shell parentShell) {
		super(parentShell);
		setTitle("Input");
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("Find Losseless Charsets");
		setMessage("Enter test text and press \"Find suitable charsets\" button", IMessageProvider.INFORMATION);
		setTitle("Input");
		UiUtils.setDialigInitialBounds(getShell());
		setHelpAvailable(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		
		SashForm sashForm = new SashForm(area, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite containerTop = new Composite(sashForm, SWT.NONE);
		containerTop.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(2, false);
		containerTop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		containerTop.setLayout(layout);

		Composite containerBottom = new Composite(sashForm, SWT.NONE);
		containerBottom.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new GridLayout(2, false);
		containerBottom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		containerBottom.setLayout(layout);
		
		new Label(containerTop, SWT.NO_TRIM).setText("Test text");
		losslessTestText = createText(containerTop, PREF_ENTRY_LABEL, true, 2);
		
		testButton = new Button(containerBottom, SWT.PUSH);
		testButton.setText("Find suitable charsets");
		testButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				findCodepages(losslessTestText.getText());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		logText = createText(containerBottom, null, true, 2);
		logText.setWordWrap(false);
		
		new Label(containerBottom, SWT.NO_TRIM).setText("These charsets can be used to store this text without a data loss");
		codepageNamesOutput = createText(containerBottom, null, true, 2);
		codepageNamesOutput.setEditable(false);
		
		sashForm.setWeights(new int[] {20, 80});
		return area;
	}

	protected void findCodepages(String text) {
		Cursor oldCursor = getShell().getCursor();
		try {
			getShell().setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_WAIT));
			
			logText.setText("");
			for (String name : Encoding.getNames()) {
				Encoding enc = Encoding.create(name);
				byte [] bytes = enc.getBytes(text);
				String rountripResult = enc.getString(bytes);
				boolean ok = rountripResult.equals(text);
				String hex = DecodeUtils.getHex(bytes, true);
				
				StyleRange styleRange = new StyleRange();
				styleRange.start = logText.getCharCount();
				styleRange.background = null;
				styleRange.fontStyle = SWT.NORMAL;
				styleRange.foreground = getShell().getDisplay().getSystemColor(ok ? SWT.COLOR_BLUE : SWT.COLOR_RED);
				
				String label = name + (! enc.isSingleByte() ? "*" : "");
				logText.append(label);
				styleRange.length = logText.getCharCount() - styleRange.start;
				logText.setStyleRange(styleRange);
	
				logText.append(": \"" + text + "==>" + hex + "==>\"" + rountripResult + "\" ");
				
				styleRange = new StyleRange();
				styleRange.start = logText.getCharCount();
				styleRange.background = null;
				styleRange.fontStyle = SWT.NORMAL;
				
				if (ok) {
					codepageNamesOutput.append(label);
					codepageNamesOutput.append("\n");
					logText.append("OK");
					styleRange.foreground = getShell().getDisplay().getSystemColor(SWT.COLOR_BLUE);
				} else {
					styleRange.foreground = getShell().getDisplay().getSystemColor(SWT.COLOR_RED);
					logText.append("Data loss!");
				}
				logText.append("\n");
				styleRange.length = logText.getCharCount() - styleRange.start;
				logText.setStyleRange(styleRange);
			}
			if (codepageNamesOutput.getCharCount() == 0) {
				codepageNamesOutput.setText("No character sets found");
			}
		} finally {
			getShell().setCursor(oldCursor);			
		}
	}

	private StyledText createText(Composite container, String prefEntry, boolean wrap, int horisontalSpan) {
		GridData ld = new GridData();
		ld.grabExcessHorizontalSpace = true;
		ld.grabExcessVerticalSpace = wrap;
		ld.horizontalAlignment = GridData.FILL;
		ld.verticalAlignment = GridData.FILL;
		ld.horizontalSpan = horisontalSpan;
		ld.heightHint = 0;

		int style = SWT.BORDER;
		if (wrap) {
			style |= (SWT.H_SCROLL | SWT.V_SCROLL);
		}
		style |= wrap ? SWT.H_SCROLL | SWT.V_SCROLL : SWT.SINGLE;
		
		StyledText text = new StyledText(container, style);
		if (wrap) {
			text.setWordWrap(true);
		} 
		text.setFont(JFaceResources.getTextFont());		
		
		text.setLayoutData(ld);

		if (prefEntry != null) {
			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID); 
			String s = prefs.get(prefEntry, "");
			text.setText(s);
			text.addModifyListener(new EntrySavingModifyListener(prefEntry));
		}
		return text;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
	}
}