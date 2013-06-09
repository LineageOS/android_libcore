package java.lang;

import java.io.IOException;
import java.io.InputStream;

/**
 * Simulates an empty InputStream
 * @author Svyatoslav Hresyk
 * {@hide}
 */
public class PrivacyInputStream extends InputStream {

    public PrivacyInputStream() {
    }
    
    @Override
    public int read() throws IOException {
        return -1;
    }
    
    @Override
    public void close() throws IOException {
        super.close();
    }
    
    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        return -1;
    }
    
    @Override
    public int read(byte[] b) throws IOException {
        return -1;
    }
    
}   
