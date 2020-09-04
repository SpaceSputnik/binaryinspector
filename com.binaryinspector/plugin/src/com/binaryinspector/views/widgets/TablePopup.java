package com.binaryinspector.views.widgets;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;


public class TablePopup<T extends ColumnDataSource> extends Popup {
	private final class LabelProvider implements ITableLabelProvider, ITableColorProvider {
		@Override
		public void removeListener(ILabelProviderListener listener) {}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void dispose() {}

		@Override
		public void addListener(ILabelProviderListener listener) {}

		@SuppressWarnings("unchecked")
		@Override
		public String getColumnText(Object element, int columnIndex) {
			return ((T)element).getData(columnIndex);
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Color getForeground(Object element, int columnIndex) {
			RGB rgb = ((T)element).getColor(columnIndex);
			return rgb == null ? null : new Color(shell.getDisplay(), rgb);
		}

		@Override
		public Color getBackground(Object element, int columnIndex) {
			return null;
		}
	}

	private int [] columnWidths;
	
	private java.util.List<T> items = null;
	private Table table = null;
	private TableViewer tableViewer = null;
	private SelectionListener selectionListener = null;
	private MouseMoveListener mouseMoveListener = null;

	public TablePopup(StyledText parent, int [] columnWidths) {
		super(parent);
		this.columnWidths = columnWidths;
	}

	/**
	 * Creates a list as a content of the pop-up window.
	 */
	@Override
	protected void createContent() {
		table = new Table(shell, SWT.SINGLE | SWT.FULL_SELECTION);
		
		for (int i = 0; i < columnWidths.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setResizable(true);
			column.setWidth(columnWidths[i]);
		}
		
		table.addSelectionListener(selectionListener);
		table.addMouseMoveListener(mouseMoveListener);
		table.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		
		tableViewer = new TableViewer(table);
		setItems(items);
	}

	public void setItems(java.util.List<T> items) {
		this.items = items;
		if (tableViewer == null) {
			return;
		}
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new LabelProvider());
		
		tableViewer.setInput(items);
	}

	/**
	 * Shows the pop-up window.
	 */
	@Override
	public void show(Point p) {
		if (items == null || items.size() == 0) {
			return; // don't show anything
		}
		super.show(p);
	}

	/**
	 * Select an item
	 * 
	 * @param index
	 */
	public void select(int index) {
		table.select(index);
	}

	/**
	 * Returns the index of the currently selected list item text.
	 * 
	 * @return the index.
	 */
	public int getSelectionIndex() {
		return table.getSelectionIndex();
	}

	/**
	 * Returns the currently selected list item text.
	 * 
	 * @return the currently selected list item text.
	 */
	public T getSelection() {
		int index = table.getSelectionIndex();
		return index < 0 ? null : items.get(index);
	}

	/**
	 * Selects next item in the list.
	 */
	protected void selectNext() {
		int selectionIndex = table.getSelectionIndex();
		if (selectionIndex < items.size() - 1) {
			table.select(selectionIndex + 1);
		} else {
			table.select(0);
		}
		table.showSelection();
	}

	/**
	 * Selects previous item in the list.
	 */
	protected void selectPrevious() {
		int selectionIndex = table.getSelectionIndex();
		if (selectionIndex == 0) {
			table.select(items.size() - 1);
		} else {
			table.select(selectionIndex - 1);
		}
		table.showSelection();
	}
	
	public void addSelectionListener(SelectionListener l) {
		this.selectionListener = l;
		if (tableViewer != null) {
			table.addSelectionListener(l);
		}
	}
	
	public Table getTable() {
		return table;
	}

	public void addMouseMoveListener(MouseMoveListener l) {
		this.mouseMoveListener = l;
		if (tableViewer != null) {
			table.addMouseMoveListener(l);
		}
	}
}