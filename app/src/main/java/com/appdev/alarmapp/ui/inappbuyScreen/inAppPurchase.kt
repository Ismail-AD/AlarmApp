package com.appdev.alarmapp.ui.inappbuyScreen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.BillingResultState
import com.appdev.alarmapp.ModelClasses.ItemDs
import com.appdev.alarmapp.R
import com.appdev.alarmapp.checkOutViewModel
import com.appdev.alarmapp.ui.theme.linear
import com.google.pay.button.PayButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun InAppPurchase(
    controller: NavHostController,
    checkOutViewModel: checkOutViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val scrollStateTwo = rememberScrollState()
    val scope = rememberCoroutineScope()


    val purchaseList = checkOutViewModel.itemListFlow.collectAsStateWithLifecycle()
    val billingState = checkOutViewModel.billingUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var loading by remember { mutableStateOf(purchaseList.value.isEmpty()) }
    var selectedOption by remember { mutableStateOf<ItemDs?>(null) }
    BackHandler {
        controller.popBackStack()
    }

    LaunchedEffect(key1 = purchaseList.value) {
        loading = purchaseList.value.isEmpty()

        if (selectedOption == null && purchaseList.value.isNotEmpty()) {
            selectedOption = purchaseList.value[0]
        }
    }


    LaunchedEffect(key1 = billingState.value) {
        when (billingState.value) {
            BillingResultState.AlreadySubscribed -> Toast.makeText(
                context,
                "Already Subscribed",
                Toast.LENGTH_SHORT
            ).show()

            BillingResultState.BillingUnavailable -> Toast.makeText(
                context,
                "Billing Unavailable",
                Toast.LENGTH_SHORT
            ).show()

            is BillingResultState.Error -> {
                val msg = (billingState.value as BillingResultState.Error).debugMessage
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }

            is BillingResultState.FeatureNotSupported -> {
                val msg = (billingState.value as BillingResultState.FeatureNotSupported).msg
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }

            BillingResultState.InvalidPurchase -> Toast.makeText(
                context,
                "Invalid Purchase ",
                Toast.LENGTH_SHORT
            ).show()

            BillingResultState.NetworkError -> Toast.makeText(
                context,
                "Network Error",
                Toast.LENGTH_SHORT
            ).show()

            BillingResultState.NotStarted -> Toast.makeText(
                context,
                "Collecting updated data",
                Toast.LENGTH_SHORT
            ).show()

            BillingResultState.Pending -> Toast.makeText(
                context,
                "Pending",
                Toast.LENGTH_SHORT
            ).show()

            BillingResultState.Success -> Toast.makeText(
                context,
                "Payment Succeed",
                Toast.LENGTH_SHORT
            ).show()

            BillingResultState.UnspecifiedState -> Toast.makeText(
                context,
                "Unspecified State",
                Toast.LENGTH_SHORT
            ).show()

            BillingResultState.UserCanceled -> Toast.makeText(
                context,
                "User Canceled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(modifier = Modifier.fillMaxWidth(), bottomBar = {}) { pd ->
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Dialog(onDismissRequest = { /*TODO*/ }) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(pd)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .background(Color(0xff1C1F26))
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                IconButton(onClick = {
                    controller.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIos,
                        contentDescription = "",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.wheat),
                        contentDescription = "",
                        modifier = Modifier.size(70.dp)
                    )
                }
                Text(
                    "#1 Ranked alarm app in 97 countries",
                    fontSize = 16.sp,
                    letterSpacing = 0.sp,
                    color = Color(0xffA6ACB5),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "It is not charged right now,",
                    color = Color.White,
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp), fontWeight = FontWeight.W500
                )
                Text(
                    text = "Free Trial for 7 Days",
                    color = Color.White,
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(), fontWeight = FontWeight.W500
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 15.dp, top = 40.dp)
                ) {
                    purchaseList.value.forEach { item ->
                        PurchaseChoice(
                            Modifier.weight(1f),
                            isSelected = item == selectedOption, item = item
                        ) {
                            scope.launch {
                                delay(1000)
                                checkOutViewModel.planeIdx = item.planIndex
                            }
                            selectedOption = item
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                }
                Spacer(modifier = Modifier.height(20.dp))
            }
            Column(
                modifier = Modifier
                    .background(Color(0xff24272E))
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                Text(
                    text = "Experience the life",
                    color = Color.White,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp), fontWeight = FontWeight.W500
                )
                Text(
                    text = "50 million users are enjoying",
                    color = Color.White,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 1.dp), fontWeight = FontWeight.W500
                )
                Text(
                    text = "for yourself",
                    color = Color.White,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 1.dp), fontWeight = FontWeight.W500
                )
                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollStateTwo)
                        .padding(20.dp)
                        .fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Testimonial(
                        text = "Finally found an alarm app that understands the struggle of waking up. The challenges feature is genius – it makes sure I'm fully awake before I dismiss the alarm.",
                        name = "Emily White"
                    )
                    Testimonial(
                        text = "Finally found an alarm app that understands the struggle of waking up. The challenges feature is genius – it makes sure I'm fully awake before I dismiss the alarm.",
                        name = "Emily White"
                    )
                    Testimonial(
                        text = "Finally found an alarm app that understands the struggle of waking up. The challenges feature is genius – it makes sure I'm fully awake before I dismiss the alarm.",
                        name = "Emily White"
                    )
                }
                Text(
                    "No commitment. Cancel anytime. If you do not cancel auto renewal until 24 hours before the current period ends, you'll be automatically charged via your Play Store account.",
                    fontSize = 14.sp,
                    letterSpacing = 0.sp,
                    color = Color(0xff93969F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp, horizontal = 18.dp)
                )
                Text(
                    "You can use all free features even if you do not subscribe after free trial.",
                    fontSize = 14.sp,
                    letterSpacing = 0.sp,
                    color = Color(0xff93969F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 18.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 58.dp)
                ) {
                    Text(
                        "Terms & Conditions / Privacy Policy",
                        fontSize = 15.sp,
                        letterSpacing = 0.sp,
                        color = Color(0xffA6ACB5),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Divider(
                        color = Color(0xffA6ACB5),
                        thickness = (1.1).dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 22.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .padding(horizontal = 20.dp),
                        shape = CircleShape,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(linear, CircleShape)
                                .clickable {
                                    selectedOption?.let {
                                        checkOutViewModel.subscribeProduct(context)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Start Premium",
                                fontSize = 18.sp,
                                letterSpacing = 0.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.W500
                            )
                        }
                    }
                }

//                Text(
//                    "7 days free, then Rs 11,600.00/year",
//                    fontSize = 14.sp,
//                    letterSpacing = 0.sp,
//                    color = Color(0xffA6ACB5),
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 14.dp)
//                )
            }
        }
    }
}

@Composable
fun Testimonial(text: String, name: String) {
    Column(
        modifier = Modifier
            .height(220.dp)
            .width(278.dp)
            .background(Color(0xff1C1F26), RoundedCornerShape(9.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "",
                tint = Color.Yellow,
                modifier = Modifier.size(15.dp)
            )
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "",
                tint = Color.Yellow,
                modifier = Modifier.size(15.dp)
            )
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "",
                tint = Color.Yellow,
                modifier = Modifier.size(15.dp)
            )
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "",
                tint = Color.Yellow,
                modifier = Modifier.size(15.dp)
            )
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "",
                tint = Color.Yellow,
                modifier = Modifier.size(15.dp)
            )
        }
        Text(
            name,
            fontSize = 15.sp,
            letterSpacing = 0.sp,
            color = Color(0xffA6ACB5),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp), fontWeight = FontWeight.W500
        )
        Text(
            text,
            fontSize = 14.sp,
            letterSpacing = 0.sp,
            color = Color(0xffA6ACB5),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp, start = 10.dp, end = 10.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseChoice(
    modifier: Modifier,
    isSelected: Boolean, item: ItemDs,
    onCLick: () -> Unit
) {
    Card(
        onClick = {
            onCLick()
        },
        modifier = modifier,
        shape = RoundedCornerShape(15.dp), // Adjust the corner radius as needed
        border = BorderStroke(
            width = if (isSelected) 2.dp else 0.dp,
            color = if (isSelected) Color.LightGray else Color.Transparent
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.Transparent else Color(0xff24272E)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 18.dp)
            ) {
                Text(
                    if(item.planIndex==0) "Yearly Plan" else "Basic Plan",
                    fontSize = 14.sp,
                    letterSpacing = 0.sp,
                    color = Color(0xffA6ACB5),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    RadioButton(selected = isSelected, onClick = { onCLick() },colors = RadioButtonDefaults.colors(
                        selectedColor = Color.White, // Change color for selected state
                        unselectedColor = Color.Gray // Change color for unselected state
                    ))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    item.planePrice,
                    fontSize = 14.sp,
                    letterSpacing = 0.sp,
                    color = Color(0xffA6ACB5),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

//                if (item.previousPrice > 0) {
//                    Spacer(modifier = Modifier.height(10.dp))
//                    Text(
//                        "Rs " + item.previousPrice.toString() + ".00/month",
//                        fontSize = 14.sp,
//                        letterSpacing = 0.sp,
//                        color = Color(0xffA6ACB5),
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                }
//                if (item.perYear <= 0) {
//                    Spacer(modifier = Modifier.height(10.dp))
//                }
//                if (item.perMonth > 0) {
//                    Text(
//                        "Rs " + item.perMonth.toString() + "/month",
//                        fontSize = if (item.previousPrice > 0) 14.sp else 19.sp,
//                        letterSpacing = 0.sp,
//                        color = if (item.previousPrice > 0) Color.White else Color(0xffA6ACB5),
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                    )
//                }
//                if (item.perYear > 0) {
//                    Text(
//                        "Rs " + item.perYear.toString() + ".00/year",
//                        fontSize = 17.sp,
//                        letterSpacing = 0.sp,
//                        color = Color.White,
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 10.dp)
//                    )
//                }
            }
        }
    }
}


//MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgC7PfTZ23vW/lbxDRua38cXk77215qui7uDfvFsSFAGHh7p0Rb5vzzsIGplZVBdHgz2vumm3MsvqYsdhslPiQAO7x6t/K7Ew/VO7gBx+8veLh/Ef3Js+Pei724xCIu4Lld9dR9xQ+Ah8CTaGfLd/qrc/D25cD5Gy8LXh9Q1DlvNjgsS4oGRokrP1w1rk16M8EuNVv5QUAHLRD4YiH4i5noDzbHr6VOK7K2QWl2rZx7C6HsQSo/8tiBA46ogEEMO4gRNb+WOyGr7molBvht65isRDY3jscpqIRsatSfwOcuyOAI5jSogkKG55HBEgjgnIk96FKjppafAX1jAY8rE0CwIDAQAB