package com.aarav.geowav.presentation.auth

import android.graphics.drawable.Icon
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aarav.geowav.R
import com.aarav.geowav.domain.authentication.GoogleSignInClient
import com.aarav.geowav.ui.theme.manrope
import com.aarav.geowav.ui.theme.sora
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun SignupScreen(
    googleSignInClient: GoogleSignInClient,
    navigateToHome: () -> Unit,
    navigateToLogin: () -> Unit

    //modifier: Modifier, navigateToLogin : () -> Unit, navigateToHome : () -> Unit
) {

    //val authViewmodel : AuthViewModel = viewModel()

    val context = LocalContext.current.applicationContext

//    val googleSignInClient = GoogleSignInClient(context)

    var show by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        if (show) {
            Dialog(
                onDismissRequest = {},
                content = {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize()
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

            //Create your GeoWav account
            //Stay synced with your circle

            Text(
                text = "Create your GeoWav account",
                fontSize = 16.sp,
                fontFamily = sora,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            val coroutine = rememberCoroutineScope()

            // Google Signup
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable {
                        coroutine.launch {
                            val b = googleSignInClient.signIn()
                            if (b) {
                                show = true
                                navigateToHome()
                            }
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
                text = "Or sign up with email",
                fontSize = 14.sp,
                fontFamily = sora,
                color = MaterialTheme.colorScheme.inverseSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var name by remember { mutableStateOf("") }

            TextField(
                value = name,
                onValueChange = { name = it },
                label = {
                    Text(
                        "Name",
                        fontFamily = sora,
                        color = MaterialTheme.colorScheme.inverseSurface
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.user),
                        contentDescription = "user icon",
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.DarkGray,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = {
                    Text(
                        "Email",
                        fontFamily = sora,
                        color = MaterialTheme.colorScheme.inverseSurface
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.email),
                        contentDescription = "email icon",
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.DarkGray,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(
                        "Password",
                        fontFamily = sora,
                        color = MaterialTheme.colorScheme.inverseSurface
                    )
                },leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.password),
                        contentDescription = "password icon",
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.DarkGray,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            //Spacer(modifier = Modifier.height(12.dp))

//            TextField(
//                value = confirmPassword,
//                onValueChange = { confirmPassword = it },
//                label = { Text("Confirm Password", fontFamily = sora) },
//                modifier = Modifier.fillMaxWidth(),
//                singleLine = true,
//                colors = TextFieldDefaults.colors(
//                    focusedContainerColor = Color.White,
//                    unfocusedContainerColor = Color.White,
//                    focusedIndicatorColor = Color(0xFFEF476F),
//                    unfocusedIndicatorColor = Color.Transparent,
//                    focusedLabelColor = Color(0xFFEF476F),
//                    unfocusedLabelColor = Color.DarkGray,
//                    cursorColor = Color(0xFFEF476F)
//                ),
//                shape = RoundedCornerShape(12.dp)
//            )

            Spacer(modifier = Modifier.height(16.dp))

            val context = LocalContext.current
//            val signUpSuccess by authViewmodel.signUpSuccess.observeAsState()

            val scope = rememberCoroutineScope()
//            LaunchedEffect(signUpSuccess) {
//                Log.d("SIGNUP_SUCCESS", "signUpSuccess: $signUpSuccess")
//                if (signUpSuccess == true) {
//                    navigateToHome()
//                }
//            }

            LaunchedEffect(show) {
                if (show) {
                    navigateToHome()
                }
            }
            // val userFlowVM : UserLearningFlowViewModel = hiltViewModel()

            Button(
                onClick = {
                    scope.launch {
                        googleSignInClient.signUpUsingEmailAndPassword(name, email, password, {
                            show = it
                        })

                    }
                    //userFlowVM.setupProgressForNewUser()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                )
            ) {
                Text(
                    text = "Sign Up",
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
                    text = "Already have an account?",
                    fontFamily = sora,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.width(0.dp))

                val context = LocalContext.current


                TextButton(
                    onClick = {
                        navigateToLogin()
                        Toast.makeText(context, "Clicked", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text(
                        text = "Login",
                        fontSize = 14.sp,
                        fontFamily = sora,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

            }

            //Spacer(modifier = Modifier.height(24.dp))

//            Text(
//                text = "Skip To Home",
//                color = primaryLight,
//                modifier = Modifier.clickable{
//                    navigateToHome()
//                }
//            )
        }
    }
}