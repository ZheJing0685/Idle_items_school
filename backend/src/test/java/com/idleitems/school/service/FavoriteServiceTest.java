package com.idleitems.school.service;

import com.idleitems.school.entity.Favorite;
import com.idleitems.school.entity.Item;
import com.idleitems.school.repository.FavoriteRepository;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    private Item testItem;
    private Favorite testFavorite;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setTitle("测试物品");
        testItem.setFavoriteCount(5);

        testFavorite = new Favorite();
        testFavorite.setId(1L);
        testFavorite.setUserId(1L);
        testFavorite.setItemId(1L);
    }

    @Test
    void addFavorite_WhenValidRequest_AddsFavorite() {
        // Arrange
        when(favoriteRepository.existsByUserIdAndItemId(1L, 1L)).thenReturn(false);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(testFavorite);

        // Act
        Favorite result = favoriteService.addFavorite(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(1L, result.getItemId());
        verify(favoriteRepository, times(1)).existsByUserIdAndItemId(1L, 1L);
        verify(itemRepository, times(1)).findById(1L);
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
        verify(itemRepository, times(1)).incrementFavoriteCount(1L);
    }

    @Test
    void addFavorite_WhenAlreadyFavorited_ThrowsException() {
        // Arrange
        when(favoriteRepository.existsByUserIdAndItemId(1L, 1L)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            favoriteService.addFavorite(1L, 1L);
        });
    }

    @Test
    void addFavorite_WhenItemNotExists_ThrowsException() {
        // Arrange
        when(favoriteRepository.existsByUserIdAndItemId(1L, 999L)).thenReturn(false);
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            favoriteService.addFavorite(1L, 999L);
        });
    }

    @Test
    void removeFavorite_WhenValidRequest_RemovesFavorite() {
        // Arrange
        when(favoriteRepository.existsByUserIdAndItemId(1L, 1L)).thenReturn(true);

        // Act
        favoriteService.removeFavorite(1L, 1L);

        // Assert
        verify(favoriteRepository, times(1)).existsByUserIdAndItemId(1L, 1L);
        verify(favoriteRepository, times(1)).deleteByUserIdAndItemId(1L, 1L);
        verify(itemRepository, times(1)).decrementFavoriteCount(1L);
    }

    @Test
    void removeFavorite_WhenNotFavorited_ThrowsException() {
        // Arrange
        when(favoriteRepository.existsByUserIdAndItemId(1L, 1L)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            favoriteService.removeFavorite(1L, 1L);
        });
    }

    @Test
    void isFavorited_WhenFavorited_ReturnsTrue() {
        // Arrange
        when(favoriteRepository.existsByUserIdAndItemId(1L, 1L)).thenReturn(true);

        // Act
        boolean result = favoriteService.isFavorited(1L, 1L);

        // Assert
        assertTrue(result);
        verify(favoriteRepository, times(1)).existsByUserIdAndItemId(1L, 1L);
    }

    @Test
    void isFavorited_WhenNotFavorited_ReturnsFalse() {
        // Arrange
        when(favoriteRepository.existsByUserIdAndItemId(1L, 1L)).thenReturn(false);

        // Act
        boolean result = favoriteService.isFavorited(1L, 1L);

        // Assert
        assertFalse(result);
        verify(favoriteRepository, times(1)).existsByUserIdAndItemId(1L, 1L);
    }
}
