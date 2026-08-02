/***********************************************************
 **
 ** Kevin Randolph
 ** CS-320-13659-M01
 ** Professor Angelo Luo
 ** Assignment 3-2 Milestone: Contact Service
 ** 11/14/2025
 ** ContactService.java
 **
 ** This class manages the collection of contact objects in memory.
 ** It handles adding, updating, and deleting contacts while making
 ** sure each one keeps a unique ID.
 **
 ***********************************************************/


package contactservice;

import java.util.ArrayList;
import java.util.List;

public class ContactService {

    private final List<Contact> contacts = new ArrayList<>();

    // helper method finds contact by ID
    private Contact findContact(String contactId) {
        for (Contact contact : contacts) {
            if (contact.getContactId().equals(contactId)) {
                return contact;
            }
        }
        throw new IllegalArgumentException("Contact not found");
    }

    // add contact with unique ID
    public void addContact(String contactId, String firstName,
                           String lastName, String phone,
                           String address) {

        // ensure unique ID
        for (Contact contact : contacts) {
            if (contact.getContactId().equals(contactId)) {
                throw new IllegalArgumentException("Duplicate contact ID");
            }
        }

        Contact newContact = new Contact(contactId, firstName, lastName, phone, address);
        contacts.add(newContact);
    }

    // delete contact by ID
    public void deleteContact(String contactId) {
        Contact contactToRemove = findContact(contactId);
        contacts.remove(contactToRemove);
    }

    // update first name
    public void updateFirstName(String contactId, String newFirstName) {
        Contact contact = findContact(contactId);
        contact.setFirstName(newFirstName);
    }

    // update last name
    public void updateLastName(String contactId, String newLastName) {
        Contact contact = findContact(contactId);
        contact.setLastName(newLastName);
    }

    // update phone
    public void updatePhone(String contactId, String newPhone) {
        Contact contact = findContact(contactId);
        contact.setPhone(newPhone);
    }

    // update address
    public void updateAddress(String contactId, String newAddress) {
        Contact contact = findContact(contactId);
        contact.setAddress(newAddress);
    }

    // helper for tests
    public List<Contact> getAllContacts() {
        return new ArrayList<>(contacts);
    }
}



