package org.atomicode.inf.route.inter;

import io.netty.handler.codec.http.HttpRequest;

public enum RequestMethod {
    //http
    GET("GET", SignalType.HTTP), //获取资源
    POST("POST", SignalType.HTTP), //新建资源

    PUT("PUT", SignalType.HTTP), //修改资源 //客户端要提供改变后的完整资源
    DELETE("DELETE", SignalType.HTTP), //删除资源
    PATCH("PATCH", SignalType.HTTP), //修改资源 //客户端只提供改变的局部属性

    HEAD("HEAD", SignalType.HTTP), //相当于GET，但不返回内容


    TRACE("TRACE", SignalType.HTTP),//回馈服务器收到的请求，用于远程诊断服务器。
    OPTIONS("OPTIONS", SignalType.HTTP), //获取服务器支持的HTTP请求方法
    CONNECT("CONNECT", SignalType.HTTP),//用于代理进行传输

    /**
     * http general all
     */
    HTTP("HTTP", SignalType.HTTP),

    /**
     * web socket send
     */
    WEBSOCKET("WEBSOCKET", SignalType.WEBSOCKET),

    /**
     * socket listen
     */
    SOCKET("SOCKET", SignalType.SOCKET),


    ALL("ALL", SignalType.ALL);

    public final String name;
    public final SignalType signal;

    RequestMethod(String name, SignalType signal) {
        this.name = name;
        this.signal = signal;
    }

    public static RequestMethod of(HttpRequest request) {
        final var methodName = request.method().name();
        return switch (methodName) {
            case "GET" ->
                // final var headers = request.headers();
                // if (headers.contains(HttpHeaderNames.UPGRADE) && headers.get(HttpHeaderNames.UPGRADE).equalsIgnoreCase("websocket")) {
                //     yield WEBSOCKET;
                // }
                // yield GET;
                GET;

            case "POST" -> POST;
            case "PUT" -> PUT;
            case "DELETE" -> DELETE;
            case "PATCH" -> PATCH;
            case "HEAD" -> HEAD;
            case "TRACE" -> TRACE;
            case "OPTIONS" -> OPTIONS;
            case "CONNECT" -> CONNECT;
            case "HTTP" -> HTTP;
            case "WEBSOCKET", "WS", "WSS" -> WEBSOCKET;
            case "SOCKET" -> SOCKET;
            case "ALL" -> ALL;
            default -> throw new IllegalArgumentException("unknown method type: " + methodName);
        };
    }
}