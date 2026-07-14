package com.patacargo.virchm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patacargo.virchm.ui.PataCargoViewModel
import com.patacargo.virchm.ui.components.PataCargoVectorLogo
import com.patacargo.virchm.ui.theme.*

@Composable
fun OnboardingAuthScreen(
    viewModel: PataCargoViewModel,
    onGoogleSignInIntent: () -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("ENVIADOR") }
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
            PataCargoVectorLogo(modifier = Modifier.size(100.dp), isDarkTheme = true)
            
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
            PataCargoVectorLogo(modifier = Modifier.size(80.dp), isDarkTheme = true)

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
