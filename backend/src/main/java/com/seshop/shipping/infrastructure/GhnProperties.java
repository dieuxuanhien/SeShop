package com.seshop.shipping.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "seshop.integrations.ghn")
public class GhnProperties {

    private boolean enabled = false;
    private String baseUrl = "https://dev-online-gateway.ghn.vn";
    private String token;
    private String shopId;
    private String createOrderPath = "/shiip/public-api/v2/shipping-order/create";
    private String trackPath = "/shiip/public-api/v2/shipping-order/detail";
    private String createShopPath = "/shiip/public-api/v2/shop/register";
    private int paymentTypeId = 2;
    private int serviceTypeId = 2;
    private int defaultWeightGrams = 500;
    private int defaultLengthCm = 12;
    private int defaultWidthCm = 12;
    private int defaultHeightCm = 12;
    private int defaultCodAmount = 0;
    private int defaultInsuranceValue = 0;
    private int defaultToDistrictId = 0;
    private String defaultToWardCode = "";
    private String defaultToAddress = "";
    private String defaultFromName = "SeShop Dev";
    private String defaultFromPhone = "0987654321";
    private String defaultFromAddress = "1 Quốc Hương";
    private String defaultFromWardName = "Phường Thảo Điền";
    private String defaultFromDistrictName = "Quận 2";
    private String defaultFromProvinceName = "HCM";
    private String requiredNote = "KHONGCHOXEMHANG";
    private String defaultContent = "SeShop order";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getCreateOrderPath() {
        return createOrderPath;
    }

    public void setCreateOrderPath(String createOrderPath) {
        this.createOrderPath = createOrderPath;
    }

    public String getTrackPath() {
        return trackPath;
    }

    public void setTrackPath(String trackPath) {
        this.trackPath = trackPath;
    }

    public String getCreateShopPath() {
        return createShopPath;
    }

    public void setCreateShopPath(String createShopPath) {
        this.createShopPath = createShopPath;
    }

    public int getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(int paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public int getServiceTypeId() {
        return serviceTypeId;
    }

    public void setServiceTypeId(int serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }

    public int getDefaultWeightGrams() {
        return defaultWeightGrams;
    }

    public void setDefaultWeightGrams(int defaultWeightGrams) {
        this.defaultWeightGrams = defaultWeightGrams;
    }

    public int getDefaultLengthCm() {
        return defaultLengthCm;
    }

    public void setDefaultLengthCm(int defaultLengthCm) {
        this.defaultLengthCm = defaultLengthCm;
    }

    public int getDefaultWidthCm() {
        return defaultWidthCm;
    }

    public void setDefaultWidthCm(int defaultWidthCm) {
        this.defaultWidthCm = defaultWidthCm;
    }

    public int getDefaultHeightCm() {
        return defaultHeightCm;
    }

    public void setDefaultHeightCm(int defaultHeightCm) {
        this.defaultHeightCm = defaultHeightCm;
    }

    public int getDefaultCodAmount() {
        return defaultCodAmount;
    }

    public void setDefaultCodAmount(int defaultCodAmount) {
        this.defaultCodAmount = defaultCodAmount;
    }

    public int getDefaultInsuranceValue() {
        return defaultInsuranceValue;
    }

    public void setDefaultInsuranceValue(int defaultInsuranceValue) {
        this.defaultInsuranceValue = defaultInsuranceValue;
    }

    public int getDefaultToDistrictId() {
        return defaultToDistrictId;
    }

    public void setDefaultToDistrictId(int defaultToDistrictId) {
        this.defaultToDistrictId = defaultToDistrictId;
    }

    public String getDefaultToWardCode() {
        return defaultToWardCode;
    }

    public void setDefaultToWardCode(String defaultToWardCode) {
        this.defaultToWardCode = defaultToWardCode;
    }

    public String getDefaultToAddress() {
        return defaultToAddress;
    }

    public void setDefaultToAddress(String defaultToAddress) {
        this.defaultToAddress = defaultToAddress;
    }

    public String getDefaultFromName() {
        return defaultFromName;
    }

    public void setDefaultFromName(String defaultFromName) {
        this.defaultFromName = defaultFromName;
    }

    public String getDefaultFromPhone() {
        return defaultFromPhone;
    }

    public void setDefaultFromPhone(String defaultFromPhone) {
        this.defaultFromPhone = defaultFromPhone;
    }

    public String getDefaultFromAddress() {
        return defaultFromAddress;
    }

    public void setDefaultFromAddress(String defaultFromAddress) {
        this.defaultFromAddress = defaultFromAddress;
    }

    public String getDefaultFromWardName() {
        return defaultFromWardName;
    }

    public void setDefaultFromWardName(String defaultFromWardName) {
        this.defaultFromWardName = defaultFromWardName;
    }

    public String getDefaultFromDistrictName() {
        return defaultFromDistrictName;
    }

    public void setDefaultFromDistrictName(String defaultFromDistrictName) {
        this.defaultFromDistrictName = defaultFromDistrictName;
    }

    public String getDefaultFromProvinceName() {
        return defaultFromProvinceName;
    }

    public void setDefaultFromProvinceName(String defaultFromProvinceName) {
        this.defaultFromProvinceName = defaultFromProvinceName;
    }

    public String getRequiredNote() {
        return requiredNote;
    }

    public void setRequiredNote(String requiredNote) {
        this.requiredNote = requiredNote;
    }

    public String getDefaultContent() {
        return defaultContent;
    }

    public void setDefaultContent(String defaultContent) {
        this.defaultContent = defaultContent;
    }
}
