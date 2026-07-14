package com.patacargo.virchm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.patacargo.virchm.data.ShipmentEntity
import com.patacargo.virchm.data.UserEntity
import com.patacargo.virchm.ui.PataCargoViewModel
import com.patacargo.virchm.ui.components.*
import com.patacargo.virchm.ui.theme.*

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
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
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
            val user = currentUser
            when (currentTab) {
                "BUSCADOR" -> {
                    if (user != null && !user.isMercadoPagoConnected) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            MercadoPagoConnectionForm(viewModel = viewModel, userId = user.id)
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
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
                                        viewModel = viewModel,
                                        onSelected = { onPlaceOfferClicked(s) }
                                    )
                                }
                            }
                            HorizontalDivider(color = CardBorderColor)
                        }

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
                                        viewModel = viewModel,
                                        onSelected = { onPlaceOfferClicked(s) }
                                    )
                                }
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
                                    viewModel = viewModel,
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
    viewModel: PataCargoViewModel,
    onSelected: () -> Unit
) {
    val senderState = produceState<UserEntity?>(initialValue = null, key1 = shipment.senderId) {
        value = viewModel.repository.userDao.getUserById(shipment.senderId)
    }
    val completedCountState = produceState(initialValue = 0, key1 = shipment.senderId) {
        value = viewModel.repository.shipmentDao.getCompletedCountForSender(shipment.senderId)
    }

    val sender = senderState.value
    val completedCount = completedCountState.value

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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(13.dp))
                Text(
                    text = "Enviador: ${sender?.name ?: "Cargador Comuna"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Filled.Star, contentDescription = null, tint = SunsetGold, modifier = Modifier.size(13.dp))
                val ratingStr = if (sender != null && sender.rating > 0f) String.format("%.1f", sender.rating) else "5.0"
                Text(
                    text = ratingStr,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SunsetGold
                )

                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ValleyGreen.copy(0.12f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$completedCount exitosos ✓",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ValleyGreen
                    )
                }
            }

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
    viewModel: PataCargoViewModel,
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
                        "PAGO_PENDIENTE" -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SunsetGold.copy(0.1f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Esperando pago de enviador...",
                                    fontSize = 11.sp,
                                    color = SunsetGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = TealLight.copy(0.4f)),
                                    border = BorderStroke(1.dp, PatagonianTeal.copy(0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Navigation, contentDescription = null, tint = PatagonianTeal, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Ubicación Reportada Activa", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PatagonianTeal)
                                        }
                                        
                                        val lastLoc = if (shipment.lastCheckpoint.isEmpty()) "Sin reportes aún (En ruta de origen)" else shipment.lastCheckpoint
                                        Text(
                                            text = lastLoc,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))
                                        
                                        var showCheckpointDialog by remember { mutableStateOf(false) }
                                        
                                        Button(
                                            onClick = { showCheckpointDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PatagonianTeal),
                                            border = BorderStroke(1.dp, PatagonianTeal),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(11.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Actualizar ubicación", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        if (showCheckpointDialog) {
                                            var checkpointInput by remember { mutableStateOf("") }
                                            AlertDialog(
                                                onDismissRequest = { showCheckpointDialog = false },
                                                title = { Text("Reportar Ubicación de Tránsito", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                                                text = {
                                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Text("Describe tu hito o posición del viaje por carretera (ej: 'Saliendo de Trelew por Ruta 3' o 'Pasando por Gaiman'). Los enviadores lo verán al instante.", fontSize = 11.sp, color = Color.Gray)
                                                        OutlinedTextField(
                                                            value = checkpointInput,
                                                            onValueChange = { checkpointInput = it },
                                                            placeholder = { Text("Ej: Pasando el empalme de Ruta 3 y 40") },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                    }
                                                },
                                                confirmButton = {
                                                    Button(
                                                        onClick = {
                                                            if (checkpointInput.trim().isNotEmpty()) {
                                                                viewModel.updateShipmentCheckpoint(shipment.id, checkpointInput.trim())
                                                            }
                                                            showCheckpointDialog = false
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = PatagonianTeal),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("Actualizar", fontWeight = FontWeight.Bold)
                                                    }
                                                },
                                                dismissButton = {
                                                    TextButton(onClick = { showCheckpointDialog = false }) {
                                                        Text("Cancelar", color = Color.Gray)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

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
            }

            if (shipment.status == "ENTREGADO") {
                var showRateSenderDialog by remember { mutableStateOf(false) }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { showRateSenderDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PatagonianTeal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = SunsetGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Calificar al Enviador ⭐", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (showRateSenderDialog) {
                    var stars by remember { mutableIntStateOf(5) }
                    var comment by remember { mutableStateOf("Excelente comunicación, muy amable y puntual.") }
                    val context = LocalContext.current

                    AlertDialog(
                        onDismissRequest = { showRateSenderDialog = false },
                        title = { Text("Calificar al Enviador de Carga", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Aporta una valoración honesta sobre este cliente para cuidar la reputación de la comarca.", fontSize = 11.sp, color = Color.Gray)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    (1..5).forEach { index ->
                                        IconButton(onClick = { stars = index }) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "$index Estrellas",
                                                tint = if (index <= stars) SunsetGold else Color.LightGray,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = comment,
                                    onValueChange = { comment = it },
                                    label = { Text("Comentario/Reseña del enviador") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.submitReview(
                                        shipmentId = shipment.id,
                                        rating = stars,
                                        comment = comment,
                                        writerId = shipment.carrierId ?: "",
                                        targetId = shipment.senderId
                                    )
                                    showRateSenderDialog = false
                                    Toast.makeText(context, "¡Calificación de Enviador registrada!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ValleyGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Guardar Calificación", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRateSenderDialog = false }) {
                                Text("Cancelar", color = Color.Gray)
                            }
                        }
                    )
                }
            }

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

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, CardBorderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Sumar Tramo Habitual", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
