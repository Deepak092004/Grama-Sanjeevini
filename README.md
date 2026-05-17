## Grama-Sanjeevini – Rural Pharmacy Network (Compose + Firestore)

### What you get
- **Medicine Search** across multiple pharmacies (distance + stock status)
- **Emergency medicines** highlighted with a **red “EMERGENCY” badge**
- **Admin panel** (toggle pharmacist/admin mode) to **add/update/delete** medicines
- **3 mock pharmacies + 5–10 medicines** auto-seeded to **Firestore** on first run
- **Jetpack Compose only**, **Material 3**, **MVVM**, **repository pattern**, **StateFlow**

---

### Android Studio setup (important)
1. Open Android Studio → **Open** → select this folder: `GramaSanjeevini`
2. Wait for Gradle sync.

---

### Firebase Firestore setup (required to run)
1. Go to Firebase Console → create a project (or use existing).
2. Add an Android app:
   - **Package name**: `com.gramasanjeevini`
3. Download `google-services.json`
4. Copy it to:
   - `GramaSanjeevini/app/google-services.json`
5. In Firebase Console → **Firestore Database**:
   - Create a database (test mode is fine for demos)

When you run the app the first time, it will auto-create:

```
pharmacies/{pharmacyId}
  name: string
  location: { latitude: number, longitude: number }
  medicines/{medicineId}
    name: string
    stock: boolean
    isEmergency: boolean
```

---

### How to run
1. Connect an emulator/device
2. Run the app
3. Splash screen seeds mock data (only if Firestore is empty)
4. Use **Home** screen to search medicines.
5. Turn on **Pharmacist mode** toggle → open **Admin** → manage medicines.

---

### Notes
- User distance is computed from a **mock user location** stored in `AppStateViewModel`.
- If you want a real location-based radius, add Location permissions + fused location provider.

