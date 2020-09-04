package com.binaryinspector.views.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

public abstract class Popup {
	protected StyledText parent;
	private int offset;
	private int lineIndex;
	protected Shell shell = null;
	protected boolean limitDisplay = false;

	/**
	 * PopUp class constructor.
	 * 
	 * @param parent
	 *            the parent MultiLineCodeText class.
	 */
	protected Popup(StyledText parent) {
		this.parent = parent;

		offset = parent.getCaretOffset();
		lineIndex = parent.getLineAtOffset(offset);
	}

	/**
	 * Returns the initial offset in the parent composite.
	 * 
	 * @return the initial offset in the parent composite.
	 */
	public int offset() {
		return offset;
	}

	/**
	 * Returns the initial line index in the parent composite.
	 * 
	 * @return the initial line index in the parent composite.
	 */
	protected int lineIndex() {
		return lineIndex;
	}

	/**
	 * Hides and disposes the pop-up window.
	 */
	public void dispose() {
		if (shell != null && ! shell.isDisposed()) {
			shell.setVisible(false);
			shell.dispose();
		}
		shell = null;
	}

	/**
	 * Creates the specific content of the pop-up window.
	 */
	protected abstract void createContent();

	/**
	 * Shows the pop-up window.
	 * 
	 * @param p window location
	 */
	public void show(Point p) {
		if (shell == null || shell.isDisposed()) {
			shell = new Shell(parent.getShell(), SWT.TOOL);
			FillLayout fillLayout = new FillLayout();
			shell.setLayout(fillLayout);
			createContent();
		}

		Rectangle bounds = calcBounds(p);
		shell.setBounds(bounds);
		if (! shell.isVisible()) {
			shell.setVisible(true);
			shell.getChildren()[0].setFocus();
		}
	}
	
	public void hide() {
		if (shell != null && ! shell.isDisposed()) {
			shell.setVisible(false);
		}
	}


	/**
	 * Calculate the best position and the size of the popup.

 	 * @param p window location
	 * @return popup bounds
	 */
	protected Rectangle calcBounds(Point p) {
		Rectangle dispBounds = shell.getDisplay().getBounds();
		if (limitDisplay) {
			// Reduce the screen space we work in to avoid shell placement
			// issues on gtk (observed on Linux), specifically that if we place
			// the shell so
			// it covers the GNome taskbar the shell position will be shifted by
			// the system and there is no way to set it where it needs to be
			// even by
			// reducing the size of the shell....weird stuff.
			// Will also help on Windows 7 where the shell otherwise may get
			// under the taskbar.
			// Label popups are not sensitive to a position.
			dispBounds.y += 125;
			dispBounds.height -= 250;
		}

		// calculate a position - either above or below depending how close we
		// are to the edge of the screen
		Point size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point pt = parent.toDisplay(p.x, p.y);

		size.y = Math.min(size.y, dispBounds.height);
		// find the best position
		boolean below = true;
		if (pt.y + size.y > dispBounds.y + dispBounds.height) {
			// doesn't fit below
			if (size.y <= pt.y - dispBounds.y - parent.getLineHeight()) {
				// will fit above - leave it there
				below = false;
			} else {
				// doesn't fit above or below - find where there's most space
				// and place it there
				int spaceBelow = dispBounds.y + dispBounds.height - pt.y;
				int spaceAbove = pt.y - parent.getLineHeight() - dispBounds.y;
				below = spaceBelow >= spaceAbove;
			}
		}
		if (!below) {
			pt.y -= (size.y + parent.getLineHeight());
		}
		Rectangle bounds = new Rectangle(pt.x, pt.y, size.x, size.y);
		// adjust horizontal bounds in case of ridiculous window positions
		if (bounds.x + bounds.width > dispBounds.width + dispBounds.x) {
			bounds.x = dispBounds.width + dispBounds.x - bounds.width;
			bounds.x = Math.max(bounds.x, 0);
		}
		// make sure the popup does not run off the edge of the screen
		bounds.intersect(dispBounds);
		return bounds;

	}

	/**
	 * Resets the tooltip window.
	 * 
	 * @param parent
	 *            the new MultiLineCodeText parent class.
	 * @param p
	 *            the new point.
	 * @param text
	 *            the new tooltip text.
	 */
	protected void reset(StyledText parent, Point p, String text) {
	}
	
	public boolean isDisposed() {
		return shell == null || shell.isDisposed();
	}
} // PopUp
