'use strict';

document.addEventListener('DOMContentLoaded', () => {
    const body = document.body;

    // 1. Dark mode toggle (tersimpan di localStorage)
    const toggle = document.getElementById('themeToggle');
    const applyTheme = (dark) => {
        body.classList.toggle('dark-mode', dark);
        document.documentElement.setAttribute('data-bs-theme', dark ? 'dark' : 'light');
        if (toggle) {
            toggle.innerHTML = dark ? '<i class="bi bi-sun"></i>' : '<i class="bi bi-moon-stars"></i>';
            toggle.setAttribute('aria-label', dark ? 'Mode terang' : 'Mode gelap');
        }
    };
    let dark = localStorage.getItem('app-theme') === 'dark';
    applyTheme(dark);
    if (toggle) {
        toggle.addEventListener('click', () => {
            dark = !dark;
            localStorage.setItem('app-theme', dark ? 'dark' : 'light');
            applyTheme(dark);
        });
    }

    // 2. Jam live di navbar (Asia/Jakarta)
    const clock = document.getElementById('liveClock');
    const tick = () => {
        if (!clock) return;
        try {
            clock.textContent = new Intl.DateTimeFormat('id-ID', {
                weekday: 'short', day: '2-digit', month: 'short',
                hour: '2-digit', minute: '2-digit', second: '2-digit',
                timeZone: 'Asia/Jakarta',
            }).format(new Date());
        } catch (e) { clock.textContent = new Date().toLocaleString(); }
    };
    tick();
    setInterval(tick, 1000);

    // 3. Animasi angka statistik (count-up)
    document.querySelectorAll('[data-count-up]').forEach((el) => {
        const target = parseInt(el.dataset.countUp || '0', 10);
        if (!Number.isFinite(target)) return;
        const dur = 800;
        const t0 = performance.now();
        const step = (t) => {
            const p = Math.min(1, (t - t0) / dur);
            el.textContent = Math.round(target * (1 - Math.pow(1 - p, 3)));
            if (p < 1) requestAnimationFrame(step);
        };
        requestAnimationFrame(step);
    });

    // 4. Konfirmasi hapus global (gantikan confirm bawaan yg kaku)
    document.querySelectorAll('form[data-confirm]').forEach((form) => {
        form.addEventListener('submit', (ev) => {
            if (!form.dataset.confirmed && !window.confirm(form.dataset.confirm || 'Yakin?')) {
                ev.preventDefault();
            }
        });
    });

    // 5. Back-to-top
    const btt = document.getElementById('backToTop');
    const onScroll = () => {
        if (!btt) return;
        const show = window.scrollY > 300;
        btt.style.display = show ? 'grid' : 'none';
    };
    window.addEventListener('scroll', onScroll, { passive: true });
    onScroll();
    if (btt) btt.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));

    // 6. Tutup sidebar mobile otomatis setelah klik menu
    document.querySelectorAll('.app-sidebar .nav-link[href]').forEach((a) => {
        a.addEventListener('click', () => {
            const sb = document.getElementById('appSidebar');
            if (window.innerWidth < 992 && sb && sb.classList.contains('show') && window.bootstrap) {
                window.bootstrap.Offcanvas.getOrCreateInstance(sb).hide();
            }
        });
    });

    // 7. PWA Service Worker Registration
    if ('serviceWorker' in navigator && (window.location.protocol === 'https:' || window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1')) {
        window.addEventListener('load', () => {
            navigator.serviceWorker.register('/absensi-siswa-qrcode/sw.js')
                .then((reg) => {
                    console.info('ServiceWorker aktif dengan scope:', reg.scope);
                })
                .catch((err) => {
                    console.debug('ServiceWorker tidak aktif:', err.message);
                });
        });
    }

    // 8. Deteksi Koneksi Online/Offline Real-time
    const showNetworkStatus = (online) => {
        let toastEl = document.getElementById('networkStatusToast');
        if (!toastEl) {
            toastEl = document.createElement('div');
            toastEl.id = 'networkStatusToast';
            toastEl.className = 'network-toast';
            document.body.appendChild(toastEl);
        }
        if (online) {
            toastEl.innerHTML = '<i class="bi bi-wifi me-1"></i> Terhubung kembali ke internet';
            toastEl.className = 'network-toast network-toast-online show';
            setTimeout(() => { toastEl.classList.remove('show'); }, 3000);
        } else {
            toastEl.innerHTML = '<i class="bi bi-wifi-off me-1"></i> Tidak ada koneksi internet. Absensi belum dapat disimpan ke server.';
            toastEl.className = 'network-toast network-toast-offline show';
        }
    };
    window.addEventListener('online', () => showNetworkStatus(true));
    window.addEventListener('offline', () => showNetworkStatus(false));

    // 9. Highlight Menu Aktif di Mobile Bottom Nav
    const currentPath = window.location.pathname;
    document.querySelectorAll('.mobile-bottom-nav .bottom-nav-item').forEach((item) => {
        const href = item.getAttribute('href');
        if (href && (currentPath.endsWith(href) || (href !== '/' && currentPath.includes(href)))) {
            item.classList.add('active');
        }
    });

    console.info('Absensi Siswa QR Code siap - SMK Tritech Informatika Medan.');
});