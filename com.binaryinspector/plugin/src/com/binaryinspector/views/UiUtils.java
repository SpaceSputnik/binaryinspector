package com.binaryinspector.views;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

public class UiUtils {
	public static String [] compare(boolean asDialog, String left, String right, final String leftHeader, 
			final String rightHeader, boolean leftEdidable, boolean rightEditable) {
		final ComparatorInput origInput = new ComparatorInput(left, leftEdidable);
		final ComparatorInput currInput = new ComparatorInput(right, rightEditable);
		
		CompareConfiguration cc = new CompareConfiguration() {
			@Override
			public String getLeftLabel(Object input) {
				return leftHeader;
			}
			@Override
            public String getRightLabel(Object input) {
				return rightHeader;
			}
		};
		
		CompareEditorInput editorInput = new CompareEditorInput(cc) {
			@Override
            protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				Differencer d = new Differencer();
				Object o = d.findDifferences(false, new NullProgressMonitor(), null,
						null, origInput, currInput);
				return o;
			}	
		};
		if (asDialog) {
			CompareUI.openCompareDialog(editorInput);
		} else {
			CompareUI.openCompareEditor(editorInput);
		}
		
		String [] result = new String [2];
		if (leftEdidable) {
			result[0] = origInput.getContent();
		}
		if (rightEditable) {
			result[1] = currInput.getContent();
		}
		return result;
	}
	
    public static int getWitdhInPixels(Control control, String str, boolean bold) {
        GC gc = new GC(control);
        Font font = control.getFont();
        if (bold) {
	        FontData[] fd = font.getFontData();
	        fd[0].setStyle(fd[0].getStyle() | SWT.BOLD);
	        font = new Font(font.getDevice(), fd);
        }
		gc.setFont(font);
        int w = gc.textExtent(str).x;
        gc.dispose();
        return w;
    }
    
    public static void addChildToTreeNode(TreeNode parent, TreeNode child) {
    	child.setParent(parent);
    	TreeNode [] children = parent.getChildren();
    	TreeNode [] newChildren = children == null ? new TreeNode[1] : Arrays.copyOf(children, children.length + 1);
    	newChildren[newChildren.length - 1] = child;
    	child.setParent(parent);
    	parent.setChildren(newChildren);
    }
    
    public static TreeNode findChild(TreeNode parent, Object childValue) {
    	TreeNode[] children = parent.getChildren();
    	if (children == null) {
    		return null;
    	}
		for (TreeNode child : children) {
    		if (childValue.equals(child.getValue())) {
    			return child;
    		}
    	}
    	return null;
    }
    
	public static void setDialigInitialBounds(Shell shell) {
        Monitor monitor = shell.getMonitor();
        Rectangle bounds = monitor.getClientArea();
        int w = bounds.width * 3/4;
        int h = bounds.height * 3/4;
        int x = bounds.x + (bounds.width - w)/2;
        int y = bounds.y + (bounds.height - h)/2; 
        shell.setBounds(x, y, w, h);
   }
}
