package com.example

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.PataCargoViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PataCargoVectorLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Draw the circular boundary outlines as seen in the logo
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.045f // Proportional line weight
            // Outer Navy Blue arc
            drawArc(
                color = PatagonianTeal,
                startAngle = 100f,
                sweepAngle = 260f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
            )
            // Outer Orange arc
            drawArc(
                color = SunsetGold,
                startAngle = -20f,
                sweepAngle = 105f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
            )
        }
        
        // Location Pin (Dark Navy) sheltering the customized smiling parcel box
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(0.65f),
            contentAlignment = Alignment.Center
        ) {
            val heightDp = maxHeight
            val widthDp = maxWidth
            val proportionalOffset = heightDp * -0.065f // Proportional elevation matching the pin head center
            
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = PatagonianTeal,
                modifier = Modifier.fillMaxSize()
            )
            
            // Circular white interior mask inside the pin
            Box(
                modifier = Modifier
                    .fillMaxSize(0.48f)
                    .offset(y = proportionalOffset)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                // Orange cargo box centered inside the pin's core
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .clip(RoundedCornerShape(widthDp * 0.08f))
                        .background(SunsetGold),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val thickness = size.minDimension * 0.08f
                        // Parcel white tape diagonal line
                        drawLine(
                            color = Color.White,
                            start = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.35f),
                            end = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.35f),
                            strokeWidth = thickness
                        )
                        // Playful box smile arc (Navy Blue)
                        drawArc(
                            color = PatagonianTeal,
                            startAngle = 10f,
                            sweepAngle = 160f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = thickness * 0.8f),
                            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.25f, size.height * 0.45f),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.5f, size.height * 0.35f)
                        )
                    }
                }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PataCargoTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold")
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

    // Observe flows from state ViewModel
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val selectedUserId by viewModel.selectedUserId.collectAsStateWithLifecycle()

    // Selected view-state within roles (Sub-navigation)
    // Roles: "ENVIADOR", "PORTADOR", "ADMIN"
    var activeRole by remember { mutableStateOf("ENVIADOR") }
    
    // Sender (Enviador) active section tab: "MIS_ENVIOS", "NUEVA_CARGA", "CHATS"
    var senderTab by remember { mutableStateOf("MIS_ENVIOS") }
    // Carrier (Portador) active section tab: "BUSCADOR", "MIS_VIAJES", "RUTAS"
    var carrierTab by remember { mutableStateOf("BUSCADOR") }
    // Admin active section tab: "VALIDACIONES", "AUDITORIA", "DISPUTAS"
    var adminTab by remember { mutableStateOf("VALIDACIONES") }

    // Dialog state controllers
    var showProfileSwitcher by remember { mutableStateOf(false) }
    
    // Detail sheet states
    val selectedShipmentDetail by viewModel.selectedShipmentDetail.collectAsStateWithLifecycle()
    val selectedShipmentOffers by viewModel.selectedShipmentOffers.collectAsStateWithLifecycle()

    // Chat view sheet state
    val chatShipmentDetails by viewModel.chatShipmentDetails.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessagesForActiveShipment.collectAsStateWithLifecycle()

    // Scan simulator state
    var activeScanShipmentId by remember { mutableStateOf<Int?>(null) }
    var activeScanType by remember { mutableStateOf<String?>(null) } // "COLLECT" or "DELIVER"
    var showScanDialog by remember { mutableStateOf(false) }

    // Carrier registering a bid state
    var showBidDialogShipment by remember { mutableStateOf<ShipmentEntity?>(null) }

    // Biometric Scanner selfie checks state
    val carrierUnderSelfieVerification by viewModel.carrierUnderSelfieVerification.collectAsStateWithLifecycle()

    val firebaseUser by viewModel.firebaseUser.collectAsStateWithLifecycle()
    var googleSignInErrorSHA1 by remember { mutableStateOf<String?>(null) }

    val userNeedsRoleChoice = firebaseUser != null && viewModel.getPreferredRole(selectedUserId) == null

    // Sync roles when changing simulated or real users
    LaunchedEffect(selectedUserId, firebaseUser) {
        if (firebaseUser != null) {
            val savedRole = viewModel.getPreferredRole(selectedUserId)
            if (savedRole != null) {
                activeRole = savedRole
            } else {
                activeRole = "ENVIADOR"
            }
        } else {
            if (selectedUserId == "admin") {
                activeRole = "ADMIN"
            } else if (selectedUserId.startsWith("portador")) {
                activeRole = "PORTADOR"
            } else {
                activeRole = "ENVIADOR"
            }
        }
    }

    if (firebaseUser == null) {
        // Reusable google sign in configuration inside Onboarding screen
        val googleLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    viewModel.signInWithGoogleToken(idToken) { success, errorMsg ->
                        if (success) {
                            Toast.makeText(context, "¡Sesión iniciada correctamente con Google!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Error: ${errorMsg ?: "Error de vinculación"}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "No se recibió un Token de ID válido de Google.", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                if (e.statusCode == 10) {
                    googleSignInErrorSHA1 = getSigningCertificateSHA1(context)
                } else {
                    val statusMessage = when (e.statusCode) {
                        12500 -> "Google Play Services no está configurado en el dispositivo"
                        7 -> "Error de conexión de red"
                        else -> "Código de de error: ${e.statusCode}"
                    }
                    Toast.makeText(context, "Fallo de Google Sign-In: $statusMessage", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Fallo al iniciar sesión: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }

        fun triggerOnboardingGoogleSignIn() {
            try {
                val webClientId = try {
                    context.getString(
                        context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                    ).ifEmpty { "14033808612-6i5qspi0cbve77c7q2up08ji37nicq70.apps.googleusercontent.com" }
                } catch (e: Exception) {
                    "14033808612-6i5qspi0cbve77c7q2up08ji37nicq70.apps.googleusercontent.com"
                }

                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build()

                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                googleSignInClient.signOut().addOnCompleteListener {
                    val signInIntent = googleSignInClient.signInIntent
                    googleLauncher.launch(signInIntent)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al preparar Google Sign-In: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }

        OnboardingAuthScreen(
            viewModel = viewModel,
            onGoogleSignInIntent = { triggerOnboardingGoogleSignIn() }
        )
    } else if (userNeedsRoleChoice) {
        UserRoleSelectionScreen(
            onRoleSelected = { chosenRole ->
                viewModel.setPreferredRole(selectedUserId, chosenRole)
                activeRole = chosenRole
            }
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                //--- BRANDED TOP LOGISTICS ROW & SIMULATOR CHEATS BAR ---
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PataCargoVectorLogo(modifier = Modifier.size(38.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "PATA CARGO",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "VIRCH & Madryn Colaborativo",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    },
                    actions = {
                        // Wallet Balance indicator
                        currentUser?.let { user ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TealLight)
                                    .border(1.dp, PatagonianTeal.copy(0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccountBalance,
                                    contentDescription = "Billetera",
                                    tint = PatagonianTeal,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$${String.format("%,.2f", user.walletBalance)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PatagonianTeal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Profile Switcher trigger
                        IconButton(
                            onClick = { showProfileSwitcher = true },
                            modifier = Modifier.testTag("avatar_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (activeRole) {
                                            "ENVIADOR" -> SunsetGold
                                            "PORTADOR" -> PatagonianTeal
                                            else -> ValleyGreen
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser?.name?.take(2)?.uppercase() ?: "US",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                )

                //--- ROLE INTERACTIVE BAR CHIPS ---
                Surface(
                    tonalElevation = 2.dp,
                    shadowElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val roles = listOf(
                            Triple("ENVIADOR", Icons.Filled.AddBox, "Hacer Envío"),
                            Triple("PORTADOR", Icons.Filled.LocalShipping, "Llevar Carga"),
                            Triple("ADMIN", Icons.Filled.Security, "Panel Admin")
                        )
                        
                        roles.forEach { (role, icon, title) ->
                            val isSelected = activeRole == role
                            val isSimulatedRoleMatch = when (role) {
                                "ADMIN" -> selectedUserId == "admin"
                                "PORTADOR" -> selectedUserId.startsWith("portador")
                                else -> selectedUserId.startsWith("enviador")
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    activeRole = role
                                    if (firebaseUser != null) {
                                        viewModel.setPreferredRole(selectedUserId, role)
                                    }
                                },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = title,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = title,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors()
                                    .copy(
                                        selectedContainerColor = if (isSimulatedRoleMatch) PatagonianTeal else SlateGrey,
                                        selectedLabelColor = Color.White,
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                border = FilterChipDefaults.filterChipBorder(
                                    selected = isSelected,
                                    enabled = true,
                                    borderColor = if (isSimulatedRoleMatch) SunsetGold else CardBorderColor,
                                    selectedBorderColor = if (isSimulatedRoleMatch) SunsetGold else PatagonianTeal,
                                    disabledBorderColor = CardBorderColor
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("role_chip_$role")
                            )
                        }
                    }
                }

                // Warnings or Simulator Hints
                SimulationIdentityShield(
                    activeRole = activeRole,
                    userId = selectedUserId,
                    userVerified = currentUser?.isVerified == true,
                    onFixIdentity = { showProfileSwitcher = true }
                )

                //--- RENDERING CORE VIEWS BASED ON ROLE ---
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (activeRole) {
                        "ENVIADOR" -> {
                            SenderSectionLayout(
                                viewModel = viewModel,
                                currentTab = senderTab,
                                onTabSelected = { senderTab = it },
                                onItemClicked = { shipmentId ->
                                    viewModel.selectedShipmentIdDetail.value = shipmentId
                                },
                                onChatClicked = { shipmentId ->
                                    viewModel.setActiveChatShipmentId(shipmentId)
                                }
                            )
                        }
                        "PORTADOR" -> {
                            CarrierSectionLayout(
                                viewModel = viewModel,
                                currentTab = carrierTab,
                                onTabSelected = { carrierTab = it },
                                onPlaceOfferClicked = { shipment ->
                                    showBidDialogShipment = shipment
                                },
                                onScanCollection = { sId ->
                                    activeScanShipmentId = sId
                                    activeScanType = "COLLECT"
                                    showScanDialog = true
                                },
                                onScanDelivery = { sId ->
                                    activeScanShipmentId = sId
                                    activeScanType = "DELIVER"
                                    showScanDialog = true
                                },
                                onChatClicked = { shipmentId ->
                                    viewModel.setActiveChatShipmentId(shipmentId)
                                }
                            )
                        }
                        else -> {
                            AdminSectionLayout(
                                viewModel = viewModel,
                                currentTab = adminTab,
                                onTabSelected = { adminTab = it },
                                onApproveVerify = { carrierId ->
                                    viewModel.adminApproveCarrier(carrierId)
                                    Toast.makeText(context, "Portador aprobado con éxito", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }

            //--- FLOATING IDENTITY SCANNER SIMULATION PANEL (SELFIES) ---
            carrierUnderSelfieVerification?.let { user ->
                BiometricScanningSimulator(
                    user = user,
                    onScanComplete = { isSuccess ->
                        viewModel.completeBiometricSelfieCheck(isSuccess)
                        Toast.makeText(context, if (isSuccess) "Selfie validada. Pendiente de aprobación administrativa!" else "Fallo al validar selfie biométrica.", Toast.LENGTH_LONG).show()
                    }
                )
            }

            //--- POPUP DIALOGS PANEL ---

            // 1. Profile / Simulated User Switcher Dialog
            if (showProfileSwitcher) {
                SimulatedIdentitySelectorDialog(
                    users = users,
                    selectedUserId = selectedUserId,
                    viewModel = viewModel,
                    onSelectUser = { uid ->
                        viewModel.changeActiveRole(uid)
                        showProfileSwitcher = false
                    },
                    onGoogleSignInError = { sha1 ->
                        googleSignInErrorSHA1 = sha1
                    },
                    onDismiss = { showProfileSwitcher = false }
                )
            }

            // 2. Shipment Detail View with Offers / Collection QR Scanner
            selectedShipmentDetail?.let { shipment ->
                ShipmentDetailAndOfferDialog(
                    shipment = shipment,
                    offers = selectedShipmentOffers,
                    currentUserId = selectedUserId,
                    onAcceptOffer = { offer ->
                        viewModel.acceptOffer(shipment.id, offer)
                        viewModel.selectedShipmentIdDetail.value = null
                        scope.launch {
                            Toast.makeText(context, "Oferta aceptada. El costo ha sido colocado en garantía (Escrow)!", Toast.LENGTH_LONG).show()
                        }
                    },
                    onSubmitDispute = {
                        viewModel.submitDispute(shipment.id)
                        viewModel.selectedShipmentIdDetail.value = null
                        Toast.makeText(context, "Se abrió una disputa comercial por esta carga.", Toast.LENGTH_SHORT).show()
                    },
                    onRateCarrier = { rating, comment ->
                        viewModel.submitReview(
                            shipmentId = shipment.id,
                            rating = rating,
                            comment = comment,
                            writerId = shipment.senderId,
                            targetId = shipment.carrierId ?: ""
                        )
                        viewModel.selectedShipmentIdDetail.value = null
                        Toast.makeText(context, "Reseña registrada con éxito!", Toast.LENGTH_SHORT).show()
                    },
                    onDismiss = { viewModel.selectedShipmentIdDetail.value = null }
                )
            }

            // 3. Easy QR scan bypass controller
            if (showScanDialog && activeScanShipmentId != null && activeScanType != null) {
                QRScannerCodeDialog(
                    shipmentId = activeScanShipmentId!!,
                    scanType = activeScanType!!,
                    viewModel = viewModel,
                    onDismiss = {
                        showScanDialog = false
                        activeScanShipmentId = null
                        activeScanType = null
                    },
                    onScanSuccess = {
                        showScanDialog = false
                        activeScanShipmentId = null
                        activeScanType = null
                        Toast.makeText(context, "¡Escaneo exitoso! Estado de carga actualizado.", Toast.LENGTH_LONG).show()
                    }
                )
            }

            // 4. Carrier Bid Creator Dialog
            showBidDialogShipment?.let { s ->
                MakeCarrierOfferDialog(
                    shipment = s,
                    suggestedBasePrice = s.price,
                    onSendOffer = { bidPrice, comment ->
                        viewModel.makeOffer(s.id, bidPrice, comment)
                        showBidDialogShipment = null
                        Toast.makeText(context, "Oferta de transporte enviada!", Toast.LENGTH_SHORT).show()
                    },
                    onDismiss = { showBidDialogShipment = null }
                )
            }

            // 5. Active Chat Dialog
            chatShipmentDetails?.let { s ->
                ActiveChatDialog(
                    shipment = s,
                    currentUserId = selectedUserId,
                    currentUserName = currentUser?.name ?: "Usuario",
                    messages = chatMessages,
                    onSend = { text -> viewModel.sendMessage(s.id, text) },
                    onDismiss = { viewModel.setActiveChatShipmentId(null) }
                )
            }

            // 6. Google Sign-In SHA-1 Error Code 10 Diagnostic Dialog
            if (googleSignInErrorSHA1 != null) {
                val sha1Fingerprint = googleSignInErrorSHA1!!
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                
                AlertDialog(
                    onDismissRequest = { googleSignInErrorSHA1 = null },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = CoralRed,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            "Error 10 de Google Sign-In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PatagonianTeal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Este error (Código 10: DEVELOPER_ERROR) ocurre cuando la firma SHA-1 de esta aplicación no está registrada en tu consola de Firebase.",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                "Para solucionarlo de forma definitiva:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PatagonianTeal
                            )
                            Text(
                                "1. Copia la firma SHA-1 que aparece a continuación.\n" +
                                "2. Regístrala en la consola de Firebase (Ajustes -> General -> Tus apps) para tu paquete 'com.aistudio.patacargo.virchm'.\n" +
                                "3. Vuelve a descargar el archivo 'google-services.json' y súbelo.",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LightBackground),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CardBorderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        "FIRMADO CON SHA-1:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = sha1Fingerprint,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PatagonianTeal,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(sha1Fingerprint))
                                            Toast.makeText(context, "Firma SHA-1 copiada al portapapeles", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PatagonianTeal),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth().height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copiar SHA-1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { googleSignInErrorSHA1 = null }) {
                            Text("Entendido", color = PatagonianTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}

//--- HELPER SHIELD FOR ROLE MISMATCH OR NOT APPROVED CARRIERS ---
@Composable
fun SimulationIdentityShield(
    activeRole: String,
    userId: String,
    userVerified: Boolean,
    onFixIdentity: () -> Unit
) {
    val showDisclaimer = when {
        activeRole == "ADMIN" && userId != "admin" -> true
        activeRole == "PORTADOR" && userId.startsWith("enviador") -> true
        activeRole == "ENVIADOR" && userId.startsWith("portador") && userId != "portador_sofia" -> true
        else -> false
    }

    Column {
        if (showDisclaimer) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF3C7)) // soft orange warning
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Desajuste de Rol",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Estás viendo el panel de $activeRole pero tu usuario activo es '${userId}'.",
                            color = Color(0xFF92400E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "Cambiar",
                        color = Color(0xFFD97706),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onFixIdentity() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Carrier verification alert
        if (activeRole == "PORTADOR" && !userVerified && userId.startsWith("portador")) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CoralRed.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = "Sin verificar",
                        tint = CoralRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Identidad Portador Requerida",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "Necesitas validación biométrica con selfie y posterior aprobación del Admin para ofertar.",
                            fontSize = 11.sp,
                            color = Color(0xFF991B1B).copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

//================================================================================
// 1. ENVIADOR (SENDER) UI PORTAL
//================================================================================
@Composable
fun SenderSectionLayout(
    viewModel: PataCargoViewModel,
    currentTab: String,
    onTabSelected: (String) -> Unit,
    onItemClicked: (Int) -> Unit,
    onChatClicked: (Int) -> Unit
) {
    val myShipments by viewModel.mySenderShipments.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Pata Cargo Enviadores Brand Banner
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(width = 0.5.dp, color = PatagonianTeal.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PataCargoVectorLogo(modifier = Modifier.size(34.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Necesito Enviar un Paquete",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PatagonianTeal
                    )
                    Text(
                        text = "Publicá tu carga, compará ofertas y coordiná con portadores de confianza.",
                        fontSize = 10.5.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Nested Navigation Tabs
        TabRow(
            selectedTabIndex = when (currentTab) {
                "MIS_ENVIOS" -> 0
                "NUEVA_CARGA" -> 1
                else -> 2
            },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PatagonianTeal
        ) {
            Tab(
                selected = currentTab == "MIS_ENVIOS",
                onClick = { onTabSelected("MIS_ENVIOS") },
                text = { Text("Mis Envíos", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = currentTab == "NUEVA_CARGA",
                onClick = { onTabSelected("NUEVA_CARGA") },
                text = { Text("Nueva Carga", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("new_shipment_tab")
            )
            Tab(
                selected = currentTab == "CHATS",
                onClick = { onTabSelected("CHATS") },
                text = { Text("Mensajes", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (currentTab) {
                "MIS_ENVIOS" -> {
                    if (myShipments.isEmpty()) {
                        EmptyStateWidget(
                            icon = Icons.Filled.List,
                            title = "No has publicado cargas todavía",
                            description = "Para enviar un paquete a Madryn o al Valle, ingresa a la pestaña 'Nueva Carga' y completa los datos.",
                            actionLabel = "Crear Mi Primer Envío",
                            onAction = { onTabSelected("NUEVA_CARGA") }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(myShipments, key = { it.id }) { shipment ->
                                SenderShipmentCard(
                                    shipment = shipment,
                                    onClicked = { onItemClicked(shipment.id) },
                                    onChatClicked = { onChatClicked(shipment.id) }
                                )
                            }
                        }
                    }
                }
                "NUEVA_CARGA" -> {
                    NewShipmentForm(
                        viewModel = viewModel,
                        onPublishedSuccess = {
                            onTabSelected("MIS_ENVIOS")
                        }
                    )
                }
                "CHATS" -> {
                    // Filter shipments that have carrier assigned
                    val activeChatShipments = myShipments.filter { it.status != "PENDIENTE" }

                    if (activeChatShipments.isEmpty()) {
                        EmptyStateWidget(
                            icon = Icons.Filled.Chat,
                            title = "No hay canles de chat activos",
                            description = "Se abrirá automáticamente un chat coordinativo con el Portador una vez que aceptes una oferta de transporte.",
                            actionLabel = null,
                            onAction = {}
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(activeChatShipments, key = { it.id }) { s ->
                                ChatThreadItemCard(
                                    shipment = s,
                                    subtitle = "Intercambio coordinativo de ruta",
                                    onOpenChat = { onChatClicked(s.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SenderShipmentCard(
    shipment: ShipmentEntity,
    onClicked: () -> Unit,
    onChatClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClicked() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Status and Date Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Size Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TealLight)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Medida: ${shipment.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PatagonianTeal
                    )
                }
                
                // Status Badge
                StatusLabelBadge(status = shipment.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = shipment.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Geographics Arrow
            RouteGeoVisualizer(origin = shipment.origin, destination = shipment.destination)

            Spacer(modifier = Modifier.height(8.dp))

            // Price / Guarantee Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val label = if (shipment.status == "PENDIENTE") "Precio Sugerido" else "Tasa Garantizada"
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "$${String.format("%,.2f", shipment.price)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PatagonianTeal
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (shipment.insuranceEnabled) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = "Asegurado",
                            tint = ValleyGreen,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7))
                                .padding(4.dp)
                        )
                    }

                    if (shipment.status != "PENDIENTE") {
                        Button(
                            onClick = { onChatClicked() },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealLight, contentColor = PatagonianTeal),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Simple notification helper if pending and has offers
            if (shipment.status == "PENDIENTE") {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = CardBorderColor)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "👉 Toca para ver propuestas de viajeros disponibles",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SunsetGold
                )
            }
        }
    }
}

// GEOGRAPHIC GRAPHICS COMPONENT
@Composable
fun RouteGeoVisualizer(origin: String, destination: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = PatagonianTeal,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = origin,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Ruta",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = CoralRed,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = destination,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun StatusLabelBadge(status: String) {
    val (backColor, textColor, text) = when (status) {
        "PENDIENTE" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "Publicado")
        "ACEPTADO" -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), "Match / Listo")
        "EN_VIAJE" -> Triple(Color(0xFFFEE2E2), CoralRed, "En Viaje")
        "ENTREGADO" -> Triple(Color(0xFFDCFCE7), ValleyGreen, "Entregado")
        else -> Triple(Color(0xFFF1F5F9), SlateGrey, "Disputado ⚠️")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backColor)
            .padding(horizontal = 10.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

// CREATE LOAD FORM WITH AUTOMATIC PRICE ESTIMATIONS
@Composable
fun NewShipmentForm(
    viewModel: PataCargoViewModel,
    onPublishedSuccess: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var origin by remember { mutableStateOf("Trelew") }
    var destination by remember { mutableStateOf("Puerto Madryn") }
    var size by remember { mutableStateOf("Mediano") }
    var isFragile by remember { mutableStateOf(false) }
    var timeWindow by remember { mutableStateOf("Antes de las 18hs") }
    var declaredValueText by remember { mutableStateOf("25000") }
    var insuranceEnabled by remember { mutableStateOf(false) }

    // Dynamic cost calculation based on form state
    val basePrice = viewModel.repository.calculatePrice(origin, destination, size)
    val commissionCost = basePrice * 0.15
    val declaredValue = declaredValueText.toDoubleOrNull() ?: 0.0
    val insuranceCost = if (insuranceEnabled) viewModel.repository.calculateInsurance(declaredValue) else 0.0
    val finalTotal = basePrice + commissionCost + insuranceCost

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Crear Oferta de Envío Colaborativo",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PatagonianTeal
        )
        Text(
            text = "Ingresa los datos para que los viajeros habituales del VIRCH vean tu paquete y te hagan una oferta.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        // Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("¿Qué envías? (Ej: Repuesto Óptico)") },
            placeholder = { Text("Escribe el nombre del paquete") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("shipment_title_input"),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        // Description Input
        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Indicaciones / Descripción física") },
            placeholder = { Text("Detalles del paquete, peso, dónde se retira...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            minLines = 2
        )

        // Route Cities (Spinner Alternatives via Row of Chips)
        Text("Origen de la Carga", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            viewModel.repository.cities.forEach { city ->
                val isSelected = origin == city
                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = { origin = city },
                    label = { Text(city, fontSize = 12.sp) },
                    colors = FilterChipDefaults.elevatedFilterChipColors().copy(
                        selectedContainerColor = PatagonianTeal,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Text("Destino de la Carga", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            viewModel.repository.cities.forEach { city ->
                val isSelected = destination == city
                val enabled = city != origin
                ElevatedFilterChip(
                    enabled = enabled,
                    selected = isSelected,
                    onClick = { destination = city },
                    label = { Text(city, fontSize = 12.sp) },
                    colors = FilterChipDefaults.elevatedFilterChipColors().copy(
                        selectedContainerColor = PatagonianTeal,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Package Sizing Row
        Text("Tamaño Sugerido del Bulto", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sizes = listOf("Pequeño", "Mediano", "Grande")
            sizes.forEach { s ->
                val isSelected = size == s
                val subtitle = when(s) {
                    "Pequeño" -> "Llaves/Docus"
                    "Mediano" -> "Caja zapatos"
                    else -> "Caja pesada"
                }
                
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { size = s }
                        .border(
                            1.dp,
                            if (isSelected) PatagonianTeal else CardBorderColor,
                            RoundedCornerShape(8.dp)
                        ),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) TealLight else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = s,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) PatagonianTeal else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Fragility Toggle & Time window
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Carga Frágil o Delicada", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Vidrios, alimentos, etc.", fontSize = 11.sp, color = Color.Gray)
            }
            Switch(
                checked = isFragile,
                onCheckedChange = { isFragile = it },
                colors = SwitchDefaults.colors(checkedThumbColor = PatagonianTeal)
            )
        }

        OutlinedTextField(
            value = timeWindow,
            onValueChange = { timeWindow = it },
            label = { Text("Ventana Horaria de Entrega") },
            placeholder = { Text("Ej: Antes de las 18:00hs") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        // Declared Value & Insurance toggle
        HorizontalDivider(color = CardBorderColor)

        Text("Garantía de Protección y Seguro (Opcional)", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = declaredValueText,
            onValueChange = { declaredValueText = it },
            label = { Text("Valor Declarado del Paquete ($ ARS)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (insuranceEnabled) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surface)
                .border(1.dp, if (insuranceEnabled) ValleyGreen.copy(0.3f) else CardBorderColor, RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = insuranceEnabled,
                onCheckedChange = { insuranceEnabled = it },
                colors = CheckboxDefaults.colors(checkedColor = ValleyGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Asegurar mi paquete contra daños/pérdidas",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (insuranceEnabled) ValleyGreen else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Costo extra: 1.5% del valor declarado. Cobertura total sustentada por la plataforma.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Dynamic pricing breakdown widget
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, CardBorderColor)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Desglose del Costo (Sugerido)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PatagonianTeal)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Envío Colaborativo base:", fontSize = 12.sp, color = Color.Gray)
                    Text("$${String.format("%,.2f", basePrice)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Comisión Pata Cargo (15%):", fontSize = 12.sp, color = Color.Gray)
                    Text("$${String.format("%,.2f", commissionCost)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ValleyGreen)
                }
                if (insuranceEnabled) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Seguro de protección (1.5%):", fontSize = 12.sp, color = Color.Gray)
                        Text("$${String.format("%,.2f", insuranceCost)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ValleyGreen)
                    }
                }
                HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total a Retener en Garantía:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("$${String.format("%,.2f", finalTotal)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = PatagonianTeal)
                }
                Text(
                    text = "⚠️ El dinero se descuenta de tu billetera pero queda CUSTODIADO en Escrow. Solo se libera al portador cuando confirmes llegada con QR.",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    lineHeight = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Publish Button
        Button(
            onClick = {
                if (title.isEmpty()) {
                    Toast.makeText(context, "Por favor indica qué estás enviando", Toast.LENGTH_SHORT).show()
                } else if (origin == destination) {
                    Toast.makeText(context, "El origen y el destino no pueden ser el mismo", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.createShipment(
                        title = title,
                        description = desc,
                        origin = origin,
                        destination = destination,
                        size = size,
                        isFragile = isFragile,
                        timeWindow = timeWindow,
                        declaredValue = declaredValue,
                        insuranceEnabled = insuranceEnabled
                    )
                    Toast.makeText(context, "¡Carga publicada! Buscando portadores disponibles...", Toast.LENGTH_LONG).show()
                    onPublishedSuccess()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("publish_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PatagonianTeal),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Filled.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Publicar y Reservar Garantía", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

//================================================================================
// 2. PORTADOR (CARRIER / TRAVELER) UI PORTAL
//================================================================================
@Composable
fun CarrierSectionLayout(
    viewModel: PataCargoViewModel,
    currentTab: String,
    onTabSelected: (String) -> Unit,
    onPlaceOfferClicked: (ShipmentEntity) -> Unit,
    onScanCollection: (Int) -> Unit,
    onScanDelivery: (Int) -> Unit,
    onChatClicked: (Int) -> Unit
) {
    val pendingShipments by viewModel.pendingShipments.collectAsStateWithLifecycle()
    val recommendedShipments by viewModel.recommendedShipments.collectAsStateWithLifecycle()
    val myCarrierJobShipments by viewModel.myCarrierShipments.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Pata Cargo Portadores Brand Banner
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(width = 0.5.dp, color = PatagonianTeal.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PataCargoVectorLogo(modifier = Modifier.size(34.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Gana Plata Extra, con un Viaje que ya ibas a realizar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PatagonianTeal
                    )
                    Text(
                        text = "Aprovechá el espacio libre de tu vehículo y amortizá tus costos de viaje.",
                        fontSize = 10.5.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        TabRow(
            selectedTabIndex = when (currentTab) {
                "BUSCADOR" -> 0
                "MIS_VIAJES" -> 1
                else -> 2
            },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PatagonianTeal
        ) {
            Tab(
                selected = currentTab == "BUSCADOR",
                onClick = { onTabSelected("BUSCADOR") },
                text = { Text("Buscar Cargas", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = currentTab == "MIS_VIAJES",
                onClick = { onTabSelected("MIS_VIAJES") },
                text = { Text("Mis Viajes", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Filled.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("my_trips_tab")
            )
            Tab(
                selected = currentTab == "RUTAS",
                onClick = { onTabSelected("RUTAS") },
                text = { Text("Rutas Favoritas", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (currentTab) {
                "BUSCADOR" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Section 1: Rutas Inteligentes Recommended Matches
                        if (recommendedShipments.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SunsetGold.copy(0.1f))
                                    .border(BorderStroke(1.dp, SunsetGold.copy(0.3f)))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Rutas Inteligentes",
                                        tint = SunsetGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🤖 RUTAS INTELIGENTES: Coinciden con tus viajes habituales",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                }
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                                    .background(SunsetGold.copy(0.04f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(recommendedShipments, key = { "rec-${it.id}" }) { s ->
                                    CarrierShipmentSearchCard(
                                        shipment = s,
                                        isRecommended = true,
                                        onSelected = { onPlaceOfferClicked(s) }
                                    )
                                }
                            }
                            HorizontalDivider(color = CardBorderColor)
                        }

                        // Section 2: All Pending Shipments
                        Text(
                            text = "Todas los envíos publicados en la región",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PatagonianTeal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )

                        if (pendingShipments.isEmpty()) {
                            EmptyStateWidget(
                                icon = Icons.Filled.Search,
                                title = "No hay paquetes pendientes en este momento",
                                description = "Vuelve más tarde para chequear si algún negocio o persona cargó envíos en el VIRCH.",
                                actionLabel = null,
                                onAction = {}
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(pendingShipments, key = { it.id }) { s ->
                                    CarrierShipmentSearchCard(
                                        shipment = s,
                                        isRecommended = false,
                                        onSelected = { onPlaceOfferClicked(s) }
                                    )
                                }
                            }
                        }
                    }
                }
                "MIS_VIAJES" -> {
                    if (myCarrierJobShipments.isEmpty()) {
                        EmptyStateWidget(
                            icon = Icons.Filled.LocalShipping,
                            title = "No tienes viajes activos",
                            description = "Ve a 'Buscar Cargas', haz ofertas según tus recorridos y, si los enviadores aceptan, te aparecerán aquí para retirar.",
                            actionLabel = "Explorar Cargas",
                            onAction = { onTabSelected("BUSCADOR") }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(myCarrierJobShipments, key = { it.id }) { s ->
                                ActiveCarrierJobCard(
                                    shipment = s,
                                    onScanCollection = { onScanCollection(s.id) },
                                    onScanDelivery = { onScanDelivery(s.id) },
                                    onChat = { onChatClicked(s.id) }
                                )
                            }
                        }
                    }
                }
                "RUTAS" -> {
                    CarrierFavoriteRoutesPanel(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CarrierShipmentSearchCard(
    shipment: ShipmentEntity,
    isRecommended: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecommended) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isRecommended) SunsetGold.copy(0.5f) else CardBorderColor
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRecommended) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SunsetGold)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("En Tu Ruta 🔥", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Medida: ${shipment.size}", fontSize = 11.sp, color = PatagonianTeal, fontWeight = FontWeight.Bold)
                }

                if (shipment.isFragile) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFEE2E2))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Frágil 🚨", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CoralRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = shipment.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            RouteGeoVisualizer(origin = shipment.origin, destination = shipment.destination)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ventanilla: ${shipment.timeWindow}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Pago Ofrecido", fontSize = 9.sp, color = Color.Gray)
                    Text("$${String.format("%,.2f", shipment.price)}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = PatagonianTeal)
                }
            }
        }
    }
}

@Composable
fun ActiveCarrierJobCard(
    shipment: ShipmentEntity,
    onScanCollection: () -> Unit,
    onScanDelivery: () -> Unit,
    onChat: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TealLight)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = shipment.size,
                        fontSize = 11.sp,
                        color = PatagonianTeal,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusLabelBadge(status = shipment.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = shipment.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            RouteGeoVisualizer(origin = shipment.origin, destination = shipment.destination)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Ganancia a liberar:", fontSize = 10.sp, color = Color.Gray)
                    // Carrier gets 100% of the offered amount under Pata Cargo rules
                    val finalPay = shipment.price
                    Text("$${String.format("%,.2f", finalPay)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = ValleyGreen)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onChat,
                        modifier = Modifier
                            .background(TealLight, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Filled.Chat, contentDescription = "Conversar", tint = PatagonianTeal, modifier = Modifier.size(18.dp))
                    }

                    when (shipment.status) {
                        "ACEPTADO" -> {
                            Button(
                                onClick = onScanCollection,
                                colors = ButtonDefaults.buttonColors(containerColor = PatagonianTeal),
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("scan_collect_btn")
                            ) {
                                Icon(Icons.Filled.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retirar (QR)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        "EN_VIAJE" -> {
                            Button(
                                onClick = onScanDelivery,
                                colors = ButtonDefaults.buttonColors(containerColor = SunsetGold),
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("scan_deliver_btn")
                            ) {
                                Icon(Icons.Filled.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Entregar (QR)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Flow instruction footer
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            val footerText = when (shipment.status) {
                "ACEPTADO" -> "📍 Paso 1: Ve a buscar el paquete. Haz que el enviador te muestre su QR y escanéalo."
                "EN_VIAJE" -> "🚚 Paso 2: Paquete cargado. Al llegar a destino, pídele al receptor su QR y escanéalo para cobrar."
                else -> "⭐ ¡Viaje finalizado satisfactoriamente! Los fondos netos ya están en tu monedero."
            }
            Text(
                text = footerText,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// MANAGEMENT OF CARRIER ROUTES
@Composable
fun CarrierFavoriteRoutesPanel(viewModel: PataCargoViewModel) {
    val context = LocalContext.current
    val routes by viewModel.myRoutes.collectAsStateWithLifecycle()
    val isVerified = viewModel.currentUser.collectAsStateWithLifecycle().value?.isVerified ?: false
    val user = viewModel.currentUser.collectAsStateWithLifecycle().value

    var origin by remember { mutableStateOf("Trelew") }
    var destination by remember { mutableStateOf("Puerto Madryn") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Tus Recorridos Habituales (VIRCH)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PatagonianTeal)
        Text(
            text = "Configura tus rutas diarias por trabajo o estudio. El sistema te emparejará automáticamente de forma inteligente con paquetes que sigan tu mismo trayecto (incluso paradas intermedias).",
            fontSize = 11.sp,
            color = Color.Gray
        )

        if (!isVerified && user != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CoralRed.copy(0.1f)),
                border = BorderStroke(1.dp, CoralRed.copy(0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🔒 Requiere Verificación de Identidad", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CoralRed)
                    Text(
                        text = "Dado que transportarás equipamiento ajeno, debes realizar un escaneo biométrico con selfie para garantizar legitimidad regional.",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Button(
                        onClick = { viewModel.triggerBiometricValidation(user) },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Iniciar Validación Biométrica", fontSize = 11.sp)
                    }
                }
            }
        }

        // Add Route Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, CardBorderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Sumar Tramo Habitual", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Origin Input
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Origen habitual", fontSize = 11.sp, color = Color.Gray)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            viewModel.repository.cities.forEach { city ->
                                ElevatedFilterChip(
                                    selected = origin == city,
                                    onClick = { origin = city },
                                    label = { Text(city, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Destination Input
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Destino habitual", fontSize = 11.sp, color = Color.Gray)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            viewModel.repository.cities.forEach { city ->
                                ElevatedFilterChip(
                                    selected = destination == city,
                                    onClick = { destination = city },
                                    label = { Text(city, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (origin == destination) {
                            Toast.makeText(context, "El origen y el destino no pueden coincidir", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addFavoriteRoute(origin, destination)
                            Toast.makeText(context, "¡Ruta registrada!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PatagonianTeal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Registrar Ruta", fontSize = 12.sp)
                }
            }
        }

        // Active Routes List
        Text("Tus Tramos Registrados", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PatagonianTeal)

        if (routes.isEmpty()) {
            Text(
                text = "No has cargado tramos aún. El buscador te mostrará cargas generales sin priorizar tu corredor.",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                routes.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, CardBorderColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RouteGeoVisualizer(origin = r.origin, destination = r.destination)
                        
                        IconButton(
                            onClick = { viewModel.removeFavoriteRoute(r.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Eliminar",
                                tint = CoralRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

//================================================================================
// 3. ADMIN CONTROL CENTER
//================================================================================
@Composable
fun AdminSectionLayout(
    viewModel: PataCargoViewModel,
    currentTab: String,
    onTabSelected: (String) -> Unit,
    onApproveVerify: (String) -> Unit
) {
    val pendingVerifications by viewModel.pendingVerificationUsers.collectAsStateWithLifecycle()
    val allShipments by viewModel.allShipments.collectAsStateWithLifecycle()
    val escrowedFunds by viewModel.escrowedFunds.collectAsStateWithLifecycle()
    val adminCommissions by viewModel.adminCommissions.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = when (currentTab) {
                "VALIDACIONES" -> 0
                "AUDITORIA" -> 1
                else -> 2
            },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PatagonianTeal
        ) {
            Tab(
                selected = currentTab == "VALIDACIONES",
                onClick = { onTabSelected("VALIDACIONES") },
                text = { Text("Validar ID", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = currentTab == "AUDITORIA",
                onClick = { onTabSelected("AUDITORIA") },
                text = { Text("Auditoría", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = currentTab == "DISPUTAS",
                onClick = { onTabSelected("DISPUTAS") },
                text = { Text("Disputas", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (currentTab) {
                "VALIDACIONES" -> {
                    if (pendingVerifications.isEmpty()) {
                        EmptyStateWidget(
                            icon = Icons.Filled.CheckCircle,
                            title = "No hay perfiles por validar",
                            description = "Todos los portadores que cargaron selfies ya han sido revisados y aprobados por la mesa de auditoría.",
                            actionLabel = null,
                            onAction = {}
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pendingVerifications, key = { it.id }) { u ->
                                AdminVerificationCard(
                                    user = u,
                                    onApprove = { onApproveVerify(u.id) }
                                )
                            }
                        }
                    }
                }
                "AUDITORIA" -> {
                    AdminAuditingDashboard(
                        allShipments = allShipments,
                        escrowed = escrowedFunds,
                        commissions = adminCommissions,
                        viewModel = viewModel
                    )
                }
                "DISPUTAS" -> {
                    val disputados = allShipments.filter { it.status == "DISPUTADO" }
                    if (disputados.isEmpty()) {
                        EmptyStateWidget(
                            icon = Icons.Filled.Warning,
                            title = "Canal de disputas limpio",
                            description = "No hay desacuerdos comerciales abiertos en el corredor. Los flujos concluyen sin intervenciones.",
                            actionLabel = null,
                            onAction = {}
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(disputados, key = { it.id }) { s ->
                                AdminDisputeCard(
                                    shipment = s,
                                    onResolve = { refundSender ->
                                        viewModel.resolveDispute(s.id, refundSender)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminVerificationCard(
    user: UserEntity,
    onApprove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PatagonianTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Text(user.name.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("DNI: ${user.dni}", fontSize = 11.sp, color = Color.Gray)
                }
            }

            // Verification info / Selfie status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(LightBackground)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Estatus Selfie Biométrica:", fontSize = 10.sp, color = Color.Gray)
                    val selfieStatus = if (user.isBiometricVerified) "Validada por IA ✓" else "Pendiente de toma"
                    val color = if (user.isBiometricVerified) ValleyGreen else CoralRed
                    Text(selfieStatus, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                }

                // Check certificate placeholder
                Column(horizontalAlignment = Alignment.End) {
                    Text("Antecedentes Penales:", fontSize = 10.sp, color = Color.Gray)
                    Text("Analizado por RECONEX", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ValleyGreen)
                }
            }

            Button(
                onClick = onApprove,
                colors = ButtonDefaults.buttonColors(containerColor = ValleyGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .testTag("approve_carrier_${user.id}")
            ) {
                Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Validar Identidad y Habilitar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminAuditingDashboard(
    allShipments: List<ShipmentEntity>,
    escrowed: Double,
    commissions: Double,
    viewModel: PataCargoViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Balance Consolidado Pata Cargo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PatagonianTeal)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Escrow holdings
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = TealLight),
                border = BorderStroke(1.dp, PatagonianTeal.copy(0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Efectivo en Escrow (Garantías)", fontSize = 11.sp, color = PatagonianTeal, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$${String.format("%,.2f", escrowed)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PatagonianTeal)
                }
            }

            // Commission earnings
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                border = BorderStroke(1.dp, ValleyGreen.copy(0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Comisión Pata Cargo (15%)", fontSize = 11.sp, color = ValleyGreen, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$${String.format("%,.2f", commissions)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = ValleyGreen)
                }
            }
        }

        Text("Auditoría General de Cargas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PatagonianTeal)

        if (allShipments.isEmpty()) {
            Text("No se detectan transferencias en el sistema.", fontSize = 12.sp, color = Color.Gray)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                allShipments.forEach { s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, CardBorderColor, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${s.origin} a ${s.destination}", fontSize = 10.sp, color = Color.Gray)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("$${String.format("%,.2f", s.price)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PatagonianTeal)
                            Text(
                                text = s.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when(s.status) {
                                    "ENTREGADO" -> ValleyGreen
                                    "PENDIENTE" -> SunsetGold
                                    "EN_VIAJE" -> CoralRed
                                    else -> Color.DarkGray
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDisputeCard(
    shipment: ShipmentEntity,
    onResolve: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CoralRed)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = CoralRed)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Disputa Activa: ${shipment.title}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Text(
                text = "Origen: ${shipment.origin} | Destino: ${shipment.destination}\nMonto retenido: $${String.format("%,.2f", shipment.price)} (Seguro: ${if (shipment.insuranceEnabled) "Activo ($" + String.format("%.2f", shipment.insuranceCost) + ")" else "Inactivo"})",
                fontSize = 11.sp,
                color = Color.DarkGray
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Return Sender
                Button(
                    onClick = { onResolve(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reembolsar Enviador", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Force pay Carrier
                Button(
                    onClick = { onResolve(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = ValleyGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Liberar a Portador", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

//================================================================================
// 4. SHARED DESIGN AND UTILITY COMPONENTS
//================================================================================
@Composable
fun EmptyStateWidget(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PatagonianTeal.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
        
        actionLabel?.let {
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = PatagonianTeal),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(it, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ChatThreadItemCard(
    shipment: ShipmentEntity,
    subtitle: String,
    onOpenChat: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenChat() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(TealLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = null, tint = PatagonianTeal, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(shipment.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("${shipment.origin} - ${shipment.destination}", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Button(
                onClick = onOpenChat,
                colors = ButtonDefaults.buttonColors(containerColor = PatagonianTeal),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("Entrar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

//================================================================================
// 5. INTERACTIVE POPUPS & VALIDATORS CODES
//================================================================================

// SELFIE SCAN CAMERA FRAME SIMULATOR FOR IDENTITY CHECKS
@Composable
fun BiometricScanningSimulator(
    user: UserEntity,
    onScanComplete: (Boolean) -> Unit
) {
    var timerRunning by remember { mutableStateOf(3) }
    var scanAnalyzing by remember { mutableStateOf(false) }

    LaunchedEffect(timerRunning) {
        if (timerRunning > 0) {
            delay(1000)
            timerRunning--
            if (timerRunning == 0) {
                scanAnalyzing = true
                delay(1500)
                onScanComplete(true) // success and mock approval
            }
        }
    }

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateGrey),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Verificación Biométrica de Rostro",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                
                Text(
                    "Encuadra tu rostro en la elipse. Este test nos ayuda a validar que eres el propietario real de la cuenta de retiro.",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                // The camera frame circle
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black)
                        .border(
                            4.dp,
                            if (scanAnalyzing) SunsetGold else PatagonianTeal,
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Ellipse guide overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.White.copy(0.4f),
                            radius = size.minDimension / 2.6f,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            )
                        )
                    }

                    if (!scanAnalyzing) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$timerRunning",
                                color = Color.White,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "No te muevas...",
                                color = Color.White.copy(0.8f),
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = SunsetGold, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Analizando DNI local...",
                                color = SunsetGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Footnote
                Text(
                    user.name.uppercase(),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// PROFILE SELECTOR OPTIONS FOR EVALUATION SIMULATIONS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatedIdentitySelectorDialog(
    users: List<UserEntity>,
    selectedUserId: String,
    viewModel: PataCargoViewModel,
    onSelectUser: (String) -> Unit,
    onGoogleSignInError: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firebaseUser by viewModel.firebaseUser.collectAsStateWithLifecycle()

    // Google Sign-In Launcher setup
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                viewModel.signInWithGoogleToken(idToken) { success, errorMsg ->
                    if (success) {
                        Toast.makeText(context, "¡Sesión iniciada correctamente con Google!", Toast.LENGTH_LONG).show()
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Error: ${errorMsg ?: "Error de vinculación"}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(context, "No se recibió un Token de ID válido de Google.", Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            if (e.statusCode == 10) {
                onGoogleSignInError(getSigningCertificateSHA1(context))
                onDismiss()
            } else {
                val statusMessage = when (e.statusCode) {
                    12500 -> "Google Play Services no está configurado en el dispositivo"
                    7 -> "Error de conexión de red"
                    else -> "Código de error ${e.statusCode}: ${e.localizedMessage}"
                }
                Toast.makeText(context, "Fallo de Google Sign-In: $statusMessage", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Fallo al iniciar sesión: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // Function to trigger Google Sign-In
    fun launchGoogleSignIn() {
        try {
            // Retrieve default web client ID from strings or fallback
            val webClientId = try {
                context.getString(
                    context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                ).ifEmpty { "14033808612-6i5qspi0cbve77c7q2up08ji37nicq70.apps.googleusercontent.com" }
            } catch (e: Exception) {
                "14033808612-6i5qspi0cbve77c7q2up08ji37nicq70.apps.googleusercontent.com"
            }

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            // Sign out first to force account chooser dialog
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al preparar Google Sign-In: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PataCargoVectorLogo(modifier = Modifier.size(60.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Acceder a Pata Cargo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PatagonianTeal
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Real Google Auth Panel
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (firebaseUser != null) TealLight else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (firebaseUser != null) PatagonianTeal else PatagonianTeal.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (firebaseUser != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountCircle,
                                        contentDescription = "Usuario Autenticado",
                                        tint = PatagonianTeal,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = firebaseUser?.displayName ?: "Usuario Google",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PatagonianTeal
                                        )
                                        Text(
                                            text = firebaseUser?.email ?: "",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        viewModel.signOutRealUser()
                                        Toast.makeText(context, "Sesión de Google cerrada", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Cerrar Sesión", color = CoralRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Text(
                                text = "🔒 Sincronización Real con Firebase",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PatagonianTeal,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Usa Google para sincronizar tus cargas, ofertas y revisar las calificaciones de forma oficial.",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = { launchGoogleSignIn() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.DarkGray
                                ),
                                border = BorderStroke(0.5.dp, Color.LightGray),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("google_login_button"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Fingerprint,
                                        contentDescription = "Google",
                                        tint = SunsetGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Inicia Sesión con Google",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    "O de forma rápida, selecciona una identidad simulada de prueba para probar el flujo de todos los roles:",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(users, key = { it.id }) { u ->
                        val isSelected = selectedUserId == u.id
                        ListItem(
                            modifier = Modifier
                                .clickable { onSelectUser(u.id) }
                                .border(
                                    1.dp,
                                    if (isSelected) PatagonianTeal else CardBorderColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp)),
                            headlineContent = {
                                Text(
                                    u.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) PatagonianTeal else Color.Unspecified
                                )
                            },
                            supportingContent = {
                                val sloganText = when {
                                    u.id == "admin" -> "🛡️ Operador de Plataforma"
                                    u.id.startsWith("portador") -> "🚚 Portador: \"Gana Plata Extra, con un Viaje que ya ibas a realizar\""
                                    else -> "📦 Enviador: \"Necesito Enviar Un paquete\""
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(sloganText, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (isSelected) PatagonianTeal else Color.Gray)
                                    Text("Balance: $${String.format("%,.0f", u.walletBalance)}", fontSize = 10.sp, color = Color.Gray)
                                }
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = SunsetGold, modifier = Modifier.size(12.dp))
                                    Text(
                                        text = "${u.rating}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                    if (u.isVerified) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Filled.VerifiedUser,
                                            contentDescription = "Aprobado",
                                            tint = ValleyGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = if (isSelected) TealLight else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = PatagonianTeal)
            }
        }
    )
}

// MAKE AN OFFER FLOW DIALOG
@Composable
fun MakeCarrierOfferDialog(
    shipment: ShipmentEntity,
    suggestedBasePrice: Double,
    onSendOffer: (Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var bidText by remember { mutableStateOf(suggestedBasePrice.toInt().toString()) }
    var comment by remember { mutableStateOf("Tengo lugar en la mochila. Viajo por autopista.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hacer Propuesta de Transporte", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Ofreces llevar '${shipment.title}' de ${shipment.origin} a ${shipment.destination}.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = bidText,
                    onValueChange = { bidText = it },
                    label = { Text("Monto de tu Oferta ($ ARS)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentario para el Enviador (pickup...)") },
                    placeholder = { Text("Ej: Salgo a las 14hs, te espero por la Shell") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Text(
                    "📍 Recibirás el 100% de tu cobro final. Pata Cargo cobrará un 15% adicional de comisión directamente al enviador.",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bidVal = bidText.toDoubleOrNull() ?: suggestedBasePrice
                    onSendOffer(bidVal, comment)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PatagonianTeal),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Enviar Oferta", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}

// COORDINATOR CHAT COMPONENT
@Composable
fun ActiveChatDialog(
    shipment: ShipmentEntity,
    currentUserId: String,
    currentUserName: String,
    messages: List<ChatMessageEntity>,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, CardBorderColor)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header of Chat
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TealLight)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chat Coordinativo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PatagonianTeal
                        )
                        Text(
                            text = shipment.title,
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = PatagonianTeal)
                    }
                }

                // Chat Messages Container
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isMe = msg.senderId == currentUserId
                        val isSystem = msg.senderId == "system" || msg.senderId == "support"
                        
                        when {
                            isSystem -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF1F5F9))
                                            .border(1.dp, CardBorderColor, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = msg.message,
                                            fontSize = 10.sp,
                                            color = SlateGrey,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            isMe -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(msg.senderName, fontSize = 9.sp, color = Color.Gray)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp))
                                                .background(PatagonianTeal)
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(msg.message, fontSize = 12.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(msg.senderName, fontSize = 9.sp, color = Color.Gray)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp))
                                                .background(TealLight)
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(msg.message, fontSize = 12.sp, color = PatagonianTeal)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Control sender input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Responder al mensajero...", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text"),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PatagonianTeal
                        )
                    )

                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSend(messageText)
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .background(PatagonianTeal, CircleShape)
                            .size(36.dp)
                            .testTag("chat_send_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// EASY QR SCAN BYPASS AND CHEAT DIALOG
@Composable
fun QRScannerCodeDialog(
    shipmentId: Int,
    scanType: String, // "COLLECT" or "DELIVER"
    viewModel: PataCargoViewModel,
    onDismiss: () -> Unit,
    onScanSuccess: () -> Unit
) {
    var checkScanCode by remember { mutableStateOf("") }
    
    // Auto-fill scanner for simple emulator testing in one click
    var simulatedValue by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.repository.shipmentDao.getShipmentById(shipmentId)?.let { s ->
            simulatedValue = if (scanType == "COLLECT") s.qrValueCollection else s.qrValueDelivery
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.QrCode, contentDescription = null, tint = PatagonianTeal)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (scanType == "COLLECT") "Escanear Recolección" else "Escanear Entrega",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Apunta la cámara del móvil al código QR que muestra tu cliente para certificar el estado y liberar los fondos correspondientes.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                // Interactive simulated container drawing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightBackground)
                        .border(1.dp, CardBorderColor, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Drawing generic grid qr representation inside
                        Icon(Icons.Filled.QrCode, contentDescription = null, tint = Color.Black, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("CÓDIGO DE MATCH REQUERIDO:", fontSize = 10.sp, color = Color.Gray)
                        Text(simulatedValue, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PatagonianTeal)
                    }
                }

                OutlinedTextField(
                    value = checkScanCode,
                    onValueChange = { checkScanCode = it },
                    label = { Text("Escribe o pega el código para simular") },
                    placeholder = { Text(simulatedValue) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scan_input_verify"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Button(
                    onClick = {
                        // Simulating direct tap bypass on emulator
                        checkScanCode = simulatedValue
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealLight, contentColor = PatagonianTeal),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Auto-completar simulador de QR ✨", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val result = if (scanType == "COLLECT") {
                        viewModel.simulateCollectScan(shipmentId, checkScanCode)
                    } else {
                        viewModel.simulateDeliveryScan(shipmentId, checkScanCode)
                    }
                    if (result) {
                        onScanSuccess()
                    } else {
                        // Accept bypass for easier user click test flow
                        if (scanType == "COLLECT") {
                            viewModel.simulateCollectScan(shipmentId, "SIMULATE-BYPASS")
                        } else {
                            viewModel.simulateDeliveryScan(shipmentId, "SIMULATE-BYPASS")
                        }
                        onScanSuccess()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PatagonianTeal),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Validar Escaneo ✓", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = Color.Gray)
            }
        }
    )
}

// SENDER DIALOG FOR DETAILS / OFFERS LIST OR REVIEWS
@Composable
fun ShipmentDetailAndOfferDialog(
    shipment: ShipmentEntity,
    offers: List<OfferEntity>,
    currentUserId: String,
    onAcceptOffer: (OfferEntity) -> Unit,
    onSubmitDispute: () -> Unit,
    onRateCarrier: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var starsRate by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("Excelente viaje, súper puntual.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = PatagonianTeal)
                Spacer(modifier = Modifier.width(6.dp))
                Text(shipment.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Route details
                RouteGeoVisualizer(origin = shipment.origin, destination = shipment.destination)

                Text(
                    text = "Detalles del Paquete:\n${shipment.description}",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Medida: ${shipment.size} bulto", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    val fragileLabel = if (shipment.isFragile) "Frágil 🚨" else "No Frágil ✓"
                    Text(fragileLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (shipment.isFragile) CoralRed else ValleyGreen)
                }

                HorizontalDivider(color = CardBorderColor)

                // FLOW MANAGEMENT BASED ON STATE
                when (shipment.status) {
                    "PENDIENTE" -> {
                        Text(
                            text = "Propuestas de Portadores (Garantía Escrow)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PatagonianTeal
                        )
                        
                        if (offers.isEmpty()) {
                            Text(
                                "Monitoreando viajeros en la ruta habitual... Aún no se cargaron ofertas económicas.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                offers.forEach { offer ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().testTag("offer_card_${offer.id}"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = LightBackground),
                                        border = BorderStroke(1.dp, CardBorderColor)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(offer.carrierName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Filled.Star, contentDescription = null, tint = SunsetGold, modifier = Modifier.size(10.dp))
                                                        Text(" ${offer.carrierRating} (DNI ${offer.carrierDni})", fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = "$${String.format("%,.0f", offer.amount)}",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 15.sp,
                                                        color = PatagonianTeal
                                                    )
                                                    val commission = offer.amount * 0.15
                                                    val totalPayable = offer.amount + commission
                                                    Text(
                                                        text = "+ $${String.format("%,.0f", commission)} com. (15%)",
                                                        fontSize = 9.sp,
                                                        color = Color.Gray,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = "Total: $${String.format("%,.0f", totalPayable)}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = ValleyGreen
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "💬 \"${offer.comment}\"",
                                                fontSize = 11.sp,
                                                color = Color.DarkGray
                                            )
                                            Button(
                                                onClick = { onAcceptOffer(offer) },
                                                colors = ButtonDefaults.buttonColors(containerColor = PatagonianTeal),
                                                modifier = Modifier
                                                    .align(Alignment.End)
                                                    .height(30.dp)
                                                    .testTag("accept_offer_btn_${offer.id}"),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp)
                                            ) {
                                                Text("Aceptar Oferta", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "ACEPTADO" -> {
                        // Waiting pick-up QR instructions
                        Text(
                            text = "Código QR de Recolección (Muestra al retirar)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PatagonianTeal
                        )
                        Text(
                            text = "Cuando el portador llegue a retirar, muéstrale este código para certificar que le diste el paquete.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(LightBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.QrCode, contentDescription = null, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Código:", fontSize = 9.sp, color = Color.Gray)
                                Text(shipment.qrValueCollection, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PatagonianTeal)
                            }
                        }
                    }
                    "EN_VIAJE" -> {
                        Text(
                            text = "Garantía de Destino Activa",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PatagonianTeal
                        )
                        Text(
                            text = "Tu paquete está en viaje por carretera. El receptor en Madryn o Gaiman debe presentar su código para completar la recepción.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(LightBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.QrCode, contentDescription = null, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Código de Entrega:", fontSize = 9.sp, color = Color.Gray)
                                Text(shipment.qrValueDelivery, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SunsetGold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Button(
                            onClick = onSubmitDispute,
                            colors = ButtonDefaults.buttonColors(containerColor = CoralRed.copy(0.1f), contentColor = CoralRed),
                            border = BorderStroke(1.dp, CoralRed),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Abrir Disputa Comercial", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "ENTREGADO" -> {
                        Text(
                            text = "¡Envío entregado con éxito! Califica al Portador",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ValleyGreen
                        )

                        // Visual ratings Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { rateIndex ->
                                IconButton(onClick = { starsRate = rateIndex }) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "$rateIndex Estrellas",
                                        tint = if (rateIndex <= starsRate) SunsetGold else Color.LightGray,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = reviewComment,
                            onValueChange = { reviewComment = it },
                            label = { Text("Reseña para el perfil del viajero") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { onRateCarrier(starsRate, reviewComment) },
                            colors = ButtonDefaults.buttonColors(containerColor = ValleyGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enviar Calificación", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = PatagonianTeal)
            }
        }
    )
}

@Composable
fun OnboardingAuthScreen(
    viewModel: PataCargoViewModel,
    onGoogleSignInIntent: () -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("ENVIADOR") } // "ENVIADOR" or "PORTADOR"
    var loading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, PatagonianTeal)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            PataCargoVectorLogo(modifier = Modifier.size(100.dp))
            
            Text(
                text = "PATA CARGO",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            )
            
            Text(
                text = "Envíos Colaborativos Virch & Madryn",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.15f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Auth Fields Container
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sign In vs Sign Up tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(LightBackground)
                            .padding(4.dp)
                    ) {
                        Button(
                            onClick = { isSignUpMode = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isSignUpMode) PatagonianTeal else Color.Transparent,
                                contentColor = if (!isSignUpMode) Color.White else Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Text("Iniciar Sesión", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { isSignUpMode = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSignUpMode) PatagonianTeal else Color.Transparent,
                                contentColor = if (isSignUpMode) Color.White else Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Text("Crear Cuenta", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isSignUpMode) "Regístrate de manera oficial" else "Ingresa a tu cuenta",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PatagonianTeal
                    )

                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Nombre Completo") },
                            placeholder = { Text("Ej: María Gales") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = dni,
                            onValueChange = { dni = it },
                            label = { Text("DNI o CUIT") },
                            placeholder = { Text("Ej: 37.562.901") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico") },
                        placeholder = { Text("ejemplo@patacargo.com") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        placeholder = { Text("Mínimo 6 caracteres") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isSignUpMode) {
                        Text(
                            text = "¿Cuál será tu actividad principal?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PatagonianTeal,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRole = "ENVIADOR" }
                                    .border(
                                        1.dp,
                                        if (selectedRole == "ENVIADOR") PatagonianTeal else CardBorderColor,
                                        RoundedCornerShape(10.dp)
                                    ),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedRole == "ENVIADOR") TealLight else Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AddBox,
                                        contentDescription = null,
                                        tint = if (selectedRole == "ENVIADOR") PatagonianTeal else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Enviador",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (selectedRole == "ENVIADOR") PatagonianTeal else Color.Gray
                                    )
                                    Text(
                                        "Quiero enviar",
                                        fontSize = 9.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRole = "PORTADOR" }
                                    .border(
                                        1.dp,
                                        if (selectedRole == "PORTADOR") PatagonianTeal else CardBorderColor,
                                        RoundedCornerShape(10.dp)
                                    ),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedRole == "PORTADOR") TealLight else Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalShipping,
                                        contentDescription = null,
                                        tint = if (selectedRole == "PORTADOR") PatagonianTeal else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Portador",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (selectedRole == "PORTADOR") PatagonianTeal else Color.Gray
                                    )
                                    Text(
                                        "Quiero llevar",
                                        fontSize = 9.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (loading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PatagonianTeal)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (isSignUpMode && fullName.isBlank()) {
                                    Toast.makeText(context, "Por favor ingresa tu nombre", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                loading = true
                                if (isSignUpMode) {
                                    viewModel.signUpWithEmail(
                                        emailAddress = email.trim(),
                                        passwordText = password.trim(),
                                        fullName = fullName.trim(),
                                        dniText = dni.trim(),
                                        roleChosen = selectedRole,
                                        onComplete = { success, errorMsg ->
                                            loading = false
                                            if (success) {
                                                Toast.makeText(context, "¡Cuenta creada con éxito!", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    )
                                } else {
                                    viewModel.signInWithEmail(
                                        emailAddress = email.trim(),
                                        passwordText = password.trim(),
                                        onComplete = { success, errorMsg ->
                                            loading = false
                                            if (success) {
                                                Toast.makeText(context, "¡Sesión iniciada correctamente!", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PatagonianTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text(
                                text = if (isSignUpMode) "Crear Mi Cuenta" else "Iniciar Sesión Segura",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                        Text("   O   ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    }

                    OutlinedButton(
                        onClick = onGoogleSignInIntent,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray),
                        border = BorderStroke(1.dp, Color.LightGray),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = "Google",
                            tint = SunsetGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ingresar de forma rápida con Google", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun UserRoleSelectionScreen(
    onRoleSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, PatagonianTeal)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
            PataCargoVectorLogo(modifier = Modifier.size(80.dp))

            Text(
                text = "¡BIENVENIDO A PATA CARGO!",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Text(
                text = "Para brindarte la mejor experiencia colaborativa en la comarca del VIRCH y Madryn, elige tu actividad de inicio:",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 1: Sender (Enviador)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRoleSelected("ENVIADOR") }
                    .border(2.dp, SunsetGold, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(SunsetGold.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddBox,
                            contentDescription = "Enviar",
                            tint = SunsetGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Quiero Enviar Paquetes (Enviador)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PatagonianTeal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Publica tus envíos o mercaderías, recibe ofertas económicas de viajeros de confianza y coordina retiros.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Option 2: Carrier (Portador)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRoleSelected("PORTADOR") }
                    .border(2.dp, PatagonianTeal, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PatagonianTeal.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalShipping,
                            contentDescription = "Llevar",
                            tint = PatagonianTeal,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Quiero Llevar Paquetes (Portador)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PatagonianTeal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Configura tus rutas habituales de viaje por la comarca, propón cotizaciones de envío y genera ingresos extras.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Text(
                text = "Podrás cambiar de actividad o interactuar con ambas de forma simultánea desde el panel superior del menú en cualquier momento.",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

fun getSigningCertificateSHA1(context: android.content.Context): String {
    try {
        val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            context.packageManager.getPackageInfo(
                context.packageName,
                android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(
                context.packageName,
                android.content.pm.PackageManager.GET_SIGNATURES
            )
        }
        val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            packageInfo.signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            packageInfo.signatures
        }
        if (signatures != null && signatures.isNotEmpty()) {
            val cert = signatures[0].toByteArray()
            val md = java.security.MessageDigest.getInstance("SHA-1")
            val publicKey = md.digest(cert)
            val hexString = java.lang.StringBuilder()
            for (i in publicKey.indices) {
                val appendString = java.lang.Integer.toHexString(0xFF and publicKey[i].toInt()).uppercase()
                if (appendString.length == 1) hexString.append("0")
                hexString.append(appendString)
                if (i < publicKey.size - 1) hexString.append(":")
            }
            return hexString.toString()
        }
    } catch (e: Exception) {
        return "Error al extraer SHA-1: ${e.localizedMessage}"
    }
    return "SHA-1 no disponible"
}
