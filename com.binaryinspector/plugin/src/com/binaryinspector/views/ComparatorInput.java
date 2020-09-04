package com.binaryinspector.views;

import java.io.*;

import org.eclipse.compare.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

import com.binaryinspector.decoders.DecodeUtils;

class ComparatorInput implements IEditableContent, ITypedElement, IStreamContentAccessor {
	private String content;
	private final boolean editable;

	public ComparatorInput(String content, boolean editable) {
		content = content == null ? "" : content;
		try {
			byte [] bytes = DecodeUtils.parseHexString(this.content);
			content = DecodeUtils.getHex(bytes, true).toUpperCase();
		} catch (Exception e) {}
		
		this.content = content;
		this.editable = editable;
	}

	@Override
	public String getName() {
		return "name";
	}

	public Image getImage() {
		return null;
	}

	public String getType() {
		return ITypedElement.TEXT_TYPE;
	}

	public InputStream getContents() throws CoreException {
		return new ByteArrayInputStream(content.getBytes());
	}

	@Override
	public boolean isEditable() {
		return editable;
	}
	
	public String getContent() {
		return content;
	}
	
	@Override
	public void setContent(byte[] newContent) {
		content = new String(newContent);
	}

	@Override
	public ITypedElement replace(ITypedElement dest, ITypedElement src) {
		return null;
	}
}