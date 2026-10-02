# Winter Arc Launcher

Winter Arc is a 90-day challenge that tests a man's discipline, consistency, mental strength, and commitment to becoming a better version of himself. It's about stepping away from distractions, breaking bad habits, and building the mindset and physique you've always wanted.

But this isn't just another challenge. This app is the challenge. And you have to accept it.

For the next 90 days, you'll face daily tasks, push your limits, track your progress, and prove to yourself that you can stay committed even when motivation fades.

No shortcuts. No excuses. No giving up halfway.

Every day is a choice: stay the same or level up.

Your Winter Arc starts now. Are you ready to accept the challenge?

## Features
- **Valley of Disappointment Graphs**: Custom aesthetic data visualizations that track your consistency exactly like the famous concept.
- **Editable Goals**: Instantly tap and edit your core daily targets (e.g., Workout, 10k Steps, 3L Water, Read/Learn).
- **Background Motivation Engine**: A persistent background service that automatically updates your system notifications with legendary quotes every 15 minutes.
- **Glassmorphism Aesthetic**: Deep black themes paired with premium glass-card overlays and bright glowing accents.

## How to Install the App
Since this is a custom Android Launcher, it isn't available on the Google Play Store. You must install it manually using Android Studio or via ADB:

### Method 1: Build & Install via Android Studio (Recommended)
1. Clone this repository to your computer: `git clone <your-repo-url>`
2. Open **Android Studio** and click **Open Project**.
3. Select the `WinterArcLauncher` folder.
4. Connect your Android device to your computer via USB (Ensure "USB Debugging" is enabled in Developer Options).
5. Wait for the Gradle sync to finish.
6. Click the green **Run (▶)** button at the top of Android Studio to compile and install the app directly onto your phone.

### Method 2: Command Line (Gradle)
1. Ensure you have the Android SDK installed.
2. Connect your Android phone via USB.
3. Open a terminal in the project directory and run: 
   ```bash
   ./gradlew installDebug
   ```
4. Check your phone's app drawer for the **Winter Arc** app!

*Note: You may need to grant Notification permissions when opening the app for the first time to allow the Motivation Engine to run.*
