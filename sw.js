/**
 * Service Worker untuk SMK Tritech Informatika Medan Absensi PWA
 * Caching shell aplikasi & aset statis.
 * Sesuai aturan: TIDAK MEMBUAT absensi offline palsu.
 */

const CACHE_NAME = 'tritech-absensi-v1.3';
const STATIC_ASSETS = [
    '/absensi-siswa-qrcode/assets/css/style.css',
    '/absensi-siswa-qrcode/assets/js/app.js',
    '/absensi-siswa-qrcode/assets/img/logo.png',
    'https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css',
    'https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css',
    'https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js'
];

self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) => {
            return cache.addAll(STATIC_ASSETS).catch(() => {
                // Abaikan jika ada aset CDN yang gagal di awal
            });
        })
    );
    self.skipWaiting();
});

self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((keys) => {
            return Promise.all(
                keys.map((key) => {
                    if (key !== CACHE_NAME) {
                        return caches.delete(key);
                    }
                })
            );
        })
    );
    self.clients.claim();
});

self.addEventListener('fetch', (event) => {
    // Lewatkan request POST / absensi langsung ke jaringan (tidak pernah dicache)
    if (event.request.method !== 'GET') {
        return;
    }

    event.respondWith(
        fetch(event.request).catch(() => {
            return caches.match(event.request);
        })
    );
});
