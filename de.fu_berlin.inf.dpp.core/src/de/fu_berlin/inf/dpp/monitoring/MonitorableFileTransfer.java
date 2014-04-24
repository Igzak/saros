package de.fu_berlin.inf.dpp.monitoring;

import org.apache.commons.lang.time.StopWatch;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import de.fu_berlin.inf.dpp.util.CoreUtils;

public class MonitorableFileTransfer {
    private static final int INTERVAL = 200;

    private FileTransfer transfer;
    private IProgressMonitor monitor;

    public enum TransferStatus {
        OK, CANCEL, ERROR
    }

    /**
     * @param transfer
     *            A {@link FileTransfer} with an already negotiated stream (e.g.
     *            through
     *            {@link OutgoingFileTransfer#sendFile(java.io.File, String)
     *            transfer.sendFile()} or
     *            {@link IncomingFileTransfer#recieveFile()
     *            transfer.receiveFile()}).
     * @param monitor
     *            can be <code>null</code>
     */
    public MonitorableFileTransfer(FileTransfer transfer,
        IProgressMonitor monitor) {

        this.transfer = transfer;

        if (monitor == null)
            this.monitor = new NullProgressMonitor();
        else
            this.monitor = monitor;
    }

    /**
     * Monitors the running file transfer in fixed intervals {@link #INTERVAL
     * interval} and reports on current throughput and remaining time. The file
     * transfer can be canceled through the monitor.
     * 
     * @return Returns an {@link TransferStatus} with code
     *         {@link TransferStatus#OK} if and when the transfer is
     *         successfully completed.<br>
     *         Returns a code {@link TransferStatus#CANCEL} if the transfer was
     *         refused or we definitely know that it was canceled on the remote
     *         side.<br>
     *         In some cases, in which we cannot distinguish between
     *         cancellation or transfer error, this will return a code
     *         {@link TransferStatus#ERROR}.
     */
    public TransferStatus monitorTransfer() {
        final long fileSize = transfer.getFileSize();

        int lastWorked = 0;

        StopWatch watch = new StopWatch();
        watch.start();

        while (!transfer.isDone()) {
            if (monitor.isCanceled()) {
                transfer.cancel();
                continue;
            }

            // may return -1 if the transfer has not yet started
            long bytesWritten = transfer.getAmountWritten();

            if (bytesWritten < 0)
                bytesWritten = 0;

            int worked = (int) ((100 * bytesWritten) / (fileSize == 0 ? 1
                : fileSize));

            int delta = worked - lastWorked;

            if (delta > 0) {
                lastWorked = worked;
                monitor.worked(delta);
            }

            long bytesPerSecond = watch.getTime();

            if (bytesPerSecond > 0)
                bytesPerSecond = (bytesWritten * 1000) / bytesPerSecond;

            long secondsLeft = 0;

            if (bytesPerSecond > 0)
                secondsLeft = (fileSize - bytesWritten) / bytesPerSecond;

            String remaingTime = "Remaining time: "
                + (bytesPerSecond == 0 ? "N/A" : CoreUtils
                    .formatDuration(secondsLeft)
                    + " ("
                    + CoreUtils.formatByte(bytesPerSecond) + "/s)");

            monitor.subTask(remaingTime);

            try {
                Thread.sleep(INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                monitor.setCanceled(true);
                continue;
            }
        }

        Status status = transfer.getStatus();

        if (status.equals(Status.complete)) {
            if (transfer.getAmountWritten() < fileSize)
                return TransferStatus.CANCEL;
            else
                return TransferStatus.OK;
        }

        if (status.equals(Status.error))
            return TransferStatus.ERROR;

        // either cancelled or refused
        return TransferStatus.CANCEL;
    }
}
