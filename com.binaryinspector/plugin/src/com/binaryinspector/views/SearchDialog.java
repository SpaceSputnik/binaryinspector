package com.binaryinspector.views;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.eclipse.core.runtime.preferences.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import com.binaryinspector.Activator;
import com.binaryinspector.decoders.*;

public abstract class SearchDialog extends TitleAreaDialog {
	private static final String PREF_ENTRY_TEXT = "searchText";
	private static final String PREF_ENTRY_SEL_DIRECTION = "searchDirection";	
	private static final String PREF_ENTRY_SEL_DIRECTION_FORWARD_LABEL = "forward";	
	private static final String PREF_ENTRY_SEL_DIRECTION_BACK_LABEL = "backward";
	private static final String PREF_ENTRY_SEL_RELATIVE = "searchRelative";	
	public static final String PREF_ENTRY_CASE_SENSITIVE = "searchCaseSensitive";
	public static final String PREF_ENTRY_FLOAT_PRECISION = "searchFloatPrecision";
	private static final String PREF_ENTRY_SEL_RELATIVE_BEGINNING_LABEL = "the beginning";	
	private static final String PREF_ENTRY_SEL_RELATIVE_CURRENT_LABEL = "cursor";
	private static final String PREF_ENTRY_SEL_RELATIVE_END_LABEL = "the end";
	
	private static final RGB ERROR_FOREGROUND = new RGB(0xFF, 0, 0);
	
	private static final IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);

	private Text searchText;
	private Combo directionCombo;
	private Combo relativeToCombo;
	
	private Button caseSensitiveButton;
	private Text floatPrecision;
	
	private Table entriesTable;
	private CheckboxTableViewer entriesViewer;
	
	private Color errorForeground;
	
	private String searchValue;
	
	private Decoder.SearchMode searchMode;
	
	private boolean needToSave = false;
	
	public SearchDialog(Shell parentShell) {
		super(parentShell);
		setHelpAvailable(false);
	}

	@Override
	public void create() {
		super.create();
		
		Shell shell = getShell();
		UiUtils.setDialigInitialBounds(shell);

		shell.setText("Search for a Value");
		setTitle("Input");
		shell.setImage(Activator.getImageDescriptor("icons/goto_input.gif").createImage());
		validate();
		
		getButton(OK).setText("Search");
		getButton(CANCEL).setText("Close");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		errorForeground = new Color(getShell().getDisplay(), ERROR_FOREGROUND);
		
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.minimumHeight = 10;
		gd.minimumWidth = 30;
		container.setLayoutData(gd); 
    	container.setLayout(new GridLayout(5, false));

		Label label = new Label(container, SWT.NONE);
		label.setText("Search for");
    	
		searchText = new Text(container, SWT.BORDER);
		searchText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 
		searchText.setText(prefs.get(PREF_ENTRY_TEXT, ""));
		searchText.addModifyListener(new EntrySavingModifyListener(PREF_ENTRY_TEXT));
		searchText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});

		directionCombo = new Combo(container, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		directionCombo.add(PREF_ENTRY_SEL_DIRECTION_FORWARD_LABEL);
		directionCombo.add(PREF_ENTRY_SEL_DIRECTION_BACK_LABEL);

		directionCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				syncSearchModeCombos();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		directionCombo.addModifyListener(new EntrySavingModifyListener(PREF_ENTRY_SEL_DIRECTION));
		
		label = new Label(container, SWT.NONE);
		label.setText("from");

		relativeToCombo = new Combo(container, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);

		directionCombo.addModifyListener(new EntrySavingModifyListener(PREF_ENTRY_SEL_DIRECTION));
		relativeToCombo.addModifyListener(new EntrySavingModifyListener(PREF_ENTRY_SEL_RELATIVE));
		syncSearchModeCombos();

		Group decodersGroup = new Group(container, SWT.NO_TRIM);
		decodersGroup.setText("Use Decoders");
		gd = new GridData();
		gd.horizontalSpan = ((GridLayout)container.getLayout()).numColumns;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		gd.horizontalAlignment = SWT.FILL;
		gd.minimumHeight = 10;
		gd.minimumWidth = 30;
		decodersGroup.setLayoutData(gd);
		decodersGroup.setLayout(new GridLayout(5, false));

		entriesViewer = CheckboxTableViewer.newCheckList(decodersGroup, SWT.BORDER);
		entriesTable = entriesViewer.getTable();
		entriesViewer.setLabelProvider(new EntryLabelProvider());
		entriesTable.setLinesVisible(true);
		entriesTable.setHeaderVisible(true);
		
		entriesViewer.setCheckStateProvider(new ICheckStateProvider() {
			@Override
			public boolean isGrayed(Object element) {
				return false;
			}
			
			@Override
			public boolean isChecked(Object element) {
				return ((Decoder)element).isIncludeInSearch();
			}
		});
		entriesViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent e) {
				needToSave = true;
				((Decoder)e.getElement()).setIncludeInSearch(e.getChecked());
				validate();
			}
		});

		gd = new GridData();
		gd.horizontalSpan = ((GridLayout)decodersGroup.getLayout()).numColumns;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		gd.horizontalAlignment = SWT.FILL;
		gd.minimumHeight = 30;
		gd.minimumWidth = 40;
		
		entriesTable.setLayoutData(gd);

		TableColumn column = new TableColumn(entriesTable, SWT.LEFT);		
		column.setText("Decoder");
		column.setWidth(UiUtils.getWitdhInPixels(entriesTable, "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW", false) + 30);

		// validation error column
		column = new TableColumn(entriesTable, SWT.LEFT, 1);		
		column.setText("");
		column.setWidth(UiUtils.getWitdhInPixels(entriesTable, "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW", false) + 30);

		Button configureButton = new Button(decodersGroup, SWT.PUSH);
		final ConfigureDecodersAction configAction = new ConfigureDecodersAction(getShell()) {
			@Override
			public void run() {
				super.run();
				loadEntries();
				validate();
			}

		};
		configureButton.setImage(configAction.getImageDescriptor().createImage());
		configureButton.setToolTipText(configAction.getToolTipText());
		configureButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				configAction.run();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		Button selectAllButton = new Button(decodersGroup, SWT.PUSH);
		selectAllButton.setText("Select all");
		selectAllButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectAllDecoders(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		Button deselectAllButton = new Button(decodersGroup, SWT.PUSH);
		deselectAllButton.setText("Deselect all");
		deselectAllButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectAllDecoders(false);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		Group settingsGroup = new Group(container, SWT.NO_TRIM);
		settingsGroup.setText("Settings");
		gd = new GridData();
		gd.horizontalSpan = ((GridLayout)container.getLayout()).numColumns;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = false;
		gd.verticalAlignment = SWT.FILL;
		gd.horizontalAlignment = SWT.FILL;
		settingsGroup.setLayoutData(gd);
		settingsGroup.setLayout(new GridLayout(2, false));

		caseSensitiveButton = new Button(settingsGroup, SWT.CHECK);
		caseSensitiveButton.addSelectionListener(new EntrySavingSelectionListener(PREF_ENTRY_CASE_SENSITIVE));
		caseSensitiveButton.setSelection(prefs.getBoolean(PREF_ENTRY_CASE_SENSITIVE, false));
		caseSensitiveButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		caseSensitiveButton.setText("Case-sensitive");
		
		label = new Label(settingsGroup, SWT.NONE);
		label.setText("Precision of floating point comparissions");
		floatPrecision = new Text(settingsGroup, SWT.BORDER);
		floatPrecision.setText(prefs.get(PREF_ENTRY_FLOAT_PRECISION, "0.000001"));
		floatPrecision.addModifyListener(new EntrySavingModifyListener(PREF_ENTRY_FLOAT_PRECISION));
		floatPrecision.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});

		loadEntries();
		
		return area;
	}
	
	protected void selectAllDecoders(boolean b) {
		for (Decoder d : DecodeUtils.decodeFactory.getEnabledDecoders()) {
			d.setIncludeInSearch(b);
		}
		needToSave = true;
		DecodeUtils.decodeFactory.saveDecoders();
		loadEntries();
		validate();
	}

	private void syncSearchModeCombos() {
		if (directionCombo.getText().isEmpty()) {
			directionCombo.setText(prefs.get(PREF_ENTRY_SEL_DIRECTION, ""));
		}
		if (directionCombo.getText().isEmpty()) {
			directionCombo.setText(PREF_ENTRY_SEL_DIRECTION_FORWARD_LABEL);
		}
		
		String relativeToText = relativeToCombo.getText();
		if (relativeToText.isEmpty()) {
			relativeToText = prefs.get(PREF_ENTRY_SEL_RELATIVE, "");
		}

		if (PREF_ENTRY_SEL_DIRECTION_BACK_LABEL.equals(directionCombo.getText())) {
			relativeToCombo.removeAll();
			relativeToCombo.add(PREF_ENTRY_SEL_RELATIVE_END_LABEL);
			relativeToCombo.add(PREF_ENTRY_SEL_RELATIVE_CURRENT_LABEL);
			
			if (relativeToText.isEmpty() || PREF_ENTRY_SEL_RELATIVE_BEGINNING_LABEL.equals(relativeToText)) {
				relativeToCombo.setText(PREF_ENTRY_SEL_RELATIVE_END_LABEL);
			} else {
				relativeToCombo.setText(relativeToText);
			}
		} else {
			relativeToCombo.removeAll();
			relativeToCombo.add(PREF_ENTRY_SEL_RELATIVE_BEGINNING_LABEL);
			relativeToCombo.add(PREF_ENTRY_SEL_RELATIVE_CURRENT_LABEL);
			
			if (relativeToText.isEmpty() || PREF_ENTRY_SEL_RELATIVE_END_LABEL.equals(relativeToText)) {
				relativeToCombo.setText(PREF_ENTRY_SEL_RELATIVE_BEGINNING_LABEL);
			} else {
				relativeToCombo.setText(relativeToText);
			}
		}
	}

	
	private void loadEntries() {
		entriesViewer.setItemCount(0);
		ArrayList<Decoder> decoders = DecodeUtils.decodeFactory.getEnabledDecoders();
		entriesViewer.setContentProvider(new ArrayContentProvider());
		entriesViewer.setInput(decoders);
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}

	// save content of the Text fields because they get disposed
	// as soon as the Dialog closes
	private void saveInput() {
		searchValue = searchText.getText();
		if (PREF_ENTRY_SEL_DIRECTION_FORWARD_LABEL.equals(directionCombo.getText())) {
			searchMode = PREF_ENTRY_SEL_RELATIVE_BEGINNING_LABEL.equals(
					relativeToCombo.getText()) ? Decoder.SearchMode.FORWARD_FROM_BEGINNING : Decoder.SearchMode.FORWARD_FROM_CURRENT;
		} else {
			searchMode = PREF_ENTRY_SEL_RELATIVE_END_LABEL.equals(
					relativeToCombo.getText()) ? Decoder.SearchMode.BACKWARD_FROM_END : Decoder.SearchMode.BACKWARD_FROM_CURRENT;
		}
	}
	
	public abstract String go();
	
	@Override
	protected void okPressed() {
		saveInput();
		String error = go();
		if (error == null) {
			super.okPressed();
		} else {
			setErrorMessage(error);
		};
	}

	public void validate() {
		setErrorMessage(null);
		setMessage(null, IMessageProvider.WARNING);
		setMessage("Enter a value like ABC or 123 and select what decoders to use during search");
		
		@SuppressWarnings("unchecked")
		ArrayList<Decoder> decoders = (ArrayList<Decoder>)entriesViewer.getInput();
		for (Decoder d : decoders) {
			// clear all decoder errors
			d.setTag(null);
		}

		String searchValue = searchText.getText();
		if (searchValue.isEmpty()) {
			setErrorMessage("Enter a non-empty value to search for");
		} else {
			for (Decoder d : decoders) {
				// validate with each decoder and save the message as it's tag
				if (d.isIncludeInSearch() && entriesViewer.getChecked(d)) {
					String error = d.validate(searchValue);
					d.setTag(error);
					if (error != null) {
						setMessage("Some decoders are not valid for value \"" + searchValue + "\"", IMessageProvider.WARNING);
					}
				}
			}
		}
		entriesViewer.refresh();
		
		if (getErrorMessage() == null) {
			boolean haveDecoder = false;
			for (Decoder d : DecodeUtils.decodeFactory.getEnabledDecoders()) {
				if (d.isIncludeInSearch() && d.getTag() == null) {
					haveDecoder = true;
					break;
				}
			}
			if (! haveDecoder) {
				setErrorMessage("Select at least one valid decoder");
			}
		}
		
		boolean validFloatPrecision = false;
		try {
			BigDecimal bd = new BigDecimal(floatPrecision.getText());
			validFloatPrecision = bd.signum() >= 0;
		} catch (NumberFormatException e) {}
		
		if (! validFloatPrecision) {
			setErrorMessage("Invalid value of the floating precision");
		}

		
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			okButton.setEnabled(getErrorMessage() == null);
		}
	}
	
	public String getSearchValue() {
		return searchValue;
	}

	public Decoder.SearchMode getSearchMode() {
		return searchMode;
	}
	
	@Override
	public boolean close() {
		if (needToSave) {
			DecodeUtils.decodeFactory.saveDecoders();
		}
		return super.close();
	}

	
	private class EntryLabelProvider implements ITableLabelProvider, ITableColorProvider {
		@Override
		public void addListener(ILabelProviderListener listener) {}

		@Override
		public void dispose() {}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return element.toString();
			case 1:
				if (! searchText.getText().isEmpty()) {
					return (String)((Decoder)element).getTag();
				} else {
					return null;
				}
			}
			return null;
		}

		@Override
		public Color getForeground(Object element, int columnIndex) {
			if (columnIndex == 1) {
				Decoder d = (Decoder)element;
				if (d.getTag() != null) {
					return errorForeground;
				}
			}
			return null;
		}

		@Override
		public Color getBackground(Object element, int columnIndex) {
			return null;
		}
	}
}