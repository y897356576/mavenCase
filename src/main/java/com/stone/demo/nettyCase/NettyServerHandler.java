package com.stone.demo.nettyCase;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by admin on 2018/3/25.
 */
@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelHandlerAdapter {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("服务端与客户端 " + ctx.channel().toString() + " 建立链接\r\n");
        ctx.writeAndFlush(Unpooled.copiedBuffer("服务端向你表示欢迎\r\n".getBytes()));
    }

    /**
     *
     * @param ctx 与客户端通道的上下文
     * @param msg 客户端发送的信息主题
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String inMsg = ((ByteBuf) msg).toString(CharsetUtil.UTF_8);
        System.out.println("服务端收到的客户端 " + ctx.channel().toString() + " 的消息是：" + inMsg);

        //验证消息是否为通知关闭连接
        this.checkCloseMsg(ctx, inMsg);

        String currentTime;
        if ("query time order".equalsIgnoreCase(inMsg)) {
            currentTime = "online time " + sdf.format(new Date());
        } else {
            currentTime = "Bad request";
        }
        ByteBuf responseBuf = Unpooled.copiedBuffer(currentTime.getBytes());
        //通过与客户端的通道回写信息
        ctx.write(responseBuf);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //1、向客户端写入空字符串
        //2、向客户端发送写入的信息
        //3、添加监听，促使客户端关闭链接
//        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("主机：" + ctx.channel().remoteAddress()+ " 出现异常；" + cause.getMessage());
        ctx.close();
    }

    private void checkCloseMsg(ChannelHandlerContext ctx, String msg) {
        if("please close the connection".equalsIgnoreCase(msg)) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
