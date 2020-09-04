package com.binaryinspector.views;

import java.io.*;
import java.util.*;

import org.eclipse.core.runtime.preferences.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

import com.binaryinspector.Activator;
import com.binaryinspector.decoders.*;
import com.binaryinspector.decoders.Decoder.SearchMode;
import com.binaryinspector.views.widgets.TablePopup;

public class MdaComposite extends Composite {
	private static final Image SAVE_IMAGE = Activator.getImageDescriptor("icons/save_edit.gif").createImage();

	private static final String NO_SEARCH_RESULTS = "No search results";

	static final String PREF_ENTRY = "data";

	private static final String DESCR_END = ":";

	private static final String PREF_ENTRY_SEARCH_RESULTS_HIGHLIGHT = "searchResultsHighlight";
	private static final String PREF_ENTRY_SEARCH_RESULTS_OFFET_HEX = "searchResultsOffsetHex";
	private static final String PREF_ENTRY_SEARCH_RESULTS_LEN_HEX = "searchResultsLenHex";

	private StyledText hexData;
	private TablePopup<DecodeResult> popup = null;
	private StyledText resultsText;
	private Label searchResultLabel;
	private Table searchResultsTable;
	private TableViewer searchResultsViewer;
	private final MdaView mdaView;
	private Button nextSearchResultButton;
	private Button prevSearchResultButton;
	private Button highLightSearchResultsCheckbox;
	private Button searchResultOffsetHexCheckbox;
	private Button searchResultByteLengthHexCheckbox;
	private Button clearSearchResultButton;

	private Color matchBackground;

	private Font font;
	
	private Base64Action base64Action = new Base64Action();
	private GenerateTextAction generateTextAction = new GenerateTextAction(); 
	private LoadFromFileAction loadFileAction = new LoadFromFileAction();
	private ToBase64Action toBase64Action = new ToBase64Action();
	private HexDelimAction addHexDelimAction = new HexDelimAction(true);
	private HexDelimAction removeHexDelimAction = new HexDelimAction(false);
	private HexCaseAction uppercaseHexAction = new HexCaseAction(true);
	private HexCaseAction lowercaseHexAction = new HexCaseAction(false);
	private HexCompareAction compareHexAction;
	private TextCompareAction textCompareAction = new TextCompareAction();
	private FindLosslessCodepageAction findLosslessCodepage = new FindLosslessCodepageAction();
	private WrapLinesAction wrapLineAction;
	private GoAndSelectToolbarAction goAndSelectToolbarAction = new GoAndSelectToolbarAction();
	private ShowGoAndSelectDialogAction showGoAndSelectDialogAction = new ShowGoAndSelectDialogAction();
	private ShowSearchDialogAction showSearchDialogAction = new ShowSearchDialogAction();
	private ConfigureDecodersAndDecodeAction configureDecodersAction = new ConfigureDecodersAndDecodeAction();
	private ToggleDiscoveryModeAction toggleDiscoveryModeAction = new ToggleDiscoveryModeAction();
	
	private FindReplaceStyledTextAdapter findReplaceAdapter;
	private Base64ConvertingListener base64ConvertingListener;

	private TableEditor searchResultTableEditor = null;
	
	private PopupMouseMoveListener popupMouseListener = new PopupMouseMoveListener();
	private boolean discoveryMode = false;

	public IAction[] getToolbarActions() {
		return new IAction[] {toggleDiscoveryModeAction, goAndSelectToolbarAction, showSearchDialogAction, configureDecodersAction, 
				generateTextAction, compareHexAction};
	}
	
	public IAction[] getPulldownActions() {
		return new IAction[] {toggleDiscoveryModeAction, showGoAndSelectDialogAction, showSearchDialogAction, null, 
				configureDecodersAction, null, base64Action, toBase64Action, null, loadFileAction, null, addHexDelimAction, 
				removeHexDelimAction, null, uppercaseHexAction, lowercaseHexAction,	wrapLineAction, null, generateTextAction, 
				findLosslessCodepage, compareHexAction, textCompareAction};
	}

	public MdaComposite(MdaView mdaView, Composite parent, int style) {
		super(parent, style);
		this.mdaView = mdaView;
		init(parent);
	}

	private void init(Composite parent) {
		matchBackground = new Color(getDisplay(), new RGB(0xBD, 0xBD, 0xBD));
		
		setLayout(new FillLayout());
		
		SashForm sashForm = new SashForm(this, SWT.VERTICAL);
		
		hexData = new StyledText(sashForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		
		compareHexAction = new HexCompareAction(hexData);
		wrapLineAction = new WrapLinesAction(hexData);
		
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID); 
		String s = prefs.get(PREF_ENTRY, "");
		hexData.setText(s);
		
		base64ConvertingListener = Base64ConvertingListener.add(hexData);
		hexData.addModifyListener(new EntrySavingModifyListener(PREF_ENTRY));
		
//		hexData.addMouseTrackListener(new MouseTrackAdapter() {
//			@Override
//			public void mouseHover(MouseEvent e) {
//				if (hexData.getSelectionCount() == 0) {
//					showPopup(hexData.getSelection().x);
//				}
//			}
//		});
		
		hexData.addCaretListener(new CaretListener() {
			@Override
			public void caretMoved(CaretEvent event) {
				hexData.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						setStatusLine();
						decodeSelection();
						if (discoveryMode) {
							underlineMatches(hexData.getSelection().x, true);
							showPopupAtCursor();
						}
					}
				});
			}
		});
		
		hexData.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				hexData.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						setStatusLine();
						decodeSelection();
						if (discoveryMode) {
							underlineMatches(hexData.getSelection().x, true);
							showPopupAtCursor();
						}
					}
				});
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		hexData.addMouseListener(popupMouseListener);
		hexData.addMouseMoveListener(popupMouseListener);

		hexData.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					hidePopup();
				} else if (e.keyCode == SWT.CTRL) {
					underlineMatches(discoveryMode);
				}
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (! discoveryMode) {
					resetHexDataStyles();
				}
			}
		});
		
		SashForm lowerArea = new SashForm(sashForm, SWT.HORIZONTAL);
		
		resultsText = new StyledText(lowerArea, SWT.CENTER | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		
		Composite searchResultsComposite = new Composite(lowerArea, SWT.NO_TRIM);
		searchResultsComposite.setLayout(new GridLayout(4, false));
		
		searchResultLabel = new Label(searchResultsComposite, SWT.NONE);
		searchResultLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		searchResultLabel.setText(NO_SEARCH_RESULTS);
		
		searchResultsTable = new Table(searchResultsComposite, SWT.FULL_SELECTION);
		searchResultsTable.setHeaderVisible(true);
		searchResultsTable.setLinesVisible(true);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 
				((GridLayout)searchResultsComposite.getLayout()).numColumns, 1);
		gd.minimumHeight = 30;
		gd.minimumWidth = 40;
		searchResultsTable.setLayoutData(gd);
		
		int index = 0;
		TableColumn column = new TableColumn(searchResultsTable, SWT.CENTER, index++);	
		column.setText("");
		column.setWidth(SAVE_IMAGE.getBounds().width + 20);
		column.setAlignment(SWT.CENTER);

		column = new TableColumn(searchResultsTable, SWT.LEFT, index);	
		column.setText("Offset");
		column.setWidth(UiUtils.getWitdhInPixels(searchResultsTable, "WWWWW", false) + 30);
		column.addSelectionListener(new SearchResultColumnListener(index++));
		
		column = new TableColumn(searchResultsTable, SWT.LEFT, index);	
		column.setText("Byte Length");
		column.setWidth(UiUtils.getWitdhInPixels(searchResultsTable, "WWWWW", false) + 30);
		column.addSelectionListener(new SearchResultColumnListener(index++));

		column = new TableColumn(searchResultsTable, SWT.LEFT, index);	
		column.setText("Value");
		column.setWidth(UiUtils.getWitdhInPixels(searchResultsTable, "WWWWWWWW", false) + 30);
		column.addSelectionListener(new SearchResultColumnListener(index++));
		
		column = new TableColumn(searchResultsTable, SWT.LEFT, index);	
		column.setText("Decoder");
		column.setWidth(UiUtils.getWitdhInPixels(searchResultsTable, "WWWWWWWWWWWWWWWWWWWWWW", false) + 30);
		column.addSelectionListener(new SearchResultColumnListener(index++));		
		
		searchResultsTable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selection = searchResultsTable.getSelection();
				if (selection.length == 1) {
					createSearchResultTableEditor(selection[0], 0);
				}
				
				TableItem ti = selection[0];
				SearchResult sr = (SearchResult)ti.getData();
				selectBytesByOffset(sr.offset, sr.byteLength);
				decodeSelection();
				hidePopup();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		searchResultsViewer = new TableViewer(searchResultsTable);
		searchResultsViewer.setContentProvider(new ArrayContentProvider());
		searchResultsViewer.setLabelProvider(new SearchResultsLabelProvider());
		searchResultsViewer.setSorter(new SearchResultSorter());

		Composite searchControlsComposite = new Composite(searchResultsComposite, SWT.NO_TRIM);
		searchControlsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout layout = new GridLayout(6, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		searchControlsComposite.setLayout(layout);
		
		nextSearchResultButton = new Button(searchControlsComposite, SWT.PUSH | SWT.FLAT);
		nextSearchResultButton.setImage(Activator.getImageDescriptor("icons/down.gif").createImage());
		nextSearchResultButton.setToolTipText("Next match");
		nextSearchResultButton.setEnabled(false);
		nextSearchResultButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchResultTraverse(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		prevSearchResultButton = new Button(searchControlsComposite, SWT.PUSH | SWT.FLAT);
		prevSearchResultButton.setImage(Activator.getImageDescriptor("icons/up.gif").createImage());
		prevSearchResultButton.setToolTipText("Previous match");
		prevSearchResultButton.setEnabled(false);
		prevSearchResultButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchResultTraverse(false);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		highLightSearchResultsCheckbox = new Button(searchControlsComposite, SWT.CHECK);
		highLightSearchResultsCheckbox.setText("Highlight");
		highLightSearchResultsCheckbox.setSelection(prefs.getBoolean(PREF_ENTRY_SEARCH_RESULTS_HIGHLIGHT, true));
		highLightSearchResultsCheckbox.addSelectionListener(new EntrySavingSelectionListener(PREF_ENTRY_SEARCH_RESULTS_HIGHLIGHT));
		highLightSearchResultsCheckbox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				highlightSearchMatches(highLightSearchResultsCheckbox.getSelection());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		searchResultOffsetHexCheckbox = new Button(searchControlsComposite, SWT.CHECK);
		searchResultOffsetHexCheckbox.setText("Hex offset");
		searchResultOffsetHexCheckbox.setSelection(prefs.getBoolean(PREF_ENTRY_SEARCH_RESULTS_OFFET_HEX, false));
		searchResultOffsetHexCheckbox.addSelectionListener(new EntrySavingSelectionListener(PREF_ENTRY_SEARCH_RESULTS_OFFET_HEX));
		searchResultOffsetHexCheckbox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchResultsViewer.refresh();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		searchResultByteLengthHexCheckbox = new Button(searchControlsComposite, SWT.CHECK);
		searchResultByteLengthHexCheckbox.setText("Hex byte length");
		searchResultByteLengthHexCheckbox.setSelection(prefs.getBoolean(PREF_ENTRY_SEARCH_RESULTS_LEN_HEX, false));
		searchResultByteLengthHexCheckbox.addSelectionListener(new EntrySavingSelectionListener(PREF_ENTRY_SEARCH_RESULTS_LEN_HEX));
		searchResultByteLengthHexCheckbox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchResultsViewer.refresh();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		clearSearchResultButton = new Button(searchControlsComposite, SWT.PUSH);
		clearSearchResultButton.setText("Clear");
		clearSearchResultButton.setEnabled(false);
		clearSearchResultButton.setLayoutData(new GridData(SWT.RIGHT, SWT.DEFAULT, true, false));
		clearSearchResultButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clearSearchResults();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		lowerArea.setWeights(new int[] {5, 4});
		sashForm.setWeights(new int[] {3, 7});
		
		font = JFaceResources.getTextFont();		

		hexData.setFont(font);
		resultsText.setFont(font);
		resultsText.setEditable(false);
		
		findReplaceAdapter = new FindReplaceStyledTextAdapter(hexData) {
			@Override
			public boolean isEditable() {
				return true;
			}
		};
		
		setStatusLine();
	}
	
	public FindReplaceStyledTextAdapter getFindReplaceAdapter() {
		return findReplaceAdapter;
	}

	public void showPopupAtCursor() {
		showPopup(hexData.getSelection().x, discoveryMode);
	}

	private void showPopup(final int offset, boolean showValidOnly) {
		int end = 0;
        
		ArrayList<DecodeResult> results = new ArrayList<DecodeResult>();
		for (Decoder d : DecodeUtils.decodeFactory.getFixedLengthDecoders()) {
			int bl = d.getByteLength();
			if (bl > 0) {
				byte [] bytes = null;
				try {
					Point p = DecodeUtils.searchBytePositions(hexData.getText(), 0, offset, bl, true);
					end = Math.max(end, p.y);
					String hex = hexData.getText().substring(p.x, p.y);
					bytes = DecodeUtils.parseHexString(hex);
				} catch (EnvelopeException e) {}
				
				DecodeResult res = d.decode(bytes);
				if (! showValidOnly || showValidOnly && res.isSuccess()) {
					res.setDecoder(d);
					res.setDescription(d.toString());
					results.add(res);
				}
			}
		}
		
        if (popup == null || popup.isDisposed()) {
        	popup = new TablePopup<DecodeResult>(hexData, new int[]{300, 500});
        }
		
		popup.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DecodeResult r = popup.getSelection();
				if (r != null) {
					Decoder d = r.getDecoder();
					Point p = null;
					try {
						p = DecodeUtils.searchBytePositions(hexData.getText(), 0, offset, d.getByteLength(), true);
						hexData.setSelection(p);
						decodeSelection();
					} catch (EnvelopeException e1) {}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		popup.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				resetHexDataStyles();
				TableItem item = popup.getTable().getItem(new Point(e.x, e.y));
				if (item != null) {
					DecodeResult dr = (DecodeResult)item.getData();
					Point p = null;
					try {
						p = DecodeUtils.searchBytePositions(hexData.getText(), 0, offset, dr.getDecoder().getByteLength(), true);
						StyleRange sr = new StyleRange();
						sr.foreground = getDisplay().getSystemColor(dr.isSuccess() ? SWT.COLOR_BLUE : SWT.COLOR_RED);
						sr.underline = true;
						sr.start = p.x;
						sr.length = p.y - p.x;
						hexData.setStyleRange(sr);
					} catch (EnvelopeException e1) {}
				}
			}
		});
		
		popup.setItems(results);
		
        Point endLocation = hexData.getLocationAtOffset(end);
        int lineIndex = hexData.getLineAtOffset(offset);
        endLocation.y += hexData.getLineHeight(lineIndex) + 4;
        Point startLocation = hexData.getLocationAtOffset(offset);
        Point popupLocation = new Point(startLocation.x, endLocation.y);

		popup.show(popupLocation);
        hexData.setFocus();
	}

	private void hidePopup() {
		if (popup != null) {
			popup.hide();
		}
	}
	
	public void decodeSelection() {
		resultsText.setText("");
		String selected = hexData.getSelectionText();
		if (selected.length() == 0) {
			return;
		}
		java.util.List<DecodeResult> results = DecodeUtils.decode(selected);
		
		for (DecodeResult res : results) {
			showResult(res);
		}
	}

	private void showResult(DecodeResult res) {
		int from = resultsText.getCharCount();
		String description = res.getDescription();
		if (description != null && description.length() > 0) {
			resultsText.append(description);
			resultsText.append(DESCR_END);
			StyleRange styleRange = new StyleRange();
			styleRange.start = from;
			styleRange.length = description.length() + DESCR_END.length();
			styleRange.foreground = getDisplay().getSystemColor(SWT.COLOR_BLACK);
			styleRange.background = null;
			styleRange.fontStyle = SWT.NORMAL;
		
			resultsText.setStyleRange(styleRange);
			
		}
		resultsText.append("\n");				
		
		from = resultsText.getCharCount();
		String value = res.getValue();
		resultsText.append(value);
		StyleRange styleRange = new StyleRange();
		styleRange.start = from;
		styleRange.length = value.length();
		styleRange.foreground = getDisplay().getSystemColor(res.isSuccess() ? SWT.COLOR_BLUE : SWT.COLOR_RED);
		styleRange.background = null;
		styleRange.fontStyle = SWT.NORMAL;
		resultsText.setStyleRange(styleRange);

		resultsText.append("\n");
	}
	
	public void setStatusLine() {
		setStatusLine(hexData.getSelection().x);
	}
	
	public void setStatusLine(int selStart) {
		int carOffset = hexData.getSelection().x;
		int selLen = hexData.getSelection().y - hexData.getSelection().x;
		int selectionLen = carOffset > selStart ? -selLen : selLen;
		
		int lineNum = hexData.getLineAtOffset(carOffset);
		String s = "Line: " + lineNum;
		
		int bytePos = DecodeUtils.findByteByTextPos(hexData.getText(), carOffset);
		
		if (bytePos > -1) {
			s += (", Offset: " + bytePos);
		}

		if (selectionLen < 0) {
			carOffset += selectionLen;
			selectionLen = -selectionLen;
		}
		boolean validBytesSelected = true;
		if (selectionLen > 1) {
			String selected = hexData.getSelectionText();
			try {
				byte[] bytes = DecodeUtils.parseHexString(selected);
				s += (", " + bytes.length  + " Bytes Selected");
			} catch (EnvelopeException e) {
				validBytesSelected = false;
			}
		}
		
		mdaView.setStatisLine(s, bytePos == -1 || ! validBytesSelected);
	}

	public String performGoAndSelect(GoAndSelectEntry entry) {
		int bytePosStart = entry.offset;
		int fromPos = entry.relative ? hexData.getSelection().x : 0;
		
		try {
			Point res = DecodeUtils.searchBytePositions(hexData.getText(), bytePosStart, fromPos, entry.byteLength, false);
			hexData.setSelection(res.x, res.y);
			setStatusLine();
			decodeSelection();
			return null;
		} catch (EnvelopeException e) {
			return e.getMessage();
		}
	}
	
	public void clearSearchResults() {
		cancelEditing();
		searchResultLabel.setText(NO_SEARCH_RESULTS);
		searchResultsViewer.setInput(new ArrayList<SearchResult>());
		searchResultsViewer.refresh();
		highlightSearchMatches(false);
		hexData.setSelection(hexData.getSelection().x);
		nextSearchResultButton.setEnabled(false);
		prevSearchResultButton.setEnabled(false);
		clearSearchResultButton.setEnabled(false);
		searchResultsTable.setSortColumn(null);
	}
	
	public String performSearch(String searchValue, SearchMode searchMode) {
		// TODO: optimize
		highlightSearchMatches(false);
		cancelEditing();
		
		searchResultLabel.setText("Search results for \"" + searchValue + "\"");
		hexData.setSelection(hexData.getSelection().x);
		
		String hexDigits = hexData.getText();
		byte[] binary;
		try {
			binary = DecodeUtils.parseHexString(hexDigits);

			int offset = -1;
			switch (searchMode) {
			case FORWARD_FROM_CURRENT:
			case BACKWARD_FROM_CURRENT:
				offset = DecodeUtils.findByteByTextPos(hexDigits, hexData.getSelection().x);
			}
			ArrayList<SearchResult> allMatches = new ArrayList<SearchResult>();
			
			for (Decoder d : DecodeUtils.decodeFactory.getEnabledDecoders()) {
				if (! d.isIncludeInSearch() || d.getTag() != null) {
					continue;
				}
				Collection<SearchResult> matches = d.search(searchValue, binary, offset, searchMode);
				if (matches == null) {
					continue;
				}
				allMatches.addAll(matches);
			}
			
			if (allMatches.size() > 0) {
				SearchResult firstSr = allMatches.iterator().next();
				selectBytesByOffset(firstSr.offset, firstSr.byteLength);
				decodeSelection();
			}

			int sortOrder = SearchResultSorter.ASCENDING;
			switch (searchMode) {
			case BACKWARD_FROM_CURRENT:
			case BACKWARD_FROM_END:
				sortOrder = SearchResultSorter.DESCENDING;
			}
			((SearchResultSorter)searchResultsViewer.getSorter()).doSort(1, sortOrder);
			searchResultsViewer.setInput(allMatches);
			searchResultsViewer.refresh();
			
			highlightSearchMatches(true);
			
			boolean enabled = allMatches.size() > 1;
			prevSearchResultButton.setEnabled(enabled);
			nextSearchResultButton.setEnabled(enabled);
			
			clearSearchResultButton.setEnabled(allMatches.size() > 0);
			
		} catch (EnvelopeException e) {
			return e.getMessage();
		}
		return null;
	}

	public void highlightSearchMatches(boolean highlight) {
		Object o = searchResultsViewer.getInput();
		try {
			if (highlight && highLightSearchResultsCheckbox.getSelection() && o != null) {
				@SuppressWarnings("unchecked")
				ArrayList<SearchResult> matches = (ArrayList<SearchResult>)o;
				for (SearchResult match : matches) {
					// highlight
					StyleRange style = new StyleRange();
					style.background = matchBackground;
					Point textPos = DecodeUtils.searchBytePositions(hexData.getText(), match.offset, 0, match.byteLength, true);
					style.start = textPos.x;
					style.length = textPos.y - textPos.x;
					hexData.setStyleRange(style);
				}
			} else {
				resetHexDataStyles();
			}
		} catch (EnvelopeException e) {
			// that's ok, it's already reported
		}
	}

	private void resetHexDataStyles() {
		// clear highlighting
		StyleRange style = new StyleRange();
		style.start = 0;
		style.length = hexData.getCharCount();
		hexData.setStyleRange(style);
	}

	protected void searchResultTraverse(boolean next) {
		Object o = searchResultsViewer.getInput();
		if (o == null) {
			return;
		}
		int index = searchResultsTable.getSelectionIndex();
		int lastIndex = searchResultsTable.getItemCount() - 1;
		if (index < 0) {
			index = next ? 0 : lastIndex;
		} else {
			if (next) {
				index = index < lastIndex ? index + 1 : 0;
			} else {
				index = index > 0 ? index - 1 : lastIndex;
			}
		}
		searchResultsTable.setSelection(index);
		SearchResult match = (SearchResult)searchResultsTable.getItem(index).getData();
		selectBytesByOffset(match.offset, match.byteLength);
	}
	
	public void selectBytesByOffset(int offset, int byteLength) {
		try {
			Point res = DecodeUtils.searchBytePositions(hexData.getText(), offset, 0, byteLength, false);
			hexData.setSelection(res.x, res.y);
			setStatusLine(); 
		}
		catch (EnvelopeException e) {
			// that's fine, already reported
		}
	}

	public void createSearchResultTableEditor(final TableItem tableItem, int column) {
		cancelEditing();
		Composite container = new Composite(searchResultsTable, SWT.NO_TRIM | SWT.TRANSPARENT);
		//container.setBackground(searchResultsTable.getBackground());
		GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		container.setLayout(gl);
		
		Cursor handCursor = new Cursor(container.getDisplay(), SWT.CURSOR_HAND);

		Button chooseButton = new Button(container, SWT.NO_TRIM);
		chooseButton.setBackground(searchResultsTable.getBackground());
		chooseButton.setImage(SAVE_IMAGE);
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = SWT.CENTER;

		chooseButton.setLayoutData(layoutData);
		chooseButton.setCursor(handCursor);
		chooseButton.setToolTipText("Remember this entry");

		chooseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				createRegionFromSearchResult((SearchResult)tableItem.getData());
			}
		});
		searchResultTableEditor = new TableEditor(searchResultsTable);
		searchResultTableEditor.horizontalAlignment = SWT.CENTER;
		searchResultTableEditor.grabHorizontal = true;
		searchResultTableEditor.setEditor(container, tableItem, column);
	}

	protected void createRegionFromSearchResult(SearchResult sr) {
		Collection<GoAndSelectEntry> entries = GoAndSelectEntryFactory.read();
		Collection<GoAndSelectEntry> newEntries = new ArrayList<GoAndSelectEntry>();
		String namePrefix = sr.decoder.getName() + " value \"" + sr.actualValue + "\"";
		String name = namePrefix;
		
		GoAndSelectEntry entry = new GoAndSelectEntry(name, sr.offset, false, sr.byteLength);
		for (GoAndSelectEntry e : entries) {
			if (e.equals(entry) && e.name.equals(name)) {
				// exact same entry exists
				return;
			}
		}
		
		// add under unique name
		int clashCount = 0;
		while(true) { 
			boolean clash = false;
			for (GoAndSelectEntry e : entries) {
				if (name.equalsIgnoreCase(e.name)) {
					clash = true;
					break;
				}
			}
			if (! clash) {
				break;
			}
			name = namePrefix + "#" + ((clashCount++) + 2);
		}
		entry = new GoAndSelectEntry(name, sr.offset, false, sr.byteLength);
		newEntries.add(entry);
		newEntries.addAll(entries);
		GoAndSelectEntryFactory.write(newEntries);
	}

	public void cancelEditing() {
		if (searchResultTableEditor != null) {
			Control c = searchResultTableEditor.getEditor();
			if (c != null && !c.isDisposed()) {
				c.dispose();
				searchResultTableEditor.setEditor(null);
			}
		}
	}
		
	private void underlineMatches(boolean validOnly) {
		Point mouseLocation = getDisplay().getCursorLocation();
		mouseLocation = hexData.toControl(mouseLocation);
		try {
			int offset = hexData.getOffsetAtLocation(mouseLocation);
			underlineMatches(offset, validOnly);
		} catch (IllegalArgumentException e) {}
	}
	
	private void underlineMatches(int offset, boolean validOnly) {
		resetHexDataStyles();
		int len = 0;
		int start = -1;
		int successCount = 0;
		for (Decoder d : DecodeUtils.decodeFactory.getFixedLengthDecoders()) {
			int bl = d.getByteLength();
			if (bl > 0) {
				try {
					// TODO: use search ?
					Point p = DecodeUtils.searchBytePositions(hexData.getText(), 0, offset, bl, true);
					start = p.x;
					String hex = hexData.getText().substring(p.x, p.y);
					byte [] bytes = DecodeUtils.parseHexString(hex);
					DecodeResult dr = d.decode(bytes);
					
					if (! validOnly || (validOnly && dr.isSuccess())) {
						if (dr.isSuccess()) {
							successCount++;
						}
						start = p.x;
						len = Math.max(len, p.y - p.x);
					}
				} catch (EnvelopeException ex) {}
			}
		}
		if (start < hexData.getCharCount()) {
			StyleRange range = new StyleRange();
			//range.foreground = getDisplay().getSystemColor(SWT.COLOR_BLUE);
			range.underline = true;
			range.start = start;
			range.length = len;
			// underline in red if all failed, blue if all succeeded or leave the color unchanged
			// if some failed and some succeeded
			if ((validOnly && successCount > 0) || successCount == DecodeUtils.decodeFactory.getFixedLengthDecoders().size()) {
				range.foreground = getDisplay().getSystemColor(SWT.COLOR_BLUE);
			} else if (successCount == 0) {
				range.foreground = getDisplay().getSystemColor(SWT.COLOR_RED);
			}
			hexData.setStyleRange(range);
		}

	}

	private final class PopupMouseMoveListener extends MouseAdapter implements MouseMoveListener {
		private boolean needModifier = true;
		
		private boolean doIt(MouseEvent e) {
			return ! needModifier || needModifier && (e.stateMask & SWT.CTRL) == SWT.CTRL;
		}
		
		@Override
		public void mouseMove(MouseEvent e) {
			if (doIt(e)) {
				underlineMatches(discoveryMode);
			} else {
				resetHexDataStyles();
			}
		}
		@Override
		public void mouseDown(MouseEvent e) {
			if (doIt(e)) {
				try {
					int offset = hexData.getOffsetAtLocation(new Point(e.x, e.y));
					showPopup(offset, discoveryMode);
				} catch (IllegalArgumentException ex) {}
			} else {
				hidePopup();
			}
		}
		
		public void setNeedModifier(boolean needModifier) {
			this.needModifier = needModifier;
		}
	}
	
	public class Base64Action extends Action {
		@Override
		public void run() {
			Base64ConvertingListener.convertBase64(hexData);
		}
		
		public String getText() { 
			return "Convert from base64Binary";
		};
	}
	
	public class ToBase64Action extends Action {
		@Override
		public void run() {
			base64ConvertingListener.convertToBase64(hexData);
		}
		
		public String getText() { 
			return "Convert to base64Binary";
		};
	}
	
	public class GenerateTextAction extends Action {
		GenerateTextAction() {
			setImageDescriptor(Activator.getImageDescriptor("icons/bytesFromText.png"));
			setToolTipText("Get byte representation of text using a specified charset");
		}
		
		@Override
		public void run() {
			new GenerateTextInputDialog(getShell()).open();
		}
		
		public String getText() { 
			return "Bytes from Text";
		};
	}
	
	public class HexDelimAction extends Action {
		private boolean delimit = false;
		
		public HexDelimAction(boolean delimit) {
			this.delimit = delimit;
		}
		
		@Override
		public void run() {
			String text = hexData.getText();
			byte[] bytes = null;
			try {
				bytes = DecodeUtils.parseHexString(text);
			} catch (EnvelopeException e) {
				DecodeResult res = new DecodeResult();
				res.setError(e.getMessage());
				showResult(res);
			}
			Point sel = hexData.getSelection();
			hexData.setText(DecodeUtils.getHex(bytes, delimit));
			hexData.setSelection(sel);
		}
		
		public String getText() { 
			return delimit ? "Add Hex Delimiters" : "Remove Hex Deimiters";
		};
	}
	
	public class HexCaseAction extends Action {
		private boolean uppercase = false;
		
		public HexCaseAction(boolean uppercase) {
			super(uppercase ? "To Uppercase" : "To Lowercase");
			this.uppercase = uppercase;
		}
		
		@Override
		public void run() {
			Point sel = hexData.getSelection();
			String text = hexData.getText();
			hexData.setText(uppercase ? text.toUpperCase() : text.toLowerCase());
			hexData.setSelection(sel);
		}
	}
	
	public class HexCompareAction extends Action {
		private StyledText text;
		
		public HexCompareAction(StyledText text) {
			this.text = text;
			setImageDescriptor(Activator.getImageDescriptor("icons/compareBytes.gif"));
			setToolTipText("Compare the byte area");
		}
		
		@Override
		public void run() {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			ByteAreaCompareInputDialog d = new ByteAreaCompareInputDialog(shell, true);
			d.create();
			if (d.open() == Window.OK) {
				byte[] bytes1;
				try {
					bytes1 = DecodeUtils.parseHexString(hexData.getText());
					byte[] bytes2 = DecodeUtils.parseHexString(d.getData());
					String t1 = DecodeUtils.getHex(bytes1, true);
					String t2 = DecodeUtils.getHex(bytes2, true);
					UiUtils.compare(true, t1, t2, "Current Data", "External Data", false, true);
				} catch (EnvelopeException e) {
			        MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR);
			        messageBox.setText("Error");
			        messageBox.setMessage(e.getMessage());
			        messageBox.open();
				}
			}
		}
		
		public String getText() { 
			return "Compare Bytes to...";
		};
	}
	
	public class TextCompareAction extends Action {
		
		public TextCompareAction() {
			setImageDescriptor(Activator.getImageDescriptor("icons/compareText.gif"));
			setToolTipText("Open Text Compare View");
		}
		
		@Override
		public void run() {
			try {
				IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				activePage.showView(TextCompareView.ID);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
		
		public String getText() { 
			return "Text Compare";
		};
	}
	
	public class FindLosslessCodepageAction extends Action {
		public FindLosslessCodepageAction() {}
		
		@Override
		public void run() {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			LosslesCodepageInputDialog d = new LosslesCodepageInputDialog(shell);
			d.open();
		}
		
		public String getText() { 
			return "Find lossless charsets...";
		};
	}
	
	
	public class WrapLinesAction extends Action {
		private static final String PREF_KEY = "wrap";
		private StyledText text;
		
		public WrapLinesAction(StyledText text) {
			super("Wrap Text", AS_CHECK_BOX);
			this.text = text;
			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID); 
			String s = prefs.get(PREF_KEY, "");
			setChecked(Boolean.TRUE.toString().equals(s));
			adjustText();
		}

		@Override
		public void run() {
			adjustText();
			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID); 
			prefs.put(PREF_KEY, String.valueOf(isChecked()));
		}

		public void adjustText() {
			text.setWordWrap(isChecked());
		}
	}
	
	public class ShowGoAndSelectDialogAction extends Action {
		public ShowGoAndSelectDialogAction() {
			setImageDescriptor(Activator.getImageDescriptor("icons/goto_input.gif"));
			setToolTipText("Go to offset and select bytes");
		}

		@Override
		public void run() {
			GoAndSelectDialog d = new GoAndSelectDialog(getShell()) {
				@Override
				public String go() {
					return performGoAndSelect(getEntry());
				}
			};
			d.open();
		}
		
		public String getText() { 
			return "Go to...";
		};
	}

	
	public class GoAndSelectToolbarAction extends Action implements IMenuCreator{
		private Menu menu;
		
		public GoAndSelectToolbarAction() {
			setMenuCreator(this);
			setImageDescriptor(Activator.getImageDescriptor("icons/goto_input.gif"));
			setToolTipText("Go to offset and select bytes");
		}

		public void dispose() {
			if (menu != null) {
				menu.dispose();
				menu = null;
			}
		}

		public Menu getMenu(Menu parent) {
			return null;
		}

		public Menu getMenu(Control parent) {
			if (menu != null)
				menu.dispose();

			menu = new Menu(parent);
			
			Collection<GoAndSelectEntry> entries = GoAndSelectEntryFactory.read();
			for (GoAndSelectEntry entry : entries) {
				final GoAndSelectEntry ent = entry;
				Action action = new Action(entry.toString()) {
					public void run() {
						String error = performGoAndSelect(ent);
						if (error != null) {
					        MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR);
					        messageBox.setText("Error");
					        messageBox.setMessage(error);
					        messageBox.open();
						}
					}
				};	
				addActionToMenu(menu, action);
			}
			if (entries.isEmpty()) {
				Action dummy = new Action("(no entries)") {
					public void run() {}
				};
				dummy.setEnabled(false);
				addActionToMenu(menu, dummy);
			};
			new MenuItem(menu, SWT.SEPARATOR);
			addActionToMenu(menu, showGoAndSelectDialogAction);

			return menu;
		}

		protected void addActionToMenu(Menu parent, Action action) {
			ActionContributionItem item = new ActionContributionItem(action);
			item.fill(parent, -1);
		}

		@Override
		public void run() {
			GoAndSelectDialog d = new GoAndSelectDialog(getShell()) {
				@Override
				public String go() {
					return performGoAndSelect(getEntry());
				}
			};
			d.open();
		}
		
		public String getText() { 
			return "Go to...";
		};
	}
	
	class ShowSearchDialogAction extends Action {
		public ShowSearchDialogAction() {
			setImageDescriptor(Activator.getImageDescriptor("icons/browse_16x16.png"));
			setToolTipText("Search for a decoded value");
		}

		@Override
		public void run() {
			SearchDialog d = new SearchDialog(getShell()) {
				@Override
				public String go() {
					return performSearch(getSearchValue(), getSearchMode());
				}
			};
			d.open();
			decodeSelection(); // decoders might have changed
		}
		
		public String getText() { 
			return "Search...";
		};
	}
	
	class ConfigureDecodersAndDecodeAction extends ConfigureDecodersAction {
		public ConfigureDecodersAndDecodeAction() {
			super(null);
		}

		@Override
		public void run() {
			setShell(getShell());
			super.run();
			if (! isCancelled()) {
				decodeSelection();
				if (popup != null) {
					// update decoders in the popup
					showPopupAtCursor();
				}
			}
		}
	}

	class LoadFromFileAction extends Action {
		private static final String LOAD_FROM_FILE = "Load from File...";

		public LoadFromFileAction() {
			setText(LOAD_FROM_FILE);
		}

		@Override
		public void run() {
			FileDialog fileDialog = new FileDialog(getShell());
		    fileDialog.setText(LOAD_FROM_FILE);
		    String selected = fileDialog.open();
		    if (selected == null) {
		    	return;
		    }
		    try {
		    	byte[] bytes = DecodeUtils.readBytesFromFile(new File(selected));
		    	String hex = DecodeUtils.getHex(bytes, true);
				hexData.setText(hex);
		    } catch (IOException e) {
		        MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR);
		        messageBox.setText("Error");
		        messageBox.setMessage("Error while reading file: " + e.toString());
		        messageBox.open();
		    }
		}
	}
	class ToggleDiscoveryModeAction extends Action {
		public ToggleDiscoveryModeAction() {
			super("Discovery Mode", AS_CHECK_BOX);
			setImageDescriptor(Activator.getImageDescriptor("icons/targetinternal_obj.gif"));
			setToolTipText("Toggle Discovery Mode");
		}

		@Override
		public void run() {
			discoveryMode = ! discoveryMode;
			popupMouseListener.setNeedModifier(! discoveryMode);
			resetHexDataStyles();
			hidePopup();
		}
	}

	class SearchResultsLabelProvider implements ITableLabelProvider {
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
			SearchResult m = (SearchResult)element;
			switch (columnIndex) {
			case 1:
				return DecodeUtils.renderIntOrHex(m.offset, searchResultOffsetHexCheckbox.getSelection());
			case 2:
				return DecodeUtils.renderIntOrHex(m.byteLength, searchResultByteLengthHexCheckbox.getSelection());
			case 3:
				return m.actualValue;
			case 4:
				return m.decoder.toString();
			}
			return null;
		}
	}
	
	class SearchResultSorter extends ViewerSorter {
		private static final int ASCENDING = 0;
		private static final int DESCENDING = 1;
		private int column;
		private int direction;

		public void doSort(int column, int direction) {
			this.column = column;
			this.direction = direction;
			searchResultsTable.setSortColumn(searchResultsTable.getColumns()[column]);
			searchResultsTable.setSortDirection(direction == ASCENDING ? SWT.UP : SWT.DOWN);
		}

		public void doSort(int column) {
			if (column == this.column) {
				// same column as last sort; toggle the direction
				doSort(column, 1 - direction);
			} else {
				// new column; do an ascending sort
				doSort(column, ASCENDING);
			}
		}

		/**
		 * Compares the object for sorting
		 */
		public int compare(Viewer viewer, Object e1, Object e2) {
			int rc = 0;
			SearchResult sr1 = (SearchResult) e1;
			SearchResult sr2 = (SearchResult) e2;

			// Determine which column and do the appropriate sort
			switch (column) {
			case 1:
				rc = Integer.compare(sr1.offset, sr2.offset);
				break;
			case 2:
				rc = Integer.compare(sr1.byteLength, sr2.byteLength);
				break;
			case 3:
				rc = sr1.actualValue.compareTo(sr2.actualValue);
				break;
			case 4:
				rc = sr1.decoder.toString().compareTo(sr2.decoder.toString());
			}

			if (direction == DESCENDING)
				rc = -rc;
			return rc;
		}
	}
	
	class SearchResultColumnListener extends SelectionAdapter {
		private final int columnIndex;

		public SearchResultColumnListener(int columnIndex) {
			this.columnIndex = columnIndex;
		}

		public void widgetSelected(SelectionEvent event) {
			((SearchResultSorter) searchResultsViewer.getSorter()).doSort(columnIndex);
			searchResultsViewer.refresh();
		}
	}
}
