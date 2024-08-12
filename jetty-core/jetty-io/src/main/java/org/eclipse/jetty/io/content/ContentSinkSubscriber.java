//
// ========================================================================
// Copyright (c) 1995 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package org.eclipse.jetty.io.content;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.thread.Invocable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A {@link Flow.Subscriber} that wraps a {@link Content.Sink}.</p>
 * <p>Content delivered to the {@link #onNext(Content.Chunk)} method is
 * written to {@link Content.Sink#write(boolean, ByteBuffer, Callback)}
 * and the chunk is released once the write callback is succeeded or failed.</p>
 */
public class ContentSinkSubscriber implements Flow.Subscriber<Content.Chunk>
{
    private static final Logger LOG = LoggerFactory.getLogger(ContentSinkSubscriber.class);

    private final AtomicReference<Content.Sink> sink;
    private final AtomicReference<Callback> callback;
    private final AtomicReference<State> state;

    public ContentSinkSubscriber(Content.Sink sink, Callback callback)
    {
        Objects.requireNonNull(sink, "Content.Sink must not be null");
        Objects.requireNonNull(callback, "Callback must not be null");
        this.sink = new AtomicReference<>(sink);
        this.callback = new AtomicReference<>(callback);
        this.state = new AtomicReference<>(new State());
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription)
    {
        // As per rule 2.13, we need to throw a `java.lang.NullPointerException`
        // if the `Subscription` is `null`
        Objects.requireNonNull(subscription, "Flow.Subscription must not be null");

        Content.Sink sink = this.sink.getAndSet(null);
        if (sink != null)
        {
            subscription.request(1);
        }
        else
        {
            subscription.cancel();
        }
    }

    @Override
    public void onNext(Content.Chunk chunk)
    {
        // As per rule 2.13, we need to throw a `java.lang.NullPointerException`
        // if the `Content.Chunk` is `null`
        Objects.requireNonNull(chunk, "Content.Chunk must not be null");

        // TODO: before onSubscribe
        // TODO: more elements than requested
        // TODO: after final signal onComplete/onError

        // Retain the chunk because the write may not complete immediately.
        chunk.retain();
        sink.write(chunk.isLast(), chunk.getByteBuffer(), new Callback()
        {
            public void succeeded()
            {
                chunk.release();
                if (chunk.isLast())
                    complete();
                else
                    subscription.request(1);
            }

            public void failed(Throwable failure)
            {
                chunk.release();
                subscription.cancel();
                error(failure);
            }

            @Override
            public InvocationType getInvocationType()
            {
                return Invocable.getInvocationType(callback);
            }
        });
    }

    @Override
    public void onError(Throwable failure)
    {
        // As per rule 2.13, we need to throw a `java.lang.NullPointerException`
        // if the `Content.Chunk` is `null`
        Objects.requireNonNull(failure, "Throwable must not be null");

        // TODO: before onSubscribe
        // TODO: after final signal onComplete/onError
        error(failure);
    }

    @Override
    public void onComplete()
    {
        // TODO: before onSubscribe
        // TODO: after final signal onComplete/onError
        complete();
    }

    private void error(Throwable failure)
    {
        if (callbackComplete.compareAndSet(false, true))
            callback.failed(failure);
    }

    private void complete()
    {
        // Success the callback only when called twice:
        // once from last write success and once from the publisher.
        if (lastAndComplete.decrementAndGet() == 0)
        {
            if (callbackComplete.compareAndSet(false, true))
                callback.succeeded();
        }
    }

    private sealed interface State
    {

        void onSubscribe

        public static final class Ready implements State {}
    }
}

/*
1. onComplete без данных
2.9/2.10 A Subscriber MUST be prepared to receive an onComplete/onError signal with or without
a preceding Subscription.request(long n) call.

2. multiple onSubscribe - надо делать cancel подписки
2.5 A Subscriber MUST call Subscription.cancel() on the given Subscription after an
onSubscribe signal if it already has an active Subscription.

3. Subscription.request/cancel - если throws exception

4. если onNext не выполняет контракт и присылает больше, чем просили

5.
Subscriber.onComplete() and Subscriber.onError(Throwable t) MUST NOT call any methods
on the Subscription or the Publisher.

Subscriber.onComplete() and Subscriber.onError(Throwable t) MUST consider the Subscription
cancelled after having received the signal.

Subscriber.onSubscribe MUST be called at most once for a
given Subscriber (based on object equality).

6. ???Может произойти StackOverflow - по идее нет
request(1)
  onNext(chunk)
    sink.write()
       request(1)
          onNext(chunk)
            sink.write()

7. ???в конце делаем sink.write(last=true), даже если была ошибка
*/