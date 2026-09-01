- name: Build APK
  run: chmod +x gradlew && ./gradlew assembleDebug

- name: Upload APK
  uses: actions/upload-artifact@v4
  with:
    name: app-debug
    path: app/build/outputs/apk/debug/*.apk
