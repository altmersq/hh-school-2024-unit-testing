package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryManagerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private LibraryManager libraryManager;

    @BeforeEach
    void setUp() {
        libraryManager.addBook("book1", 10);
        libraryManager.addBook("book2", 5);
    }

    @Test
    void testAddBook() {
        libraryManager.addBook("book3", 3);
        assertEquals(3, libraryManager.getAvailableCopies("book3"));
    }

    @Test
    void testBorrowBookSuccess() {
        when(userService.isUserActive("user1")).thenReturn(true);

        boolean result = libraryManager.borrowBook("book1", "user1");

        assertTrue(result);
        assertEquals(9, libraryManager.getAvailableCopies("book1"));
        verify(notificationService).notifyUser("user1", "You have borrowed the book: book1");
    }

    @Test
    void testBorrowBookUserNotActive() {
        when(userService.isUserActive("user1")).thenReturn(false);

        boolean result = libraryManager.borrowBook("book1", "user1");

        assertFalse(result);
        verify(notificationService).notifyUser("user1", "Your account is not active.");
    }

    @Test
    void testBorrowBookWhenBookNotInInventory() {
        String nonExistentBookId = "bookNotInInventory";

        boolean result = libraryManager.borrowBook(nonExistentBookId, "user1");

        assertFalse(result);
    }

    @Test
    void testBorrowBookWhenBookOutOfStock() {
        libraryManager.addBook("bookOutOfStock", 0);

        boolean result = libraryManager.borrowBook("bookOutOfStock", "user1");

        assertFalse(result);
    }

    @Test
    void testBorrowBookNoAvailableCopies() {
        when(userService.isUserActive("user1")).thenReturn(true);

        boolean result = libraryManager.borrowBook("book2", "user1");

        assertTrue(result);

        boolean secondResult = libraryManager.borrowBook("book2", "user2");

        assertFalse(secondResult);
    }

    @Test
    void testReturnBookSuccess() {
        when(userService.isUserActive("user1")).thenReturn(true);

        libraryManager.borrowBook("book1", "user1");
        boolean result = libraryManager.returnBook("book1", "user1");

        assertTrue(result);
        assertEquals(10, libraryManager.getAvailableCopies("book1"));
        verify(notificationService).notifyUser("user1", "You have returned the book: book1");
    }

    @Test
    void testReturnBookNotBorrowed() {
        boolean result = libraryManager.returnBook("book1", "user2");

        assertFalse(result);
    }

    @Test
    void testGetAvailableCopies() {
        assertEquals(10, libraryManager.getAvailableCopies("book1"));
        assertEquals(5, libraryManager.getAvailableCopies("book2"));
        assertEquals(0, libraryManager.getAvailableCopies("book3"));
    }

    @ParameterizedTest
    @CsvSource({
            "5, false, false, 2.50",
            "5, true, false, 3.75",
            "5, true, true, 3.00",
            "0, false, false, 0.00"
    })
    void testCalculateDynamicLateFee(int overdueDays, boolean isBestseller, boolean isPremiumMember, double expectedFee) {
        double fee = libraryManager.calculateDynamicLateFee(overdueDays, isBestseller, isPremiumMember);
        assertEquals(expectedFee, fee);
    }

    @Test
    void testCalculateDynamicLateFeeNegativeDays() {
        assertThrows(IllegalArgumentException.class, () -> libraryManager.calculateDynamicLateFee(-1, false, false));
    }

}
