package com.rent1.api.dto;

/**
 * 联系人DTO
 */
public class ContactDTO {

    private String contactId;
    
    @Override
    public String toString() {
        return "ContactDTO{" +
                "contactId='" + contactId + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", relationship='" + relationship + '\'' +
                ", relationshipName='" + relationshipName + '\'' +
                ", isDefault=" + isDefault +
                ", isEmergency=" + isEmergency +
                '}';
    }

    private String name;
    private String phone;
    private String relationship;
    private String relationshipName;
    private Boolean isDefault;
    private Boolean isEmergency;

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public void setRelationshipName(String relationshipName) {
        this.relationshipName = relationshipName;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getIsEmergency() {
        return isEmergency;
    }

    public void setIsEmergency(Boolean isEmergency) {
        this.isEmergency = isEmergency;
    }
}
