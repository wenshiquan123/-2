package com.hlzx.wenutil.http.able;

/**
 * Created by alan on 2016/3/12.
 */
public interface QueueAble {
    /**
     * Are already in the queue ?
     *
     * @return true: In the queue, false: not in the queue.
     */
    boolean isQueue();

    /**
     * Change queue state.
     *
     * @param queue true: In the queue, false: not in the queue.
     */
    void queue(boolean queue);

    /**
     * Change the current queue status as contrary to the current status.
     */
    void toggleQueue();
}
