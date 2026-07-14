package com.patacargo.virchm.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.patacargo.virchm.data.*
import com.patacargo.virchm.ui.PataCargoViewModel
import com.patacargo.virchm.ui.components.*
import com.patacargo.virchm.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BiometricScanningSimulator(
    user: UserEntity,
    onScanComplete: (Boolean) -> Unit
) {
    var timerRunning by remember { mutableIntStateOf(3) }
    var scanAnalyzing by remember { mutableStateOf(false) }

    LaunchedEffect(timerRunning) {
        if (timerRunning > 0) {
            delay(1000)
            timerRunning--
            if (timerRunning == 0) {
                scanAnalyzing = true
                delay(1500)
                onScanComplete(true)
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
    val firebaseUser by viewModel.firebaseUser.collectAsStateWithLifecycle()

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
                Toast.makeText(context, "Fallo de Google Sign-In: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Fallo al iniciar sesión: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun launchGoogleSignIn() {
        try {
            val webClientId = try {
                context.getString(
                    context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                ).ifEmpty { "14033808612-6i5qspi0cbve77c7q2up08ji37nicq70.apps.googleusercontent.com" }
            } catch (e: Exception) {
                "14033808612-6i5qspi0cbve77c7q2up08ji37nicq70.apps.googleusercontent.com"
            }

            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(context, gso)
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
                                    }
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
                                    .testTag("google_login_button")
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
                    "O de forma rápida, selecciona una identidad simulada de prueba:",
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
                                    u.id.startsWith("portador") -> "🚚 Portador"
                                    else -> "📦 Enviador"
                                }
                                Text(sloganText, fontSize = 11.sp, color = if (isSelected) PatagonianTeal else Color.Gray)
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
                    label = { Text("Comentario para el Enviador") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Text(
                    "📍 Recibirás el 100% de tu cobro final. Pata Cargo cobrará un 15% adicional al enviador.",
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
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF1F5F9))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = msg.message, fontSize = 10.sp, color = SlateGrey)
                                    }
                                }
                            }
                            isMe -> {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
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
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
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

                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Responder...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).testTag("chat_input_text"),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSend(messageText)
                                messageText = ""
                            }
                        },
                        modifier = Modifier.background(PatagonianTeal, CircleShape).size(36.dp).testTag("chat_send_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QRScannerCodeDialog(
    shipmentId: Int,
    scanType: String,
    viewModel: PataCargoViewModel,
    onDismiss: () -> Unit,
    onScanSuccess: () -> Unit
) {
    var checkScanCode by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasCameraPermission = isGranted
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Security, contentDescription = null, tint = PatagonianTeal)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (scanType == "COLLECT") "Certificar Entrega en Origen" else "Certificar Recepción en Destino",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Verifica la transacción de dos formas posibles:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "1. Escanea el código QR.\n2. O ingresa la Palabra Clave.",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )

                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, PatagonianTeal, RoundedCornerShape(12.dp))
                    ) {
                        CameraQRScannerView(
                            onQRCodeScanned = { scannedText ->
                                checkScanCode = scannedText
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text("Activar Cámara")
                    }
                }

                OutlinedTextField(
                    value = checkScanCode,
                    onValueChange = { checkScanCode = it },
                    label = { Text("Palabra clave o QR") },
                    modifier = Modifier.fillMaxWidth().testTag("scan_input_verify"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (checkScanCode.isBlank()) return@Button
                    val callback: (Boolean) -> Unit = { if (it) onScanSuccess() }
                    if (scanType == "COLLECT") viewModel.simulateCollectScan(shipmentId, checkScanCode.trim(), callback)
                    else viewModel.simulateDeliveryScan(shipmentId, checkScanCode.trim(), callback)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PatagonianTeal)
            ) {
                Text("Validar ✓", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

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
    var starsRate by remember { mutableIntStateOf(5) }
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
                LogisticTimelineStepper(shipment = shipment)
                RouteGeoVisualizer(origin = shipment.origin, destination = shipment.destination)
                Text(text = "Detalles: ${shipment.description}", fontSize = 11.sp)

                when (shipment.status) {
                    "PENDIENTE" -> {
                        if (offers.isEmpty()) {
                            Text("Sin ofertas aún.", fontSize = 11.sp, color = Color.Gray)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                offers.forEach { offer ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        border = BorderStroke(1.dp, CardBorderColor)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(offer.carrierName, fontWeight = FontWeight.Bold)
                                            Text("$${String.format("%,.0f", offer.amount)}", color = PatagonianTeal)
                                            Button(onClick = { onAcceptOffer(offer) }) {
                                                Text("Aceptar")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "ACEPTADO" -> {
                        QRCodeDisplay(text = shipment.qrValueCollection, modifier = Modifier.size(100.dp))
                        Text("Palabra clave: ${shipment.qrValueCollection.removePrefix("COLL-")}")
                    }
                    "EN_VIAJE" -> {
                        QRCodeDisplay(text = shipment.qrValueDelivery, modifier = Modifier.size(100.dp))
                        Text("Palabra clave receptor: ${shipment.qrValueDelivery.removePrefix("DELI-")}")
                        Button(onClick = onSubmitDispute, colors = ButtonDefaults.buttonColors(containerColor = CoralRed)) {
                            Text("Abrir Disputa")
                        }
                    }
                    "ENTREGADO" -> {
                        Text("¡Entregado! Califica:", color = ValleyGreen)
                        Row {
                            (1..5).forEach { i ->
                                IconButton(onClick = { starsRate = i }) {
                                    Icon(Icons.Filled.Star, null, tint = if (i <= starsRate) SunsetGold else Color.LightGray)
                                }
                            }
                        }
                        OutlinedTextField(value = reviewComment, onValueChange = { reviewComment = it })
                        Button(onClick = { onRateCarrier(starsRate, reviewComment) }) {
                            Text("Enviar Calificación")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
