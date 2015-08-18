package org.epiclouds.ebayItemSpider.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * @author ZhuYicong
 *
 */
public class EbayItemBean{
	private volatile String itemId;
	private volatile String itemName;
	private volatile String itemPrice;
	private volatile List<SoldNumberBean> soldNumbers=Collections.synchronizedList(new LinkedList<SoldNumberBean>());
	private volatile  String categoryId;
	private volatile  String itemViewUrl;
	private volatile  String shippingType;
	private volatile  String sellerName;
	private volatile  String shippingPrice;
	private volatile  String itemLocation;
	private volatile  String shipToLocation;
	private volatile  List<String> itemPicUrls=Collections.synchronizedList(new ArrayList<String>());
	private volatile  String sellerFeedbackScore;
	private volatile  String sellerPositiveFeedback;
	private volatile  String postAddress;
	private volatile  String sellerPhone;
	private volatile  String sellerEmail;
	private volatile String sellerFax;
	
	public String getSellerFeedbackScore() {
		return sellerFeedbackScore;
	}
	public void setSellerFeedbackScore(String sellerFeedbackScore) {
		this.sellerFeedbackScore = sellerFeedbackScore;
	}
	public String getSellerPositiveFeedback() {
		return sellerPositiveFeedback;
	}
	public void setSellerPositiveFeedback(String sellerPositiveFeedback) {
		this.sellerPositiveFeedback = sellerPositiveFeedback;
	}
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getItemPrice() {
		return itemPrice;
	}
	public void setItemPrice(String itemPrice) {
		this.itemPrice = itemPrice;
	}


	public String getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	public String getItemViewUrl() {
		return itemViewUrl;
	}
	public void setItemViewUrl(String itemViewUrl) {
		this.itemViewUrl = itemViewUrl;
	}
	public String getShippingType() {
		return shippingType;
	}
	public void setShippingType(String shippingType) {
		this.shippingType = shippingType;
	}
	public String getSellerName() {
		return sellerName;
	}
	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}
	public String getShippingPrice() {
		return shippingPrice;
	}
	public void setShippingPrice(String shippingPrice) {
		this.shippingPrice = shippingPrice;
	}
	public String getItemLocation() {
		return itemLocation;
	}
	public void setItemLocation(String itemLocation) {
		this.itemLocation = itemLocation;
	}
	public String getShipToLocation() {
		return shipToLocation;
	}
	public void setShipToLocation(String shipToLocation) {
		this.shipToLocation = shipToLocation;
	}
	public List<String> getItemPicUrls() {
		return itemPicUrls;
	}
	public void setItemPicUrls(List<String> itemPicUrls) {
		this.itemPicUrls = itemPicUrls;
	}
	public List<SoldNumberBean> getSoldNumbers() {
		return soldNumbers;
	}
	public void setSoldNumbers(List<SoldNumberBean> soldNumbers) {
		this.soldNumbers = soldNumbers;
	}
	public String getPostAddress() {
		return postAddress;
	}
	public void setPostAddress(String postAddress) {
		this.postAddress = postAddress;
	}
	public String getSellerPhone() {
		return sellerPhone;
	}
	public void setSellerPhone(String sellerPhone) {
		this.sellerPhone = sellerPhone;
	}
	public String getSellerEmail() {
		return sellerEmail;
	}
	public void setSellerEmail(String sellerEmail) {
		this.sellerEmail = sellerEmail;
	}
	public String getSellerFax() {
		return sellerFax;
	}
	public void setSellerFax(String sellerFax) {
		this.sellerFax = sellerFax;
	}
	
}
