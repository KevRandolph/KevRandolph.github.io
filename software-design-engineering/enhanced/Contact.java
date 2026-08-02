/***********************************************************
 **
 ** Kevin Randolph
 ** CS-320 
 ** Contact Service
 ** 7/19/2026
 ** Contact.java: Enhancements
 **
 ** This class represents a single contact in the application.
 ** It stores the contacts information and enforces all validation
 ** rules like field lengths and required values.
 **
 ** Enhanced for CS 499 Milestone Two: added checks that reject
 ** empty/blank strings, not just null or too long values
 **
 ***********************************************************/
package contactservice;
public class Contact {
    private final String contactId; 
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    public Contact(String contactId, String firstName, String lastName,
                   String phone, String address) {
        // validates contactId
        // enhancement: rejects empty/blank contact IDs, not just null or too long
        if (contactId == null || contactId.trim().isEmpty() || contactId.length() > 10) {
            throw new IllegalArgumentException("Invalid contact ID");
        }
        this.contactId = contactId;
        // use update methods to reuse validation logic
        setFirstName(firstName);
        setLastName(lastName);
        setPhone(phone);
        setAddress(address);
    }
    // getters
    public String getContactId() {
        return contactId;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getPhone() {
        return phone;
    }
    public String getAddress() {
        return address;
    }
    // setters 
    public void setFirstName(String firstName) {
        // enhancement: rejects empty/blank first names, not just null or too long
        if (firstName == null || firstName.trim().isEmpty() || firstName.length() > 10) {
            throw new IllegalArgumentException("Invalid first name");
        }
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        // enhancement: rejects empty/blank last names, not just null or too long
        if (lastName == null || lastName.trim().isEmpty() || lastName.length() > 10) {
            throw new IllegalArgumentException("Invalid last name");
        }
        this.lastName = lastName;
    }
    public void setPhone(String phone) {
        // empty string already fails the length != 10 check
        if (phone == null || phone.length() != 10 || !phone.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid phone number");
        }
        this.phone = phone;
    }
    public void setAddress(String address) {
        // enhancement: rejects empty/blank addresses, not just null or too long
        if (address == null || address.trim().isEmpty() || address.length() > 30) {
            throw new IllegalArgumentException("Invalid address");
        }
        this.address = address;
    }
}