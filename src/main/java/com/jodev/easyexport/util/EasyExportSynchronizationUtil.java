package com.jodev.easyexport.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class EasyExportSynchronizationUtil {
    public static ReentrantLock LOCK = new ReentrantLock();
    public static Condition STOP_RENDERERS_CONDITION = LOCK.newCondition();
    public static Condition OPEN_FOR_PROCESSING_CONDITION = LOCK.newCondition();
    public static Condition NEW_RENDERER_CAN_START_CONDITION = LOCK.newCondition();
    public static Condition NEW_RENDERER_IS_OPEN = LOCK.newCondition();
}
