package com.aarav.geowav.presentation.circle

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.FeatureAccess
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.PendingInvite
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.presentation.components.CustomBottomSheet
import com.aarav.geowav.presentation.components.DeleteDialog
import com.aarav.geowav.presentation.components.IdentityAvatar
import com.aarav.geowav.presentation.components.SnackbarManager
import com.aarav.geowav.presentation.components.UpgradeBottomSheetContent
import com.aarav.geowav.presentation.locationsharing.itemShape
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope



@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text.uppercase(),
            fontFamily = manrope,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.08.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}


@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontFamily = manrope,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.05.sp,
        color = MaterialTheme.colorScheme.outline
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CircleScreen(
    viewModel: CircleVM,
    subscriptionVM: SubscriptionViewModel,
    back: () -> Unit,
    navigateToPaywall: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val plan by subscriptionVM.userPlan.collectAsState()
    var upgradeContext by remember { mutableStateOf<UpgradeContext?>(null) }

    upgradeContext?.let {
        CustomBottomSheet(onDismissRequest = { upgradeContext = null }) {
            UpgradeBottomSheetContent(
                context = it,
                onUpgradeClick = { upgradeContext = null; navigateToPaywall() },
                onDismiss = { upgradeContext = null }
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CircleUiEvent.InviteSent -> SnackbarManager.showMessage("Invite sent")
                is CircleUiEvent.ShowError -> SnackbarManager.showMessage(event.message)
                is CircleUiEvent.InviteAccepted -> SnackbarManager.showMessage("Invite Accepted")
                is CircleUiEvent.MemberDeleted -> SnackbarManager.showMessage("Member deleted")
                is CircleUiEvent.LocationRequestSent -> SnackbarManager.showMessage("Location request sent to ${event.recipientName}")
                is CircleUiEvent.ShowUpgrade -> upgradeContext = event.context
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadLovedOnes()
        viewModel.loadPendingInvites()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Text(
                        text = "Your Circle",
                        fontFamily = manrope,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { back() }) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ContainedLoadingIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading your circle...",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            else -> CircleContent(
                modifier = Modifier.padding(padding),
                uiState = uiState,
                userPlan = plan,
                updateName = viewModel::updateName,
                updateEmail = viewModel::updateEmail,
                onSendInvite = viewModel::sendInvite,
                onAcceptInvite = viewModel::acceptInvite,
                onRejectInvite = viewModel::rejectInvite,
                onDeleteMember = viewModel::showDeleteDialog,
                dismissDialog = viewModel::hideDeleteDialog,
                deleteMember = viewModel::deleteMember,
                onRequestLocation = viewModel::requestLocation
            )
        }
    }
}


@Composable
fun CircleContent(
    modifier: Modifier = Modifier,
    uiState: CircleUiState,
    userPlan: UserPlan,
    updateName: (String) -> Unit,
    updateEmail: (String) -> Unit,
    onSendInvite: (String, String, UserPlan) -> Unit,
    onAcceptInvite: (String) -> Unit,
    onRejectInvite: (String) -> Unit,
    onDeleteMember: () -> Unit,
    dismissDialog: () -> Unit,
    deleteMember: (String) -> Unit,
    onRequestLocation: (String, String) -> Unit,
) {
    var confirmDeleteFor by remember { mutableStateOf<String?>(null) }
    var pendingRequestMember by remember { mutableStateOf<CircleMember?>(null) }

    DeleteDialog(
        shouldShowDialog = uiState.showDeleteDialog && confirmDeleteFor != null,
        onDismissRequest = dismissDialog,
        dismissButtonText = "Cancel",
        onDismissClick = dismissDialog,
        title = "Remove Member",
        icon = R.drawable.trash,
        message = "Are you sure you want to remove this member from your circle?",
        confirmButtonText = "Remove"
    ) {
        confirmDeleteFor?.let {
            deleteMember(it)
            dismissDialog()
        }
    }

    pendingRequestMember?.let { member ->
        CustomBottomSheet(
            onDismissRequest = {
                if (uiState.requestingLocationMemberId == null) {
                    pendingRequestMember = null
                }
            }
        ) {
            RequestLocationBottomSheetContent(
                member = member,
                isLoading = uiState.requestingLocationMemberId == member.id,
                onConfirm = {
                    val displayName = member.alias?.takeIf { it.isNotBlank() } ?: member.profileName
                    onRequestLocation(member.id, displayName)
                    pendingRequestMember = null
                },
                onDismiss = { pendingRequestMember = null }
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            top = 8.dp,
            bottom = 32.dp
        )
    ) {
        item {
            SectionLabel(
                text = "My circle",
                modifier = Modifier.padding(top = 4.dp, start = 12.dp, end = 12.dp)
            )
        }
        item {
            MyCircleSection(
                lovedOnesList = uiState.lovedOnes,
                deletingMemberId = uiState.deletingMemberId,
                requestingLocationMemberId = uiState.requestingLocationMemberId,
                onDeleteMember = onDeleteMember,
                confirmDelete = { confirmDeleteFor = it },
                onRequestLocation = { member -> pendingRequestMember = member }
            )
        }

        item {
            ConnectionUsageCard(
                current = uiState.lovedOnes.size,
                plan = userPlan,
                isLoading = uiState.isLoading,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        item {
            SectionLabel(
                text = "Invite someone you trust",
                modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp)
            )
        }
        item {
            AddLovedOneCard(
                uiState = uiState,
                userPlan = userPlan,
                nameUpdate = updateName,
                emailUpdate = updateEmail,
                isLoading = uiState.sendingRequest,
                onSendInvite = onSendInvite
            )
        }

        if (uiState.pendingInvites.isNotEmpty()) {
            item {
                SectionLabel(
                    text = "Pending invites",
                    modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp)
                )
            }
            item {
                PendingInviteSection(
                    pendingInvites = uiState.pendingInvites,
                    acceptingInviteId = uiState.acceptingInviteId,
                    rejectingInviteId = uiState.rejectingInviteId,
                    acceptInvite = onAcceptInvite,
                    rejectInvite = onRejectInvite
                )
            }
        }
    }
}


@Composable
fun AddLovedOneCard(
    uiState: CircleUiState,
    userPlan: UserPlan,
    nameUpdate: (String) -> Unit,
    emailUpdate: (String) -> Unit,
    isLoading: Boolean,
    onSendInvite: (String, String, UserPlan) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            val focusRequester = remember { FocusRequester() }


            Text(
                text = "Bring someone into your circle",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = manrope
                ),
                fontSize = 16.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "They'll receive an email when you're ready to stay connected",
                fontFamily = manrope,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(Modifier.height(16.dp))

            FieldLabel("Name")
            Spacer(Modifier.height(5.dp))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { nameUpdate(it) },
                placeholder = {
                    Text(
                        "Enter their name",
                        fontFamily = manrope,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                },
                isError = uiState.nameError != null,
                supportingText = {
                    if (uiState.nameError != null) {
                        Text(
                            text = uiState.nameError,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = manrope,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusRequester.requestFocus() }
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.user),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(14.dp)
            )

            val focusManager = LocalFocusManager.current

            Spacer(Modifier.height(4.dp))

            FieldLabel("Email")
            Spacer(Modifier.height(5.dp))
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { emailUpdate(it) },
                placeholder = {
                    Text(
                        "Enter their email",
                        fontFamily = manrope,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                },
                isError = uiState.emailError != null,
                supportingText = {
                    if (uiState.emailError != null) {
                        Text(
                            text = uiState.emailError,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = manrope,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    onSendInvite(uiState.email, uiState.name, userPlan)
                }),
                modifier = Modifier.fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.email),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(8.dp))

            SendInviteButton(!isLoading) {
                focusManager.clearFocus()
                onSendInvite(uiState.email, uiState.name, userPlan)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SendInviteButton(
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val btnBrush = if (isEnabled) {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }

    Button(
        enabled = isEnabled,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(btnBrush, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.outline
        )
    ) {
        AnimatedContent(
            targetState = isEnabled,
            transitionSpec = {
                fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                fadeOut(animationSpec = tween(90))
            },
            label = "SendInviteButtonContent"
        ) { enabled ->
            if (enabled) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Send Invite",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        painter = painterResource(R.drawable.send_invite),
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.outline,
                    strokeWidth = 2.5.dp
                )
            }
        }
    }
}


@Composable
fun MyCircleSection(
    lovedOnesList: List<CircleMember>,
    deletingMemberId: String?,
    requestingLocationMemberId: String? = null,
    onDeleteMember: () -> Unit,
    confirmDelete: (String) -> Unit,
    onRequestLocation: (CircleMember) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.animateContentSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "People you care about",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope
                    ),
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )

                if (lovedOnesList.isNotEmpty()) {

                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${lovedOnesList.size} in circle",
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (lovedOnesList.isEmpty()) {
                val infiniteTransition = rememberInfiniteTransition()
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.92f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = EaseInOut),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.user),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Your Circle is Empty",
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Add friends or family members below to stay connected and view real-time location sharing.",
                        fontFamily = manrope,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            } else {
                lovedOnesList.forEachIndexed { index, member ->
                    if (index > 0) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    LovedOneCardCircle(
                        connection = member,
                        index = index,
                        count = lovedOnesList.size,
                        deletingMemberId = deletingMemberId,
                        requestingLocationMemberId = requestingLocationMemberId,
                        onDeleteMember = onDeleteMember,
                        confirmDelete = confirmDelete,
                        onRequestLocation = onRequestLocation
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}


@Composable
fun PendingInviteSection(
    pendingInvites: List<PendingInvite>,
    acceptingInviteId: String?,
    rejectingInviteId: String?,
    acceptInvite: (String) -> Unit,
    rejectInvite: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.animateContentSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = pendingInvites.isNotEmpty()) { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Waiting on response",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope
                    ),
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )

                if (pendingInvites.isNotEmpty()) {

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryFixedDim)
                    ) {
                        Text(
                            text = pendingInvites.size.toString(),
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryFixed,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }

                Icon(
                    painter = painterResource(
                        if (expanded) R.drawable.up_arrow else R.drawable.down_arrow
                    ),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (pendingInvites.isEmpty()) {
                Text(
                    text = "No pending invites",
                    fontFamily = manrope,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                )
            }

            if (expanded && pendingInvites.isNotEmpty()) {
                pendingInvites.forEachIndexed { index, invite ->
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    PendingInviteRow(
                        acceptingInviteId = acceptingInviteId,
                        rejectingInviteId = rejectingInviteId,
                        connection = invite,
                        index = index,
                        count = pendingInvites.size,
                        onAccept = acceptInvite,
                        onDecline = rejectInvite
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LovedOneCardCircle(
    connection: CircleMember,
    index: Int,
    count: Int,
    deletingMemberId: String? = null,
    requestingLocationMemberId: String? = null,
    onDeleteMember: () -> Unit = {},
    confirmDelete: (String) -> Unit = {},
    onRequestLocation: (CircleMember) -> Unit = {}
) {

    val (avatarBg, avatarFg) = when (index % 3) {
        0 -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        1 -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        else -> Pair(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
    val displayName = connection.alias?.takeIf { it.isNotBlank() } ?: connection.profileName
    val presenceContext = "In your circle"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .border(
                    width = 1.5.dp,
                    color = avatarBg.copy(alpha = 0.4f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            IdentityAvatar(
                avatarUrl = connection.avatarUrl,
                displayName = displayName,
                backgroundColor = avatarBg,
                contentColor = avatarFg,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = presenceContext,
                color = MaterialTheme.colorScheme.outline,
                fontFamily = manrope,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        val isRequesting = requestingLocationMemberId == connection.id
        val isDeleting = deletingMemberId == connection.id
        val isAnyActionBusy = deletingMemberId != null || requestingLocationMemberId != null

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Refined Request Location Action Pill
            Surface(
                onClick = { onRequestLocation(connection) },
                enabled = !isAnyActionBusy,
                shape = RoundedCornerShape(99.dp),
                color = if (isRequesting) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                },
                border = BorderStroke(
                    1.dp,
                    if (isRequesting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                ),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (isRequesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(13.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Asking...",
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.location_sharing),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Request",
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Delete Member Action Button
            if (isDeleting) {
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = MaterialTheme.colorScheme.outline,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        onDeleteMember()
                        confirmDelete(connection.id)
                    },
                    enabled = !isAnyActionBusy,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.trash),
                        contentDescription = "Remove $displayName from circle",
                        tint = if (isAnyActionBusy)
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f)
                        else
                            MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun PendingInviteRow(
    acceptingInviteId: String?,
    rejectingInviteId: String?,
    connection: PendingInvite,
    index: Int,
    count: Int,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit
) {
    val (avatarBg, avatarFg) = when (index % 3) {
        0 -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        1 -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        else -> Pair(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
    }

    val isAccepting = acceptingInviteId == connection.senderId
    val isDeclining = rejectingInviteId == connection.senderId
    val isRowBusy = isAccepting || isDeclining
    val isAnyInviteBusy = acceptingInviteId != null || rejectingInviteId != null

    val isAcceptEnabled = !isAnyInviteBusy
    val acceptBg = when {
        isAccepting -> MaterialTheme.colorScheme.secondaryContainer
        isAnyInviteBusy -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val acceptFg = when {
        isAccepting -> MaterialTheme.colorScheme.onSecondaryContainer
        isAnyInviteBusy -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    val isDeclineEnabled = !isAnyInviteBusy
    val declineBg = when {
        isDeclining -> MaterialTheme.colorScheme.errorContainer
        isAnyInviteBusy -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val declineFg = when {
        isDeclining -> MaterialTheme.colorScheme.onErrorContainer
        isAnyInviteBusy -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.onErrorContainer
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(avatarBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (connection.senderProfileName?.take(1) ?: "?").uppercase(),
                color = avatarFg,
                fontFamily = manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = connection.senderProfileName ?: "",
            fontFamily = manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )


        Surface(
            shape = RoundedCornerShape(10.dp),
            color = acceptBg,
            modifier = Modifier.clickable(enabled = isAcceptEnabled) {
                onAccept(connection.senderId)
            }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                if (isAccepting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = acceptFg,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Accept",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = acceptFg
                    )
                }
            }
        }

        Spacer(Modifier.width(6.dp))


        Surface(
            shape = RoundedCornerShape(10.dp),
            color = declineBg,
            modifier = Modifier.clickable(enabled = isDeclineEnabled) {
                onDecline(connection.senderId)
            }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                if (isDeclining) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = declineFg,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Decline",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = declineFg
                    )
                }
            }
        }
    }
}


@Composable
fun shimmerBrush(
    showShimmer: Boolean = true,
    targetValue: Float = 1000f
): Brush {
    return if (showShimmer) {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation by transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = EaseInOut),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerTranslate"
        )
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            ),
            start = androidx.compose.ui.geometry.Offset(translateAnimation, translateAnimation),
            end = androidx.compose.ui.geometry.Offset(translateAnimation + 300f, translateAnimation + 300f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent)
        )
    }
}

@Composable
fun ConnectionUsageCard(
    current: Int,
    plan: UserPlan,
    textSize: TextUnit? = null,
    showPlanInfo: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val max = FeatureAccess.maxConnections(plan)
    val isUnlimited = max == Int.MAX_VALUE
    val isLimitReached = !isUnlimited && current >= max

    val planText = when (plan) {
        UserPlan.FREE -> "GeoWav Free"
        UserPlan.PREMIUM -> "GeoWav Premium"
        UserPlan.PRO -> "GeoWav Pro"
    }

    val usageText = if (isUnlimited) "$current connection" else "$current / $max connections used"


    val cardBg = if (isLimitReached)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.surfaceContainer

    val cardBorder = if (isLimitReached)
        BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
    else
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

    val cardFg = if (isLimitReached)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onSurface

    val cardFgMuted = if (isLimitReached)
        cardFg.copy(alpha = 0.65f)
    else
        MaterialTheme.colorScheme.outline


    val badgeBg = if (isLimitReached)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.surfaceContainerLow

    val badgeFg = if (isLimitReached)
        MaterialTheme.colorScheme.onError
    else
        MaterialTheme.colorScheme.outline

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isLoading) {
                val shimmer = shimmerBrush()
                if (showPlanInfo) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(shimmer)
                        )
                        Box(
                            modifier = Modifier
                                .width(55.dp)
                                .height(18.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(shimmer)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmer)
                )
                if (!isUnlimited) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(shimmer)
                    )
                }
            } else {
                if (showPlanInfo) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = planText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = manrope,
                            color = cardFgMuted
                        )
                        Surface(
                            shape = RoundedCornerShape(99.dp),
                            color = badgeBg
                        ) {
                            Text(
                                text = if (isLimitReached) "Limit reached" else "Active",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = manrope,
                                color = badgeFg
                            )
                        }
                    }
                }

                Text(
                    text = usageText,
                    fontSize = textSize ?: 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = manrope,
                    color = cardFg
                )

                if (!isUnlimited) {
                    val targetProgress = current.toFloat() / max
                    var progressAnimatable by remember { mutableStateOf(0f) }
                    LaunchedEffect(targetProgress) {
                        progressAnimatable = targetProgress
                    }
                    val animatedProgress by animateFloatAsState(
                        targetValue = progressAnimatable,
                        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                        label = "ConnectionUsageProgress"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(cardFg.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(99.dp))
                                .background(
                                    brush = if (isLimitReached) {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.error,
                                                MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    }
                                )
                        )
                    }
                }

                if (isLimitReached) {
                    Text(
                        text = "Upgrade to add more connections",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = manrope,
                        color = cardFgMuted
                    )
                }
            }
        }
    }
}

@Composable
fun RequestLocationBottomSheetContent(
    member: CircleMember,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val displayName = member.alias?.takeIf { it.isNotBlank() } ?: member.profileName

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Avatar with location sharing badge
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            IdentityAvatar(
                avatarUrl = member.avatarUrl,
                displayName = displayName,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(60.dp)
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .size(26.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.location_sharing),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "Request Location",
            fontFamily = manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Subtitle with clear emphasis
        Text(
            text = buildAnnotatedString {
                append("Ask ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                    append(displayName)
                }
                append(" to share their real-time location with you.")
            },
            fontFamily = manrope,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Transparency & Privacy Info Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.bell),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Instant Notification",
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$displayName will get an alert with your request.",
                            fontFamily = manrope,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.clock),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Audience & Duration Control",
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "They choose how long to share (15m, 1h, or ongoing).",
                            fontFamily = manrope,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Button(
            onClick = onConfirm,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.location_sharing),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Send Request",
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onDismiss,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Cancel",
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

