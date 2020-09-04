package com.binaryinspector.views;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.binaryinspector.Activator;

public class TextCompareComposite extends Composite {
	private static final String PREF_ENTRY_LEFT_LABEL = "compareTextLeftLabel";
	private static final String PREF_ENTRY_RIGHT_LABEL = "compareTextRightLabel";	
	private static final String PREF_ENTRY_LEFT = "compareTextLeft";
	private static final String PREF_ENTRY_RIGHT = "compareTextRight";
	private static final String AS_DIALOG_ENTRY = "asDialog";

	private StyledText leftLabel;
	private StyledText rightLabel;
	
	private StyledText leftText;
	private StyledText rightText;
	
	private String leftDataLabel;
	private String rightDataLabel;
	
	private boolean leftTextFocused;
	private boolean rightTextFocused;
	
	private Button compareButton;
	
	private TextCompareAction compareAction = new TextCompareAction();

	
	private FindReplaceStyledTextAdapter leftTextFindReplaceAdapter;
	private FindReplaceStyledTextAdapter rightTextFindReplaceAdapter;
	private Button editorRadioButton;
	private Button dialogRadioButton;
	
	public TextCompareComposite(Composite parent, int style) {
		super(parent, style | SWT.NO_TRIM);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.minimumHeight = 0;
		layoutData.minimumWidth = 0;
		setLayoutData(layoutData);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);
		
		SashForm sashForm = new SashForm(this, SWT.VERTICAL);
		layoutData = new GridData(GridData.FILL_BOTH);
		
		sashForm.setLayoutData(layoutData);
		
		Composite containerTop = new Composite(sashForm, SWT.NONE);
		containerTop.setLayoutData(layoutData);
		layout = new GridLayout(2, false);
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		containerTop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		containerTop.setLayout(layout);

		new Label(containerTop, SWT.NO_TRIM).setText("Left Label");
		leftLabel = createText(containerTop, PREF_ENTRY_LEFT_LABEL, false, 1);
		leftText = createText(containerTop, PREF_ENTRY_LEFT, true, 2);
		
		Composite containerBottom = new Composite(sashForm, SWT.NONE);
		containerBottom.setLayoutData(layoutData);
		layout = new GridLayout(2, false);
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		containerBottom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		containerBottom.setLayout(layout);
		
		leftText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				leftTextFocused = true;
				rightTextFocused = false;
			}
		});
		
		new Label(containerBottom, SWT.NO_TRIM).setText("Right Label");
		rightLabel = createText(containerBottom, PREF_ENTRY_RIGHT_LABEL, false, 1);
		rightText = createText(containerBottom, PREF_ENTRY_RIGHT, true, 2);
		
		leftTextFindReplaceAdapter = new FindReplaceStyledTextAdapter(leftText) {
			@Override
			public boolean isEditable() {
				return true;
			}
		};
		
		rightTextFindReplaceAdapter = new FindReplaceStyledTextAdapter(rightText) {
			@Override
			public boolean isEditable() {
				return true;
			}
		};

		rightText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				rightTextFocused = true;
				leftTextFocused = true;
			}
		});

		
	    Composite buttonComp = new Composite(this, SWT.NULL);
	    buttonComp.setLayout(new GridLayout(3, false));
		
		compareButton = new Button(buttonComp, SWT.PUSH);
		compareButton.setText("Compare");
		compareButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				compareAction.run();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

    
	    editorRadioButton = new Button(buttonComp, SWT.RADIO);
	    editorRadioButton.setText("Editor");
	    dialogRadioButton = new Button(buttonComp, SWT.RADIO);
	    dialogRadioButton.setText("Dialog");
	    
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID); 
		String s = prefs.get(AS_DIALOG_ENTRY, Boolean.FALSE.toString());
		boolean asDialog = Boolean.TRUE.toString().equals(s);
		editorRadioButton.setSelection(! asDialog);
		dialogRadioButton.setSelection(asDialog);
		
		layout(true, true);
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
		} 
		text.setFont(JFaceResources.getTextFont());
		
		text.setLayoutData(ld);

		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID); 
		String s = prefs.get(prefEntry, "");
		text.setText(s);
		
		text.addModifyListener(new EntrySavingModifyListener(prefEntry));
		return text;
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
	
	public FindReplaceStyledTextAdapter getFindReplaceAdapter() {
		if (leftTextFocused) {
			return leftTextFindReplaceAdapter;
		} else if (rightTextFocused) {
			return rightTextFindReplaceAdapter;
		}
		return null;
	}
	
	public class TextCompareAction extends Action {
		
		public TextCompareAction() {
			setImageDescriptor(Activator.getImageDescriptor("icons/compareText.gif"));
			setToolTipText("Open Text Comparator");
		}
		
		@Override
		public void run() {
			boolean asDialog = dialogRadioButton.getSelection();
			
			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID); 
			prefs.put(AS_DIALOG_ENTRY, String.valueOf(asDialog));
			
			String[] res = UiUtils.compare(asDialog, leftText.getText(), rightText.getText(), 
					leftLabel.getText(), rightLabel.getText(), true, true);
			leftText.setText(res[0]);
			rightText.setText(res[1]);
		}
		
		public String getText() { 
			return "Text Comparator...";
		};
	}
}
