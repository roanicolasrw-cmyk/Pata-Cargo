package com.patacargo.virchm

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.patacargo.virchm.data.ShipmentEntity
import com.patacargo.virchm.ui.PataCargoViewModel
import com.patacargo.virchm.ui.components.*
import com.patacargo.virchm.ui.dialogs.*
import com.patacargo.virchm.ui.screens.*
import com.patacargo.virchm.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PataCargoTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize().testTag("main_scaffold")
                ) { innerPadding ->
                    PataCargoAppShell(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PataCargoAppShell(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: PataCargoViewModel = viewModel()
    val scope = rememberCoroutineScope()

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val selectedUserId by viewModel.selectedUserId.collectAsStateWithLifecycle()

    var activeRole by remember { mutableStateOf("ENVIADOR") }
    var senderTab by remember { mutableStateOf("MIS_ENVIOS") }
    var carrierTab by remember { mutableStateOf("BUSCADOR") }
    var adminTab by remember { mutableStateOf("VALIDACIONES") }

    val selectedShipmentDetail by viewModel.selectedShipmentDetail.collectAsStateWithLifecycle()
    val selectedShipmentOffers by viewModel.selectedShipmentOffers.collectAsStateWithLifecycle()
    val chatShipmentDetails by viewModel.chatShipmentDetails.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessagesForActiveShipment.collectAsStateWithLifecycle()
    val carrierUnderSelfieVerification by viewModel.carrierUnderSelfieVerification.collectAsStateWithLifecycle()
    val firebaseUser by viewModel.firebaseUser.collectAsStateWithLifecycle()

    var activeScanShipmentId by remember { mutableStateOf<Int?>(null) }
    var activeScanType by remember { mutableStateOf<String?>(null) }
    var showScanDialog by remember { mutableStateOf(false) }
    var showBidDialogShipment by remember { mutableStateOf<ShipmentEntity?>(null) }
    var googleSignInErrorSHA1 by remember { mutableStateOf<String?>(null) }

    val isAdmin = firebaseUser?.email == "patacargo.app@gmail.com"

    var preferredRoleInState by remember(selectedUserId, firebaseUser, currentUser) {
        val calculated = currentUser?.mainRole ?: if (selectedUserId.isNotEmpty()) viewModel.getPreferredRole(selectedUserId) else null
        mutableStateOf(if (isAdmin) "ADMIN" else calculated)
    }

    val userNeedsRoleChoice = firebaseUser != null && selectedUserId.isNotEmpty() && !isAdmin && preferredRoleInState == null

    LaunchedEffect(selectedUserId, firebaseUser, currentUser) {
        if (isAdmin) {
            activeRole = "ADMIN"
            preferredRoleInState = "ADMIN"
        } else if (currentUser != null) {
            val role = currentUser?.mainRole ?: "ENVIADOR"
            preferredRoleInState = role
            activeRole = role
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (firebaseUser == null) {
            val googleLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    account?.idToken?.let { token ->
                        viewModel.signInWithGoogleToken(token) { success, msg ->
                            if (success) Toast.makeText(context, "¡Sesión iniciada!", Toast.LENGTH_SHORT).show()
                            else Toast.makeText(context, "Error: $msg", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: ApiException) {
                    if (e.statusCode == 10) googleSignInErrorSHA1 = getSigningCertificateSHA1(context)
                }
            }

            OnboardingAuthScreen(
                viewModel = viewModel,
                onGoogleSignInIntent = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("14033808612-6i5qspi0cbve77c7q2up08ji37nicq70.apps.googleusercontent.com")
                        .requestEmail().build()
                    val client = GoogleSignIn.getClient(context, gso)
                    client.signOut().addOnCompleteListener { googleLauncher.launch(client.signInIntent) }
                }
            )
        } else if (userNeedsRoleChoice) {
            UserRoleSelectionScreen(onRoleSelected = { chosenRole ->
                viewModel.setPreferredRole(selectedUserId, chosenRole)
                preferredRoleInState = chosenRole
                activeRole = chosenRole
            })
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PataCargoVectorLogo(modifier = Modifier.size(38.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("PATA CARGO", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PatagonianTeal)
                                Text("VIRCH & Madryn Colaborativo", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    },
                    actions = {
                        currentUser?.let { user ->
                            Row(
                                modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(TealLight).padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.AccountBalance, null, tint = PatagonianTeal, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$${String.format("%,.2f", user.walletBalance)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PatagonianTeal)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        var showLogoutMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showLogoutMenu = true }) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(if (activeRole == "ENVIADOR") SunsetGold else PatagonianTeal), contentAlignment = Alignment.Center) {
                                Text(currentUser?.name?.take(2)?.uppercase() ?: "US", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            DropdownMenu(expanded = showLogoutMenu, onDismissRequest = { showLogoutMenu = false }) {
                                DropdownMenuItem(text = { Text("Cerrar Sesión") }, leadingIcon = { Icon(Icons.Filled.ExitToApp, null, tint = CoralRed) }, onClick = { viewModel.signOutRealUser() })
                            }
                        }
                    }
                )

                val roles = if (isAdmin) listOf(Triple("ENVIADOR", Icons.Filled.AddBox, "Hacer Envío"), Triple("PORTADOR", Icons.Filled.LocalShipping, "Llevar Carga"), Triple("ADMIN", Icons.Filled.Security, "Panel Admin"))
                else listOf(Triple("ENVIADOR", Icons.Filled.AddBox, "Hacer Envío"), Triple("PORTADOR", Icons.Filled.LocalShipping, "Llevar Carga"))

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    roles.forEach { (role, icon, title) ->
                        val selected = activeRole == role
                        FilterChip(
                            selected = selected,
                            onClick = { activeRole = role },
                            label = { Text(title, fontSize = 12.sp) },
                            leadingIcon = { Icon(icon, null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (activeRole) {
                        "ENVIADOR" -> SenderSectionLayout(viewModel, senderTab, { senderTab = it }, { viewModel.selectedShipmentIdDetail.value = it }, { viewModel.setActiveChatShipmentId(it) })
                        "PORTADOR" -> CarrierSectionLayout(viewModel, carrierTab, { carrierTab = it }, { showBidDialogShipment = it }, { activeScanShipmentId = it; activeScanType = "COLLECT"; showScanDialog = true }, { activeScanShipmentId = it; activeScanType = "DELIVER"; showScanDialog = true }, { viewModel.setActiveChatShipmentId(it) })
                        "ADMIN" -> if (isAdmin) AdminSectionLayout(viewModel, adminTab, { adminTab = it }, { viewModel.adminApproveCarrier(it) })
                    }
                }
            }

            carrierUnderSelfieVerification?.let { user ->
                BiometricScanningSimulator(user, onScanComplete = { viewModel.completeBiometricSelfieCheck(it) })
            }

            selectedShipmentDetail?.let { shipment ->
                ShipmentDetailAndOfferDialog(
                    shipment = shipment,
                    offers = selectedShipmentOffers,
                    currentUserId = selectedUserId,
                    onAcceptOffer = { offer ->
                        viewModel.acceptOffer(shipment.id, offer) { url ->
                            if (!url.isNullOrEmpty()) context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                        }
                        viewModel.selectedShipmentIdDetail.value = null
                    },
                    onSubmitDispute = { viewModel.submitDispute(shipment.id); viewModel.selectedShipmentIdDetail.value = null },
                    onRateCarrier = { r, c -> viewModel.submitReview(shipment.id, r, c, shipment.senderId, shipment.carrierId ?: ""); viewModel.selectedShipmentIdDetail.value = null },
                    onDismiss = { viewModel.selectedShipmentIdDetail.value = null }
                )
            }

            if (showScanDialog) {
                QRScannerCodeDialog(activeScanShipmentId!!, activeScanType!!, viewModel, { showScanDialog = false }, { showScanDialog = false })
            }

            showBidDialogShipment?.let { s ->
                MakeCarrierOfferDialog(s, s.price, { p, c -> viewModel.makeOffer(s.id, p, c); showBidDialogShipment = null }, { showBidDialogShipment = null })
            }

            chatShipmentDetails?.let { s ->
                ActiveChatDialog(s, selectedUserId, currentUser?.name ?: "Usuario", chatMessages, { viewModel.sendMessage(s.id, it) }, { viewModel.setActiveChatShipmentId(null) })
            }

            if (googleSignInErrorSHA1 != null) {
                AlertDialog(onDismissRequest = { googleSignInErrorSHA1 = null }, title = { Text("Error de Configuración") }, text = { Text("SHA-1: $googleSignInErrorSHA1") }, confirmButton = { TextButton(onClick = { googleSignInErrorSHA1 = null }) { Text("OK") } })
            }
        }
    }
}
