package org.epiclouds.ebayItemSpider.handler;

import io.netty.handler.codec.http.FullHttpResponse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.epiclouds.ebayItemSpider.bean.EbayItemBean;
import org.epiclouds.ebayItemSpider.bean.SoldNumberBean;
import org.epiclouds.handlers.AbstractNettyCrawlerHandler;
import org.epiclouds.handlers.CrawlerClient;
import org.epiclouds.spiders.dbstorage.condition.impl.EqualCondition;
import org.epiclouds.spiders.dbstorage.manager.abstracts.StorageBean;
import org.epiclouds.spiders.dbstorage.manager.abstracts.StorageBean.Builder;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ItemHandler extends AbstractNettyCrawlerHandler{
	
	private EbayItemBean ebayItem;
	private int error_302_try_number=0;
	private int handleCount=0;


	public static HashMap<String,String> hs;
	static{
		hs=new HashMap<String,String>();
		//headers
	    hs.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

	    hs.put("Origin","http://www.ebay.co.uk/");
	}
	public EbayItemBean getEbayItem() {
		return ebayItem;
	}

	public void setEbayItem(EbayItemBean ebayItem) {
		this.ebayItem = ebayItem;
	}
	public ItemHandler(String url, String host,
			AbstractSpiderObject spider,String itemId,String categoryId) {
		super(host,url,spider);
		// TODO Auto-generated constructor stub
		ebayItem=new EbayItemBean();
		ebayItem.setItemId(itemId);
		ebayItem.setCategoryId(categoryId);
		this.getHeaders().putAll(hs);
	}
	
	public void onError(Object response) {
		// TODO Auto-generated method stub
		FullHttpResponse cres=(FullHttpResponse)response;
		if(cres.status().code()==302||cres.status().code()==301){
			if(error_302_try_number>2||(cres.headers().get("Location")+"").
					contains("Error")||(cres.headers().get("Location")+"").
					contains("error")
					){
				stop();
				System.out.println("Category"+ebayItem.getCategoryId()+": "+ebayItem.getItemId()+" is on error and it has been stopped.");
				return;
			}
			CrawlerClient.mainlogger.error("error:"+cres.status()+":"+this.getUrl());
			error_302_try_number++;
			this.setUrl(cres.headers().get("Location")+"");
			requestSelf();
		}else{
			super.onError(response);
		}
	}

	@Override
	public void handle(String content) throws Exception {
		
		
		handleCount ++;
		// TODO Auto-generated method stub
		if(content.equals("")){
			throw new Exception("return content is null!");
		}
		Document doc=Jsoup.parse(content);
		if(doc==null){
			throw new Exception("return content is null!");
		}
		
		/************get seller name*************/
		Elements els;
		String seller_name="";
		els=doc.select("a[href][aria-label]");
		for(int i=0;i<els.size();i++){
			seller_name=els.get(i).attr("href");
			if(seller_name.startsWith("http://www.ebay.co.uk/usr/")){
				int start="http://www.ebay.co.uk/usr/".length();
				seller_name=seller_name.substring(start);
				int end;
				for(int j=0;j<seller_name.length();j++){
					if(seller_name.charAt(j)=='?'){
						end=j;
						seller_name=seller_name.substring(0, end);
						break;
					}
				}
				break;
			}
		}
		if(seller_name.equals("")){
			throw new Exception("can't get the seller name"+", itemId: "+ebayItem.getItemId()+", Exception count: "+ handleCount);
		}
		//System.out.println(seller_name);
//		if(doc.select("span.mbg-nw").size()>0){
//			seller_name=doc.select("span.mbg-nw").first().ownText();
//			ebayItem.setSellerName(seller_name);
//		}
		
		/************get item name*************/
		String item_name=doc.select("h1#itemTitle").first().ownText();
		if(item_name.equals(""))
			throw new Exception("can't get item_name"+", itemId: "+ebayItem.getItemId()+", Exception count: "+ handleCount);
		
		String shipping_type=doc.select("span[id=fShippingSvc]").text();
		String shipping_price=doc.select("span[id=fshippingCost]").text();
		if(doc.select("#mapPrcDChkUrl")!=null&&doc.select("#mapPrcDChkUrl").size()>0){
			stop();
			return;
		}
		{
			Element el=doc.select("#shSummary").first();
			if(el!=null&&el.text().contains("In-store pickup only")){
				stop();
				return;
			}
		}
		/************get price list*************/
		Elements price_list=doc.select("span[class=notranslate]");
		String item_price="";
		
		if(price_list.size()==1){
			item_price=price_list.first().text();
		}
		else{
			item_price=price_list.select("#mm-saleDscPrc").text();
			if(item_price.equals("")){
				item_price=price_list.select("#prcIsum").text();
			}
		}
		Element es;
		if(item_price.equals("")){
			es=doc.select("span#convbidPrice").first();
			if(es!=null){
				item_price=es.ownText();
			}
		}
		
		if(item_price.equals("")){
			if(doc.select("#finalPrc").size()>0){
				item_price=doc.select("#finalPrc").first().text();
			}
		}
		if(item_price.equals("")){
			if(doc.select("meta[name=twitter:text:price]").size()>0){
				item_price=doc.select("meta[name=twitter:text:price]").first().attr("content");
			}
		}
		if(item_price.equals("")){
			item_price=doc.select("div.pd-dt.pd-ip-pd").text();
			//System.out.println(item_price);
			if(item_price.equals("")||item_price.split(" ").length<2){
				this.ebayItem.setSoldNumbers(null);
				stop();
				return;
			}
			item_price=item_price.split(" ")[3]+item_price.split(" ")[4];
		}
		//System.out.println(item_price);
		if(item_price.equals("")){
			throw new Exception("can't get the item_price"+", itemId: "+ebayItem.getItemId()+", Exception count: "+ handleCount);
		}
			
		/************get sold number*************/
		String sold_number=doc.select("[href^=http://offer.ebay.co.uk/ws/eBayISAPI.dll?ViewBidsLogin]").text();	
		sold_number = sold_number.replace(" sold", "");
		
		/************get shipTo location && item location*************/
		String item_location="";
		String shipTo_location="";
		els=doc.select("div.iti-eu-bld-gry");
		if(els.size()>0)
			item_location=els.get(0).ownText();
		if(els.size()>1){
			shipTo_location=els.get(1).text();
			String suf=els.get(1).select("a[href=#shpCntId]").text();
			if(!"".equals(suf)){
				shipTo_location=shipTo_location.substring(0, shipTo_location.length()-suf.length());
			}
		}
		if(shipTo_location.equals(""))
			throw new Exception("can't get the shipTo_location"+", itemId: "+ebayItem.getItemId()+", Exception count: "+ handleCount);
		int strend = shipTo_location.length() - 1;
		while(strend>= 0 && (shipTo_location.charAt(strend)==' ' || shipTo_location.charAt(strend)==160 || shipTo_location.charAt(strend)==124)){
			strend --;
		}
		shipTo_location = shipTo_location.substring(0, strend+1);
		
		if(item_location.equals("")){
			Elements locations=doc.select("div.u-flL.lable:contains(Item location:)");
			if(locations.size()>0){
				item_location=locations.get(0).nextElementSibling().text();
			}
		}
		
		if(item_location.equals(""))
			throw new Exception("can't get the item_location"+", itemId: "+ebayItem.getItemId()+", Exception count: "+ handleCount);
		
		
		/************get picture url list*************/
		List<String> pic_url_list=new ArrayList<String>();
		Elements pic_url_els=doc.select("div#vi_main_img_fs").select("table.img");
		String pic_url="";

		if(pic_url_els.size()==0){
			Elements pic_url_big=doc.select("img#icImg");
			if(pic_url_big.size()>0){
				pic_url_list.add(picUrlAmplifier(pic_url_big.get(0).attr("src")));
				//System.out.println("the big picture Url is"+pic_url_big.get(0).attr("src"));
			}
			
		}
		else{
		//System.out.println(els.size());
			for(int i=0;i<pic_url_els.size();i++){
				try{

					pic_url=pic_url_els.get(i).select("img").attr("src");
					if(pic_url=="")
						throw new Exception("the pic ur is null");
					if(pic_url.startsWith("http://i.ebayimg.com"))
						pic_url_list.add(picUrlAmplifier(pic_url));
					else
						pic_url_list.add(pic_url);
				}catch(Exception e){
					System.out.println("exist null pic url in item "+ebayItem.getItemViewUrl());
				}
			}
		}
		if(pic_url_list.size()==0)
		{
			throw new Exception("can't get picture url"+", itemId: "+ebayItem.getItemId()+", Exception count: "+ handleCount);
		}
		
		/************get feedback*************/
		String sellerPositiveFeedback="";
		String sellerFeedbackScore="";
		
		//feebbackscore
		Elements eles=doc.select("span.mbg-l");
		String text=eles.text();
		int start=0;
		int end=0;
		while(text != null && start<text.length() && (text.charAt(start)<'0' || text.charAt(start)>'9')){
			start++;
		}
		end=start;
		while(text != null && end<text.length() && text.charAt(end)>='0' && text.charAt(end)<='9'){
			end++;
		}
		sellerFeedbackScore=text.substring(start, end);
			
		//sellerPositiveFeedback
		eles=doc.select("div#si-fb");
		if(eles.first()!=null){
			sellerPositiveFeedback=eles.first().ownText().split(String.valueOf((char)160))[0];
		}
		
	/******************Get contact information of seller******************/
		
		els=doc.select("div[class=\"bsi-c1\"]").select("div");
		String postAddress = "";
		for(int i=1;i<els.size();i++){
			postAddress += reverseString(els.get(i).text()) + ", ";
		}
		if (postAddress.length() > 1)
			postAddress = postAddress.substring(0, postAddress.length() - 2);
		
		els=doc.select("div[class=\"bsi-c2\"]").select("div");
		String sellerPhone = "";
		String sellerEmail = "";
		String sellerFax = "";
	//	System.out.println("HTML: "+els.html());
		for(int i=1;i<els.size();i++) {
			Elements els2 = els.get(i).select("span");
			if(els2.size() >= 2 && reverseString(els2.get(0).text()).equalsIgnoreCase("Email:")) {
				sellerEmail = reverseString(els2.get(1).text());
			}
			if(els2.size() >= 2 && reverseString(els2.get(0).text()).equalsIgnoreCase("Phone:")) {
				sellerPhone = reverseString(els2.get(1).text());
			}
			if(els2.size() >= 2 && reverseString(els2.get(0).text()).equalsIgnoreCase("Fax:")) {
				sellerFax = reverseString(els2.get(1).text());
			}
		}
		
/*		System.out.println(ebayItem.getCategoryId()+": "+ebayItem.getItemId()+": postAddress: " + postAddress);
		System.out.println(ebayItem.getCategoryId()+": "+ebayItem.getItemId()+": sellerPhone: " + sellerPhone);
		System.out.println(ebayItem.getCategoryId()+": "+ebayItem.getItemId()+": sellerEmail: " + sellerEmail);
		System.out.println(ebayItem.getCategoryId()+": "+ebayItem.getItemId()+": sellerFax: " + sellerFax);*/
		//System.out.println("sellerFeedbackScore "+sellerFeedbackScore);
		//System.out.println("sellerPositiveFeedback "+ sellerPositiveFeedback);
//		if(sellerFeedbackScore.equals("") || sellerPositiveFeedback.equals("")){
//			throw new Exception("feebakc is null");
//		}
		//System.out.println(els.html());
		ebayItem.setSellerName(seller_name);
		ebayItem.setItemLocation(item_location);
		ebayItem.setItemName(item_name);
		ebayItem.setItemPrice(item_price);
		ebayItem.setShippingPrice(shipping_price);
		ebayItem.setShippingType(shipping_type);
		ebayItem.setShipToLocation(shipTo_location);
		ebayItem.setItemPicUrls(pic_url_list);
		ebayItem.setSellerPositiveFeedback(sellerPositiveFeedback);
		ebayItem.setSellerFeedbackScore(sellerFeedbackScore);
		ebayItem.setPostAddress(postAddress);
		ebayItem.setSellerEmail(sellerEmail);
		ebayItem.setSellerPhone(sellerPhone);
		ebayItem.setSellerFax(sellerFax);

		/******************Get sold number******************/
		if(ebayItem.getSoldNumbers()!=null&&ebayItem.getSoldNumbers().size()>0){
			SoldNumberBean bean=ebayItem.getSoldNumbers().get
					(ebayItem.getSoldNumbers().size()-1);
	        DateTimeFormatter format = DateTimeFormat .forPattern("yyyy-MM-dd HH:mm:ss");  
	        //时间解析    
			if(DateTime.parse(bean.getTime(), format).toString("yyyy-MM-dd").equals((new DateTime().toString("yyyy-MM-dd")))){
				ebayItem.getSoldNumbers().remove(ebayItem.getSoldNumbers().size()-1);
			}
		}
		
		ebayItem.getSoldNumbers().add(new SoldNumberBean(sold_number));
		stop();
	}
	
	public String picUrlAmplifier(String url)throws Exception{
		if("".equals(url)){
			throw new Exception("the pic ur is null");
		}
		String place_str="12";
		int suf=0;
		int pre=0;
		int l=url.length();
		for(int i=l-1;i>=0;i--){
			if(url.charAt(i)=='.')suf=i;
			if(url.charAt(i)=='_'){
				pre=i;
				break;
			}
		}
		return url.substring(0, pre+1)+place_str+url.substring(suf);

	}

	@Override
	protected void onBefore() {
		DBObject cond=new BasicDBObject();
		cond.put("categoryId", ebayItem.getCategoryId());
		cond.put("itemId", ebayItem.getItemId());
		StorageBean sb=StorageBean.Builder.newBuilder(StorageBean.OperationType.FIND
				,this.getSpider().getConfigManager().getValue("spider_database", String.class),
				this.getSpider().getConfigManager().getValue("spider_ebayitem_table", String.class))
				.addConditon(new EqualCondition<String>("categoryId", ebayItem.getCategoryId())).
				addConditon(new EqualCondition<String>("itemId", ebayItem.getItemId())).
				build();
		List<EbayItemBean> re=new LinkedList<EbayItemBean>();
		try {
			re = this.getSpider().getDbmanager().find(sb, EbayItemBean.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.getEbayItem().getSoldNumbers().clear();
		if(re.size()>0){
			this.setEbayItem(re.get(0));
		}
	}

	@Override
	protected void onNormalFinished() {
		// TODO Auto-generated method stub
		this.getSpider().finish();
	}
	

	@Override
	protected void onDataFinished() {
		// TODO Auto-generated method stub
		try {
			if (ebayItem.getItemName() != null && ebayItem.getItemPrice() != null) {
			System.out.println(ebayItem.getItemId()+" storage done");
			Builder builder=StorageBean.Builder.newBuilder(StorageBean.OperationType.UPORINSERT
					,this.getSpider().getConfigManager().getValue("spider_database", String.class),
					this.getSpider().getConfigManager().getValue("spider_ebayitem_table", String.class))
					.addConditon(new EqualCondition<String>("categoryId", ebayItem.getCategoryId())).
					addConditon(new EqualCondition<String>("itemId", ebayItem.getItemId()));
			this.getSpider().getDbmanager().storage(builder, ebayItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String reverseString (String abc) {
		String cba = "";
		for (int i=abc.length()-1; i>=0; i--) {
			cba += abc.charAt(i);
		}
		return cba;
	}

}
