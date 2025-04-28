package ap.mobile.composablemap.model

import ap.mobile.composablemap.PreferenceState

data class SettingsUIState (
  val preference: PreferenceState = PreferenceState(),
  val options: Map<String, String> = mapOf<String, String>(),
  val hostFriendlyValue: String = "",
  val optimizerFriendlyValue: String = "",
  val optMethodFriendlyValue: String = "",
  val useOnlineApiFriendlyValue: String = ""
)