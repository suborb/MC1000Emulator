package mc1000;


import mc1000.z80core.MemIoOps;
import mc1000.z80core.NotifyOps;

public class Z80Wrap  implements Cpu, NotifyOps{

    private final mc1000.z80core.Z80 z80;
    private boolean running;
    MemIoOps memIo;


    public Z80Wrap(MC1000machine machine, int startAddress) {
        memIo = new MemIoOps(machine);
        z80 = new mc1000.z80core.Z80(memIo, this);
        z80.setRegPC(startAddress);
    }

    @Override
    public void run(int nbCycles) {
        long start = memIo.getTstates();
        memIo.setActiveInt();
        while ( start + nbCycles > memIo.getTstates()) {
            z80.execute();
        }
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public void reset(int startAddr) {
        z80.setRegPC(startAddr);
    }

    @Override
    public int breakpoint(int address, int opcode) {
        return 0;
    }

    @Override
    public void execDone() {

    }
}
