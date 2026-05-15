package com.aarav.geowav.presentation.auth

import android.app.Activity
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.presentation.theme.manrope

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true)
@Composable
fun SignupScreen(
    signUpVM: SignUpVM, navigateToHome: () -> Unit, navigateToLogin: () -> Unit
) {


    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by signUpVM.uiState.collectAsState()

    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        signUpVM.events.collect { event ->
            when (event) {
                is SignUpEvent.NavigateToHome -> navigateToHome()
                is SignUpEvent.ShowError -> signUpVM.showError(event.message)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AuthSpatialBackground()


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "GeoWav",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = manrope,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create a calm space for trusted movement sharing.",
                fontSize = 15.sp,
                fontFamily = manrope,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "GeoWav only shares location when you allow it.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontFamily = manrope,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Google Signup
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .alpha(if (uiState.isLoading) 0.64f else 1f)
                    .clickable(enabled = !uiState.isLoading) {
                        activity?.let {
                            signUpVM.signInWithGoogle(it)
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.google),
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
                text = "Or continue with email",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = manrope,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthErrorMessage(
                visible = uiState.showErrorDialog,
                message = uiState.error ?: "An unknown error occurred",
                onDismiss = { signUpVM.clearError() }
            )

            if (uiState.showErrorDialog) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            TextField(
                value = uiState.username,
                onValueChange = { signUpVM.updateUsername(it) },
                label = {
                    Text(
                        "Name",
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.user),
                        contentDescription = "user icon",
                        modifier = Modifier.size(24.dp)
                    )
                },
                enabled = !uiState.isLoading,
                isError = uiState.usernameError != null,
                supportingText = {
                    if (uiState.usernameError != null) {
                        Text(
                            text = uiState.usernameError.toString(),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = manrope,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { emailFocusRequester.requestFocus() }
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = authTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = uiState.email,
                onValueChange = { signUpVM.updateEmail(it) },
                label = {
                    Text(
                        "Email",
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.email),
                        contentDescription = "email icon",
                        modifier = Modifier.size(24.dp)
                    )
                },
                enabled = !uiState.isLoading,
                isError = uiState.emailError != null,
                supportingText = {
                    if (uiState.emailError != null) {
                        Text(
                            text = uiState.emailError.toString(),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = manrope,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocusRequester),
                singleLine = true,
                colors = authTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = uiState.password,
                onValueChange = { signUpVM.updatePassword(it) },
                enabled = !uiState.isLoading,
                visualTransformation = if (uiState.isPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (uiState.isPasswordVisible)
                        R.drawable.eye
                    else
                        R.drawable.eye_closed

                    IconButton(
                        enabled = !uiState.isLoading,
                        onClick = {
                        if (uiState.isPasswordVisible) {
                            signUpVM.hidePassword()
                        } else {
                            signUpVM.showPassword()
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
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.password),
                        contentDescription = "password icon",
                        modifier = Modifier.size(24.dp)
                    )
                },
                isError = uiState.passwordError != null,
                supportingText = {
                    if (uiState.passwordError != null) {
                        Text(
                            text = uiState.passwordError.toString(),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = manrope,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        signUpVM.signUpWithEmailAndPassword(
                            uiState.username,
                            uiState.email,
                            uiState.password
                        )
                    }
                ),
                colors = authTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )


            Spacer(modifier = Modifier.height(16.dp))


            Button(
                onClick = {
                    keyboardController?.hide()
                    signUpVM.signUpWithEmailAndPassword(
                        uiState.username,
                        uiState.email,
                        uiState.password
                    )
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.42f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Text(
                        text = "Create account",
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            AuthLoadingNote(
                visible = uiState.isLoading,
                text = "Creating your account securely..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account?",
                    fontFamily = manrope,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.width(0.dp))

                val context = LocalContext.current


                TextButton(
                    enabled = !uiState.isLoading,
                    onClick = {
                        navigateToLogin()
                    }) {
                    Text(
                        text = "Login",
                        fontSize = 14.sp,
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

            }

        }
    }
}
