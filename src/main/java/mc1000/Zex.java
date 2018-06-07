package mc1000;

import java.io.*;
import java.math.BigInteger;

class mymemory implements Memory {
    int[] mem;

    mymemory() {
        mem = new int[65536];
        for (int i = 0; i < 65536; i++)
            mem[i] = 0;

        try {
            DataInput in = new DataInputStream(getClass().getResourceAsStream("/zexall.com"));
            for (int i = 0; i < 8587; i++)
                mem[i + 0x100] = (int) (in.readByte() & 0xFF);
        } catch (Exception e) {
            System.out.println("Cannot read mc1000.rom" + e) ;
            System.exit(1);
        }

        mem[5] = 0x32;
        mem[6] = 0x00;
        mem[7] = 0x00;
        mem[8] = 0x79;
        mem[9] = 0x32;
        mem[10] = 0x81;
        mem[11] = 0x00;
        mem[12] = 0x7b;
        mem[13] = 0x32;
        mem[14] = 0x82;
        mem[15] = 0x00;
        mem[16] = 0x7a;
        mem[17] = 0x32;
        mem[18] = 0x83;
        mem[19] = 0x00;
        mem[20] = 0xc9;

    }

    public void writeByte(int addr, int data) {
        mem[addr] = data;
    }

    public int readByte(int addr) {
        int p;

        if (addr == 20 && mem[addr] == 0xc9) {
            if (mem[0x81] == 9) {
                p = mem[0x82] + mem[0x83] * 256;
                while (mem[p] != '$') {
                    System.out.print(new Character((char) mem[p]));
                    p++;
                }
            }
            if (mem[0x81] == 2)
                System.out.print(new Character((char) mem[0x82]));
        }
        return mem[addr & 65535];
    }

    @Override
    public void loadROM() throws IOException {

    }

    @Override
    public void loadProgram(String fileToLoad) throws IOException {

    }

    @Override
    public void vramStatus(int set) {

    }

    @Override
    public void vram80Status(int set) {

    }

}

class myports implements Ports {
    public void out(int addr, int data, BigInteger v) {
    }

    public int in(int addr, BigInteger v) {
        return 0xff;
    }
}

public class Zex {
    static Cpu core;

    public static void main(String[] argv) {


        MC1000machine mac = new MC1000machine();
        mac.ports = new myports();
        mac.memory = new mymemory();

        core = new Z80Wrap(mac,0x100);
        while (true) core.run(59600);
    }
}