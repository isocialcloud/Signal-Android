/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.thoughtcrime.securesms.apkupdate

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.signal.core.util.PendingIntentFlags
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.MainActivity
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.notifications.NotificationChannels
import org.thoughtcrime.securesms.notifications.NotificationIds
import org.thoughtcrime.securesms.util.ServiceUtil

object ApkUpdateNotifications {

  val TAG = Log.tag(ApkUpdateNotifications::class.java)

  /**
   * Shows a notification to prompt the user to install the app update. Only shown when silently auto-updating is not possible or are disabled by the user.
   * Note: This is an 'ongoing' notification (i.e. not-user dismissable) and never dismissed programatically. This is because the act of installing the APK
   * will dismiss it for us.
   */
  @SuppressLint("LaunchActivityFromNotification")
  fun showInstallPrompt(context: Context, downloadId: Long) {
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      1,
      Intent(context, ApkUpdateNotificationReceiver::class.java).apply {
        action = ApkUpdateNotificationReceiver.ACTION_INITIATE_INSTALL
        putExtra(ApkUpdateNotificationReceiver.EXTRA_DOWNLOAD_ID, downloadId)
      },
      PendingIntentFlags.immutable()
    )

    val notification = NotificationCompat.Builder(context, NotificationChannels.getInstance().APP_UPDATES)
      .setOngoing(true)
      .setContentTitle(context.getString(R.string.ApkUpdateNotifications_prompt_install_title))
      .setContentText(context.getString(R.string.ApkUpdateNotifications_prompt_install_body))
      .setSmallIcon(R.drawable.ic_notification)
      .setColor(ContextCompat.getColor(context, R.color.core_ultramarine))
      .setContentIntent(pendingIntent)
      .build()

    ServiceUtil.getNotificationManager(context).notify(NotificationIds.APK_UPDATE_PROMPT_INSTALL, notification)
  }

  fun showInstallFailed(context: Context, reason: FailureReason) {
    val pendingIntent = PendingIntent.getActivity(
      context,
      0,
      Intent(context, MainActivity::class.java),
      PendingIntentFlags.immutable()
    )

    val notification = NotificationCompat.Builder(context, NotificationChannels.getInstance().APP_UPDATES)
      .setContentTitle(context.getString(R.string.ApkUpdateNotifications_failed_general_title))
      .setContentText(context.getString(R.string.ApkUpdateNotifications_failed_general_body))
      .setSmallIcon(R.drawable.ic_notification)
      .setColor(ContextCompat.getColor(context, R.color.core_ultramarine))
      .setContentIntent(pendingIntent)
      .build()

    ServiceUtil.getNotificationManager(context).notify(NotificationIds.APK_UPDATE_FAILED_INSTALL, notification)
  }

  enum class FailureReason {
    UNKNOWN,
    ABORTED,
    BLOCKED,
    INCOMPATIBLE,
    INVALID,
    CONFLICT,
    STORAGE,
    TIMEOUT
  }
}
