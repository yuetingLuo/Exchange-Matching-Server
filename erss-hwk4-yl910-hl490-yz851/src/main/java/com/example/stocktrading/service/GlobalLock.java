package com.example.stocktrading.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class GlobalLock {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public void acquireReadLock() {
        readLock.lock();
    }

    public void releaseReadLock() {
        readLock.unlock();
    }

    public void acquireWriteLock() {
        writeLock.lock();
    }

    public void releaseWriteLock() {
        writeLock.unlock();
    }
}
