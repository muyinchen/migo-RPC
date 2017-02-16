package com.nia.rpc.core.protocol;

import com.nia.rpc.core.serializer.KryoSerializer;
import com.nia.rpc.core.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author  知秋
 * Created by Auser on 2017/2/17.
 */
//常用的处理大数据分包传输问题的解决类:LengthFieldBasedFrameDecoder
public class RpcDecoder extends LengthFieldBasedFrameDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcDecoder.class);
    private Serializer serializer = new KryoSerializer();

    public RpcDecoder(int maxFrameLength) {
        super(maxFrameLength, 0, 4, 0, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf decode = (ByteBuf) super.decode(ctx, in);
        if (decode != null) {
            int byteLength = decode.readableBytes();
            byte[] byteHolder = new byte[byteLength];
            decode.readBytes(byteHolder);
            Object deserialize = serializer.deserialize(byteHolder);
            return deserialize;
        }
        LOGGER.debug("Decoder Result is null");
        return null;
    }
}
