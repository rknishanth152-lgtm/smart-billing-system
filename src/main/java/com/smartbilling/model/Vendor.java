package com.smartbilling.model;

import java.sql.Timestamp;

/**
 * Vendor
 * 
 * Model POJO representing a vendor/supplier in smart_billing_db.
 */
public class Vendor {

    private int vendorId;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private Timestamp createdAt;

    public Vendor() {
    }

    public Vendor(int vendorId, String name) {
        this.vendorId = vendorId;
        this.name = name;
    }

    public Vendor(int vendorId, String name, String contactPerson, String phone, String email, String address) {
        this.vendorId = vendorId;
        this.name = name;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public int getVendorId() {
        return vendorId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name; // Returned for JComboBox display in Swing dialogs
    }
}
