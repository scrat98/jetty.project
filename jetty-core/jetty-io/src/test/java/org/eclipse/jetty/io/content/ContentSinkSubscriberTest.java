//package org.eclipse.jetty.io.content;
//
//import java.nio.ByteBuffer;
//import java.util.Random;
//import java.util.concurrent.Flow;
//
//import org.eclipse.jetty.io.Content;
//import org.eclipse.jetty.util.Callback;
//import org.reactivestreams.tck.TestEnvironment;
//import org.reactivestreams.tck.flow.FlowSubscriberWhiteboxVerification;
//
//public final class ContentSinkSubscriberTest extends FlowSubscriberWhiteboxVerification<Content.Chunk>
//{
//    public static final int chunkSize = 16;
//    private static final Random random = new Random();
//
//    public ContentSinkSubscriberTest()
//    {
//        super(new TestEnvironment());
//    }
//
//    @Override
//    public Content.Chunk createElement(int element)
//    {
//        return randomChunk(false);
//    }
//
//    @Override
//    protected Flow.Subscriber<Content.Chunk> createFlowSubscriber(WhiteboxSubscriberProbe<Content.Chunk> probe)
//    {
//        return new ContentSinkSubscriber((last, byteBuffer, callback) -> callback.succeeded(), Callback.NOOP)
//        {
//            @Override
//            public void onSubscribe(Flow.Subscription subscription)
//            {
//                super.onSubscribe(subscription);
//                probe.registerOnSubscribe(new SubscriberPuppet()
//                {
//                    @Override
//                    public void triggerRequest(long elements)
//                    {
//                        subscription.request(elements);
//                    }
//
//                    @Override
//                    public void signalCancel()
//                    {
//                        subscription.cancel();
//                    }
//                });
//            }
//
//            @Override
//            public void onNext(Content.Chunk chunk)
//            {
//                super.onNext(chunk);
//                probe.registerOnNext(chunk);
//            }
//
//            @Override
//            public void onError(Throwable failure)
//            {
//                super.onError(failure);
//                probe.registerOnError(failure);
//            }
//
//            @Override
//            public void onComplete()
//            {
//                super.onComplete();
//                probe.registerOnComplete();
//            }
//        };
//    }
//
//    private static Content.Chunk randomChunk(boolean last)
//    {
//        byte[] payload = new byte[chunkSize];
//        random.nextBytes(payload);
//        return Content.Chunk.from(ByteBuffer.wrap(payload), last);
//    }
//}