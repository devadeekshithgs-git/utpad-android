package com.example.gudgum_prod_flow.ui.screens.production

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gudgum_prod_flow.ui.components.BarcodeScannerButton
import com.example.gudgum_prod_flow.ui.navigation.AppRoute
import com.example.gudgum_prod_flow.ui.viewmodels.DispatchViewModel
import com.example.gudgum_prod_flow.ui.viewmodels.SubmitState
import kotlinx.coroutines.launch
import com.example.gudgum_prod_flow.ui.theme.UtpadPrimary
import com.example.gudgum_prod_flow.ui.theme.UtpadSuccess
import com.example.gudgum_prod_flow.ui.theme.UtpadError
import com.example.gudgum_prod_flow.ui.theme.UtpadOutline
import com.example.gudgum_prod_flow.ui.theme.UtpadTextPrimary
import com.example.gudgum_prod_flow.ui.theme.UtpadTextSecondary
import com.example.gudgum_prod_flow.ui.theme.UtpadBackground
import com.example.gudgum_prod_flow.ui.theme.UtpadSurface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispatchScreen(
    allowedRoutes: Set<String>,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    viewModel: DispatchViewModel = hiltViewModel(),
) {
    val currentStock by viewModel.currentStock.collectAsState()
    val batchCode by viewModel.batchCode.collectAsState()
    val qtyTakenFromPacking by viewModel.qtyTakenFromPacking.collectAsState()
    val qtyDispatched by viewModel.qtyDispatched.collectAsState()
    val remainingBalance by viewModel.remainingBalance.collectAsState()
    val isPending by viewModel.isPending.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val currentStep by viewModel.currentWizardStep.collectAsState()
    val destination by viewModel.destination.collectAsState()
    val vehicleNumber by viewModel.vehicleNumber.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(submitState) {
        when (val state = submitState) {
            is SubmitState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearSubmitState()
            }
            is SubmitState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearSubmitState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Dispatch Wizard", fontWeight = FontWeight.Bold, color = UtpadTextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = UtpadTextPrimary
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = UtpadPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = UtpadBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 132.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
            ) {
                OperationsModuleTabs(
                    currentRoute = AppRoute.Dispatch,
                    allowedRoutes = allowedRoutes,
                    onNavigateToRoute = onNavigateToRoute,
                )

                // Current Stock Card (always visible)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = UtpadPrimary.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "CURRENT STOCK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = UtpadPrimary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Available in Packing Floor",
                            style = MaterialTheme.typography.bodySmall,
                            color = UtpadTextPrimary,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Text(
                                text = "%,d".format(currentStock),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = UtpadPrimary,
                            )
                            Text(
                                text = " units",
                                style = MaterialTheme.typography.bodyMedium,
                                color = UtpadTextPrimary,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp),
                            )
                        }
                    }
                }

                WizardProgressBar(
                    currentStep = currentStep,
                    totalSteps = 3,
                    stepTitle = when(currentStep) {
                        1 -> "Batch Code"
                        2 -> "Quantities"
                        else -> "Vehicle & Review"
                    }
                )

                when (currentStep) {
                    // ── Step 1: Batch Code ──
                    1 -> {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = UtpadSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Batch Details",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = UtpadTextPrimary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "BATCH CODE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = UtpadTextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    OutlinedTextField(
                                        value = batchCode,
                                        onValueChange = { viewModel.onBatchCodeChanged(it) },
                                        placeholder = { Text("Scan or enter code", color = UtpadTextSecondary) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = UtpadPrimary,
                                            unfocusedBorderColor = UtpadOutline,
                                            focusedContainerColor = UtpadBackground,
                                            unfocusedContainerColor = UtpadSurface,
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    BarcodeScannerButton(
                                        prompt = "Scan dispatch batch barcode",
                                        onBarcodeScanned = viewModel::onBatchCodeChanged,
                                        onScanError = { message ->
                                            if (message != "Scan cancelled") {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(message)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // ── Step 2: Dispatch Quantities ──
                    2 -> {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = UtpadSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Text(
                                    text = "Dispatch Entry",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = UtpadTextPrimary
                                )

                                // Qty Taken from Packing
                                Column {
                                    Text(
                                        text = "QTY TAKEN FROM PACKING",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = UtpadTextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = qtyTakenFromPacking,
                                        onValueChange = { viewModel.onQtyTakenChanged(it) },
                                        placeholder = { Text("0", color = UtpadTextSecondary) },
                                        suffix = { Text("kg", color = UtpadTextSecondary) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = UtpadPrimary,
                                            unfocusedBorderColor = UtpadOutline,
                                            focusedContainerColor = UtpadBackground,
                                            unfocusedContainerColor = UtpadSurface,
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                    )
                                }

                                // Qty to Dispatch
                                Column {
                                    Text(
                                        text = "QTY UTILIZED / DISPATCHED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = UtpadTextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = qtyDispatched,
                                        onValueChange = { viewModel.onQtyDispatchedChanged(it) },
                                        placeholder = { Text("0", color = UtpadTextSecondary) },
                                        suffix = { Text("kg", color = UtpadTextSecondary) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = UtpadPrimary,
                                            unfocusedBorderColor = UtpadOutline,
                                            focusedContainerColor = UtpadBackground,
                                            unfocusedContainerColor = UtpadSurface,
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                    )
                                }
                            }
                        }
                    }

                    // ── Step 3: Destination, Vehicle & Review ──
                    3 -> {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = UtpadSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Text(
                                    text = "Shipping Details",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = UtpadTextPrimary
                                )

                                // Destination
                                Column {
                                    Text(
                                        text = "DESTINATION",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = UtpadTextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = destination,
                                        onValueChange = { viewModel.onDestinationChanged(it) },
                                        placeholder = { Text("Enter destination address", color = UtpadTextSecondary) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = UtpadPrimary,
                                            unfocusedBorderColor = UtpadOutline,
                                            focusedContainerColor = UtpadBackground,
                                            unfocusedContainerColor = UtpadSurface,
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                    )
                                }

                                // Vehicle Number
                                Column {
                                    Text(
                                        text = "VEHICLE NUMBER",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = UtpadTextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = vehicleNumber,
                                        onValueChange = { viewModel.onVehicleNumberChanged(it) },
                                        placeholder = { Text("e.g. MH-12-AB-1234", color = UtpadTextSecondary) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = UtpadPrimary,
                                            unfocusedBorderColor = UtpadOutline,
                                            focusedContainerColor = UtpadBackground,
                                            unfocusedContainerColor = UtpadSurface,
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                    )
                                }
                            }
                        }

                        // Status / Balance Review Card
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = UtpadBackground,
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "STATUS RESULT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = UtpadTextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    val statusColor = if (isPending) {
                                        UtpadPrimary
                                    } else {
                                        UtpadSuccess
                                    }
                                    val statusContainerColor = if (isPending) {
                                        UtpadPrimary.copy(alpha = 0.1f)
                                    } else {
                                        UtpadSuccess.copy(alpha = 0.1f)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = statusContainerColor,
                                    ) {
                                        Text(
                                            text = if (isPending) "PENDING" else "DISPATCHED",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom,
                                ) {
                                    Text(
                                        text = "REMAINING BALANCE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = UtpadTextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        val balanceColor = if (remainingBalance < 100) {
                                            UtpadError
                                        } else {
                                            UtpadTextPrimary
                                        }
                                        Text(
                                            text = "%,d".format(remainingBalance),
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = balanceColor,
                                        )
                                        Text(
                                            text = " kg",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = UtpadTextSecondary,
                                            modifier = Modifier.padding(bottom = 2.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom action bar
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                shadowElevation = 20.dp,
                color = UtpadSurface,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (currentStep > 1) {
                                    viewModel.previousStep()
                                } else {
                                    viewModel.reset()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                contentColor = UtpadTextPrimary
                            )
                        ) {
                            if (currentStep > 1) {
                                Text("Back", fontWeight = FontWeight.Bold)
                            } else {
                                Text("Reset", fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                if (currentStep < 3) {
                                    viewModel.nextStep()
                                } else {
                                    viewModel.submit()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = UtpadPrimary,
                                contentColor = androidx.compose.ui.graphics.Color.White
                            ),
                            enabled = submitState !is SubmitState.Loading,
                        ) {
                            if (currentStep < 3) {
                                Text("Continue", fontWeight = FontWeight.Bold)
                            } else {
                                Text("Confirm Dispatch", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (AppRoute.Inwarding in allowedRoutes) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = { onNavigateToRoute(AppRoute.Inwarding) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Back to Inwarding", color = UtpadPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardProgressBar(
    currentStep: Int,
    totalSteps: Int,
    stepTitle: String,
    modifier: Modifier = Modifier
) {
    val progress = currentStep.toFloat() / totalSteps.toFloat()
    val percentage = (progress * 100).toInt()
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "STEP $currentStep OF $totalSteps",
                    style = MaterialTheme.typography.labelSmall,
                    color = UtpadPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stepTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = UtpadTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelMedium,
                color = UtpadTextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        // Progress Bar Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(UtpadOutline, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress)
                    .height(6.dp)
                    .background(UtpadPrimary, RoundedCornerShape(3.dp))
            )
        }
    }
}
