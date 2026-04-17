// Confirmation avant suppression
function confirmDelete(message) {
    return confirm(message || 'Voulez-vous vraiment supprimer cet élément ?');
}

// Formatage des nombres
function formatNumber(number) {
    return new Intl.NumberFormat('fr-FR').format(number);
}

// Initialisation des tooltips Bootstrap
document.addEventListener('DOMContentLoaded', function() {
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Currency Logic Init
    updateCurrencyDisplay();
    
    // Currency Toggle Listeners
    const radios = document.querySelectorAll('input[name="currencyToggle"]');
    radios.forEach(radio => {
        radio.addEventListener('change', (e) => {
            currentCurrency = e.target.value;
            localStorage.setItem('preferredCurrency', currentCurrency);
            updateCurrencyDisplay();
        });
    });

    // Automatically append active currency to Facture actions
    document.addEventListener('click', function(e) {
        const link = e.target.closest('a');
        if (link && link.href) {
            if (link.href.includes('/factures/telecharger/') || link.href.includes('/factures/envoyer/')) {
                e.preventDefault();
                const url = new URL(link.href);
                url.searchParams.set('devise', currentCurrency);
                window.location.href = url.toString();
            }
        }
    });
});

// Global Currency Logic
const EXCHANGE_RATE_EUR = 4500; // 1 EUR = 4500 Ar
let currentCurrency = localStorage.getItem('preferredCurrency') || 'MGA';

function updateCurrencyDisplay() {
    const formatCurrent = currentCurrency === 'EUR' ? '€' : 'Ar';
    const divisor = currentCurrency === 'EUR' ? EXCHANGE_RATE_EUR : 1;

    document.querySelectorAll('.amount-convertible').forEach(el => {
        let mgaValueStr = el.getAttribute('data-mga');
        if (mgaValueStr === null || mgaValueStr === "") return;
        
        let mgaValue = parseFloat(mgaValueStr);
        if(!isNaN(mgaValue)) {
            let convertedStr;
            if (currentCurrency === 'EUR') {
                const eurValue = mgaValue / divisor;
                convertedStr = new Intl.NumberFormat('fr-FR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(eurValue) + ' ' + formatCurrent;
            } else {
                convertedStr = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(mgaValue) + ' ' + formatCurrent;
            }
            
            // Si l'élément a la classe '.keep-slash-j', on rajoute '/j'
            if (el.classList.contains('keep-slash-j')) {
                convertedStr += '/j';
            }
            
            el.textContent = convertedStr;
        }
    });

    const radioMGA = document.getElementById('currMGA');
    const radioEUR = document.getElementById('currEUR');
    if (radioMGA && radioEUR) {
        if (currentCurrency === 'MGA') radioMGA.checked = true;
        if (currentCurrency === 'EUR') radioEUR.checked = true;
    }
}

// Expose formatCurrency to global window for dynamic updates (like in reserver.html stepper)
window.formatCurrency = updateCurrencyDisplay;