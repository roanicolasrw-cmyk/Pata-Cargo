package com.patacargo.virchm.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patacargo.virchm.data.ShipmentEntity
import com.patacargo.virchm.data.UserEntity
import com.patacargo.virchm.ui.PataCargoViewModel
import com.patacargo.virchm.ui.components.EmptyStateWidget
import com.patacargo.virchm.ui.theme.*

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
                Button(
                    onClick = { onResolve(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reembolsar Enviador", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

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
