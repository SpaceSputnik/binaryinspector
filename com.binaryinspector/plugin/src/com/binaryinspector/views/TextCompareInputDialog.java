package com.binaryinspector.views;

import org.eclipse.core.runtime.preferences.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import com.binaryinspector.Activator;

public class TextCompareInputDialog extends TitleAreaDialog {
	private static final String PREF_ENTRY_LEFT_LABEL = "compareTextLeftLabel";
	private static final String PREF_ENTRY_RIGHT_LABEL = "compareTextRightLabel";	
	private static final String PREF_ENTRY_LEFT = "compareTextLeft";
	private static final String PREF_ENTRY_RIGHT = "compareTextRight";

	private StyledText leftLabel;
	private StyledText rightLabel;
	
	private StyledText leftText;
	private StyledText rightText;
	
	private String leftDataLabel;
	private String rightDataLabel;

	private String leftData;
	private String rightData;
	
	public TextCompareInputDialog(Shell parentShell) {
		super(parentShell);
		setTitle("Input");
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("Input");
		setMessage("Enter text to compare", IMessageProvider.INFORMATION);
		setTitle("Input");
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

		new Label(containerTop, SWT.NO_TRIM).setText("Left Label");
		leftLabel = createText(containerTop, PREF_ENTRY_LEFT_LABEL, false, 1);
		leftText = createText(containerTop, PREF_ENTRY_LEFT, true, 2);
		
		Composite containerBottom = new Composite(sashForm, SWT.NONE);
		containerBottom.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new GridLayout(2, false);
		containerBottom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		containerBottom.setLayout(layout);
		
		new Label(containerBottom, SWT.NO_TRIM).setText("Right Label");
		rightLabel = createText(containerBottom, PREF_ENTRY_RIGHT_LABEL, false, 1);
		rightText = createText(containerBottom, PREF_ENTRY_RIGHT, true, 2);
		return area;
	}

	private StyledText createText(Composite container, String prefEntry, boolean wrap, int horisontalSpan) {
		GridData ld = new GridData();
		ld.grabExcessHorizontalSpace = true;
		ld.grabExcessVerticalSpace = wrap;
		ld.horizontalAlignment = GridData.FILL;
		ld.verticalAlignment = GridData.FILL;
		ld.horizontalSpan = horisontalSpan;

		int style = SWT.BORDER;
		if (wrap) {
			style |= (SWT.H_SCROLL | SWT.V_SCROLL);
		}
		style |= wrap ? SWT.H_SCROLL | SWT.V_SCROLL : SWT.SINGLE;
		
		StyledText text = new StyledText(container, style);
		if (wrap) {
			text.setWordWrap(true);
			text.setFont(JFaceResources.getTextFont());		
		} 
		
		text.setLayoutData(ld);

		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID); 
		String s = prefs.get(prefEntry, "");
		text.setText(s);
		
		text.addModifyListener(new EntrySavingModifyListener(prefEntry));
		return text;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	// save content of the Text fields because they get disposed
	// as soon as the Dialog closes
	private void saveInput() {
		leftDataLabel = leftLabel.getText();
		rightDataLabel = rightLabel.getText();
		leftData = leftText.getText();
		rightData = rightText.getText();
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	public String getLeftData() {
		return leftData;
	}

	public String getRightData() {
		return rightData;
	}
	
	public String getLeftDataLabel() {
		return leftDataLabel;
	}

	public String getRightDataLabel() {
		return rightDataLabel;
	}
	
	public static void saveData(String left, String rigth) {
		EntrySavingModifyListener.save(PREF_ENTRY_LEFT, left);
		EntrySavingModifyListener.save(PREF_ENTRY_RIGHT, rigth);
	}
}