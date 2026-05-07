package com.seshop.commerce.api.dto;

import jakarta.validation.constraints.NotNull;

public class CheckoutRequest {

    @NotNull(message = "Cart ID is required")
    private Long cartId;

    @NotNull(message = "Shipping address is required")
    private ShippingAddress shippingAddress;

    private ShippingAddress billingAddress;

    private String promotionCode;
    private String discountCode;
    
    private String paymentProvider;
    private String paymentMethod;

    // Getters and Setters
    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }

    public ShippingAddress getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(ShippingAddress shippingAddress) { this.shippingAddress = shippingAddress; }

    public ShippingAddress getBillingAddress() { return billingAddress; }
    public void setBillingAddress(ShippingAddress billingAddress) { this.billingAddress = billingAddress; }

    public String getPromotionCode() { return promotionCode; }
    public void setPromotionCode(String promotionCode) { this.promotionCode = promotionCode; }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public String getPaymentProvider() { return paymentProvider; }
    public void setPaymentProvider(String paymentProvider) { this.paymentProvider = paymentProvider; }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String resolvePaymentProvider() {
        if (paymentMethod != null && !paymentMethod.isBlank()) {
            return paymentMethod;
        }
        return paymentProvider;
    }

    public String shippingAddressText() {
        return shippingAddress == null ? null : shippingAddress.toText();
    }

    public String billingAddressText() {
        ShippingAddress address = billingAddress == null ? shippingAddress : billingAddress;
        return address == null ? null : address.toText();
    }

    public static class ShippingAddress {
        private String fullName;
        private String phoneNumber;
        private String line1;
        private String ward;
        private String district;
        private String city;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getLine1() { return line1; }
        public void setLine1(String line1) { this.line1 = line1; }
        public String getWard() { return ward; }
        public void setWard(String ward) { this.ward = ward; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        private String toText() {
            return String.join(", ",
                    nonNull(fullName),
                    nonNull(phoneNumber),
                    nonNull(line1),
                    nonNull(ward),
                    nonNull(district),
                    nonNull(city));
        }

        private String nonNull(String value) {
            return value == null ? "" : value;
        }
    }
}
