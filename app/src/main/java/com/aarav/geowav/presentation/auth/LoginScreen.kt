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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aarav.geowav.R
import com.aarav.geowav.domain.authentication.GoogleSignInClient
import com.aarav.geowav.ui.theme.nunito
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun LoginScreen(
    googleSignInClient: GoogleSignInClient,
    navigateToMap: () -> Unit,
    navigateToSignUp: () -> Unit
) {


//    var googleSignInClient: GoogleSignInClient = GoogleSignInClient(LocalContext.current.applicationContext)
//
//    var isLoggedIn by rememberSaveable {
//        mutableStateOf(googleSignInClient.isLoggedIn())
//    }


    var show by remember { mutableStateOf(false) }


//    val authViewModel : AuthViewModel = viewModel()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Soft pink background

    ) {

        if(show){
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
                        ){
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
            // Logo or App Name
            Text(
                text = "GeoWav",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = nunito,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Stay synced with your circle",
                fontSize = 16.sp,
                fontFamily = nunito,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            val coroutine = rememberCoroutineScope()


            // Google Login Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable {
                        coroutine.launch {
                            val b = googleSignInClient.signIn()
                            if (b) {
                                show = true
                                navigateToMap()
                            }
                        }
                    },
//                    .clickable {
//                        coroutine.launch {
//                            val b = googleSignInClient.signIn()
//                            if(b){
//                                show = true
//                                navigateToHome()
//                            }
//                        }
//                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.google), // Add your Google icon
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.inverseOnSurface
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Continue with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = nunito,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Optional: email login (for later)
            Text(
                text = "Or sign in with email",
                fontSize = 14.sp,
                fontFamily = nunito,
                color = MaterialTheme.colorScheme.inverseSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            var email by remember {
                mutableStateOf("")
            }
            // Email Field (if you want)
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", fontFamily = nunito, color = MaterialTheme.colorScheme.inverseSurface) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.DarkGray,
                    cursorColor = MaterialTheme.colorScheme.primary             // dark grey text
                ),
                shape = RoundedCornerShape(12.dp)
            )
            var password by remember {
                mutableStateOf("")
            }


            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", fontFamily = nunito, color = MaterialTheme.colorScheme.inverseSurface) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.DarkGray,
                    cursorColor = MaterialTheme.colorScheme.primary                // dark grey text
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val context = LocalContext.current

//            val loginSuccess by authViewModel.loginSuccess.observeAsState()
//
//            LaunchedEffect(loginSuccess) {
//                if(loginSuccess == true){
//                    navigateToHome()
//                }
//            }

            LaunchedEffect(show) {
                if(show)
                    navigateToMap()
            }

            Button(
                onClick = {
                    googleSignInClient.signInWithEmailAndPassword(
                        email,
                        password
                    ){
                        show = it
                    }
                    //authViewModel.loginWithEmailAndPassword(email, password, context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,   // Vibrant pink/red (primary)
                    contentColor = MaterialTheme.colorScheme.onPrimary,          // Text color
                    disabledContainerColor = Color(0xFFFFC9D2), // Soft pink when disabled
                    disabledContentColor = Color.White.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "Login",
                    fontFamily = nunito,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row{

                Text(
                    text = "New to GeoWav?",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontFamily = nunito
                )

                Spacer(modifier = Modifier.width(6.dp))

                val context = LocalContext.current

                Text(
                    text = "Sign Up",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontFamily = nunito,
                    modifier = Modifier.clickable{
                        navigateToSignUp()
                        Toast.makeText(context, "Clicked", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }
}
