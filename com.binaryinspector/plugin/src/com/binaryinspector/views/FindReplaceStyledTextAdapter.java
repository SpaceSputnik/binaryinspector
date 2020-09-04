package com.binaryinspector.views;

import java.util.regex.*;
import java.util.regex.Pattern;

import org.eclipse.jface.preference.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * This class adapts a StyledText to find/replace target interfaces.
 *
 */
public abstract class FindReplaceStyledTextAdapter implements IFindReplaceTarget, IFindReplaceTargetExtension, IFindReplaceTargetExtension3 {
	private StyledText contents;
	
    protected StyleRange scopeRange = null;
	private Color scopeHighlightColor;
	
	public FindReplaceStyledTextAdapter(StyledText contents) {
		this.contents = contents;
		scopeHighlightColor = createColor(EditorsPlugin.getDefault().getPreferenceStore(), AbstractTextEditor.PREFERENCE_COLOR_FIND_SCOPE, PlatformUI.getWorkbench().getDisplay());
	}
	
    private Color createColor(IPreferenceStore store, String key, Display display) {
        RGB rgb = null;
        if (store.contains(key)) {
            rgb = store.isDefault(key) ?
                PreferenceConverter.getDefaultColor(store, key) : PreferenceConverter.getColor(store, key);
            if (rgb != null) {
                return new Color(display, rgb);
            }
        }
        return null;
    }
	
    public Point searchString(String text, int offset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord) {
        if (! caseSensitive) {
            text = text.toLowerCase();
            findString = findString.toLowerCase();
        }
        
        int end = text.length()-1;
        int matchIndex = -1;
        int matchEnd = -1;
        do {
            matchIndex = searchForward ? text.indexOf(findString, offset) : text.lastIndexOf(findString, offset);
            matchEnd = matchIndex+findString.length()-1;
            offset = matchIndex + (searchForward ? 1 : -1);
        } while (wholeWord && (matchIndex >= 0) &&
            ((matchIndex > 0) && (! Character.isWhitespace(text.charAt(matchIndex-1))) ||
            ((matchEnd < end) && (! Character.isWhitespace(text.charAt(matchEnd+1))))));
    
        return (matchIndex >= 0) ? new Point(matchIndex, findString.length()) : null;
    }

    public Point searchRegEx(String text, int offset, String findString, boolean searchForward, boolean caseSensitive) {
        int flags = Pattern.MULTILINE;
        if (! caseSensitive) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        Pattern pattern = Pattern.compile(findString, flags);
        Matcher matcher = pattern.matcher(text);

        int start = -1;
        int length = -1;
        if (searchForward) {
            if (matcher.find(offset) && matcher.group().length() > 0) {
                start = matcher.start();
                length = matcher.group().length();
            }
        } else {
            while (matcher.find() && (matcher.group().length() > 0) && (matcher.start() < offset)) {
                start = matcher.start();
                length = matcher.group().length();
            }
        }

        return (start > 0) ? new Point(start, length) : null;
    }
    @Override
    public int findAndSelect(int offset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {
        StyledTextContent content = contents.getContent();

        String text = (scopeRange == null) ? content.getTextRange(0, content.getCharCount()):
            content.getTextRange(scopeRange.start, scopeRange.length);

        if (offset == -1) {
        	// just wrapped
        	if (scopeRange == null) {
        		offset = searchForward ? 0 : text.length();
        	} else {
        		offset = searchForward ? scopeRange.start : scopeRange.start + scopeRange.length;
        	}
        }
        
        if ((scopeRange != null) && (offset >= 0)) {
            offset = offset - scopeRange.start;
        }

        Point match = regExSearch ?
            searchRegEx(text, offset, findString, searchForward, caseSensitive) :
            searchString(text, offset, findString, searchForward, caseSensitive, wholeWord);
            
        if (match == null) {
            return -1;
        }

        int matchIndex = match.x;
        int matchEnd = matchIndex+match.y;
        if (matchEnd > text.length()) {
            return -1;
        }

        if (scopeRange != null) {
            matchIndex += scopeRange.start;
            matchEnd += scopeRange.start;
        }
        contents.setSelection(matchIndex, matchEnd);
        return matchIndex;
    }

	@Override
    public Point getLineSelection() {
		Point p = contents.getSelection();
        return new Point(p.x, p.y - p.x);
    }

    private void replaceSelection(String text, boolean regExReplace, Point allowedRange) {
        Point selection = contents.getSelection();
        if (selection.y > selection.x) {
        	if (allowedRange != null) {
        		allowedRange.x = Math.max(allowedRange.x, 0);
        		allowedRange.y = allowedRange.y < 0 ? contents.getText().length() : allowedRange.y;
        		if (! (selection.x >= allowedRange.x && selection.y <= allowedRange.y)) {
        			// no content changes outside of allowedRange
        			return;
        		}
        	}
            contents.replaceTextRange(selection.x, (selection.y - selection.x), text);
            contents.setSelection(selection.x, selection.x+text.length());
        }
    }

	@Override
    public void replaceSelection(String text, boolean regExReplace) {
    	replaceSelection(text, regExReplace, null);
    }
    
	@Override
    public IRegion getScope() {
        return (scopeRange == null) ? null : new Region(scopeRange.start, scopeRange.length);
    }
    
    private void clearScope() {
        if (scopeRange != null) {
            scopeRange.background = null;
            contents.setStyleRange(scopeRange);
            scopeRange = null;
        }
    }
    
    private void setTextScope(IRegion scope) {
        clearScope();
        if (scope == null) {
            return;
        }

        scopeRange = new StyleRange(scope.getOffset(), scope.getLength(), null, scopeHighlightColor, SWT.NORMAL);
        contents.setStyleRange(scopeRange);
        contents.setSelection(scope.getOffset(), scope.getOffset());
    }
    
	@Override
	public void setScope(IRegion scope) {
        setTextScope((scope == null) ? scope : new Region(scope.getOffset(), scope.getLength()));
	}

	@Override
    public void beginSession() {
        scopeRange = null;
    }

	@Override
    public void endSession() {
        clearScope();
    }
	
	@Override
    public void setScopeHighlightColor(Color color) {
        scopeHighlightColor = color;
    }

	@Override
	public void setReplaceAllMode(boolean replaceAll) {
	}

	@Override
    public String getSelectionText() {
        if ((contents == null) || contents.isDisposed()) {
            return null;
        }
        return contents.getSelectionText();
    }

	public void setSelection(int offset, int length) {
		contents.setSelection(offset, offset + length);
	}

	@Override
	public boolean canPerformFind() {
		return true;
	}

	@Override
    public int findAndSelect(int widgetOffset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord) {
        return findAndSelect(widgetOffset, findString, searchForward, caseSensitive, wholeWord, false);
    }

	@Override
	public Point getSelection() {
		return getLineSelection();
	}

	@Override
	public void replaceSelection(String text) {
		replaceSelection(text, false, null);
	}
	
	@Override
	public abstract boolean isEditable();
}
	
