package mc1000;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MC1000emu extends Panel implements ActionListener {
    MC1000machine machine;
    Timer timer;

    public MC1000emu() {
    }

    public void actionPerformed(ActionEvent e) {
        machine.z80core.run(59600);
        paint(getGraphics());
    }

    public void paint(Graphics g) {
        BufferedImage buffer = machine.vdp.draw();
        g.drawImage(buffer, 0, 0, null);
    }

    public void update(Graphics g) {
    }

    public void init() {
        boolean has48kb = false;
        String tape = null;

//    if (getParameter("ram")!=null)
//      if (getParameter("ram").equals("48"))
        has48kb = true;

//    tape=getParameter("tape");

        machine = new MC1000machine(has48kb);

        try {
            machine.memory.loadROM();
//      if (tape!=null)
//        machine.tape.readFromURL(this,tape);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        timer = new Timer(16, this);
        addKeyListener(machine.psg.getKeyListener());
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public String getAppletInfo() {
        return "BrMC1000: an MC-1000 emulator by Ricardo Bittencourt";
    }

    public static void main(String[] args) {
        Frame f = new Frame();
        f.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }

            ;
        });

        MC1000emu emu = new MC1000emu();
        emu.setSize(256, 192); // same size as defined in the HTML APPLET
        f.add(emu);

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem loadItem = new MenuItem("Load Binary");
        loadItem.addActionListener( (e) -> {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Binary files", "bin");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                emu.loadFile(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        fileMenu.add(loadItem);
//        MenuItem tapeItem = new MenuItem("Load Tape");
//        tapeItem.addActionListener( (e) -> {
//            JFileChooser chooser = new JFileChooser();
//            FileNameExtensionFilter filter = new FileNameExtensionFilter(
//                    "Tape files", "wav");
//            chooser.setFileFilter(filter);
//            int returnVal = chooser.showOpenDialog(null);
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                emu.insertTape(chooser.getSelectedFile().getAbsolutePath());
//            }
//        });
//        fileMenu.add(tapeItem);
        MenuItem quitItem = new MenuItem("Quit");
        quitItem.addActionListener( (e) -> System.exit(0));
        fileMenu.add(quitItem);
        menuBar.add(fileMenu);
        f.setMenuBar(menuBar);


        f.pack();
        emu.init();
        f.setSize(256, 192 + 20); // add 20, seems enough for the Frame title,
        f.setTitle("CCE MC-1000 Emulator");
        f.show();
        emu.start();
    }

    private void insertTape(String absolutePath) {
        try {
            machine.tape.readFromFile(absolutePath);
        } catch (CSWError cswError) {
            cswError.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFile(String absolutePath) {
        try {
            machine.memory.loadProgram(absolutePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}