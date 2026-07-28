package com.idleitems.school.task;

import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchedulerLockTest {

    @Mock
    private LockProvider lockProvider;

    @BeforeEach
    void setUp() {
        LockAssert.TestHelper.makeAllAssertsPass(false);
    }

    @Test
    void shouldExecuteWhenLockAcquired() {
        AtomicInteger counter = new AtomicInteger(0);
        when(lockProvider.lock(any()))
                .thenReturn(Optional.of((SimpleLock) () -> {}));

        Optional<SimpleLock> lock = lockProvider.lock(new LockConfiguration(
                Instant.now(), "testTask",
                Duration.ofSeconds(30), Duration.ofSeconds(5)));

        if (lock.isPresent()) {
            counter.incrementAndGet();
            lock.get().unlock();
        }

        assertEquals(1, counter.get());
    }

    @Test
    void shouldNotExecuteWhenLockNotAcquired() {
        AtomicInteger counter = new AtomicInteger(0);
        when(lockProvider.lock(any()))
                .thenReturn(Optional.empty());

        Optional<SimpleLock> lock = lockProvider.lock(new LockConfiguration(
                Instant.now(), "testTask",
                Duration.ofSeconds(30), Duration.ofSeconds(5)));

        if (lock.isPresent()) {
            counter.incrementAndGet();
        }

        assertEquals(0, counter.get());
    }

    @Test
    void shouldUseCorrectLockConfiguration() {
        Duration lockAtMostFor = Duration.ofSeconds(30);
        Duration lockAtLeastFor = Duration.ofSeconds(5);

        LockConfiguration config = new LockConfiguration(
                Instant.now(), "autoConfirmReceive",
                lockAtMostFor, lockAtLeastFor);

        assertEquals("autoConfirmReceive", config.getName());
        assertEquals(lockAtMostFor, config.getLockAtMostFor());
        assertEquals(lockAtLeastFor, config.getLockAtLeastFor());
    }

    @Test
    void shouldReleaseLockAfterExecution() {
        AtomicInteger lockReleased = new AtomicInteger(0);
        when(lockProvider.lock(any()))
                .thenReturn(Optional.of((SimpleLock) lockReleased::incrementAndGet));

        Optional<SimpleLock> lock = lockProvider.lock(new LockConfiguration(
                Instant.now(), "testTask",
                Duration.ofSeconds(30), Duration.ofSeconds(5)));

        lock.ifPresent(SimpleLock::unlock);

        assertEquals(1, lockReleased.get());
    }
}
