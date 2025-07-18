package com.ddd.demo.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {

    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @Column(name = "street_address", length = 255)
    private String streetAddress;

    @Column(name = "ward", length = 100)
    private String ward;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "province", length = 100)
    private String province;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "country", length = 100)
    private String country;

    // Get full address
    public String getFullAddress() {
        return String.format("%s, %s, %s, %s, %s %s, %s",
                streetAddress,
                ward != null ? ward : "",
                district != null ? district : "",
                city,
                province,
                postalCode,
                country
        ).replaceAll(", ,", ",");
    }
}