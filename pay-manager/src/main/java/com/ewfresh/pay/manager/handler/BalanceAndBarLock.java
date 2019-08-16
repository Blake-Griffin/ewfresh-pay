package com.ewfresh.pay.manager.handler;

import com.ewfresh.commons.util.lock.Lock;
import com.ewfresh.commons.util.lock.RedisLockHandler;
import com.ewfresh.pay.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:
 * @author DuanXiangming
 * Date 2019/3/22 0022
 */
@Component
public class BalanceAndBarLock {

    private  Logger logger = LoggerFactory.getLogger(BalanceAndBarLock.class);

    @Autowired
    private RedisLockHandler lockHandler;

    /**
     * Description:支付系统中根据用户uid使用余额和白条时获取分布式锁的方法
     * @author DuanXiangming
     * @param uid  用户ID
     * @return boolean     获取是否成功
     * Date    2019/3/22 0022  下午 3:03
     */
    public boolean getBalanceAndBarLock(String uid){
        boolean lockFlag = false;
        try {
            Lock lock = makeLock(uid);
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            //logger.info("this method get lock now ==============>[methodName = {}]",methodName);
            lockFlag = lockHandler.tryLock(lock);
            //logger.info("this method get lock success ==============> [methodName = {}]",methodName);
        } catch (Exception e) {
            logger.error("get balanceAndBarLock err",e);
        }finally {
            return lockFlag;
        }
    }

    /**
     * Description:支付系统中根据用户uid使用余额和白条时释放分布式锁的方法
     * @author DuanXiangming
     * @param uid 用户ID
     * Date    2019/3/22 0022  下午 3:03
     */
    public void  releaseLock(String uid){
        try {
            Lock lock = makeLock(uid);
            lockHandler.releaseLock(lock);
        } catch (Exception e) {
            logger.error("release lock err",e);
        }
    }


    private Lock makeLock(String uid){
        Lock lock = null;
        try {
            String lockName = Constants.PAY_BY_BANLANCE + Constants.JOINT + uid;
            lock = new Lock(lockName,lockName);
        } catch (Exception e) {
            logger.error("make lock err",e);
        }finally {
            return lock;
        }
    }
}
