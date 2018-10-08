package mc1000;


import java.awt.*;
import java.awt.image.*;
import java.io.*;

public class MC6847 {
    BufferedImage buffer;
    boolean[] charset;
    int[] rgbdata;
    int[] vram;
    int mode;
    MC1000machine machine;

    final int opaqueBlack = 0xFF000000;
    final int opaqueGreen = 0xFF00C000;
    final int opaqueDarkGreen = 0xFF008000;
    final int opaqueYellow = 0xFFC0C000;
    final int opaqueLightYellow = 0xFFFFFFA0;
    final int opaqueWhite = 0xFFFFFFFF;
    final int opaqueBlue = 0xFF0000C0;
    final int opaqueRed = 0xFFC00000;
    final int opaqueCyan = 0xFF00C0C0;
    final int opaqueMagenta = 0xFFC000C0;
    final int opaqueOrange = 0xFFC08000;

    MC6847(MC1000machine m) {
        machine = m;
        buffer = new BufferedImage(256, 192, BufferedImage.TYPE_INT_ARGB);
        rgbdata = new int[256 * 192];
        vram = new int[0x1800];

        for (int i = 0; i < 256 * 192; i++)
            rgbdata[i] = opaqueGreen;

        for (int i = 0; i < 0x1800; i++)
            vram[i] = 0;

        charset = new boolean[512 * 12];
        for (int i = 0; i < 7 * 512 / 32; i++)
            for (int j = 0; j < 32; j++)
                charset[3 * 512 + i * 32 + j] = ((charsetComp[i] >> (31 - j)) & 1) > 0;
    }

    public int[] getVRAM() {
        return vram;
    }

    public void changeMode(int m) {
        //System.out.println ("mode: "+Integer.toHexString(m));
        mode = m;
    }

    public BufferedImage draw() {
        BufferedImage image;

        if ((mode & 0xE0) == 0)
            image = drawText();
        else

            switch (mode & 0xFC) {
                case 0x88:
                    image = drawGR(64);
                    break;
                case 0x98:
                    image = drawGR(192);
                    break;
                case 0x9C:
                    image = drawHGR();
                    break;
                case 0x00:
                default:
                    image = drawText();
                    break;
            }

        // Draw tape progress bar
        if (machine.psg.hasTapeStarted())
            if (machine.psg.tapeProgress() < 100) {
                for (int i = 0; i < 102; i++) {
                    image.setRGB(77 + i, 180, opaqueWhite);
                    image.setRGB(77 + i, 188, opaqueWhite);
                }
                for (int i = 181; i < 188; i++) {
                    image.setRGB(77, i, opaqueWhite);
                    image.setRGB(77 + 102, i, opaqueWhite);
                }
                for (int i = 0; i < machine.psg.tapeProgress(); i++) {
                    for (int j = 181; j < 188; j++)
                        image.setRGB(78 + i, j, opaqueWhite);
                }
            }

        return image;
    }

    public BufferedImage drawGR(int rows) {
        int i, j, ii, c, posput;
        int colorGR0[] = {opaqueGreen, opaqueYellow, opaqueBlue, opaqueRed};
        int colorGR1[] = {opaqueWhite, opaqueCyan, opaqueMagenta, opaqueOrange};
        int colorGR[];

        colorGR = (mode & 2) == 0 ? colorGR0 : colorGR1;

        for (j = 0; j < rows; j++) {
            posput = j * 256;
            for (i = 0; i < 32; i++) {
                c = vram[(j << 5) + i];

                rgbdata[posput + 0] = rgbdata[posput + 1] = colorGR[((c >> 6) & 3)];
                rgbdata[posput + 2] = rgbdata[posput + 3] = colorGR[((c >> 4) & 3)];
                rgbdata[posput + 4] = rgbdata[posput + 5] = colorGR[((c >> 2) & 3)];
                rgbdata[posput + 6] = rgbdata[posput + 7] = colorGR[((c >> 0) & 3)];
                posput += 8;
            }
        }
        buffer.setRGB(0, 0, 256, 192, rgbdata, 0, 256);
        return buffer;
    }

    public BufferedImage drawHGR() {
        int i, j, ii, c, posput;
        int colorHGR0[] = {opaqueDarkGreen, opaqueGreen};
        int colorHGR1[] = {opaqueBlack, opaqueWhite};
        int colorHGR[];

        colorHGR = (mode & 2) == 0 ? colorHGR0 : colorHGR1;

        for (j = 0; j < 192; j++)
            for (i = 0; i < 32; i++) {
                c = vram[(j << 5) + i];
                posput = (j << 8) + (i << 3);
                for (ii = 0; ii < 8; ii++)
                    rgbdata[posput + ii] = colorHGR[(c >> (7 - ii)) & 1];
            }
        buffer.setRGB(0, 0, 256, 192, rgbdata, 0, 256);
        return buffer;
    }

    public BufferedImage drawText() {
        int i, j, ii, jj, posget, posput, c;
        int front, back;

        //    System.out.println ("mode="+Integer.toHexString(mode));
        front = ((mode & 2) == 0) ? opaqueLightYellow : opaqueWhite;
        back = ((mode & 2) == 0) ? opaqueDarkGreen : opaqueRed;

        for (j = 0; j < 192; j++)
            for (i = 0; i < 32; i++)
                rgbdata[(j << 8) + i] = rgbdata[(j << 8) + i + 256 - 32] = back;

        for (j = 0; j < 16; j++)
            for (jj = 0; jj < 12; jj++)
                for (i = 0; i < 32; i++) {
                    c = vram[(j << 5) + i];
                    posget = (((c + 32) & 63) << 3) + (jj << 9) + 1;
                    posput = 32 + (((j * 12) + jj) << 8) + (i * 6);
                    if ((c & 128) > 0)
                        for (ii = 0; ii < 6; ii++)
                            rgbdata[posput + ii] = (charset[posget + ii] ? back : front);
                    else
                        for (ii = 0; ii < 6; ii++)
                            rgbdata[posput + ii] = (charset[posget + ii] ? front : back);
                }
        buffer.setRGB(0, 0, 256, 192, rgbdata, 0, 256);
        return buffer;
    }

    private static final int charsetComp[] = {
            529428, 137498636, 68157440, 2, 403184668, 71179326, 471597068, 67117080,
            470301724, 1010712094, 572260898, 539107902, 1008483356, 1042424354, 572669468, 538707968,
            529428, 506603532, 134744072, 2, 605561378, 203431938, 572656652, 134221860,
            571740706, 304095264, 570950180, 540422690, 572662306, 136454690, 572654096, 537140232,
            524342, 537143308, 268704776, 4, 604504578, 339484676, 572656640, 272500740,
            35787296, 304095264, 570950184, 539634210, 572662288, 136454690, 336856080, 268708368,
            524288, 470290432, 268713534, 205389832, 604511244, 1040333832, 471728140, 536871944,
            438443040, 305675302, 1040712240, 539633186, 1008876552, 136454698, 134744080, 134481982,
            524342, 34613760, 268704776, 201326608, 604512258, 67248656, 570559500, 272500744,
            708710944, 304095266, 570958376, 539107874, 539633668, 136451114, 336072720, 67373072,
            20, 1009132544, 134744072, 67111968, 604512290, 69345824, 570559492, 134221824,
            706875938, 304095266, 570958372, 539107874, 539239458, 136451126, 570957840, 33818632,
            524308, 136714752, 68157440, 134220832, 404504092, 68951072, 471597064, 67117064,
            472005660, 1010704414, 572267554, 1042424382, 538583580, 136054818, 570965532, 35391488
    };

}