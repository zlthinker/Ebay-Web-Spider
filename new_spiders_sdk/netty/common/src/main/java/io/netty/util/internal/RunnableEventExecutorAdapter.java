/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util.internal;

import io.netty.util.concurrent.EventExecutor;

/**
 * Generic interface to wrap an {@link EventExecutor} and a {@link Runnable}.
 *
 * This interface is used internally to ensure scheduled tasks are executed by
 * the correct {@link EventExecutor} after channel migration.
 *
 * @see io.netty.util.concurrent.ScheduledFutureTask
 * @see io.netty.util.concurrent.SingleThreadEventExecutor
 */
public interface RunnableEventExecutorAdapter extends Runnable {
    /**
     * @return  the wrapped {@link EventExecutor}.
     */
    EventExecutor executor();

    /**
     * @return  the wrapped {@link Runnable}.
     */
    Runnable unwrap();
}
