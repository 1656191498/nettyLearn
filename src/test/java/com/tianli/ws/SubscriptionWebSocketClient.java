package com.tianli.ws;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.p2p.solanaj.rpc.types.RpcNotificationResult;
import org.p2p.solanaj.rpc.types.RpcRequest;
import org.p2p.solanaj.rpc.types.RpcResponse;
import org.p2p.solanaj.rpc.types.config.Commitment;
import org.p2p.solanaj.ws.SignatureNotification;
import org.p2p.solanaj.ws.listeners.NotificationEventListener;

public class SubscriptionWebSocketClient extends WebSocketClient {
    private Map<String, SubscriptionParams> subscriptions = new ConcurrentHashMap();
    private Map<String, Long> subscriptionIds = new ConcurrentHashMap();
    private Map<Long, NotificationEventListener> subscriptionListeners = new ConcurrentHashMap();
    private static final Logger LOGGER = Logger.getLogger(SubscriptionWebSocketClient.class.getName());

    public static SubscriptionWebSocketClient getExactPathInstance(String endpoint) {
        URI serverURI;
        try {
            serverURI = new URI(endpoint);
        } catch (URISyntaxException var4) {
            throw new IllegalArgumentException(var4);
        }

        SubscriptionWebSocketClient instance = new SubscriptionWebSocketClient(serverURI);
        if (!instance.isOpen()) {
            instance.connect();
        }

        return instance;
    }

    public static SubscriptionWebSocketClient getInstance(String endpoint) {
        URI serverURI;
        try {
            URI endpointURI = new URI(endpoint);
            serverURI = new URI(Objects.equals(endpointURI.getScheme(), "https") ? "wss" : "ws://" + endpointURI.getHost());
        } catch (URISyntaxException var5) {
            throw new IllegalArgumentException(var5);
        }

        SubscriptionWebSocketClient instance = new SubscriptionWebSocketClient(serverURI);
        if (!instance.isOpen()) {
            instance.connect();
        }

        return instance;
    }

    public SubscriptionWebSocketClient(URI serverURI) {
        super(serverURI);
    }

    public void accountSubscribe(String key, NotificationEventListener listener) {
        List<Object> params = new ArrayList();
        params.add(key);
        params.add(Map.of("encoding", "jsonParsed", "commitment", Commitment.PROCESSED.getValue()));
        RpcRequest rpcRequest = new RpcRequest("accountSubscribe", params);
        this.subscriptions.put(rpcRequest.getId(), new SubscriptionParams(rpcRequest, listener));
        this.subscriptionIds.put(rpcRequest.getId(), 0L);
        this.updateSubscriptions();
    }

    public void signatureSubscribe(String signature, NotificationEventListener listener) {
        List<Object> params = new ArrayList();
        params.add(signature);
        RpcRequest rpcRequest = new RpcRequest("signatureSubscribe", params);
        this.subscriptions.put(rpcRequest.getId(), new SubscriptionParams(rpcRequest, listener));
        this.subscriptionIds.put(rpcRequest.getId(), 0L);
        this.updateSubscriptions();
    }

    public void logsSubscribe(String mention, NotificationEventListener listener) {
        List<Object> params = new ArrayList();
        params.add(Map.of("mentions", List.of(mention)));
        params.add(Map.of("commitment", "finalized"));
        RpcRequest rpcRequest = new RpcRequest("logsSubscribe", params);
        this.subscriptions.put(rpcRequest.getId(), new SubscriptionParams(rpcRequest, listener));
        this.subscriptionIds.put(rpcRequest.getId(), 0L);
        this.updateSubscriptions();
    }

    public void logsSubscribe(List<String> mentions, NotificationEventListener listener) {
        List<Object> params = new ArrayList();
        params.add(Map.of("mentions", mentions));
        params.add(Map.of("commitment", "finalized"));
        RpcRequest rpcRequest = new RpcRequest("logsSubscribe", params);
        this.subscriptions.put(rpcRequest.getId(), new SubscriptionParams(rpcRequest, listener));
        this.subscriptionIds.put(rpcRequest.getId(), null);
        this.updateSubscriptions();
    }

    public void onOpen(ServerHandshake handshakedata) {
        LOGGER.info("Websocket connection opened");
        this.updateSubscriptions();
    }

    public void onMessage(String message) {
        JsonAdapter<RpcResponse<Long>> resultAdapter = (new Moshi.Builder()).build().adapter(Types.newParameterizedType(RpcResponse.class, new Type[]{Long.class}));

        try {
            RpcResponse<Long> rpcResult = (RpcResponse)resultAdapter.fromJson(message);
            String rpcResultId = rpcResult.getId();
            if (rpcResultId != null) {
                if (this.subscriptionIds.containsKey(rpcResultId)) {
                    try {
                        this.subscriptionIds.put(rpcResultId, (Long)rpcResult.getResult());
                        this.subscriptionListeners.put((Long)rpcResult.getResult(), this.subscriptions.get(rpcResultId).listener);
                        this.subscriptions.remove(rpcResultId);
                    } catch (NullPointerException var11) {
                    }
                }
            } else {
                JsonAdapter<RpcNotificationResult> notificationResultAdapter = (new Moshi.Builder()).build().adapter(RpcNotificationResult.class);
                RpcNotificationResult result = notificationResultAdapter.fromJson(message);
                NotificationEventListener listener = this.subscriptionListeners.get(result.getParams().getSubscription());
                Map value = (Map)result.getParams().getResult().getValue();
                switch (result.getMethod()) {
                    case "signatureNotification":
                        listener.onNotificationEvent(new SignatureNotification(value.get("err")));
                        break;
                    case "accountNotification":
                    case "logsNotification":
                        if (listener != null) {
                            listener.onNotificationEvent(value);
                        }
                }
            }
        } catch (Exception var12) {
            var12.printStackTrace();
        }

    }

    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
    }

    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    private void updateSubscriptions() {
        if (this.isOpen() && !this.subscriptions.isEmpty()) {
            JsonAdapter<RpcRequest> rpcRequestJsonAdapter = (new Moshi.Builder()).build().adapter(RpcRequest.class);

            for (SubscriptionParams sub : this.subscriptions.values()) {
                this.send(rpcRequestJsonAdapter.toJson(sub.request));
            }
        }

    }

    private class SubscriptionParams {
        RpcRequest request;
        NotificationEventListener listener;

        SubscriptionParams(RpcRequest request, NotificationEventListener listener) {
            this.request = request;
            this.listener = listener;
        }
    }
}
