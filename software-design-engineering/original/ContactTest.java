/***********************************************************
 **
 ** Kevin Randolph
 ** CS-320-13659-M01
 ** Professor Angelo Luo
 ** Assignment 3-2 Milestone: Contact Service
 ** 11/14/2025
 ** ContactTest.java
 **
 ** This file contains the unit tests for the Contact class.
 ** Each test checks that the object enforces its validation rules
 ** and rejects invalid data the way it?s supposed to.
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

    @Test
    public void testContactIdTooLong() {
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

    @Test
    public void testFirstNameTooLong() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("12345", "VeryLongName", "Randolph", "0123456789", "123 Main Street, NY");
        });
    }

    @Test
    public void testLastNameCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("12345", "Kevin", null, "0123456789", "123 Main Street, NY");
        });
    }

    @Test
    public void testLastNameTooLong() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("12345", "Kevin", "VeryLongName", "0123456789", "123 Main Street, NY");
        });
    }

    @Test
    public void testPhoneCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("12345", "Kevin", "Randolph", null, "123 Main Street, NY");
        });
    }

    @Test
    public void testPhoneMustBeTenDigits() {
        // too short
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("12345", "Kevin", "Randolph", "123456789", "123 Main Street, NY");
        });

        // too long
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("12345", "Kevin", "Randolph", "12345678901", "123 Main Street, NY");
        });

        // not all digits
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("12345", "Kevin", "Randolph", "12345abcde", "123 Main Street, NY");
        });
    }

    @Test
    public void testAddressCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("12345", "Kevin", "Randolph", "0123456789", null);
        });
    }

    @Test
    public void testAddressTooLong() {
        String longAddress = "123 This Address Is Way Too Long To Be Valid";
        assertThrows(IllegalArgumentException.class, () -> {
            new Contact("12345", "Kevin", "Randolph", "0123456789", longAddress);
        });
    }

    @Test
    public void testUpdateFieldsWork() {
        Contact contact = new Contact(
                "1234567890",
                "Kevin",
                "Randolph",
                "0123456789",
                "123 Main Street, NY");

        contact.setFirstName("NewFirst");
        contact.setLastName("NewLast");
        contact.setPhone("1112223333");
        contact.setAddress("New Address");

        assertEquals("NewFirst", contact.getFirstName());
        assertEquals("NewLast", contact.getLastName());
        assertEquals("1112223333", contact.getPhone());
        assertEquals("New Address", contact.getAddress());
    }
}

