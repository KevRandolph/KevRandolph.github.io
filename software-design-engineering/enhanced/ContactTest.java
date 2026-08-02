/***********************************************************
 **
 ** Kevin Randolph
 ** CS-320
 ** Contact Service
 ** 11/14/2025
 ** ContactTest.java: Enhancements
 **
 ** This file contains the unit tests for the Contact class.
 ** Each test checks that the object enforces its validation rules
 ** and rejects invalid data the way it?s supposed to.
 **
 ** Enhanced for CS 499 Milestone Two: added tests for the new
 ** empty/blank string validation, plus true boundary value tests
 ** at the exact length limits, since the original suite only ever
 ** tested clearly oversized values, not the actual edge
 **
 ***********************************************************/

package contactservice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ContactTest {

    @Test
    public void testValidContactCreation() {
        Contact contact = new Contact(
                "1234567890",
                "Kevin",
                "Randolph",
                "0123456789",
                "123 Main Street, NY");

        assertEquals("1234567890", contact.getContactId());
        assertEquals("Kevin", contact.getFirstName());
        assertEquals("Randolph", contact.getLastName());
        assertEquals("0123456789", contact.getPhone());
        assertEquals("123 Main Street, NY", contact.getAddress());
    }

    @Test
    public void testContactIdCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact(null, "Kevin", "Randolph", "0123456789", "123 Main Street, NY");
        });
    }

    // enhancement: new test confirms an empty contact ID is rejected,
    // which the original suite never covered (only null and too long)
    @Test
    public void testContactIdCannotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("", "Kevin", "Randolph", "0123456789", "123 Main Street, NY");
        });
    }

    @Test
    public void testContactIdTooLong() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("12345678901", "Kevin", "Randolph", "0123456789", "123 Main Street, NY");
        });
    }

    // enhancement: new test confirms the exact boundary for contact ID:
    // 10 characters should pass, 11 should fail (the original test only
    // used an obviously oversized 11 character string, not a true boundary check)
    @Test
    public void testContactIdAtBoundaryLength() {
        // exactly 10 characters should be valid
        Contact contact = new Contact("1234567890", "Kevin", "Randolph", "0123456789", "123 Main Street, NY");
        assertEquals("1234567890", contact.getContactId());

        // exactly 11 characters should fail
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("12345678901", "Kevin", "Randolph", "0123456789", "123 Main Street, NY");
        });
    }

    @Test
    public void testFirstNameCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("12345", null, "Randolph", "0123456789", "123 Main Street, NY");
        });
    }

    // enhancement: new test confirms an empty first name is rejected
    @Test
    public void testFirstNameCannotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("12345", "", "Randolph", "0123456789", "123 Main Street, NY");
        });
    }

    @Test
    public void testFirstNameTooLong() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("12345", "VeryLongName", "Randolph", "0123456789", "123 Main Street, NY");
        });
    }

    // enhancement: new test confirms true boundary check for first name:
    // exactly 10 characters should pass, 11 should fail
    @Test
    public void testFirstNameAtBoundaryLength() {
        // exactly 10 characters should be valid
        Contact contact = new Contact("12345", "1234567890", "Randolph", "0123456789", "123 Main Street, NY");
        assertEquals("1234567890", contact.getFirstName());

        // exactly 11 characters should fail
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("12345", "12345678901", "Randolph", "0123456789", "123 Main Street, NY");
        });
    }

    @Test
    public void testLastNameCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("12345", "Kevin", null, "0123456789", "123 Main Street, NY");
        });
    }

    // enhancement: new test confirms an empty last name is rejected
    @Test
    public void