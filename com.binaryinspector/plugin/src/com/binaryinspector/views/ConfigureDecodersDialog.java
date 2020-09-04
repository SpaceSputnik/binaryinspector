package com.binaryinspector.views;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.binaryinspector.Activator;
import com.binaryinspector.decoders.DecodeUtils;
import com.binaryinspector.decoders.Decoder;
import com.binaryinspector.decoders.parameters.BoolDescriptor;
import com.binaryinspector.decoders.parameters.Descriptor;
import com.binaryinspector.decoders.parameters.EnumDescriptor;
import com.binaryinspector.decoders.parameters.IntegerDescriptor;
import com.binaryinspector.decoders.parameters.ParameterValues;
import com.binaryinspector.decoders.parameters.StringDescriptor;

public class ConfigureDecodersDialog extends TitleAreaDialog {
	private boolean dirty = false;

	private final class DecoderLabelProvider extends LabelProvider {
		private boolean showName;
		
		DecoderLabelProvider(boolean showName) {
			this.showName = showName;
		}
		
		@Override
		public String getText(Object element) {
			Decoder d = ((Decoder)element);
			return showName ? d.getName() : d.toString();
		}
	}

	private CheckboxTableViewer tableViewer;
	private Composite mainComp;
	private Composite listButtonComp;
	private Composite paramComp;
	private Button addButton;
	private Button removeButton;
	private Button upButton;
	private Button downButton;
	private ArrayList<Decoder> decoders;
	private Button okButton;

	public ConfigureDecodersDialog(Shell parentShell, ArrayList<Decoder> decoders) {
		super(parentShell);
		this.decoders = decoders;
		setHelpAvailable(false);
	}

	public ArrayList<Decoder> getDecoders() {
		return decoders;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Configure Decoders");
		setTitleImage(Activator.getImageDescriptor("icons/wrench_yellow.png").createImage());
		enableButtons();
		Shell shell = getShell();
		shell.setText("Configuration");
		UiUtils.setDialigInitialBounds(shell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);
		
		mainComp = new Composite(parent, SWT.NO_TRIM);
		GridLayout layout2 = new GridLayout();
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		layout2.numColumns = 2;
		mainComp.setLayout(layout2);
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		mainComp.setLayoutData(gridData);

		tableViewer = CheckboxTableViewer.newCheckList(mainComp, SWT.BORDER); 
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		tableViewer.getTable().setLayoutData(gridData);
		
		tableViewer.setCheckStateProvider(new ICheckStateProvider() {
			@Override
			public boolean isGrayed(Object element) {
				return false;
			}
			
			@Override
			public boolean isChecked(Object element) {
				return ((Decoder)element).isEnabled();
			}
		});
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent e) {
				((Decoder)e.getElement()).setEnabled(e.getChecked());
				dirty = true;
				enableButtons();
			}
		});
		
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				enableButtons();
			}
		});
		
		tableViewer.setLabelProvider(new DecoderLabelProvider(false));
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(decoders);
		
		paramComp = new Composite(mainComp, SWT.NO_TRIM);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		paramComp.setLayoutData(gridData);		
		
		listButtonComp = new Composite(mainComp, SWT.NO_TRIM);
		GridLayout layout3 = new GridLayout();
		layout3.numColumns = 4;
		layout3.makeColumnsEqualWidth = true;
		layout3.marginHeight = 0;
		layout3.marginWidth = 0;		
		listButtonComp.setLayout(layout3);
		
		gridData = new GridData();
		//gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		listButtonComp.setLayoutData(gridData);
		
		addButton = new Button(listButtonComp, SWT.PUSH);
		addButton.setText("Add...");
		gridData = new GridData();
		//gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalAlignment = GridData.FILL;
		addButton.setLayoutData(gridData);
		addButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addDecoder();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		removeButton = new Button(listButtonComp, SWT.PUSH);
		removeButton.setText("Remove");
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalAlignment = GridData.FILL;
		removeButton.setLayoutData(gridData);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeDecoder();
			}
		});
		
		upButton = new Button(listButtonComp, SWT.PUSH);
		upButton.setImage(Activator.getImageDescriptor("icons/up.gif").createImage());
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalAlignment = GridData.FILL;
		upButton.setLayoutData(gridData);
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveDecoderUp();
			}
		});

		downButton = new Button(listButtonComp, SWT.PUSH);
		downButton.setImage(Activator.getImageDescriptor("icons/down.gif").createImage());
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalAlignment = GridData.FILL;
		downButton.setLayoutData(gridData);
		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveDecoderDown();
			}
		});


		tableViewer.getTable().addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fillParamComp();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		fillParamComp();
		
		return parent;
	}
	
	protected void moveDecoderDown() {
		int index = tableViewer.getTable().getSelectionIndex();
		if (index < 0) {
			return;
		}
		Decoder d = decoders.remove(index);
		index++;
		if (index > decoders.size()) {
			index = 0;
		}		
		decoders.add(index, d);
		tableViewer.refresh();
		tableViewer.getTable().select(index);
		
		dirty = true;
		enableButtons();
	}

	protected void moveDecoderUp() {
		int index = tableViewer.getTable().getSelectionIndex();
		if (index < 0) {
			return;
		}
		Decoder d = decoders.remove(index);
		index--;
		if (index < 0) {
			index = decoders.size();
		}		
		decoders.add(index, d);
		tableViewer.refresh();
		tableViewer.getTable().select(index);
		
		dirty = true;
		enableButtons();
	}

	protected void removeDecoder() {
		int index = tableViewer.getTable().getSelectionIndex();
		if (index < 0) {
			return;
		}
		decoders.remove(index);
		tableViewer.refresh();
		
		index = Math.min(index, tableViewer.getTable().getItemCount() - 1);
		if (index >= 0) {
			tableViewer.getTable().select(index);
		}
		fillParamComp();
		
		dirty = true;
		enableButtons();
	}

	protected void addDecoder() {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getShell(), new DecoderLabelProvider(true));
		dialog.setElements(DecodeUtils.decodeFactory.getTemplateArray());
		dialog.setTitle("Add Item");
		dialog.setImage(Activator.getImageDescriptor("icons/bytes.gif").createImage());
		if (dialog.open() != Window.OK) {
			return;
		}
		Object[] results = dialog.getResult();
		for (Object o : results) {
			DecodeUtils.decodeFactory.createFromTemplate((Decoder)o, decoders);
		}
		tableViewer.refresh();
		
		tableViewer.getTable().select(tableViewer.getTable().getItemCount() - 1);
		fillParamComp();
		
		dirty = true;
		enableButtons();
	}

	private void fillParamComp() {
		for (Control c : paramComp.getChildren()) {
			c.dispose();
		}
		GridLayout layout4 = new GridLayout();
		layout4.numColumns = 2;
		layout4.horizontalSpacing = 7;
		paramComp.setLayout(layout4);			

		IStructuredSelection sel = (IStructuredSelection)tableViewer.getSelection();
		final Decoder d = sel == null || sel.isEmpty() ? null : (Decoder)sel.getFirstElement(); 
		if (d == null) {
			setMessage("Select an entry from the list.");
		} else {
			setMessage("");
			ParameterValues params = d.getParams();
			
			for (final Descriptor p : d.getParams().getMetadata().values()) {
				Label label = new Label(paramComp, SWT.NONE);
				label.setText(p.toString());
				label.setVisible(true);
				
				GridData gridData = new GridData();
				gridData.horizontalAlignment = GridData.FILL;
				gridData.verticalAlignment = GridData.FILL;
				label.setLayoutData(gridData);
				
				Control c = null;
				if (p instanceof BoolDescriptor) {
					final Button check = new Button(paramComp, SWT.CHECK);
					check.setSelection(params.getBoolean(p.getName()));
					check.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							booleanParamChanged(d, p.getName(), check.getSelection());
						}
						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
							widgetSelected(e);						
						}
					});
					c = check;
				} else if (p instanceof StringDescriptor || p instanceof IntegerDescriptor) {
					final Text text = new Text(paramComp, SWT.DEFAULT);
					text.setText(params.getString(p.getName()));
					text.setEditable(true);
					text.addModifyListener(new ModifyListener() {
						@Override
						public void modifyText(ModifyEvent e) {
							if (p instanceof IntegerDescriptor) {
								intParamChanged(d, p.getName(), text.getText());
							} else {
								stringParamChanged(d, p.getName(), text.getText());
							}
						}
					});
					c = text;
				} else if (p instanceof EnumDescriptor) {
					if (((EnumDescriptor) p).getValues().size() < 10) {
						final Combo combo = new Combo(paramComp, SWT.VERTICAL
								| SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
						for (String s : ((EnumDescriptor) p).getValues()) {
							combo.add(s);
						}
						combo.setText(params.getString(p.getName()));
						combo.addSelectionListener(new SelectionListener() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								stringParamChanged(d, p.getName(),
										combo.getText());
							}

							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
								widgetSelected(e);
							}
						});
						c = combo;
					} else {
						Composite comp = new Composite(paramComp, SWT.NO_TRIM);
						GridLayout l = new GridLayout(2, false);
						l.horizontalSpacing = 0;
						l.marginHeight = 0;
						l.marginWidth = 0;
						comp.setLayout(l);
						final Text text = new Text(comp, SWT.DEFAULT);
						text.setEditable(false);
						gridData = new GridData();
						gridData.grabExcessHorizontalSpace = true;
						gridData.horizontalAlignment = GridData.FILL;
						gridData.verticalAlignment = GridData.FILL;
						text.setLayoutData(gridData);
						text.setText(params.getString(p.getName()));
						Button b = new Button(comp, SWT.PUSH);
						b.setText("Select...");
						b.addSelectionListener(new SelectionListener() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								ElementListSelectionDialog dialog = new ElementListSelectionDialog(
										getShell(), new LabelProvider());
								dialog.setElements(((EnumDescriptor) p).getValues().toArray());
								dialog.setTitle("Select Value");
								dialog.setImage(Activator.getImageDescriptor("icons/bytes.gif").createImage());
								if (dialog.open() != Window.OK) {
									return;
								}
								Object[] sel = dialog.getResult();
								String result = (String)sel[0];
								text.setText(result);
								stringParamChanged(d, p.getName(), result);
							}

							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
								widgetSelected(e);
							}
						});
						c = comp;
					}
				}
				gridData = new GridData();
				gridData.grabExcessHorizontalSpace = true;
				gridData.horizontalAlignment = GridData.FILL;
				gridData.verticalAlignment = GridData.FILL;
				c.setLayoutData(gridData);
			}
		}
		mainComp.layout(true, true);
	}
	
	private void stringParamChanged(Decoder d, String name, String newValue) {
		dirty = true;
		enableButtons();
		
		ParameterValues params = d.getParams();	
		params.addString(name, newValue);
		tableViewer.refresh();
	}
	
	private void booleanParamChanged(Decoder d, String name, boolean newValue) {
		dirty = true;
		enableButtons();

		ParameterValues params = d.getParams();	
		params.addBoolean(name, newValue);
		tableViewer.refresh();
	}
	
	private void intParamChanged(Decoder d, String name, String newValue) {
		dirty = true;
		enableButtons();

		newValue = newValue.trim();
		ParameterValues params = d.getParams();	
		if (newValue.length() == 0) {
			params.setSpecified(name, false);
			// TODO: optimize a bit
			tableViewer.refresh();
			return;
		}
		int v;
		try {
			v = Integer.valueOf(newValue);
		} catch (NumberFormatException e) {
			// invalid entry, just ignore
			return;
		}
		params.addInteger(name, v);
		tableViewer.refresh();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.CENTER;

		parent.setLayoutData(gridData);
		// Create Add button
		// Own method as we need to overview the SelectionAdapter
		createOkButton(parent, OK, "OK", true);

		// Create Cancel button
		Button cancelButton = createButton(parent, CANCEL, "Cancel", false);
		// Add a SelectionListener
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
            public void widgetSelected(SelectionEvent e) {
				setReturnCode(CANCEL);
				close();
			}
		});
	}

	protected Button createOkButton(Composite parent, int id, String label,
			boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		okButton = new Button(parent, SWT.PUSH);
		okButton.setText(label);
		okButton.setFont(JFaceResources.getDialogFont());
		okButton.setData(new Integer(id));
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
            public void widgetSelected(SelectionEvent event) {
				okPressed();
			}
		});
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(okButton);
			}
		}
		setButtonLayoutData(okButton);
		return okButton;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

	private void enableButtons() {
		boolean enable = ! tableViewer.getSelection().isEmpty();
		removeButton.setEnabled(enable);
		upButton.setEnabled(enable);
		downButton.setEnabled(enable);
		okButton.setEnabled(dirty);
	}
}