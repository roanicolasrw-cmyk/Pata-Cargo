package com.patacargo.virchm.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patacargo.virchm.data.ShipmentEntity
import com.patacargo.virchm.ui.theme.*
import android.content.pm.PackageManager

@Composable
fun PataCargoVectorLogo(modifier: Modifier = Modifier, isDarkTheme: Boolean = false) {
    val tintColor = if (isDarkTheme) Color.White else PatagonianTeal
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.045f
            drawArc(
                color = tintColor,
                startAngle = 100f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = stroke)
            )
            drawArc(
                color = SunsetGold,
                startAngle = -20f,
                sweepAngle = 105f,
                useCenter = false,
                style = Stroke(width = stroke)
            )
        }
        
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(0.65f),
            contentAlignment = Alignment.Center
        ) {
            val heightDp = maxHeight
            val widthDp = maxWidth
            val proportionalOffset = heightDp * -0.065f
            
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.fillMaxSize()
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize(0.48f)
                    .offset(y = proportionalOffset)
                    .clip(CircleShape)
                    .background(if (isDarkTheme) PatagonianTeal else Color.White),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .clip(RoundedCornerShape(widthDp * 0.08f))
                        .background(SunsetGold),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val thickness = size.minDimension * 0.08f
                        drawLine(
                            color = Color.White,
                            start = Offset(size.width * 0.15f, size.height * 0.35f),
                            end = Offset(size.width * 0.85f, size.height * 0.35f),
                            strokeWidth = thickness
                        )
                        drawArc(
                            color = PatagonianTeal,
                            startAngle = 10f,
                            sweepAngle = 160f,
                            useCenter = false,
                            style = Stroke(width = thickness * 0.8f),
                            topLeft = Offset(size.width * 0.25f, size.height * 0.45f),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.5f, size.height * 0.35f)
                        )
                    }
                }
            }
        }
    }
}

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
        "PENDIENTE" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "Aprobado")
        "PAGO_PENDIENTE" -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "Esperando Pago Escrow 🔒")
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
fun QRCodeDisplay(text: String, modifier: Modifier = Modifier) {
    val bitMatrix = remember(text) {
        try {
            com.google.zxing.qrcode.QRCodeWriter().encode(
                text,
                com.google.zxing.BarcodeFormat.QR_CODE,
                150,
                150
            )
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(2.dp, PatagonianTeal, RoundedCornerShape(12.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (bitMatrix != null) {
            val width = bitMatrix.width
            val height = bitMatrix.height
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellWidth = size.width / width
                val cellHeight = size.height / height
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        if (bitMatrix.get(x, y)) {
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(x * cellWidth, y * cellHeight),
                                size = androidx.compose.ui.geometry.Size(cellWidth + 0.15f, cellHeight + 0.15f)
                            )
                        }
                    }
                }
            }
        } else {
            Icon(Icons.Filled.QrCode, contentDescription = null, tint = PatagonianTeal, modifier = Modifier.fillMaxSize(0.6f))
        }
    }
}

@Composable
fun LogisticTimelineStepper(shipment: ShipmentEntity) {
    val steps = listOf("Creado", "Escrow", "Tránsito", "Entregado")
    
    val activeStep = when (shipment.status) {
        "PENDIENTE" -> {
            if (shipment.mpPaymentStatus == "APPROVED" || shipment.mpPaymentStatus == "approved") 1 else 0
        }
        "ACEPTADO" -> 1
        "EN_VIAJE" -> 2
        "ENTREGADO" -> 3
        else -> 0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = LightBackground),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("PROGRESO GLOBAL DEL ENVÍO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PatagonianTeal)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, stepName ->
                    val isCompleted = index <= activeStep
                    val isCurrent = index == activeStep
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCurrent -> PatagonianTeal
                                        isCompleted -> ValleyGreen
                                        else -> Color.LightGray.copy(alpha = 0.4f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted && !isCurrent) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                            } else {
                                Text(
                                    text = (index + 1).toString(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isCurrent) Color.White else Color.Gray
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Text(
                            text = stepName,
                            fontSize = 8.sp,
                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                            color = when {
                                isCurrent -> PatagonianTeal
                                isCompleted -> ValleyGreen
                                else -> Color.Gray
                            },
                            maxLines = 1
                        )
                    }
                    
                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(2.dp)
                                .background(
                                    if (index < activeStep) ValleyGreen else Color.LightGray.copy(alpha = 0.4f)
                                )
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    }
}

fun getSigningCertificateSHA1(context: android.content.Context): String {
    try {
        val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
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
