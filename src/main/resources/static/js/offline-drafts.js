document.addEventListener('DOMContentLoaded', () => {
    // 1. Service Worker Registration
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('/sw.js')
            .then(reg => console.log('Service Worker Enregistré!', reg))
            .catch(err => console.log('Échec de l\'enregistrement du Service Worker', err));
    }

    // 2. Offline Form Drafts
    // Ex: Save "Add Client" and "Add Car" forms if network goes down
    const draftForms = document.querySelectorAll('form[data-draft="true"]');
    
    draftForms.forEach(form => {
        const formId = form.getAttribute('id') || form.getAttribute('action');
        
        // Restore from LocalStorage
        const savedData = localStorage.getItem('draft_' + formId);
        if (savedData) {
            try {
                const parsedData = JSON.parse(savedData);
                for (const key in parsedData) {
                    const input = form.elements[key];
                    if (input && input.type !== 'file' && input.type !== 'password' && input.type !== 'submit') {
                        if (input.type === 'checkbox' || input.type === 'radio') {
                            input.checked = parsedData[key];
                        } else {
                            input.value = parsedData[key];
                        }
                    }
                }
                
                // Show floating banner
                showOfflineBanner('Brouillon restauré. Finissez votre saisie.', 'info');
            } catch (e) {
                console.error("Impossible de parser le brouillon", e);
            }
        }

        // Save to LocalStorage on input
        form.addEventListener('input', () => {
            if (!navigator.onLine) {
                const formData = new FormData(form);
                const dataObj = {};
                formData.forEach((value, key) => {
                    dataObj[key] = value;
                });
                localStorage.setItem('draft_' + formId, JSON.stringify(dataObj));
                showOfflineBanner('Vous êtes hors-ligne. Saisie sauvegardée localement.', 'warning');
            }
        });

        // Clear draft on successful submit
        form.addEventListener('submit', () => {
            if (navigator.onLine) {
                localStorage.removeItem('draft_' + formId);
            }
        });
    });

    // Detect Online/Offline changes
    window.addEventListener('online', () => {
        showOfflineBanner('Connexion rétablie ! Vous pouvez envoyer vos formulaires.', 'success');
        setTimeout(() => hideOfflineBanner(), 4000);
    });

    window.addEventListener('offline', () => {
        showOfflineBanner('Connexion perdue. Vos saisies seront sauvegardées en brouillon.', 'danger');
    });

    // Helper functions for Banner
    function showOfflineBanner(msg, type) {
        let banner = document.getElementById('offline-banner');
        if (!banner) {
            banner = document.createElement('div');
            banner.id = 'offline-banner';
            banner.style.position = 'fixed';
            banner.style.bottom = '20px';
            banner.style.right = '20px';
            banner.style.zIndex = '9999';
            banner.style.padding = '12px 24px';
            banner.style.borderRadius = '8px';
            banner.style.fontWeight = '500';
            banner.style.transition = 'opacity 0.3s';
            document.body.appendChild(banner);
        }
        
        banner.className = 'shadow';
        if (type === 'danger') {
            banner.style.background = '#fecaca'; banner.style.color = '#991b1b';
        } else if (type === 'warning') {
            banner.style.background = '#fef08a'; banner.style.color = '#854d0e';
        } else if (type === 'info') {
            banner.style.background = '#bfdbfe'; banner.style.color = '#1e40af';
        } else if (type === 'success') {
            banner.style.background = '#bbf7d0'; banner.style.color = '#166534';
        }

        banner.innerText = msg;
        banner.style.opacity = '1';
        banner.style.display = 'block';
    }

    function hideOfflineBanner() {
        const banner = document.getElementById('offline-banner');
        if (banner) {
            banner.style.opacity = '0';
            setTimeout(() => { banner.style.display = 'none'; }, 300);
        }
    }
});
