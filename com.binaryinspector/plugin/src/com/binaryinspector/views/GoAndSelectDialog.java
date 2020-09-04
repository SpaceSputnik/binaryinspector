package com.binaryinspector.views;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.binaryinspector.Activator;
import com.binaryinspector.decoders.DecodeUtils;

public abstract class GoAndSelectDialog extends TitleAreaDialog {
	private static final String PREF_ENTRY_POSITION = "goAndSelectPos";
	private static final String PREF_ENTRY_SEL_LENGTH = "goAndSelLength";	
	private static final String PREF_ENTRY_SEL_RELATIVE = "goAndSelRelative";	
	private static final String PREF_ENTRY_NAME = "goAndSelName";	
	private static final String PREF_ENTRY_HEX_OFFSET = "goAndSelEntryOffsetHex";
	private static final String PREF_ENTRY_HEX_BYTE_LENGTH = "goAndSelEntryByteOffsetHex";
	
	private static final String PREF_ENTRY_SEL_RELATIVE_BEGINNING_LABEL = "beginning";	
	private static final String PREF_ENTRY_SEL_RELATIVE_CURRENT_LABEL = "current position";
	
	private static final IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
	private static final Image DELETE_IMAGE = Activator.getImageDescriptor("icons/delete_obj.gif").createImage();;

	private Text offsetText;
	private Combo selectLengthCombo;
	private Combo relativeToCombo;
	private Text entryName;
	private Table entriesTable;
	private TableViewer entriesViewer;
	private Button offsetHexCheckbox;
	private Button byteLengthHexCheckbox;
	
	private TableEditor deletingEditor = null;
	
	private GoAndSelectEntry entry = null;
	
	public GoAndSelectDialog(Shell parentShell) {
		super(parentShell);
		setHelpAvailable(false);
	}

	@Override
	public void create() {
		super.create();
		setHelpAvailable(false);

		Shell shell = getShell();
		UiUtils.setDialigInitialBounds(shell);

		shell.setText("Go to");
		setMessage("Select a zero-based offset and the number of bytes to select.\nEnter integer values or hex values like 'xAF'", IMessageProvider.INFORMATION);
		setTitle("Input");
		shell.setImage(Activator.getImageDescriptor("icons/goto_input.gif").createImage());
		
		getButton(OK).setText("Go");
		getButton(CANCEL).setText("Close");
		
		validate();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		NumericValidationListener numericListener = new NumericValidationListener();
		Composite area = (Composite) super.createDialogArea(parent);
		
		Composite container = new Composite(area, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.minimumHeight = 10;
		gd.minimumWidth = 30;
		container.setLayoutData(gd); 
		container.setLayout(new GridLayout(7, false));
		
		Label label = new Label(container, SWT.NONE);
		label.setText("Go to offset ");
		offsetText = new Text(container, SWT.BORDER);
		offsetText.addModifyListener(new EntrySavingModifyListener(PREF_ENTRY_POSITION));
		offsetText.addModifyListener(numericListener);
		offsetText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 
		offsetText.setText(prefs.get(PREF_ENTRY_POSITION, "0"));

		label = new Label(container, SWT.NONE);
		label.setText(" from ");
		
		relativeToCombo = new Combo(container, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		relativeToCombo.add(PREF_ENTRY_SEL_RELATIVE_BEGINNING_LABEL);
		relativeToCombo.add(PREF_ENTRY_SEL_RELATIVE_CURRENT_LABEL);
		relativeToCombo.setText(PREF_ENTRY_SEL_RELATIVE_BEGINNING_LABEL.equals(prefs.get(PREF_ENTRY_SEL_RELATIVE, "")) ?
			PREF_ENTRY_SEL_RELATIVE_BEGINNING_LABEL : PREF_ENTRY_SEL_RELATIVE_CURRENT_LABEL);
		relativeToCombo.addModifyListener(new EntrySavingModifyListener(PREF_ENTRY_SEL_RELATIVE));

		
		label = new Label(container, SWT.NONE);
		label.setText("and select ");
		selectLengthCombo = new Combo(container, SWT.BORDER);
		selectLengthCombo.addModifyListener(new EntrySavingModifyListener(PREF_ENTRY_SEL_LENGTH));
		selectLengthCombo.addModifyListener(numericListener);
		selectLengthCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 
		selectLengthCombo.setText(prefs.get(PREF_ENTRY_SEL_LENGTH, "0"));
		selectLengthCombo.add("1");
		selectLengthCombo.add("2");
		selectLengthCombo.add("4");
		selectLengthCombo.add("8");
		selectLengthCombo.add("16");
		
		label = new Label(container, SWT.NONE);
		label.setText(" bytes");
		
		label = new Label(container, SWT.NONE);
		label.setText("Name (optional)");
		
		entryName = new Text(container, SWT.BORDER);
		entryName.addModifyListener(new EntrySavingModifyListener(PREF_ENTRY_NAME));
		entryName.setText(prefs.get(PREF_ENTRY_NAME, ""));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ((GridLayout)container.getLayout()).numColumns - 1;
		entryName.setLayoutData(gd);
		
		Group regionsGroup = new Group(container, SWT.NO_TRIM);
		regionsGroup.setText("Saved Entries");
		gd = new GridData();
		gd.horizontalSpan = ((GridLayout)container.getLayout()).numColumns;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		gd.horizontalAlignment = SWT.FILL;
		gd.minimumHeight = 10;
		gd.minimumWidth = 30;
		regionsGroup.setLayoutData(gd);
		regionsGroup.setLayout(new GridLayout(5, false));
		
		entriesTable = new Table(regionsGroup, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		entriesTable.setLinesVisible(true);
		entriesTable.setHeaderVisible(true);
		gd = new GridData();
		gd.horizontalSpan = ((GridLayout)regionsGroup.getLayout()).numColumns;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		gd.horizontalAlignment = SWT.FILL;
		entriesTable.setLayoutData(gd);
		
		entriesTable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selection = entriesTable.getSelection();
				if (selection.length == 1) {
					startEditing(selection[0], 0);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		int index = 0;
		
		TableColumn column = new TableColumn(entriesTable, SWT.CENTER, index++);	
		column.setWidth(DELETE_IMAGE.getBounds().width + 20);
		column.setAlignment(SWT.CENTER);
		
		column = new TableColumn(entriesTable, SWT.LEFT, index++);		
		column.setText("Name");
		column.setWidth(UiUtils.getWitdhInPixels(entriesTable, "WWWWWWWWWWWWWWWWWWWWW", false) + 30);

		column = new TableColumn(entriesTable, SWT.LEFT, index++);		
		column.setText("Offset");
		column.setWidth(UiUtils.getWitdhInPixels(entriesTable, column.getText(), false) + 30);

		column = new TableColumn(entriesTable, SWT.LEFT, index++);		
		column.setText("From");
		column.setWidth(UiUtils.getWitdhInPixels(entriesTable, PREF_ENTRY_SEL_RELATIVE_BEGINNING_LABEL, false) + 30);
		
		column = new TableColumn(entriesTable, SWT.LEFT, index++);		
		column.setText("Byte Length");
		column.setWidth(UiUtils.getWitdhInPixels(entriesTable, column.getText(), false) + 30);
		
		entriesViewer = new TableViewer(entriesTable);
		entriesViewer.setLabelProvider(new EntryLabelProvider());
		
		Button clearButton = new Button(regionsGroup, SWT.PUSH);
		clearButton.setText("Delete All");
		clearButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GoAndSelectEntryFactory.clearAll();
				loadEntries();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		offsetHexCheckbox = new Button(regionsGroup, SWT.CHECK);
		byteLengthHexCheckbox = new Button(regionsGroup, SWT.CHECK);

		offsetHexCheckbox.setText("Show offset as hex");
		byteLengthHexCheckbox.setText("Show byte length as hex");

		offsetHexCheckbox.setSelection(Boolean.TRUE.toString().equals(prefs.get(PREF_ENTRY_HEX_OFFSET, "0")));
		byteLengthHexCheckbox.setSelection(Boolean.TRUE.toString().equals(prefs.get(PREF_ENTRY_HEX_BYTE_LENGTH, "0")));
		
		offsetHexCheckbox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				prefs.put(PREF_ENTRY_HEX_OFFSET, (offsetHexCheckbox.getSelection() ? Boolean.TRUE : Boolean.FALSE).toString());
				loadEntries();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		byteLengthHexCheckbox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				prefs.put(PREF_ENTRY_HEX_BYTE_LENGTH, (byteLengthHexCheckbox.getSelection() ? Boolean.TRUE : Boolean.FALSE).toString());
				loadEntries();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
				loadEntries();
			}
		});
		
		loadEntries();
		
		entriesTable.addSelectionListener(new SelectionAdapter() {
			@Override
            public void widgetSelected(SelectionEvent e) {
				// copy values from the table to the fields
				IStructuredSelection selection = (IStructuredSelection)entriesViewer.getSelection();
				if (selection != null) {
					Object o = selection.getFirstElement();
					if (o != null) {
						GoAndSelectEntry ent = (GoAndSelectEntry)o;
						entryName.setText(ent.name == null ? "" : ent.name);
						offsetText.setText(DecodeUtils.renderIntOrHex(ent.offset, offsetHexCheckbox.getSelection()));
						relativeToCombo.setText(ent.relative ? PREF_ENTRY_SEL_RELATIVE_CURRENT_LABEL : PREF_ENTRY_SEL_RELATIVE_BEGINNING_LABEL);
						selectLengthCombo.setText(DecodeUtils.renderIntOrHex(ent.byteLength, byteLengthHexCheckbox.getSelection()));
					}
				}
			}

			@Override
            public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		return area;
	}

	
	private void loadEntries() {
		cancelEditing();
		Collection<GoAndSelectEntry> entries = GoAndSelectEntryFactory.read();
		entriesViewer.setContentProvider(new ArrayContentProvider());
		entriesViewer.setInput(entries);
	}
	
	private void saveEntries() {
		LinkedList<GoAndSelectEntry> list = new LinkedList<GoAndSelectEntry>();
		
		for (int i = 0; i < entriesTable.getItemCount(); i++) {
			Object el = entriesViewer.getElementAt(i);
			GoAndSelectEntry cur = (GoAndSelectEntry)el;
			if (cur.equals(entry)) {
				// same entry present, rectify
				if (cur.name == null) {
					// old unnamed entry goes away in favor of the new one, named or not
				} else {
					if (entry.name == null) {
						// old named one absorbs the new unnamed one
						entry = cur;
					} else {
						// new named one renames the old named one
					}
				} 
			} else {
				// an entry with the same name but different contents gets superseded by the new entry
				if (entry.name == null || ! entry.name.equals(cur.name)) {
					list.add(cur);
				}
			}
		}
		list.add(0, entry); // what user has entered last is always on the top

		GoAndSelectEntryFactory.write(list);
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}

	// save content of the Text fields because they get disposed
	// as soon as the Dialog closes
	private void saveInput() {
		String name = entryName.getText();
		if (name.isEmpty()) {
			name = null;
		}
		int position = DecodeUtils.parseIntOrHex(offsetText.getText());
		int selectLength = DecodeUtils.parseIntOrHex(selectLengthCombo.getText());
		String relativeTo = relativeToCombo.getText();
		entry = new GoAndSelectEntry(name, position, ! PREF_ENTRY_SEL_RELATIVE_BEGINNING_LABEL.equals(relativeTo), selectLength);
	}

	public abstract String go();
	
	@Override
	protected void okPressed() {
		saveInput();
		String error = go();
		if (error == null) {
			saveEntries();
			super.okPressed();
		} else {
			setErrorMessage(error);
		};
	}

	public GoAndSelectEntry getEntry() {
		return entry;
	}
	
	public boolean validateNumberEntry(String name, String value) {
		Integer v = DecodeUtils.parseIntOrHex(value);
		
		if (DecodeUtils.parseIntOrHex(value) == null || v.compareTo(0) < 0) {
			setErrorMessage(name + " must be a valid non-negative integer or a hex literal starting with 'x'");
			return false;
		}
		return true;
	}

	public void validate() {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton == null) {
			return;
		}
		setErrorMessage(null);
		if (validateNumberEntry("Offset", offsetText.getText())) {
			validateNumberEntry("Selection length", selectLengthCombo.getText());
		}
		okButton.setEnabled(getErrorMessage() == null);
	}
	
	public void cancelEditing() {
		if (deletingEditor != null) {
			Control c = deletingEditor.getEditor();
			if (c != null && !c.isDisposed()) {
				c.dispose();
				deletingEditor.setEditor(null);
			}
		}
	}
	
	public void startEditing(final TableItem tableItem, int column) {
		cancelEditing();
		Composite container = new Composite(entriesTable, SWT.NO_TRIM | SWT.TRANSPARENT);
		//container.setBackground(searchResultsTable.getBackground());
		GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		container.setLayout(gl);
		
		Cursor handCursor = new Cursor(container.getDisplay(), SWT.CURSOR_HAND);

		Button chooseButton = new Button(container, SWT.NO_TRIM);
		chooseButton.setBackground(entriesTable.getBackground());
		chooseButton.setImage(DELETE_IMAGE);
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = SWT.CENTER;

		chooseButton.setLayoutData(layoutData);
		chooseButton.setCursor(handCursor);
		chooseButton.setToolTipText("Delete");

		chooseButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				@SuppressWarnings("unchecked")
				Collection<GoAndSelectEntry> entries = (Collection<GoAndSelectEntry>) entriesViewer.getInput();
				entries.remove(tableItem.getData());
				GoAndSelectEntryFactory.write(entries);
				loadEntries();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		deletingEditor = new TableEditor(entriesTable);
		deletingEditor.horizontalAlignment = SWT.CENTER;
		deletingEditor.grabHorizontal = true;
		deletingEditor.setEditor(container, tableItem, column);
	}


	class NumericValidationListener implements ModifyListener {
		@Override
		public void modifyText(ModifyEvent e) {
			validate();
		}
	}
	
	private class EntryLabelProvider implements ITableLabelProvider {

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
			GoAndSelectEntry entry = (GoAndSelectEntry)element;
			
			switch (columnIndex) {
			case 1: // name
				String name = entry.name;
				return name == null ? "Untitled" : name;
				
			case 2: // offset
				return DecodeUtils.renderIntOrHex(entry.offset, offsetHexCheckbox == null ? false : offsetHexCheckbox.getSelection());
			
			case 3: // from
				return entry.relative ? "Cursor" : "Beginning";
				
			case 4: // selection length
				return DecodeUtils.renderIntOrHex(entry.byteLength, byteLengthHexCheckbox == null ? false : byteLengthHexCheckbox.getSelection());
			}
			return null;
		}
	}
}