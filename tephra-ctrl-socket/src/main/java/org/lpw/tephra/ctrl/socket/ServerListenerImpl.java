package org.lpw.tephra.ctrl.socket;

import com.alibaba.fastjson.JSONObject;
import org.lpw.tephra.ctrl.Dispatcher;
import org.lpw.tephra.ctrl.context.HeaderAware;
import org.lpw.tephra.ctrl.context.RequestAware;
import org.lpw.tephra.ctrl.context.ResponseAware;
import org.lpw.tephra.ctrl.context.SessionAware;
import org.lpw.tephra.ctrl.socket.context.HeaderAdapterImpl;
import org.lpw.tephra.ctrl.socket.context.RequestAdapterImpl;
import org.lpw.tephra.ctrl.socket.context.ResponseAdapterImpl;
import org.lpw.tephra.ctrl.socket.context.SessionAdapterImpl;
import org.lpw.tephra.nio.NioHelper;
import org.lpw.tephra.nio.ServerListener;
import org.lpw.tephra.util.Compresser;
import org.lpw.tephra.util.Context;
import org.lpw.tephra.util.Converter;
import org.lpw.tephra.util.Json;
import org.lpw.tephra.util.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;

/**
 * @author lpw
 */
@Controller("tephra.ctrl.socket.server-listener")
public class ServerListenerImpl implements ServerListener {
    @Inject
    private Context context;
    @Inject
    private Json json;
    @Inject
    private Converter converter;
    @Inject
    private Compresser compresser;
    @Inject
    private Logger logger;
    @Inject
    private NioHelper nioHelper;
    @Inject
    private HeaderAware headerAware;
    @Inject
    private SessionAware sessionAware;
    @Inject
    private RequestAware requestAware;
    @Inject
    private ResponseAware responseAware;
    @Inject
    private Dispatcher dispatcher;
    @Inject
    private SocketHelper socketHelper;
    @Value("${tephra.ctrl.socket.port:0}")
    private int port;
    @Value("${tephra.ctrl.socket.max-thread:64}")
    private int maxThread;

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public int getMaxThread() {
        return maxThread;
    }

    @Override
    public void accept(String sessionId) {
    }

    @Override
    public void receive(String sessionId, byte[] message) {
        if (logger.isDebugEnable())
            logger.debug("接收到Socket请求[{}:{}]。", sessionId, message.length);
        try {
            for (int from = 0; from < message.length; ) {
                byte[] msg = receive(message, from);
                if (msg == null) {
                    nioHelper.close(sessionId);

                    break;
                }

                from += 4 + msg.length;
                JSONObject object = json.toObject(new String(message, context.getCharset(null)));
                if (object == null) {
                    nioHelper.close(sessionId);

                    break;
                }

                execute(sessionId, object);
            }
        } catch (Throwable throwable) {
            nioHelper.close(sessionId);
            logger.warn(throwable, "处理Socket数据时发生异常！", throwable);
        }
    }

    private byte[] receive(byte[] message, int from) {
        int length = getLength(message, from);
        if (length <= 8) {
            logger.warn(null, "Socket数据[{}:{}]格式错误！", converter.toString(message), from);

            return null;
        }

        byte[] msg = new byte[length];
        System.arraycopy(message, from + 8, msg, 0, length - 8);
        int unzipLength = getLength(message, from + 4);
        if (unzipLength > 0) {
            msg = compresser.unzip(msg);
            if (msg.length != unzipLength) {
                logger.warn(null, "解压缩后数据长度[{}:{}]不匹配！", unzipLength, msg.length);

                return null;
            }
        }

        return msg;
    }

    private int getLength(byte[] message, int from) {
        int length = 0;
        for (int i = from, to = from + 4; i < to; i++)
            length = (length << 8) + (message[i] & 0xff);

        return length;
    }

    private void execute(String sessionId, JSONObject object) {
        headerAware.set(new HeaderAdapterImpl(object.getJSONObject("header"), nioHelper.getIp(sessionId)));
        if (object.containsKey("tephra-session-id")) {
            sessionAware.set(new SessionAdapterImpl(object.getString("tephra-session-id")));
            socketHelper.bind(sessionId, object.getString("tephra-session-id"));
        } else
            sessionAware.set(new SessionAdapterImpl(sessionId));
        requestAware.set(new RequestAdapterImpl(object.getJSONObject("request"), port, object.getString("uri")));
        responseAware.set(new ResponseAdapterImpl(socketHelper, sessionId));
        dispatcher.execute();
    }

    @Override
    public void disconnect(String sessionId) {
        socketHelper.unbind(sessionId, null);
    }
}
