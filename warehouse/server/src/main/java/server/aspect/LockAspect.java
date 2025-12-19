package server.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.aspect.annotation.ReadLock;
import server.aspect.annotation.WriteLock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Aspect
public class LockAspect {
    private static final Logger log = LoggerFactory.getLogger(LockAspect.class);

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    @Around("execution(* server.service.ProductService.*(..)) && @annotation(readLockAnnotation)")
    public Object handleReadLock(ProceedingJoinPoint joinPoint, ReadLock readLockAnnotation) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        log.info("--- Attempting READ Lock: {} ---", methodName);

        if (readLock.tryLock(readLockAnnotation.timeout(), readLockAnnotation.unit())) {
            try {
                log.info(">>> Locked READ: {}", methodName);
                return joinPoint.proceed();
            } finally {
                readLock.unlock();
                log.info("<<< Unlocked READ: {}", methodName);
            }
        } else {
            log.error("!!! READ Timeout: {} !!!", methodName);
            throw new RuntimeException("Hệ thống bận (Read Timeout): " + methodName);
        }
    }

    @Around("execution(* server.service.ProductService.*(..)) && @annotation(writeLockAnnotation)")
    public Object handleWriteLock(ProceedingJoinPoint joinPoint, WriteLock writeLockAnnotation) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        log.info("--- Attempting WRITE Lock: {} ---", methodName);

        if (writeLock.tryLock(writeLockAnnotation.timeout(), writeLockAnnotation.unit())) {
            try {
                log.info(">>> Locked WRITE: {}", methodName);
                return joinPoint.proceed();
            } finally {
                writeLock.unlock();
                log.info("<<< Unlocked WRITE: {}", methodName);
            }
        } else {
            log.error("!!! WRITE Timeout: {} !!!", methodName);
            throw new RuntimeException("Hệ thống bận (Write Timeout): " + methodName);
        }
    }
}