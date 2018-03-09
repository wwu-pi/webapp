package w.sym.state;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CopiedState {

	protected final ByteArrayOutputStream bytes;
	
	public CopiedState(ByteArrayOutputStream bytes) {
		this.bytes = bytes;
	}

	public static CopiedState copyState(SymbolicState state) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bos);
		out.writeObject(state);
		return new CopiedState(bos);
	}
	
	public static SymbolicState loadState(CopiedState copiedState) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(copiedState.bytes.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        SymbolicState copied = (SymbolicState) in.readObject();
        return copied;
	}
}