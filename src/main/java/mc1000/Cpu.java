package mc1000;


public interface Cpu
{
  void run(int nbCycles);
  void start();
  void stop();
  void reset(int startAddr);
}