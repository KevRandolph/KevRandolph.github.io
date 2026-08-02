/***********************************************************
 **
 ** Kevin Randolph
 ** CS-320
 ** Contact Service
 ** 7/19/2026
 ** ContactService.java : Enhancements
 **
 ** This class manages the collection of contact objects in memory.
 ** It handles adding, updating, and deleting contacts while making
 ** sure each one keeps a unique ID.
 **
 ** Enhanced for CS 499 Milestone Two: replaced the ArrayList with a
 ** HashMap keyed by contact ID. This turns contact lookup and the
 ** duplicate ID check from an O(n) scan into a single O(1) call,
 ** and removes the need for two separate loops doing similar work
 **
 ***********************************************************/
package contactservice;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ContactService {
    // enhancement: HashMap keyed by contact ID replaces the ArrayList
    private final Map<String, Contact> contacts = new HashMap<>();
    // helper method finds contact by ID
    private Contact findContact(String contactId) {
        // enhancement: direct key lookup instead of looping through every contact
        Contact contact = contacts.get(contactId);
        if (contact == null) {
            throw new IllegalArgumentException("Contact not found");
        }
        return contact;
    }
    // add contact with unique ID
    public void addContact(String contactId, String firstName,
                           String lastName, String phone,
                           String address) {
        // enhancement: containsKey() replaces manual loop that used
        // to scan every existing contact to check for a duplicate ID
        if (contacts.containsKey(contactId)) {
            throw new IllegalArgumentException("Duplicate contact ID");
        }
        Contact newContact = new Contact(contactId, firstName, lastName, phone, address);
        contacts.put(contactId, newContact);
    }
    // delete contact by ID
    public void deleteContact(String contactId) {
        // confirms the contact exists (and throws if not) before removing it
        findContact(contactId);
        contacts.remove(contactId);
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
        // enhancement: pulls from the HashMap's values instead of copying the old ArrayList
        return new ArrayList<>(contacts.values());
    }
}