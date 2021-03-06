package org.lpw.tephra.ctrl.socket;

import org.lpw.tephra.ctrl.context.Session;
import org.lpw.tephra.nio.NioHelper;
import org.lpw.tephra.util.Compresser;
import org.lpw.tephra.util.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lpw
 */
@Controller("tephra.socket.helper")
public class SocketHelperImpl implements SocketHelper {
    @Inject
    private Compresser compresser;
    @Inject
    private Logger logger;
    @Inject
    private NioHelper nioHelper;
    @Inject
    private Session session;
    @Value("${tephra.ctrl.socket.zip-size:4096}")
    private int zipSize;
    private Map<String, String> tsids = new ConcurrentHashMap<>();
    private Map<String, String> sids = new ConcurrentHashMap<>();

    @Override
    public void bind(String sessionId, String tephraSessionId) {
        tsids.put(tephraSessionId, sessionId);
        sids.put(sessionId, tephraSessionId);
    }

    @Override
    public void send(String sessionId, byte[] message) {
        if (sessionId == null)
            sessionId = session.getId();
        if (tsids.containsKey(sessionId))
            sessionId = tsids.get(sessionId);
        try {
            write(sessionId, message);
        } catch (IOException e) {
            nioHelper.close(sessionId);
            logger.warn(e, "发送数据到客户端[{}]时发生异常！", sessionId);
        }
    }

    private void write(String sessionId, byte[] message) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        if (message.length <= zipSize)
            write(dataOutputStream, 0, message);
        else
            write(dataOutputStream, message.length, compresser.zip(message));
        dataOutputStream.close();
        outputStream.close();
        nioHelper.send(sessionId, outputStream.toByteArray());
    }

    private void write(DataOutputStream dataOutputStream, int unzipLength, byte[] message) throws IOException {
        dataOutputStream.write(message.length + 8);
        dataOutputStream.write(unzipLength);
        dataOutputStream.write(message, 0, message.length);
    }

    @Override
    public void unbind(String sessionId, String tephraSessionId) {
        if (sessionId != null) {
            String tsid = sids.remove(sessionId);
            if (tsid != null)
                tsids.remove(tsid);
        }

        if (tephraSessionId != null) {
            String sid = tsids.remove(tephraSessionId);
            if (sid != null) {
                sids.remove(sid);
                nioHelper.close(sid);
            }
        }
    }
}
