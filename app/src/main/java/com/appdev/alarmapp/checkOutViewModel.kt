package com.appdev.alarmapp

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class checkOutViewModel @Inject constructor(context: Context) : ViewModel() {

    private val _paymentUiState: MutableStateFlow<PaymentUiState> =
        MutableStateFlow(PaymentUiState.NotStarted)
    val paymentUiState: StateFlow<PaymentUiState> = _paymentUiState.asStateFlow()

    val paymentsClient: PaymentsClient = PaymentUtils.createPaymentsClient(context)

    init {
        viewModelScope.launch {
            val isReadyToPayRequest =
                IsReadyToPayRequest.fromJson(PaymentUtils.googlePayBaseConfiguration.toString())
            val task: Task<Boolean> = paymentsClient.isReadyToPay(isReadyToPayRequest)
            val isReadyToPay = task.await()
            if (isReadyToPay) {
                Log.d("CHKME", "$isReadyToPay in if")
                _paymentUiState.update { PaymentUiState.Available }
            } else {
                Log.d("CHKME", "$isReadyToPay")
                _paymentUiState.update { PaymentUiState.Error(CommonStatusCodes.INTERNAL_ERROR) }
            }
        }
    }

    fun setPaymentData(paymentData: PaymentData){
        val holderName=  paymentData?.let(::getUserData) ?: "Payment Failed"
        val newState = PaymentUiState.PaymentCompleted(PaymentResult(billingName = holderName))
        _paymentUiState.update { newState }

    }

    fun getPaymentData(): Task<PaymentData> {
        val Request = PaymentDataRequest.fromJson(PaymentUtils.paymentDataRequestJson.toString())
        return paymentsClient.loadPaymentData(Request)
    }

    fun getUserData(paymentData: PaymentData): String? {
        val infoPayment = paymentData.toJson()
        try {
            val pMethodData = JSONObject(infoPayment).getJSONObject("paymentMethodData")
            val BillngName =
                pMethodData.getJSONObject("info").getJSONObject("BillingAddress").getString("name")
            return BillngName
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

sealed interface PaymentUiState {
    object NotStarted : PaymentUiState
    object Available : PaymentUiState
    data class PaymentCompleted(val paymentData: PaymentResult) : PaymentUiState
    data class Error(val code: Int, val msg: String? = null) : PaymentUiState

}

data class PaymentResult(val billingName: String)