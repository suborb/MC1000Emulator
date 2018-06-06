import javax.imageio.stream.FileImageInputStream;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.zip.*;

public class MC1000memory implements Memory {
    int[] ram;
    int[] rom;
    boolean vramEnabled;
    MC1000machine machine;

    MC1000memory(MC1000machine m, boolean has48k) {
        machine = m;
        if (has48k)
            ram = new int[48 * 1024];
        else
            ram = new int[16384];

        rom = new int[16384];
        vramEnabled = false;

        for (int i = 0; i < ram.length; i++)
            ram[i] = 0;

        for (int i = 0; i < rom.length; i++)
            rom[i] = 0;
    }

    public void loadROM() throws IOException {
        try {
            //URL http=new URL (codeBase,"mc1000.rom.gz");
            //DataInput in=new DataInputStream (new GZIPInputStream (http.openStream()));
            DataInput in = new DataInputStream(getClass().getResourceAsStream("/mc1000.rom"));
            for (int i = 0; i < 16384; i++) {
                rom[i] = in.readByte() & 0xff;
            }
        } catch (Exception e) {
            throw new IOException();
        }
    }

    public void loadProgram(String fileToLoad) throws IOException {
        try {
            //URL http=new URL (codeBase,"mc1000.rom.gz");
            //DataInput in=new DataInputStream (new GZIPInputStream (http.openStream()));
            DataInput in = new DataInputStream(new FileInputStream(fileToLoad));
            try {
                int i = 981;
                while (true) {
                    ram[i++] = in.readByte() & 0xff;
                }
            } catch (EOFException e) {
                // End of file
            }
        } catch (Exception e) {
            throw new IOException();
        }

    }

    public void vramStatus(int set) {
        vramEnabled = (set & 1) == 0;
    }

    public void vram80Status(int set) {
        /* 80 columns mode not implemented yet */
    }

    public void writeByte(int addr, int data) {
        if (addr >= 0x8000 && addr < 0x9800 && vramEnabled) {
            machine.vdp.getVRAM()[addr - 0x8000] = data;
            //System.out.println ("write ("+Integer.toHexString(addr)+")="+Integer.toHexString(data));
        } else if (addr < ram.length)
            ram[addr] = data;
    }

    public int readByte(int addr) {
        int ret;

        if (addr >= 0x8000 && addr < 0x9800 && vramEnabled)
            ret = machine.vdp.getVRAM()[addr - 0x8000];
        else if (addr < ram.length)
            ret = ram[addr];
        else if (addr >= 0xC000 && addr <= 0xFFFF)
            ret = rom[addr - 0xC000];
        else ret = 0xFF;

        //System.out.println ("IN ("+Integer.toHexString(addr)+")="+Integer.toHexString(ret));
        return ret & 0xFF;
    }

}