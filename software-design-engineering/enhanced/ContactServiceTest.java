/***********************************************************
 **
 ** Kevin Randolph
 ** CS-320
 ** Contact Service
 ** 7/19/2026
 ** ContactServiceTest.java: Enhancements
 **
 ** This file includes the JUnit tests for the ContactService class.
 ** These tests make sure contacts can be added, updated, and deleted,
 ** and that the service handles bad input or missing IDs correctly.
 **
 ** Enhanced for CS 499 Milestone Two: added tests covering the new
 ** empty string validation added to Contact.java, since the original
 ** suite only tested null and too long values, never empty strings
 **
 ***********************************************************/

package contactservice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ContactServiceTest {

    private ContactService contactService;

    @BeforeEach
    public void setUp() {
        contactService = new ContactService();
    }

    @Test
    public void testAddContactSuccessfully() {
        contactService.addContact(
                "1",
                "Kevin",
                "Randolph",
                "0123456789",
                "123 Main Street");

        assertEquals(1, contactService.getAllContacts().size());
        assertEquals("Kevin", contactService.getAllContacts().get(0).getFirstName());
    }

    @Test
    public void testAddContactWithDuplicateIdThrowsException() {
        contactService.addContact(
                "1",
                "Kevin",
                "Randolph",
                "0123456789",
                "123 Main Street");

        assertThrows(IllegalArgumentException.class, () -> {
            contactService.addContact(
                    "1",                 // same ID
                    "Other",
                    "Person",
                    "1112223333",
                    "Other Address");
        });
    }

    // enhancement: new test confirms addContact rejects an empty
    // contact ID, which the original suite never covered
    @Test
    public void testAddContactWithEmptyIdThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            contactService.addContact(
                    "",
                    "Kevin",
                    "Randolph",
                    "0123456789",
                    "123 Main Street");
        });
    }

    // enhancement: new test confirms addContact rejects an empty
    // first name, which the original suite never covered
    @Test
    public void testAddContactWithEmptyFirstNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            contactService.addContact(
                    "1",
                    "",
                    "Randolph",
                    "0123456789",
                    "123 Main Street");
        });
    }

    // enhancement: new test confirms addContact rejects a blank
    // (whitespace only) address, which the original suite never covered
    @Test
    public void testAddContactWithBlankAddressThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            contactService.addContact(
                    "1",
                    "Kevin",
                    "Randolph",
                    "0123456789",
                    "   ");
        });
    }

    @Test
    public void testDeleteContactSuccessfully() {
        contactService.addContact(
                "1",
                "Kevin",
                "Randolph",
                "0123456789",
                "123 Main Street");

        contactService.deleteContact("1");

        assertEquals(0, contactService.getAllContacts().size());
    }

    @Test
    public void testDeleteNonExistingContactThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            contactService.deleteContact("999");
        });
    }

    @Test
    public void testUpdateFirstName() {
        contactService.addContact(
                "1",
                "Kevin",
                "Randolph",
                "0123456789",
                "123 Main Street");

        contactService.updateFirstName("1", "NewFirst");

        assertEquals("NewFirst", contactService.getAllContacts().get(0).getFirstName());
    }

    @Test
    public void testUpdateLastName() {
        contactService.addContact(
                "1",
                "Kevin",
                "Randolph",
                "0123456789",
                "123 Main Street");

        contactService.updateLastName("1", "NewLast");

        assertEquals("NewLast", contactService.getAllContacts().get(0).getLastName());
    }

    @Test
    public void testUpdatePhone() {
        contactService.addContact(
                "1",
                "Kevin",
                "Randolph",
                "0123456789",
                "123 Main Street");

        contactService.updatePhone("1", "1112223333");

        assertEquals("1112223333", contactService.getAllContacts().get(0).getPhone());
    }

    @Test
    public void testUpdateAddress() {
        contactService.addContact(
                "1",
                "Kevin",
                "Randolph",
                "0123456789",
                "123 Main Street");

        contactService.updateAddress("1", "New Address");

        assertEquals("New Address", contactService.getAllContacts().get(0).getAddress());
    }

    @Test
    public void testUpdateNonExistingContactThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            contactService.updateFirstName("999", "Test");
        });
    }

    // enhancement: new test confirms updateFirstName rejects an
    // empty first name update, not just an empty value at creation
    @Test
    public void testUpdateFirstNameToEmptyThrowsException() {
        contactService.addContact(
                "1",
                "Kevin",
                "Randolph",
                "0123456789",
                "123 Main Street");

        assertThrows(IllegalArgumentException.class, () -> {
            contactService.updateFirstName("1", "");
        });
    }

    @Test
    public void testUpdateWithInvalidPhoneThrowsException() {
        contactService.addContact(
                "1",
                "Kevin",
                "Randolph",
                "0123456789",
                "123 Main Street");

        assertThrows(IllegalArgumentException.class, () -> {
            contactService.updatePhone("1", "123");   // invalid phone
        });
    }
}