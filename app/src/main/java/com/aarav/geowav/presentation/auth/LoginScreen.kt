package com.aarav.geowav.presentation.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aarav.geowav.R
import com.aarav.geowav.presentation.components.MyAlertDialog
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.sora

@Preview(showBackground = true)
@Composable
fun LoginScreen(
    loginVM: LoginVM, navigateToMap: () -> Unit, navigateToSignUp: () -> Unit
) {

    val uiState by loginVM.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)

    ) {

        if (uiState.isLoading) {
            Dialog(onDismissRequest = {}, content = {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            })
        }

        LaunchedEffect(uiState.isSignInSuccessful) {
            if (uiState.isSignInSuccessful) navigateToMap()
        }

        MyAlertDialog(
            shouldShowDialog = uiState.showErrorDialog,
            onDismissRequest = { loginVM.clearError() },
            title = "Error",
            message = uiState.error ?: "An unknown error occurred",
            confirmButtonText = "Dismiss"
        ) {
            loginVM.clearError()
        }

        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo or App Name
            Text(
                text = "GeoWav",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = manrope,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Stay synced with your circle",
                fontSize = 16.sp,
                fontFamily = sora,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Google Login Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable {
                        loginVM.signInWithGoogle()
//                        coroutine.launch {
//                            val b = googleSignInClient.signIn()
//                            if (b) {
//                                show = true
//                                navigateToMap()
//                            }
//                        }
                    }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface
                    ), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.google), // Add your Google icon
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Continue with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = "Or login with email",
                fontSize = 14.sp,
                fontFamily = sora,
                color = MaterialTheme.colorScheme.inverseSurface
            )

            Spacer(modifier = Modifier.height(16.dp))


            TextField(
                value = uiState.email, onValueChange = { loginVM.updateEmail(it) }, label = {
                    Text(
                        "Email", fontFamily = sora, color = MaterialTheme.colorScheme.inverseSurface
                    )
                },
                isError = uiState.emailError != null,
                supportingText = {
                    if(uiState.emailError != null){
                        Text(
                            text = uiState.emailError.toString(),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = sora,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(), singleLine = true, leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.email),
                        contentDescription = "email icon",
                        modifier = Modifier.size(24.dp)
                    )
                }, colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.DarkGray,
                    cursorColor = MaterialTheme.colorScheme.primary
                ), shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = uiState.password,
                onValueChange = { loginVM.updatePassword(it) },  visualTransformation = if (uiState.isPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (uiState.isPasswordVisible)
                        R.drawable.eye
                    else
                        R.drawable.eye_closed

                    IconButton(onClick = {
                        if (uiState.isPasswordVisible) {
                            loginVM.hidePassword()
                        } else {
                            loginVM.showPassword()
                        }
                    }) {
                        Icon(
                            painter = painterResource(
                                icon
                            ),
                            contentDescription = if (uiState.isPasswordVisible)
                                "Hide password"
                            else
                                "Show password",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {
                    Text(
                        "Password",
                        fontFamily = sora,
                        color = MaterialTheme.colorScheme.inverseSurface
                    )
                },
                isError = uiState.passwordError != null,
                supportingText = {
                    if(uiState.passwordError != null){
                        Text(
                            text = uiState.passwordError.toString(),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = sora,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(), leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.password),
                        contentDescription = "password icon",
                        modifier = Modifier.size(24.dp)
                    )
                }, singleLine = true, colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.DarkGray,
                    cursorColor = MaterialTheme.colorScheme.primary
                ), shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))



            Button(
                onClick = {
                    loginVM.signInWithEmailAndPassword(
                        uiState.email, uiState.password
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,   // Vibrant pink/red (primary)
                    contentColor = MaterialTheme.colorScheme.onPrimary,          // Text color
                    disabledContainerColor = Color(0xFFFFC9D2), // Soft pink when disabled
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                )
            ) {
                Text(
                    text = "Login",
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "New to GeoWav?",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontFamily = sora
                )

                Spacer(modifier = Modifier.width(0.dp))

                val context = LocalContext.current

                TextButton(
                    onClick = {
                        navigateToSignUp()
                        Toast.makeText(context, "Clicked", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text(
                        text = "Sign Up",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontFamily = sora
                    )
                }
            }
        }
    }
}
