package com.vm.coinfold.app.feature.policy

/** Where to read the policy when the settings file could not be downloaded. */
fun fallbackPolicyUrl(languageCode: String): String =
    "https://github.com/VladM31/Coinfold/blob/master/" + if (languageCode == "uk") "PRIVACY.uk.md" else "PRIVACY.md"
