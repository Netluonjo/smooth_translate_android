# Smooth Translate (Android)

Ứng dụng phiên dịch đa năng cao cấp (All-in-One Translator) chuẩn theo mẫu iOS [Smooth Translate](https://apps.apple.com/us/app/smooth-translate/id6758365838?l=vi), chạy **100% Offline / On-Device** sử dụng **Google ML Kit Translate** và **ML Kit Text Recognition**.

## 🌟 Tính năng nổi bật

1. **Dữ liệu dịch Offline hoàn toàn (Google ML Kit On-Device Translate)**:
   - Miễn phí 100% vĩnh viễn, không giới hạn ký tự.
   - Hỗ trợ hơn 50+ cặp ngôn ngữ (Anh, Việt, Nhật, Hàn, Trung, Pháp, Đức, Tây Ban Nha, v.v.).
   - Khi cần gói ngôn ngữ nào, app tải model trực tiếp về máy (~30MB/gói) và thực hiện dịch hoàn toàn offline, không cần internet, bảo mật tuyệt đối.
2. **Text Translate (Dịch văn bản)**:
   - Giao diện chuẩn capsule xanh bo tròn kèm cờ quốc gia và nút đảo chiều ngôn ngữ xoay 180° mượt mà.
   - Hộp nhập liệu trực quan: dán nhanh (Paste), xoá nhanh (Clear), đếm ký tự.
   - Kết quả dịch với tính năng phát âm Text-To-Speech (TTS), sao chép (Copy), chia sẻ (Share) và lưu danh sách lịch sử / yêu thích.
3. **Photo & Camera Translate (Dịch qua Camera / Ảnh)**:
   - Tích hợp **Android CameraX** chụp trực tiếp hoặc chọn ảnh từ thư viện (Gallery).
   - Nhận diện ký tự OCR bằng **Google ML Kit Vision Text Recognition** ngay trên máy.
   - Dịch offline sang ngôn ngữ đích và hiển thị bóng thoại / callout card đè lên màn hình theo phong cách nguyên mẫu.
4. **Voice Translate (Dịch giọng nói)**:
   - Micro thu âm với hiệu ứng sóng âm (Soundwave Visualizer) và vòng xung lực động (Pulsating rings).
   - Nhận diện giọng nói -> Dịch offline tức thì -> Tự động phát âm bằng giọng đọc tự nhiên (TTS).
5. **Face-To-Face Translation (Dịch đối thoại trực tiếp 2 người)**:
   - Màn hình chia đôi độc đáo: Nửa trên xoay 180° hướng về phía người đối diện, nửa dưới hướng về bạn.
   - Mỗi bên có nút micro và bộ chọn ngôn ngữ riêng biệt.
   - Đàm thoại 2 chiều song ngữ liên tục theo thời gian thực kèm đọc phát âm song song.
6. **Offline Language Models Manager (Quản lý dữ liệu tải về)**:
   - Xem dung lượng lưu trữ trên thiết bị.
   - Tải 1 chạm bất kỳ gói ngôn ngữ nào.
   - Xoá gói khi không dùng để giải phóng bộ nhớ.

## 🛠 Công nghệ sử dụng
- **Ngôn ngữ**: Kotlin (2.0.20)
- **UI Toolkit**: Jetpack Compose + Material 3 (Typography, HSL Color Palette, Custom Shapes & Shadow)
- **Công cụ dịch Offline**: Google ML Kit Translate (`com.google.mlkit:translate:17.0.3`)
- **Công cụ nhận diện OCR**: Google ML Kit Text Recognition (`com.google.mlkit:text-recognition:16.0.1`)
- **Camera**: AndroidX CameraX (Core, Camera2, Lifecycle, View)
- **Voice & TTS**: Android `SpeechRecognizer` + `android.speech.tts.TextToSpeech`
- **Gradle**: 8.9, Android Gradle Plugin (AGP) 8.5.2, Java 17

## 🚀 Build & Run
```bash
# Biên dịch Kotlin & Compose
./gradlew assembleDebug
```
File APK debug được tạo tại:
`app/build/outputs/apk/debug/app-debug.apk`
