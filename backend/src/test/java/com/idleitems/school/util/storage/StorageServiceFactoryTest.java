package com.idleitems.school.util.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceFactoryTest {

    @Mock
    private LocalStorageAdapter localStorageAdapter;

    private StorageServiceFactory factory;

    @BeforeEach
    void setUp() {
        factory = new StorageServiceFactory();
        ReflectionTestUtils.setField(factory, "localStorageAdapter", localStorageAdapter);
        ReflectionTestUtils.setField(factory, "storageType", "local");
    }

    @Test
    void getStorageAdapter_Default_ReturnsLocal() {
        StorageAdapter adapter = factory.getStorageAdapter();
        assertSame(localStorageAdapter, adapter);
    }

    @Test
    void getStorageAdapter_WithLocalType_ReturnsLocal() {
        StorageAdapter adapter = factory.getStorageAdapter("local");
        assertSame(localStorageAdapter, adapter);
    }

    @Test
    void getStorageAdapter_WithInvalidType_ReturnsLocal() {
        StorageAdapter adapter = factory.getStorageAdapter("s3");
        assertSame(localStorageAdapter, adapter);
    }
}
