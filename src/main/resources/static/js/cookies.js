// Barra de navegación al hacer scroll
    window.addEventListener('scroll', function() {
        const header = document.querySelector('header');
        if (window.scrollY > 50) {
            header.classList.add('shadow-md');
        } else {
            header.classList.remove('shadow-md');
        }
    });

    // Menú móvil
    const menuToggle = document.getElementById('menuToggle');
    const mobileMenu = document.getElementById('mobileMenu');
    const mobileMenuOverlay = document.getElementById('mobileMenuOverlay');
    const mobileMenuClose = document.getElementById('mobileMenuClose');

    function openMobileMenu() {
        mobileMenu.classList.add('active');
        mobileMenuOverlay.classList.add('active');
        document.body.style.overflow = 'hidden';
    }

    function closeMobileMenu() {
        mobileMenu.classList.remove('active');
        mobileMenuOverlay.classList.remove('active');
        document.body.style.overflow = 'auto';
    }

    menuToggle.addEventListener('click', openMobileMenu);
    mobileMenuClose.addEventListener('click', closeMobileMenu);
    mobileMenuOverlay.addEventListener('click', closeMobileMenu);

    // Cerrar menú móvil con tecla ESC
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && mobileMenu.classList.contains('active')) {
            closeMobileMenu();
        }
    });

    // Configuración de cookies
    document.addEventListener('DOMContentLoaded', function() {
        const cookieSettingsBtn = document.getElementById('cookieSettingsBtn');
        if (cookieSettingsBtn) {
            cookieSettingsBtn.addEventListener('click', function() {
                // Simulación de panel de configuración de cookies
                const acceptAll = confirm('¿Desea aceptar todas las cookies? Presione "Aceptar" para aceptar todas o "Cancelar" para configurar individualmente.');

                if (acceptAll) {
                    document.querySelector('.cookie-status').textContent = 'Todas aceptadas';
                    document.querySelector('.cookie-status').style.color = '#10b981';
                } else {
                    document.querySelector('.cookie-status').textContent = 'Configuradas manualmente';
                    document.querySelector('.cookie-status').style.color = '#f59e0b';
                }
            });
        }
    });