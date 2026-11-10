package com.iwat.scientific;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** A Play purchase alone never unlocks cloud access: each request is verified by the server. */
final class AiPlus {
    interface Callback { void result(String message); }
    private static final String PRODUCT = "iwat_ai_plus";
    private final Activity activity;
    private final BillingClient billing;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private ProductDetails details;
    private String token;
    private String price;
    private Callback changes;

    AiPlus(Activity activity) {
        this.activity=activity;
        billing=BillingClient.newBuilder(activity).setListener((result,purchases)->{
            if(result.getResponseCode()==BillingClient.BillingResponseCode.OK && purchases!=null) process(purchases);
            else if(result.getResponseCode()!=BillingClient.BillingResponseCode.USER_CANCELED) notifyChange("Google Play purchase unavailable");
        }).enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection().build();
        billing.startConnection(new BillingClientStateListener() {
            @Override public void onBillingSetupFinished(BillingResult result) {
                if(result.getResponseCode()==BillingClient.BillingResponseCode.OK) { refresh(); product(); }
                else notifyChange("Google Play billing unavailable");
            }
            @Override public void onBillingServiceDisconnected() { notifyChange("Google Play disconnected; retry restore"); }
        });
    }
    void setChanges(Callback callback) { changes=callback; }
    String price() { return price; }
    String token() { return token; }
    void refresh() {
        if(!billing.isReady()) { notifyChange("Connect to Google Play to restore purchases"); return; }
        billing.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),(result,purchases)->{
            if(result.getResponseCode()==BillingClient.BillingResponseCode.OK) {
                token=null; process(purchases);
                if(token==null) notifyChange("No active Play purchase on this account");
            } else notifyChange("Unable to restore Play purchases");
        });
    }
    private void process(List<com.android.billingclient.api.Purchase> purchases) {
        for(com.android.billingclient.api.Purchase purchase:purchases) {
            if(purchase.getProducts().contains(PRODUCT) && purchase.getPurchaseState()==com.android.billingclient.api.Purchase.PurchaseState.PURCHASED) {
                token=purchase.getPurchaseToken(); notifyChange("Play purchase found; checking with server");
                status(response->notifyChange(response)); return;
            }
        }
        notifyChange("Purchase pending or unavailable");
    }
    private void product() {
        QueryProductDetailsParams.Product p=QueryProductDetailsParams.Product.newBuilder().setProductId(PRODUCT).setProductType(BillingClient.ProductType.SUBS).build();
        billing.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(Collections.singletonList(p)).build(),(result,products)->{
            if(result.getResponseCode()==BillingClient.BillingResponseCode.OK && !products.getProductDetailsList().isEmpty()) {
                details=products.getProductDetailsList().get(0);
                List<ProductDetails.SubscriptionOfferDetails> offers=details.getSubscriptionOfferDetails();
                if(offers!=null && !offers.isEmpty() && !offers.get(0).getPricingPhases().getPricingPhaseList().isEmpty())
                    price=offers.get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                notifyChange("Play subscription available"+(price==null?"":"; offer starts at "+price+". Confirm recurring price in checkout."));
            } else notifyChange("AI Plus is not available in this Play account");
        });
    }
    void buy() {
        if(BuildConfig.AI_SERVER_URL.isEmpty()) { notifyChange("AI service is not configured; purchases are disabled"); return; }
        if(!billing.isReady() || details==null || details.getSubscriptionOfferDetails()==null || details.getSubscriptionOfferDetails().isEmpty()) {
            notifyChange("Subscription not available; check Google Play"); return;
        }
        BillingFlowParams.ProductDetailsParams entry=BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details)
            .setOfferToken(details.getSubscriptionOfferDetails().get(0).getOfferToken()).build();
        BillingResult result=billing.launchBillingFlow(activity,BillingFlowParams.newBuilder().setProductDetailsParamsList(Collections.singletonList(entry)).build());
        if(result.getResponseCode()!=BillingClient.BillingResponseCode.OK) notifyChange("Could not open Play checkout");
    }
    void status(Callback done) {
        if(token==null || BuildConfig.AI_SERVER_URL.isEmpty()) { done.result("Restore a subscription to check access"); return; }
        JSONObject request=new JSONObject(); try { request.put("purchaseToken",token); } catch(Exception ignored) { }
        network("entitlement",request,done);
    }
    void solve(Uri photo,Callback done) {
        if(token==null || BuildConfig.AI_SERVER_URL.isEmpty()) { done.result("AI Plus requires a verified subscription and configured service"); return; }
        String purchaseToken=token;
        worker.execute(()->{
            try {
                BitmapFactory.Options options=new BitmapFactory.Options(); options.inJustDecodeBounds=true;
                try(var in=activity.getContentResolver().openInputStream(photo)) { BitmapFactory.decodeStream(in,null,options); }
                options.inJustDecodeBounds=false;
                options.inSampleSize=Math.max(1,(int)Math.ceil(Math.max(options.outWidth,options.outHeight)/1400.0));
                Bitmap image;
                try(var in=activity.getContentResolver().openInputStream(photo)) { image=BitmapFactory.decodeStream(in,null,options); }
                if(image==null) throw new IllegalArgumentException("Could not decode camera image");
                ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                for(int quality=80;quality>=35;quality-=15) { bytes.reset(); image.compress(Bitmap.CompressFormat.JPEG,quality,bytes); if(bytes.size()<=1_400_000) break; }
                image.recycle();
                if(bytes.size()>1_400_000) throw new IllegalArgumentException("Photo too large; take a simpler picture");
                JSONObject request=new JSONObject(); request.put("purchaseToken",purchaseToken);
                request.put("requestId",UUID.randomUUID().toString());
                request.put("imageBase64",Base64.encodeToString(bytes.toByteArray(),Base64.NO_WRAP));
                String response=post("solve-photo",request);
                JSONObject result=new JSONObject(response);
                activity.runOnUiThread(()->done.result("Expression: "+result.optString("expression")+"\n\nAnswer: "+result.optString("answer")
                    +"\n\nSteps: "+result.optString("explanation")+"\n\nRemaining this month: "+result.optInt("remaining")));
            } catch(Exception e) { activity.runOnUiThread(()->done.result("AI solve failed: "+e.getMessage())); }
        });
    }
    private void network(String endpoint,JSONObject payload,Callback done) {
        worker.execute(()->{ try { String response=post(endpoint,payload); activity.runOnUiThread(()->done.result(response)); }
            catch(Exception e) { activity.runOnUiThread(()->done.result("Could not verify subscription: "+e.getMessage())); } });
    }
    private String post(String endpoint,JSONObject payload) throws Exception {
        HttpURLConnection connection=(HttpURLConnection)new URL(BuildConfig.AI_SERVER_URL.replaceAll("/+$", "")+"/v1/"+endpoint).openConnection();
        connection.setRequestMethod("POST"); connection.setConnectTimeout(12000); connection.setReadTimeout(55000);
        connection.setDoOutput(true); connection.setRequestProperty("Content-Type","application/json");
        try {
            try(var out=connection.getOutputStream()) { out.write(payload.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
            boolean ok=connection.getResponseCode()>=200 && connection.getResponseCode()<300;
            try(var in=ok?connection.getInputStream():connection.getErrorStream()) {
                if(in==null) throw new IllegalStateException("Server unavailable");
                ByteArrayOutputStream buffer=new ByteArrayOutputStream(); byte[] chunk=new byte[4096]; int read;
                while((read=in.read(chunk))!=-1) { if(buffer.size()+read>16_384) throw new IllegalStateException("Server response too large"); buffer.write(chunk,0,read); }
                String response=new String(buffer.toByteArray(),java.nio.charset.StandardCharsets.UTF_8);
                if(!ok) throw new IllegalStateException(new JSONObject(response).optString("error","Service unavailable"));
                return response;
            }
        } finally { connection.disconnect(); }
    }
    private void notifyChange(String value) { if(changes!=null) activity.runOnUiThread(()->changes.result(value)); }
    void close() { billing.endConnection(); worker.shutdownNow(); }
    static Intent manage() { return new Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/account/subscriptions?package=com.iwat.scientific&sku="+PRODUCT)); }
}
