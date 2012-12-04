package org.osk.console;

/**
 * This is the thread reading command lines from the cmd console window.
 *
 * @author J. Eickhoff
 */
public class WinGrabberThread extends Thread {
    private final ChildTextFrame cWin;
    private boolean stoprequested;
    private String cWinLine;

    public WinGrabberThread(ChildTextFrame cWin) {
        super("WinGrabber Thread");
        this.cWin = cWin;
        stoprequested = false;
        cWinLine = "";
    }

    public synchronized void requestStop() {
        stoprequested = true;
    }

    public String readcWinLine() {
        return cWinLine;
    }

    void resetcWinLine() {
        cWinLine = "";
    }

    @Override
    public void run() {
        while (!stoprequested) {
            if (this.isInterrupted()) {
                break;
            }
            cWinLine = cWin.readLine();
        }
        //System.out.println("WinGrabberThread: terminating");
    }
}
