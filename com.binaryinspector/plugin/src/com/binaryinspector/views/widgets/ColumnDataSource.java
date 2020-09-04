package com.binaryinspector.views.widgets;

import org.eclipse.swt.graphics.*;

public interface ColumnDataSource {
	public String getData(int col);
	public RGB getColor(int col);
}
