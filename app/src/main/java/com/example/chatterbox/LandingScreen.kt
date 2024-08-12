package com.example.chatterbox

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.chatterbox.backend.AuthViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LandingScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val userProfile by authViewModel.userProfile.observeAsState()
    val darkModeEnabled = userProfile?.get("isDarkModeOn") as? Boolean ?: false
    val backgroundColor = if (darkModeEnabled) Color(0xFF2E2E2E) else Color.White
    val textColor = if (darkModeEnabled) Color.White else Color.Black
    val textColorOpposite = if (darkModeEnabled) Color.Black else Color.White
    val secondaryTextColor = if (darkModeEnabled) Color(0xFF929AAB) else Color.Gray
    val inputFieldColor = if (darkModeEnabled) Color.White else Color.Black
    val buttonColor = if (darkModeEnabled) Color.White else Color(0xFF010101)
    val buttonTextColor = if (darkModeEnabled) Color.Black else Color.White

    if (showTermsDialog) {
        CustomDialog(
            onDismissRequest = { showTermsDialog = false },
            title = "Terms of Use",
            content = { TermsContent(textColor) },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("OK", color = buttonTextColor)
                }
            },
            backgroundColor = backgroundColor
        )
    }

    if (showPrivacyDialog) {
        CustomDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = "Privacy Policy",
            content = { PrivacyContent(textColor) },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("OK", color = buttonTextColor)
                }
            },
            backgroundColor = backgroundColor
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            LogoSection(darkModeEnabled)

            Spacer(Modifier.height(20.dp))
            TermsAndPrivacyText(
                onTermsClick = { showTermsDialog = true },
                onPrivacyClick = { showPrivacyDialog = true },
                textColor = textColor,
                darkModeEnabled
            )

            Spacer(modifier = Modifier.height(20.dp))
            NavigationButtons(navController, buttonColor, buttonTextColor, darkModeEnabled)
        }
    }
}

@Composable
fun CustomDialog(
    onDismissRequest: () -> Unit,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
    confirmButton: @Composable () -> Unit,
    backgroundColor: Color
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .width(360.dp)
                .padding(16.dp)
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (backgroundColor == Color(0xFF2E2E2E)) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                content()
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    confirmButton()
                }
            }
        }
    }
}

@Composable
fun TermsContent(textColor: Color) {
    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(termsOfUse) { term ->
            Text(text = term, color = textColor, modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@Composable
fun PrivacyContent(textColor: Color) {
    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(privacyPolicy) { policy ->
            Text(text = policy, color = textColor, modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@Composable
fun LogoSection(darkModeEnabled: Boolean) {
    val logoBackgroundColor = if (darkModeEnabled) Color.White else Color.Transparent

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = logoBackgroundColor),
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            Modifier
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_text_no_background),
                contentDescription = "",
                Modifier.width(230.dp)
            )

        }

    }

    Spacer(Modifier.height(80.dp))

    Image(
        painter = painterResource(id = R.drawable.mobile_app),
        contentDescription = "",
        Modifier.width(300.dp)
    )
    Spacer(Modifier.height(50.dp))
}

@Composable
fun TermsAndPrivacyText(
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    textColor: Color,
    darkModeEnabled: Boolean
) {
    val annotatedText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = textColor)) {
            append("By continuing, you understand and agree to ChatterBox's ")
        }

        pushStringAnnotation(tag = "terms", annotation = "terms")
        withStyle(style = SpanStyle(color = if (darkModeEnabled) Color.LightGray else Color.Black, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.SemiBold)) {
            append("Terms of Use")
        }
        pop()
        withStyle(style = SpanStyle(color = textColor)) {
            append(" and ")
        }

        pushStringAnnotation(tag = "privacy", annotation = "privacy")
        withStyle(style = SpanStyle(color = if (darkModeEnabled) Color.LightGray else Color.Black, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.SemiBold)) {
            append("Privacy Policy")
        }
        pop()
        append(".")
    }

    ClickableText(
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations(start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    when (annotation.tag) {
                        "terms" -> onTermsClick()
                        "privacy" -> onPrivacyClick()
                    }
                }
        },
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun NavigationButtons(navController: NavHostController, buttonColor: Color, buttonTextColor: Color, darkModeEnabled: Boolean) {
    Button(
        onClick = { navController.navigate("signup") },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(size = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = buttonTextColor
        )
    ) {
        Text(
            "Get Started",
            fontSize = 20.sp,
            color = buttonTextColor
        )
    }

    Spacer(Modifier.height(20.dp))

    OutlinedButton(
        onClick = { navController.navigate("signin") },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, buttonColor)
    ) {
        Text(
            "Sign In",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (darkModeEnabled) Color.White else Color.Black
        )
    }
}


val termsOfUse = listOf(
    "Welcome to ChatterBox! By using our services, you agree to comply with and be bound by the following terms of use. Please review the following terms carefully. If you do not agree to these terms, you should not use our services.",
    "By accessing or using ChatterBox, you agree to be bound by these terms of use and all terms incorporated by reference. If you do not agree to all of these terms, do not use ChatterBox.",
    "We reserve the right to modify these terms at any time. Any changes will be effective immediately upon posting the revised terms. Your continued use of ChatterBox following the posting of changes will constitute your acceptance of such changes. Please review these terms periodically for updates.",
    "To use ChatterBox, you must register for an account by providing your first name, last name, email address, and password. You agree to provide accurate, current, and complete information during the registration process and to update such information to keep it accurate, current, and complete. ChatterBox reserves the right to suspend or terminate your account if any information provided during the registration process or thereafter proves to be inaccurate, not current, or incomplete.",
    "You are responsible for safeguarding the password that you use to access ChatterBox and for any activities or actions under your password. You agree to notify us immediately of any unauthorized use of your password or any other breach of security. ChatterBox will not be liable for any loss or damage arising from your failure to comply with these requirements.",
    "Your use of ChatterBox is subject to our Privacy Policy, which is incorporated by reference into these terms of use. Please review our Privacy Policy to understand our practices regarding the collection, use, and disclosure of your personal information.",
    "You agree not to engage in any of the following prohibited activities:",
    "- Using ChatterBox for any illegal purpose or in violation of any local, state, national, or international law.",
    "- Posting or transmitting content that is infringing, libelous, defamatory, obscene, pornographic, abusive, or otherwise violates any law or right of any third party.",
    "- Attempting to interfere with, compromise the system integrity or security, or decipher any transmissions to or from the servers running ChatterBox.",
    "- Taking any action that imposes, or may impose at our sole discretion an unreasonable or disproportionately large load on our infrastructure.",
    "- Uploading invalid data, viruses, worms, or other software agents through the service.",
    "- Collecting or harvesting any personally identifiable information, including account names, from ChatterBox.",
    "- Using automated means, including spiders, robots, crawlers, data mining tools, or the like to download data from ChatterBox, except for internet search engines (e.g., Google) and non-commercial public archives (e.g., archive.org) that comply with our robots.txt file.",
    "- Impersonating another person or otherwise misrepresenting your affiliation with a person or entity, conducting fraud, hiding or attempting to hide your identity.",
    "By posting, uploading, or otherwise submitting any content to ChatterBox, you grant us a non-exclusive, transferable, sub-licensable, royalty-free, worldwide license to use, store, display, reproduce, modify, create derivative works, perform, and distribute your content in connection with ChatterBox.",
    "We may terminate or suspend your account and bar access to ChatterBox immediately, without prior notice or liability, under our sole discretion, for any reason whatsoever and without limitation, including but not limited to a breach of the terms. If you wish to terminate your account, you may simply discontinue using ChatterBox.",
    "In no event shall ChatterBox, nor its directors, employees, partners, agents, suppliers, or affiliates, be liable for any indirect, incidental, special, consequential or punitive damages, including without limitation, loss of profits, data, use, goodwill, or other intangible losses, resulting from (i) your access to or use of or inability to access or use ChatterBox; (ii) any conduct or content of any third party on ChatterBox; (iii) any content obtained from ChatterBox; and (iv) unauthorized access, use or alteration of your transmissions or content, whether based on warranty, contract, tort (including negligence) or any other legal theory, whether or not we have been informed of the possibility of such damage, and even if a remedy set forth herein is found to have failed of its essential purpose.",
    "These terms shall be governed by and construed in accordance with the laws of the State of California, without regard to its conflict of law provisions. Our failure to enforce any right or provision of these terms will not be considered a waiver of those rights. If any provision of these terms is held to be invalid or unenforceable by a court, the remaining provisions of these terms will remain in effect.",
    "If you have any questions about these terms, please contact us at support@chatterbox.com.",
    "By using ChatterBox, you acknowledge that you have read, understood, and agreed to these terms of use. Thank you for choosing ChatterBox!"
)

val privacyPolicy = listOf(
    "Welcome to ChatterBox! We value your privacy and are committed to protecting your personal information. This Privacy Policy outlines how we collect, use, and safeguard your data.",
    "By using ChatterBox, you agree to the collection and use of information in accordance with this policy. If you do not agree with this policy, please do not use our services.",
    "We collect information you provide directly to us when you use ChatterBox, such as when you create an account, update your profile, or send messages. The types of information we may collect include your name, email address, password, and any other information you choose to provide.",
    "We may also collect information automatically when you use ChatterBox, such as your IP address, device information, and usage data. This information helps us improve our services and provide a better user experience.",
    "We use your information to provide and improve ChatterBox, to communicate with you, to provide customer support, and to protect our users. We may also use your information to send you updates, newsletters, and other information that may be of interest to you.",
    "We take reasonable measures to protect your information from unauthorized access, use, or disclosure. However, no internet-based service can be completely secure, and we cannot guarantee the absolute security of your information.",
    "We may share your information with third-party service providers who perform services on our behalf, such as hosting, analytics, and payment processing. These third parties are obligated to protect your information and use it only for the purposes for which it was disclosed.",
    "ChatterBox may contain links to other websites or services that are not owned or controlled by us. We are not responsible for the privacy practices of these third-party websites or services.",
    "We may update this Privacy Policy from time to time. If we make any changes, we will notify you by revising the date at the top of this policy and, in some cases, we may provide additional notice (such as adding a statement to our homepage or sending you an email notification). We encourage you to review this Privacy Policy periodically to stay informed about our practices.",
    "If you have any questions about this Privacy Policy, please contact us at support@chatterbox.com.",
    "By using ChatterBox, you acknowledge that you have read, understood, and agreed to this Privacy Policy. Thank you for trusting us with your information!"
)
