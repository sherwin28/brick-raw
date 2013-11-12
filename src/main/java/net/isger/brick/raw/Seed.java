package net.isger.brick.raw;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 种子
 * 
 * @author issing
 * 
 */
public interface Seed {

    public Object getEntity();

    public InputStream getInputStream() throws IOException;

    public OutputStream getOutputStream() throws IOException;

}
