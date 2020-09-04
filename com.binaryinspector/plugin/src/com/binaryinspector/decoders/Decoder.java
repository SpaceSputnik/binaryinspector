package com.binaryinspector.decoders;

import java.util.*;

import com.binaryinspector.decoders.parameters.*;

public abstract class Decoder implements Cloneable {
	protected ParameterValues params;
	
	private boolean includeInSearch = true;
	private boolean enabled = true;
	
	private Object tag = null;

	public abstract String getName();	
	public abstract int getByteLength();
	public abstract DecodeResult doDecode(byte [] data);

	public Decoder(ParameterValues params) {
		this.params = params;
	}
	
	public ParameterValues getParams() {
		return params;
	}
	
	public Object getTag() {
		return tag;
	}
	public void setTag(Object tag) {
		this.tag = tag;
	}
	
	protected abstract void refreshParams();
	public abstract String validate(String value);

	
	@Override
	public Object clone() {
		try {
			Object clone = super.clone();
			Decoder typedClone = (Decoder)clone;
			typedClone.params = (ParameterValues)params.clone();
			typedClone.enabled = enabled;
			typedClone.includeInSearch = includeInSearch;
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String toString() {
		if (params == null) {
			return getName();
		}
		StringBuilder b = new StringBuilder();
		String delim = ", ";
		for (Descriptor d : params.getMetadata().values()) {
			if (! params.isSpecified(d)) {
				continue;
			}
			if (d instanceof BoolDescriptor) {
				if (params.getBoolean(d.getName())) {
					b.append(d.getName());
					b.append(delim);
				}
			} else {
				String dump = d.dump(params.getString(d.getName()));
				if (! dump.isEmpty()) {
					b.append(dump);
					b.append(delim);
				}
			}
		}
		String parmString = b.toString();
		if (parmString.endsWith(delim)) {
			parmString = parmString.substring(0, parmString.length() - delim.length());
		}
		b = new StringBuilder();
		if (parmString.length() != 0) {
			b.append("(");
		}
		b.append(parmString);
		if (parmString.length() != 0) {
			b.append(")");
		}		
		return getName() + " " +b.toString();
	}

	public enum SearchMode {
		FORWARD_FROM_BEGINNING,
		FORWARD_FROM_CURRENT,
		BACKWARD_FROM_END,
		BACKWARD_FROM_CURRENT
	};
	
	public Collection<SearchResult> search(String value, byte[] data, int cursorPos, SearchMode mode) {
		int start = -1;
		int end = -1;
		
		switch (mode) {
		case FORWARD_FROM_BEGINNING:
			start = 0;
			end = data.length;
			break;
		case FORWARD_FROM_CURRENT:
			start = cursorPos;
			end = data.length;
			break;
		case BACKWARD_FROM_END:
		    start = data.length;
		    end = -1;
		    break;
		case BACKWARD_FROM_CURRENT:
			start = cursorPos;
			end = -1;
			break;
		}
		
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		int fixedByteLen = getByteLength();
	
		// TODO: searches can be optimized by starting the check at the min length specified by decoder
		if (start < end) {
			// forward
			if (fixedByteLen > 0) {
				// fixed length, no need for o(n^2) looping
				for (int i = start; i <= end - fixedByteLen; i++) {
					byte [] bytes = Arrays.copyOfRange(data, i, i + fixedByteLen);
					DecodeResult r = decode(bytes);
					if (r.isSuccess()) {
						String decodedValue = r.getValue();
						if (compare(value, decodedValue)) {
							results.add(new SearchResult(decodedValue, i, bytes.length, this));
						}
					}
					
				}
			} else {
				// unspecified length, o(n^2) looping
				for (int i = start; i < end; i++) {
					for (int j = i + 1; j <= end; j++) {
						byte [] bytes = Arrays.copyOfRange(data, i, j);
						DecodeResult r = decode(bytes);
						if (r.isSuccess()) {
							String decodedValue = r.getValue();
							if (compare(value, decodedValue)) {
								results.add(new SearchResult(decodedValue, i, bytes.length, this));
							}
						}
					}
				}
			}
		} else {
			// backward
			if (fixedByteLen > 0) {
				// fixed length, no need for o(n^2) looping
				for (int i = start - fixedByteLen + 1; i > end; i--) {
					byte [] bytes = Arrays.copyOfRange(data, i, i + fixedByteLen);
					DecodeResult r = decode(bytes);
					if (r.isSuccess()) {
						String decodedValue = r.getValue();
						if (compare(value, decodedValue)) {
							results.add(new SearchResult(decodedValue, i, bytes.length, this));
						}
					}					
				}
			} else {
				// unspecified length, o(n^2) looping
				for (int i = start; i > end; i--) {
					for (int j = i; j > end; j--) {
						byte [] bytes = Arrays.copyOfRange(data, j, i + 1);
						DecodeResult r = decode(bytes);
						if (r.isSuccess()) {
							String decodedValue = r.getValue();
							if (compare(value, decodedValue)) {
								results.add(new SearchResult(decodedValue, j, bytes.length, this));
							}
						}
					}
				}
			}
		}
		return results;
	}
	
	public DecodeResult decode(byte [] data) {
		int fixedLen = getByteLength();
		if (fixedLen > 0 && fixedLen != data.length) {
			DecodeResult res = new DecodeResult();
			res.setError("Need " + fixedLen + " bytes");
			return res;
		}
		return doDecode(data);
	}

	
	protected abstract boolean compare(String val1, String val2);
	
	public void setIncludeInSearch(boolean includeInSearch) {
		this.includeInSearch = includeInSearch;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public boolean isIncludeInSearch() {
		return includeInSearch;
	}
}
