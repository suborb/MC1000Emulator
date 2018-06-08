/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mc1000.z80core;

import mc1000.MC1000machine;
import mc1000.MC1000memory;
import mc1000.Memory;
import mc1000.Ports;

import java.math.BigInteger;

/**
 *
 * @author jsanchez
 */
public class MemIoOps {
    private final Memory mc1000memory;
    private final Ports ports;
    private long tstates = 0;

    public void setActiveInt() {
        this.activeInt = true;
    }

    private boolean activeInt;

    public MemIoOps(MC1000machine machine) {
        this.mc1000memory = machine.memory;
        this.ports = machine.ports;
    }


    public int fetchOpcode(int address) {
        // 3 clocks to fetch opcode from RAM and 1 execution clock
        tstates += 4;
        return mc1000memory.readByte(address);
    }

    public int peek8(int address) {
        tstates += 3; // 3 clocks for read byte from RAM
        return mc1000memory.readByte(address);
    }

    public void poke8(int address, int value) {
        tstates += 3; // 3 clocks for write byte to RAM
        mc1000memory.writeByte(address, value);
    }

    public int peek16(int address) {
        int lsb = peek8(address);
        int msb = peek8(address + 1);
        return (msb << 8) | lsb;
    }

    public void poke16(int address, int word) {
        poke8(address, word);
        poke8(address + 1, word >>> 8);
    }

    public int inPort(int port) {
        tstates += 4; // 4 clocks for read byte from bus
        return ports.in(port, BigInteger.ZERO);
    }

    public void outPort(int port, int value) {
        tstates += 4; // 4 clocks for write byte to bus
        ports.out(port, value, BigInteger.ZERO);
    }

    public void addressOnBus(int address, int tstates) {
        // Additional clocks to be added on some instructions
        // Not to be changed, really.
        this.tstates += tstates;
    }

    public void interruptHandlingTime(int tstates) {
        // Additional clocks to be added on INT & NMI
        // Not to be changed, really.
        this.tstates += tstates;
    }

    public boolean isActiveINT() {
        boolean ret = activeInt;

        activeInt = false;

        return ret;
    }

    public long getTstates() {
        return tstates;
    }

    public void reset() {
        tstates = 0;
    }
}
