package org.swat.maracas.rest.data;

import java.util.ArrayList;
import java.util.List;

public class BreakingChangeInstance {
	private String type;
	private String declaration;
	private String path;
	private String url;
	private int startLine;
	private int endLine;
	private boolean sourceCompatible;
	private boolean binaryCompatible;
	private List<Detection> detections = new ArrayList<>();

	public BreakingChangeInstance() {

	}

	public BreakingChangeInstance(String type, String declaration, String path, int startLine, int endLine, boolean sourceCompatible, boolean binaryCompatible) {
		this.type = type;
		this.declaration = declaration;
		this.path = path;
		this.startLine = startLine;
		this.endLine = endLine;
		this.sourceCompatible = sourceCompatible;
		this.binaryCompatible = binaryCompatible;
	}

	public void addDetection(Detection d) {
		detections.add(d);
	}

	public List<Detection> getDetections() {
		return detections;
	}

	public String getType() {
		return type;
	}

	public String getDeclaration() {
		return declaration;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getStartLine() {
		return startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public boolean getSourceCompatible() {
		return sourceCompatible;
	}

	public boolean getBinaryCompatible() {
		return binaryCompatible;
	}
}
