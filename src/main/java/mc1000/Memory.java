package mc1000;

import java.io.IOException;

public interface Memory {
 public void writeByte(int addr, int data);

 public int readByte(int addr);

 public void loadROM() throws IOException;

 public void loadProgram(String fileToLoad) throws IOException;

 public void vramStatus(int set);

 public void vram80Status(int set);
}