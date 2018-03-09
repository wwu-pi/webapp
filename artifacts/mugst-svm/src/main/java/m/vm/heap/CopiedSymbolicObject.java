package m.vm.heap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class CopiedSymbolicObject implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected final byte[] bytes;
	
	public CopiedSymbolicObject(ByteArrayOutputStream bytes) {
		this.bytes = bytes.toByteArray();
	}
	
	public SymbolicObject load() throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(this.bytes);
        ObjectInputStream in = new ObjectInputStream(bis);
        SymbolicObject copied = (SymbolicObject) in.readObject();
        return copied;
	}
}
