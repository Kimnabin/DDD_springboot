package com.ddd.demo.dto.order;

import com.ddd.demo.common.validation.ValidPhoneNumber;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressRequest {

    @NotBlank(message = "Recipient name is required")
    @Size(min = 2, max = 100, message = "Recipient name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "Recipient name can only contain letters and spaces")
    private String recipientName;

    @NotBlank(message = "Phone number is required")
    @ValidPhoneNumber(message = "Please provide a valid Vietnamese phone number (e.g., +84901234567 or 0901234567)")
    private String phoneNumber;

    @NotBlank(message = "Street address is required")
    @Size(min = 5, max = 255, message = "Street address must be between 5 and 255 characters")
    private String streetAddress;

    @Size(max = 100, message = "Ward must not exceed 100 characters")
    private String ward;

    @Size(max = 100, message = "District must not exceed 100 characters")
    private String district;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "Province is required")
    @Size(max = 100, message = "Province must not exceed 100 characters")
    private String province;

    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Vietnamese postal code must be exactly 6 digits")
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Pattern(regexp = "^(Vietnam|Việt Nam|VN)$", message = "Only Vietnam is supported")
    private String country = "Vietnam";
}