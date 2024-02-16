package com.appdev.alarmapp

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.appdev.alarmapp.ModelClasses.ItemDs
import com.appdev.alarmapp.ui.inappbuyScreen.Security
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class checkOutViewModel @Inject constructor(context: Context) : ViewModel() {


    private val _billingUiState: MutableStateFlow<BillingResultState> =
        MutableStateFlow(BillingResultState.NotStarted)
    val billingUiState: StateFlow<BillingResultState> = _billingUiState.asStateFlow()

    private val _itemListFlow: MutableStateFlow<ArrayList<ItemDs>> =
        MutableStateFlow(ArrayList())

    // Public StateFlow for external access
    val itemListFlow = _itemListFlow.asStateFlow()

    // Example function to update the itemListFlow
    fun updateItemList(newList: ArrayList<ItemDs>) {
        val mergedList = (_itemListFlow.value + newList).distinctBy { it.planIndex }
        _itemListFlow.value = ArrayList(mergedList)
    }

    private var billingClient: BillingClient? = null
    var productId = "general"
    var dur: String? = null
    var planeIdx = 0

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        handleBillingResult(billingResult, purchases)
    }

    private fun handleBillingResult(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                for (purchase in purchases.orEmpty()) {
                    // Handle the purchase
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _billingUiState.update { BillingResultState.AlreadySubscribed }
            }
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> {
                _billingUiState.update { BillingResultState.FeatureNotSupported() }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingUiState.update { BillingResultState.UserCanceled }
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                _billingUiState.update { BillingResultState.BillingUnavailable }
            }
            BillingClient.BillingResponseCode.NETWORK_ERROR -> {
                _billingUiState.update { BillingResultState.NetworkError }
            }
            else -> {
                _billingUiState.update { BillingResultState.Error(billingResult.debugMessage) }
            }
        }
    }

    var acknowledgePurchaseResponseListener = AcknowledgePurchaseResponseListener { billingResult ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _billingUiState.update { BillingResultState.Success }
        }
    }

    init {
        viewModelScope.launch {
            billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()
            show_list()
        }
    }

    fun handlePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val listener = ConsumeResponseListener { billingResult, s ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

            }
        }
        billingClient!!.consumeAsync(consumeParams, listener)
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
                _billingUiState.update { BillingResultState.InvalidPurchase }
                return
            }
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient!!.acknowledgePurchase(
                    acknowledgePurchaseParams, acknowledgePurchaseResponseListener
                )
                _billingUiState.update { BillingResultState.Success }
            } else {
                _billingUiState.update { BillingResultState.AlreadySubscribed }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _billingUiState.update { BillingResultState.Pending }
        } else if (purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            _billingUiState.update { BillingResultState.UnspecifiedState}
        }
    }

    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            val security = Security()
            val base64Key =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgC7PfTZ23vW/lbxDRua38cXk77215qui7uDfvFsSFAGHh7p0Rb5vzzsIGplZVBdHgz2vumm3MsvqYsdhslPiQAO7x6t/K7Ew/VO7gBx+8veLh/Ef3Js+Pei724xCIu4Lld9dR9xQ+Ah8CTaGfLd/qrc/D25cD5Gy8LXh9Q1DlvNjgsS4oGRokrP1w1rk16M8EuNVv5QUAHLRD4YiH4i5noDzbHr6VOK7K2QWl2rZx7C6HsQSo/8tiBA46ogEEMO4gRNb+WOyGr7molBvht65isRDY3jscpqIRsatSfwOcuyOAI5jSogkKG55HBEgjgnIk96FKjppafAX1jAY8rE0CwIDAQAB"
            security.verifyPurchase(base64Key, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }


    fun show_list() {
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val executorService = Executors.newSingleThreadExecutor()
                executorService.execute {
                    val queryProductDetailsParams =
                        QueryProductDetailsParams.newBuilder().setProductList(
                            ImmutableList.of(
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(productId)
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build()
                            )
                        )
                            .build()
                    billingClient!!.queryProductDetailsAsync(
                        queryProductDetailsParams
                    ) { billingResult1: BillingResult?, productDetailsList: List<ProductDetails> ->
                        Log.d("CHKMR","${productDetailsList.size}")

                        for(productDetails in productDetailsList){
                            if(productDetails.subscriptionOfferDetails != null){
                                for (i in 0 until productDetails.subscriptionOfferDetails!!.size) {
                                    var subsName:String = productDetails.name
                                    var index:Int = i
                                    var phases =""
                                    var formattedPrice: String =productDetails.subscriptionOfferDetails?.get(i)
                                        ?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice.toString()
                                    var billingPeriod: String =productDetails.subscriptionOfferDetails?.get(i)
                                        ?.pricingPhases?.pricingPhaseList?.get(0)?.billingPeriod.toString()
                                    var recurrenceMode:String =productDetails.subscriptionOfferDetails?.get(i)
                                        ?.pricingPhases?.pricingPhaseList?.get(0)?.recurrenceMode.toString()
                                    if(recurrenceMode == "2"){
                                        when(billingPeriod){
                                            "P1M"-> billingPeriod =" For 1 Month"
                                            "P6M"-> billingPeriod =" For 6 Month"
                                            "P1Y"-> billingPeriod =" For 1 Year"
                                            "P1W"-> billingPeriod =" For 1 Week"
                                            "P3W"-> billingPeriod =" For 3 Week"
                                        }
                                    }else{
                                        when(billingPeriod){
                                            "P1M"-> billingPeriod ="/Month"
                                            "P6M"-> billingPeriod ="/Every 6 Month"
                                            "P1Y"-> billingPeriod ="/Year"
                                            "P1W"-> billingPeriod ="/Week"
                                            "P3W"-> billingPeriod ="/Every 3 Week"
                                        }
                                    }
                                    phases ="$formattedPrice$billingPeriod"
                                    for (j in 0 until (productDetails.subscriptionOfferDetails!![i]?.pricingPhases?.pricingPhaseList?.size!!)) {
                                        if(j>0){
                                            var period: String = productDetails.subscriptionOfferDetails?.get(i)?.pricingPhases
                                                ?.pricingPhaseList?.get(j)?.billingPeriod.toString()
                                            var price: String = productDetails.subscriptionOfferDetails?.get(i)?.pricingPhases
                                                ?.pricingPhaseList?.get(j)?.formattedPrice.toString()
                                            when(period){
                                                "P1M"-> period ="/Month"
                                                "P6M"-> period ="/Every 6 Month"
                                                "P1Y"-> period ="/Year"
                                                "P1W"-> period ="/Week"
                                                "P3W"-> period ="Every /3 Week"
                                            }
                                            subsName +="\n"+productDetails.subscriptionOfferDetails?.get(i)?.offerId.toString()
                                            phases += "\n$price$period"
                                        }
                                    }
                                    val tmpItm = ItemDs(subsName,phases,index)
                                    Log.d("CHKMR","${tmpItm}")
                                    updateItemList(arrayListOf(tmpItm))
                                }
                            }
                        }
//                        for (productDetails in productDetailsList) {
//
//                            for (i in 0..productDetails.subscriptionOfferDetails!!.size) {
//                                Log.d("CHKMR","${productDetails.subscriptionOfferDetails!!.size}")
//                                var subsName: String? = null
//                                val status: String? = null
//                                if (i == 0) {
//                                    subsName = productDetails.name
//                                }
//                                var phases: String
//                                val formattedPrice =
//                                    productDetails.subscriptionOfferDetails!![i].pricingPhases
//                                        .pricingPhaseList[0].formattedPrice
//                                val billingPeriod =
//                                    productDetails.subscriptionOfferDetails!![i].pricingPhases
//                                        .pricingPhaseList[0].billingPeriod
//                                val recurrenceMode =
//                                    productDetails.subscriptionOfferDetails!![i]
//                                        .pricingPhases
//                                        .pricingPhaseList[0].recurrenceMode
//                                var bp: String = billingPeriod
//                                var n: String = billingPeriod.substring(1, 2)
//                                var duration: String = billingPeriod.substring(2, 3)
//                                val nPhases = productDetails.subscriptionOfferDetails!!
//                                    .get(i).pricingPhases.pricingPhaseList.size
//                                if (recurrenceMode == 2) {
//                                    when (duration) {
//                                        "M" -> {
//                                            dur = " For $n Month"
//                                        }
//                                        "Y" -> {
//                                            dur = " For $n Year"
//                                        }
//                                        "W" -> {
//                                            dur = " For $n Week"
//                                        }
//                                        "D" -> {
//                                            dur = " For $n Days"
//                                        }
//                                    }
//                                } else {
//                                    when (bp) {
//                                        "P1M" -> {
//                                            dur = "/Monthly"
//                                        }
//                                        "P6M" -> {
//                                            dur = "/Every 6 Month"
//                                        }
//                                        "P1Y" -> {
//                                            dur = "/Yearly"
//                                        }
//                                        "P1W" -> {
//                                            dur = "/Weekly"
//                                        }
//                                        "P3W" -> {
//                                            dur = "Every /3 Week"
//                                        }
//                                    }
//                                }
//                                phases = "$formattedPrice $dur"
//                                //
//                                for (j in 0 until nPhases) {
//                                    if (j > 0) {
//                                        val price =
//                                            productDetails.subscriptionOfferDetails!![i].pricingPhases
//                                                .pricingPhaseList[j].formattedPrice
//                                        val period =
//                                            productDetails.subscriptionOfferDetails!![i].pricingPhases
//                                                .pricingPhaseList[j].billingPeriod
//                                        dur = when (period) {
//                                            "P1M" -> {
//                                                "/Monthly"
//                                            }
//                                            "P6M" -> {
//                                                "/Every 6 Month"
//                                            }
//                                            "P1Y" -> {
//                                                "/Yearly"
//                                            }
//                                            "P1W" -> {
//                                                "/Weekly"
//                                            }
//                                            "P3W" -> {
//                                                "Every /3 Week"
//                                            }
//                                            else -> {
//                                                ""
//                                            }
//                                        }
//                                        phases += """$price$dur""".trimIndent()
//                                        subsName =
//                                            productDetails.subscriptionOfferDetails!![i]
//                                                .offerId
//                                    }
//                                }
//                                //
//                                subsName?.let {
//                                    Log.d("CHKMR","${ItemDs(it, phases, it, i)}")
//                                    updateItemList(arrayListOf(ItemDs(it, phases, it, i)))
//                                }
//                            }
                        }
                    }
                }
        })
    }

    fun subscribeProduct(context: Context) {
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val queryProductDetailsParams =
                    QueryProductDetailsParams.newBuilder().setProductList(
                        ImmutableList.of(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(productId)
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build()
                        )
                    )
                        .build()
                billingClient!!.queryProductDetailsAsync(
                    queryProductDetailsParams
                ) { billingResult1: BillingResult?, productDetailsList: List<ProductDetails> ->
                    for (productDetails in productDetailsList) {
                        val offerToken = productDetails.subscriptionOfferDetails
                            ?.get(planeIdx)?.offerToken
                        val productDetailsParamsList =
                            ImmutableList.of(
                                offerToken?.let {
                                    ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .setOfferToken(it)
                                        .build()
                                }
                            )
                        val billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build()
                        billingClient!!.launchBillingFlow(context as Activity, billingFlowParams)
                    }
                }
            }
        })
    }

}


sealed interface BillingResultState {

    object NotStarted : BillingResultState
    object Success : BillingResultState
    object AlreadySubscribed : BillingResultState
    data class FeatureNotSupported(val msg: String? = null) : BillingResultState
    object UserCanceled : BillingResultState
    object BillingUnavailable : BillingResultState
    object NetworkError : BillingResultState
    data class Error(val debugMessage: String) : BillingResultState
    object InvalidPurchase : BillingResultState
    object Pending : BillingResultState
    object UnspecifiedState : BillingResultState
}
