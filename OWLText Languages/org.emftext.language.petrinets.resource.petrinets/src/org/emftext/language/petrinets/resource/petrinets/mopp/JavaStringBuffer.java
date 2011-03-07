package org.emftext.language.petrinets.resource.petrinets.mopp;

public class JavaStringBuffer {

	private StringBuffer stringBuffer;
	private int indent;

	public JavaStringBuffer() {
		this.stringBuffer = new StringBuffer();
	}
	
	private void indent() {
		this.indent++;
	}
	
	private void unIndent() {
		this.indent--;
	}
	
	public void append(String s) {
		if (s.trim().endsWith("}") && !s.contains("{")) unIndent();
		stringBuffer.append(s);
		if (s.trim().endsWith("{")) indent();
	}
	
	public void appendLine(String line) {
		stringBuffer.append("\n");
		if (line.trim().endsWith("}") && !line.contains("{")) unIndent();
		for(int i = 0; i<indent; i++) {
			stringBuffer.append("\t");
		}
		stringBuffer.append(line);
		if (line.trim().endsWith("{")) indent();
	}
	
	public String toString() {
		return stringBuffer.toString();
	}

	public void newline() {
		stringBuffer.append("\n");
		
	}
}
