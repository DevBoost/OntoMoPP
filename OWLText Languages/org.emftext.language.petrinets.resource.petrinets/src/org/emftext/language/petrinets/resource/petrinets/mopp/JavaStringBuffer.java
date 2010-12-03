package org.emftext.language.petrinets.resource.petrinets.mopp;

public class JavaStringBuffer {

	private StringBuffer stringBuffer;
	private int indent;

	public JavaStringBuffer() {
		this.stringBuffer = new StringBuffer();
	}
	
	public void indent() {
		this.indent++;
	}
	
	public void unIndent() {
		this.indent--;
	}
	
	public void append(String s) {
		stringBuffer.append(s);
	}
	
	public void appendLine(String line) {
		stringBuffer.append("\n");
		for(int i = 0; i<indent; i++) {
			stringBuffer.append("\t");
		}
		stringBuffer.append(line);
	}
	
	public String toString() {
		return stringBuffer.toString();
	}

	public void newline() {
		stringBuffer.append("\n");
		
	}
}
