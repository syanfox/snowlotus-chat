package com.qiushui.snowlotuschat.netty;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.qiushui.snowlotuschat.utils.Constant;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestChannelHandler2 extends SimpleChannelInboundHandler<Object>{

	// websocket 服务的 uri
    private static final String WEBSOCKET_PATH = "/websocket";
	
	private WebSocketServerHandshaker handshaker;
    private static final int MAX_CONN = 2;//指定最大连接数
    private static volatile int connectNum = 0;//当前连接数
    //channelHandlerContext表
    private static volatile Vector<ChannelHandlerContext> contexts = new Vector<>(2);
 
    private static ConcurrentHashMap<Long,Channel> channels = new ConcurrentHashMap<Long,Channel>();
 
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("与客户端建立连接，通道开启！");
        // TODO Auto-generated method stub
//        connectNum++;
//        //控制客户端连接数量，超过则关闭
//        if (connectNum > MAX_CONN) {
//            ctx.writeAndFlush(new TextWebSocketFrame(Unpooled.copiedBuffer("达到人数上限".getBytes())));
//            ctx.channel().close();
//            //当前连接数的更新放在channelInactive()里
//        }
//        
//        //更新contexts
//        contexts.add(ctx);
//        //控制台输出相关信息
//        InetSocketAddress socket = (InetSocketAddress) ctx.channel().remoteAddress();
//        String hostString = socket.getHostString();
//        System.out.println(socket.getAddress().getHostAddress() + ":" + socket.getPort() + "已连接");
//        System.out.println("当前连接数：" + connectNum);
//        ctx.writeAndFlush(new TextWebSocketFrame("hello client"));
 
    }
 
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        //更新当前连接数
        connectNum--;
        //更新contexts数组
        contexts.remove(ctx);
 
        System.out.println("连接断开，当前连接数：" + connectNum);
 
        //控制台输出相关信息
        InetSocketAddress socket = (InetSocketAddress) ctx.channel().remoteAddress();
        System.out.println(new java.util.Date().toString() + ' ' + socket.getAddress().getHostAddress() + ":" + socket.getPort() + "已退出");
        //对另一个客户端发出通知
        if (contexts.size() == 1) {
            contexts.get(0).writeAndFlush(new TextWebSocketFrame("对方退出聊天"));
        }
    }
 
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
 
        //传统的HTTP介入
        if(msg instanceof FullHttpRequest){
            handleHttpRequest(ctx,(FullHttpRequest) msg);
        }
 
        //WebSocket 接入
        else if(msg instanceof WebSocketFrame){
            handleWebSocketFrame(ctx,(WebSocketFrame) msg);
        }
 
    }
 
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
 
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
 
        //判断是否是关闭链路的指令
        if(frame instanceof CloseWebSocketFrame){
            handshaker.close(ctx.channel(),(CloseWebSocketFrame) frame.retain());
            return;
        }
 
        //判断是否是ping消息
        if(frame instanceof PongWebSocketFrame){
            PingWebSocketFrame ping = new PingWebSocketFrame(frame.content().retain());
            ctx.channel().writeAndFlush(ping);
            return ;
        }
 
        //判断是否是pong消息
        if(frame instanceof PingWebSocketFrame){
            PongWebSocketFrame pong = new PongWebSocketFrame(frame.content().retain());
            ctx.channel().writeAndFlush(pong);
            return ;
        }
 
        //仅支持文本消息，不支持二进制消息
        if(!(frame instanceof TextWebSocketFrame)){
            throw new UnsupportedOperationException("不支持二进制");
        }
 
        //返回应答消息
        //可以对消息进行处理
        //群发
        String request=((TextWebSocketFrame) frame).text();
 
        if (contexts.size() <= 1) {
            ctx.channel().write(new TextWebSocketFrame("对方不在线"));
            ctx.channel().write(new TextWebSocketFrame(request));
 
            //return;
        }else{
            int currentIndex = contexts.indexOf(ctx);
            int anotherIndex = Math.abs(currentIndex - 1);
            //给另一个客户端转发信息
            contexts.get(anotherIndex).writeAndFlush(new TextWebSocketFrame(request));
        }
    }
 
 
    @SuppressWarnings("deprecation")
	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception{
        //如果HTTP解码失败，则返回http异常
        if(!req.decoderResult().isSuccess()||(!"websocket".equals(req.headers().get("Upgrade")))){
            sendHttpResponse(ctx,req,new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.BAD_REQUEST));
            return;
        }
        
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.uri());
        Map<String, List<String>> parameters = queryStringDecoder.parameters();
        List<String> list = parameters.get("token");
        //String uri = req.getUri();
        //log.info(uri);
        Constant.onlineUserMap.put(Long.valueOf(list.get(0)), ctx);
        log.info("userId: {} =连接",list.get(0));
        //构造握手响应返回，本机测试
        //WebSocketServerHandshakerFactory wsfactory=new WebSocketServerHandshakerFactory("ws://localhost:7788/websocket",null,false);
        // Handshake
        WebSocketServerHandshakerFactory wsfactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, true);
        //注意，这第一个参数别被误导了，其实这里填写什么都无所谓，WS协议消息的接收不受这里控制
 
        handshaker=wsfactory.newHandshaker(req);
        if (handshaker==null){
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else{
            handshaker.handshake(ctx.channel(),req);
        }
 
    }
 
    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res){
        if(res.status().code()!=200){
            ByteBuf buf= Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
 
        //如果不是长连接，则关闭连接
        ChannelFuture future = ctx.channel().writeAndFlush(res);
        if (HttpHeaders.isKeepAlive(req)||res.status().code()!=200){
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
 
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //发生异常 关闭连接
        ctx.close();
    }
    
    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get("localhost:7788") + WEBSOCKET_PATH;
        return "ws://" + location;
    }

}
