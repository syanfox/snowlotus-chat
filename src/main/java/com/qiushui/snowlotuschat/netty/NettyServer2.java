//package com.qfty.message_center.netty;
//
//import java.util.concurrent.TimeUnit;
//
//import javax.annotation.PostConstruct;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import com.qfty.message_center.service.impl.MessageServiceImpl;
//
//import io.netty.bootstrap.ServerBootstrap;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.ChannelOption;
//import io.netty.channel.EventLoopGroup;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioServerSocketChannel;
//import io.netty.handler.codec.http.HttpObjectAggregator;
//import io.netty.handler.codec.http.HttpServerCodec;
//import io.netty.handler.stream.ChunkedWriteHandler;
//import io.netty.handler.timeout.IdleStateHandler;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Component
//public class NettyServer2 {
//
//	@Value("${netty.server.port}")
//    public Integer port;
//
//	@Autowired
//	private MessageServiceImpl messageService;
//	
//	private Thread nettyThread;
//
//    public Integer getPort() {
//        return port;
//    }
//
//    public void setPort(Integer port) {
//        this.port = port;
//    }
//
//    public void startServer(){
//        //服务端需要2个线程组  boss处理客户端连接  work进行客服端连接之后的处理
//        EventLoopGroup boss = new NioEventLoopGroup();
//        EventLoopGroup work = new NioEventLoopGroup();
//        try {
//            ServerBootstrap bootstrap = new ServerBootstrap();
//            //服务器 配置
//            bootstrap.group(boss,work).channel(NioServerSocketChannel.class)
//            .childHandler(new ChannelInitializer<SocketChannel>() {
//                protected void initChannel(SocketChannel socketChannel) throws Exception {
//                    // HttpServerCodec：将请求和应答消息解码为HTTP消息
//                    socketChannel.pipeline().addLast("http-codec",new HttpServerCodec());
//                    // HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息
//                    socketChannel.pipeline().addLast("aggregator",new HttpObjectAggregator(65536));
//                    // ChunkedWriteHandler：向客户端发送HTML5文件
//                    socketChannel.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
//                    // 进行设置心跳检测
//                    socketChannel.pipeline().addLast(new IdleStateHandler(60,30,60*30, TimeUnit.SECONDS));
//                    // 配置通道处理  来进行业务处理
//                    socketChannel.pipeline().addLast(new TestChannelHandler(messageService));
//                }
//            }).option(ChannelOption.SO_BACKLOG,1024).childOption(ChannelOption.SO_KEEPALIVE,true);
//            //绑定端口  开启事件驱动
//            log.info("【服务器启动成功========端口："+port+"】");
//            Channel channel = bootstrap.bind(port).sync().channel();
//            channel.closeFuture().sync();
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            //关闭资源
//			boss.shutdownGracefully();
//			work.shutdownGracefully();
//        }
//    }
//
//    @PostConstruct()
//    public void init(){
//        //需要开启一个新的线程来执行netty server 服务器
//        new Thread(new Runnable() {
//            public void run() {
//                startServer();
//            }
//        }).start();
//    	
////    	 nettyThread = new Thread(startServer);
////         logger.info("开启独立线程，启动Netty WebSocket服务器...");
////         nettyThread.start();
//    }
//
//}
