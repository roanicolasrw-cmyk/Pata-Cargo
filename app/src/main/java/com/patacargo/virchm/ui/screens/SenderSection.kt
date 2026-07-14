package com.patacargo.virchm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patacargo.virchm.data.ShipmentEntity
import com.patacargo.virchm.ui.PataCargoViewModel
import com.patacargo.virchm.ui.components.*
import com.patacargo.virchm.ui.theme.*

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
                                    viewModel = viewModel,
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
    viewModel: PataCargoViewModel,
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
                        text = "Medida: ${shipment.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PatagonianTeal
                    )
                }
                
                StatusLabelBadge(status = if (shipment.mpPaymentStatus == "PENDIENTE") "PAGO_PENDIENTE" else shipment.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = shipment.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            RouteGeoVisualizer(origin = shipment.origin, destination = shipment.destination)

            Spacer(modifier = Modifier.height(8.dp))

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

            if (shipment.mpPaymentStatus == "PENDIENTE" && shipment.carrierId != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SunsetGold.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, SunsetGold.copy(0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = SunsetGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "RESERVA ESCROW PATA CARGO REQUERIDA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SunsetGold
                            )
                        }
                        Text(
                            "Has seleccionado un viajero. Para confirmar el envío y activar el respaldo de fondos en escrow, debes completar el pago.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 14.sp
                        )

                        var checkingPayment by remember { mutableStateOf(false) }
                        val context = LocalContext.current

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val checkoutUrl = shipment.mpCheckoutUrl
                                    if (!checkoutUrl.isNullOrEmpty()) {
                                        try {
                                            val i = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(checkoutUrl))
                                            context.startActivity(i)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error abriendo link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Generando link en Mercado Pago...", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009EE3)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text("Pagar ARS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    checkingPayment = true
                                    viewModel.verifyMercadoPagoPayment(shipment.id) { success, paymentId ->
                                        checkingPayment = false
                                        if (success) {
                                            Toast.makeText(context, "¡Escrow abonado con éxito! ID de operación: $paymentId", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Pago aún no acreditado en Mercado Pago.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PatagonianTeal),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1.2f).height(36.dp),
                                enabled = !checkingPayment
                            ) {
                                if (checkingPayment) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Text("Verificar Pago", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            } else {
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
}

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

        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Indicaciones / Descripción física") },
            placeholder = { Text("Detalles del paquete, peso, dónde se retira...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            minLines = 2
        )

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
